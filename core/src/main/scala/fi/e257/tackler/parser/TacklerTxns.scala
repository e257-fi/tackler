/*
 * Copyright 2016-2022 E257.FI
 *
 * git2Txns is based on example
 * by: Copyright 2013, 2014 Dominik Stadler
 * license: Apache License v2.0
 * url: https://raw.githubusercontent.com/centic9/jgit-cookbook/
 * commit: 276ad0fecb4f1c616ef459ed8b7feb6d503724eb
 * file: jgit-cookbook/src/main/java/org/dstadler/jgit/api/ReadFileFromCommit.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package fi.e257.tackler.parser
import better.files._
import cats.syntax.all._
import scala.collection.parallel.CollectionConverters._
import fi.e257.tackler.api.GitInputReference
import fi.e257.tackler.core.{Settings, TacklerException}
import fi.e257.tackler.model.{OrderByTxn, TxnData, Txns}
import org.eclipse.jgit.lib.{FileMode, ObjectId, Repository}
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.{AndTreeFilter, PathFilter, PathSuffixFilter}
import org.slf4j.{Logger, LoggerFactory}

import java.nio.file.Path
import scala.util.control.NonFatal

/**
 * Helper methods for [[TacklerTxns]] and Txns Input handling.
 */
object TacklerTxns {
  type GitInputSelector = Either[String, String]

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Get input Txns paths based on configuration settings
   *
   * @param settings of base dir path and glob configuration
   * @return sequence of input txn pahts
   */
  def inputPaths(settings: Settings): Seq[Path] = {
    log.info("FS: dir = {}", settings.input_fs_dir.toString)
    log.info("FS: glob = {}", settings.input_fs_glob)

    File(settings.input_fs_dir)
      .glob(settings.input_fs_glob)(visitOptions = File.VisitOptions.follow)
      .map(f => f.path)
      .toSeq
  }

  /**
   * Make git commit id based input selector.
   *
   * @param commitId as string
   * @return git input selector for commitId
   */
  def gitCommitId(commitId: String): GitInputSelector = {
    Right[String, String](commitId)
  }

  /**
   * Make git reference based input selector from settings
   *
   * @param settings containing git reference config
   * @return git input selector for reference
   */
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def gitReference(settings: Settings) :  GitInputSelector = {
    Left[String, String](settings.input_git_ref)
  }

  /**
   * Make git reference based input selector
   *
   * @param reference git reference as string
   * @return git input selector for reference
   */
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def gitReference(reference: String) :  GitInputSelector = {
    Left[String, String](reference)
  }

}

/**
 * Generate Transactions from selected inputs.
 *
 * These take an input(s) as argument and
 * returns sequence of transactions.
 *
 * If there is an error, they throw an exception.
 *
 * @param settings to control how inputs and txns are handled
 */
class TacklerTxns(val settings: Settings) extends CtxHandler {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  // perf: private var ts_start: Long = 0
  // perf: private var ts_end: Long = 0
  // perf: private var par_total: Long = 0

  /**
   * Get Transactions from list of input paths.
   * Throws an exception in case of error.
   * See also [[TacklerTxns.inputPaths]]
   *
   * @param paths input as seq of files
   * @return TxnData
   */
  def paths2Txns(paths: Seq[Path]): TxnData = {

    TxnData(None,
      paths.par.flatMap(inputPath => {
        try {
          log.trace("FS: handle file = {}", inputPath.toString)
          val txnsCtx = TacklerParser.txnsFile(inputPath)
          handleTxns(txnsCtx)
        } catch {
          case ex: Exception => {
            log.error(
              "FS: Error while processing file" + "\n" +
              "   path = " + inputPath.toString + "\n" +
              "   msg: " + ex.getMessage)
            throw ex
          }
        }
      }).seq.sorted(OrderByTxn),
      Some(settings)
    )
  }

  /**
   * Get commit id based on input ref
   *
   * @return git commit id (as ObjectId)
   */
  private def getCommitId(repository: Repository, inputSelector: TacklerTxns.GitInputSelector): ObjectId = {
    inputSelector match {
      case Left(refStr) => {
        log.info("GIT: reference = {}", refStr)

        val refOpt = Option(repository.findRef(refStr))
        val ref = refOpt.getOrElse({
          val msg = "GIT: ref not found or it is invalid: [" + refStr + "]"
          log.error(msg)
          throw new TacklerException(msg)
        })
        ref.getObjectId
      }
      case Right(commitIdStr) => {
        log.info("GIT: commitId = {}", commitIdStr)
        try {
          // resolve fails either with null or exceptions
          Option(repository.resolve(commitIdStr))
            .getOrElse({
              // test: uuid: 7cb6af2e-3061-4867-96e3-ee175b87a114
              val msg = "GIT: Can not resolve given id: [" + commitIdStr + "]"
              log.error(msg)
              throw new TacklerException(msg)
            })
        } catch {
          case e: RuntimeException =>
            val msg = "GIT: Can not resolve commit by given id: [" + commitIdStr + "], Message: [" + e.getMessage + "]"
            log.error(msg)
            throw new TacklerException(msg)
        }
      }
    }
  }

  /**
   * Get Git repository as managed resource.
   * Repository must be bare.
   * Caller must close repository after use
   *
   * @param gitdir path/to/repo.git
   * @return repository
   */
  private def getRepo(gitdir: File): Repository = {
    log.info("GIT: repo = {}", gitdir.toString())
    try {
      (new FileRepositoryBuilder)
        .setGitDir(gitdir.toJava)
        .setMustExist(true)
        .setBare()
        .build()
    } catch {
      case e: org.eclipse.jgit.errors.RepositoryNotFoundException => {
        val msg = "GIT: Repository not found\n" +
          "   Could not find usable git repository, check repository path.\n" +
          "   Make sure repository is bare or path is pointing to .git directory.\n" +
          "   Message: " + e.getMessage
        log.error(msg)
        throw new TacklerException(msg)
      }
    }
  }

  /**
   * Get Transactions from GIT based storage.
   * Basic git repository information is read from settings,
   * but input setting (ref or commit id) is an argument.
   * Throws an exception in case of error.
   * See [[TacklerTxns.gitCommitId]] et.al.
   *
   * feature: 06b4a9b1-f48c-4b33-8811-1f32cdc44d7b
   * coverage: "sorted" tested by 1d2c22c1-e3fa-4cd4-a526-45318c15d13e
   *
   * @param inputRef Left(ref) or Right(commitId)
   * @return TxnData
   */
  @SuppressWarnings(Array(
    "org.wartremover.warts.TraversableOps",
    "org.wartremover.warts.Equals"))
  def git2Txns(inputRef: TacklerTxns.GitInputSelector): TxnData = {

    // perf: ts_start = System.currentTimeMillis()
    using(getRepo(settings.input_git_repository))(repository => {

      val gitdir = settings.input_git_dir
      val suffix = settings.input_git_suffix

      val commitId = getCommitId(repository, inputRef)

      using(new RevWalk(repository))(revWalk => {
        // a RevWalk allows to walk over commits based on defined filtering

        val commit: RevCommit = try {
          revWalk.parseCommit(commitId)
        } catch {
          case e: org.eclipse.jgit.errors.MissingObjectException =>
            val msg = "GIT: Can not find commit by given id: [" + commitId.getName + "], Message: [" + e.getMessage + "]"
            log.error(msg)
            throw new TacklerException(msg)
        }

        log.info("GIT: commit = " + commit.getName)
        log.info("GIT: dir = " + gitdir)
        log.info("GIT: suffix = " + suffix)

        val tree = commit.getTree

        // now try to find files
        using(new TreeWalk(repository))(treeWalk => {
          treeWalk.addTree(tree)
          treeWalk.setRecursive(true)

          treeWalk.setFilter(AndTreeFilter.create(
            PathFilter.create(gitdir),
            PathSuffixFilter.create(suffix)))

          // Handle files
          val rawTxns = (for {
            _ <- Iterator.continually(treeWalk.next()).takeWhile(p => p === true)
          } yield {
            val objectId = treeWalk.getObjectId(0)
            if (FileMode.REGULAR_FILE.equals(treeWalk.getFileMode(0))) {
              try {
                gitObject2Txns(repository, objectId)
              } catch {
                case NonFatal(ex) => {
                  val msg = "GIT: Error while processing git object" + "\n" +
                    "   commit id: " + commit.getName + "\n" +
                    "   object id: " + objectId.getName + "\n" +
                    "   path: " + treeWalk.getPathString + "\n" +
                    "   msg : " + ex.getMessage
                  log.error(msg)
                  throw new TacklerException(msg)
                }
              }
            } else {
              val msg = "GIT: Found matching object, but it is not regular file" + "\n" +
                "   commit id: " + commit.getName + "\n" +
                "   object id: " + objectId.getName + "\n" +
                "   path: " + treeWalk.getPathString
              log.error(msg)
              throw new TacklerException(msg)
            }
          }).toSeq

          val meta = GitInputReference(
            commit.getName,
            inputRef.left.toOption,
            gitdir,
            suffix,
            commit.getShortMessage
          )

          val txn_data = TxnData(Some(meta), rawTxns.flatten.sorted(OrderByTxn), Some(settings))
          //perf: ts_end = System.currentTimeMillis()
          //perf: System.err.println("total time: " + (ts_end - ts_start) + "ms, parse time: " + par_total + "ms, git: " + ((ts_end - ts_start) - par_total) + "ms")
          txn_data
        })
      })
    })
  }

  private def gitObject2Txns(repository: Repository, objectId: ObjectId): Txns = {

    log.trace("GIT: handle object id = {}", objectId.getName)

    val loader = repository.open(objectId, org.eclipse.jgit.lib.Constants.OBJ_BLOB)


    val txns = using(loader.openStream())(stream => {
      // perf: val ts_par_start = System.currentTimeMillis()
      val t = handleTxns(TacklerParser.txnsStream(stream))
      // perf: val ts_par_end = System.currentTimeMillis()
      // perf: par_total = par_total + (ts_par_end-ts_par_start)
      t
    })
    txns
  }

  /**
   * Parse and converts input string to Txns
   * Throws an exception in case of error.
   *
   * feature: a94d4a60-40dc-4ec0-97a3-eeb69399f01b
   * coverage: "sorted" tested by 200aad57-9275-4d16-bdad-2f1c484bcf17
   *
   * @param input as text
   * @return TxnData
   */
  def string2Txns(input: String): TxnData = {

    val txnsCtx = TacklerParser.txnsText(input)
    TxnData(None, handleTxns(txnsCtx).sorted(OrderByTxn), Some(settings))
  }
}
