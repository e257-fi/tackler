/*
 * Copyright 2016-2020 E257.FI
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
package fi.e257.tackler.cli

import java.nio.file.{Files, NoSuchFileException, Path, Paths}
import java.util.regex.PatternSyntaxException

import better.files._
import fi.e257.tackler.core.{AccountException, CommodityException, ConfigurationException, GroupByException, ReportException, TacklerException, TagsException, TxnException}
import fi.e257.tackler.parser.TacklerParseException
import fi.e257.testing.{DirSuiteLike, Glob}
import org.rogach.scallop.exceptions.{ExcessArguments, RequiredOptionNotFound, UnknownOption, ValidationFailure}


/**
 * Console
 *
 * CLI ok case is done with stdout and stderr validation
 */
class DirsuiteConsoleTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  override
  protected def mapArgs(testname: Path, args: Array[String]): Array[String] = {

    val test = File(testname)
    val basename = test.nameWithoutExtension

    val stdout = "out." + basename + ".stdout.txt"
    val stderr = "out." + basename + ".stderr.txt"
    val stdoutPath = test.parent / stdout
    val stderrPath = test.parent / stderr

    Array(stdoutPath.toString, stderrPath.toString) ++ args
  }

  runDirSuiteTestCases(basedir, Glob("cli/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {

      using(Files.newOutputStream(Paths.get(args(0))))(stdout => {
        using(Files.newOutputStream(Paths.get(args(1))))(stderr => {

          Console.withOut(stdout) {
            Console.withErr(stderr) {
              TacklerCli.runReturnValue(args.drop(2))
            }
          }

        })
      })
    }
  }
}


/**
 * Run all failure tests from everywhere
 */
class DirsuiteAllExceptionsTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  runDirSuiteTestCases(basedir, Glob("**/ex/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.FAILURE) {
      TacklerCli.runReturnValue(args)
    }
  }
}


/**
 * CLI Exceptions
 *
 * CLI ok cases are done with stdout and stderr validation
 */
class DirsuiteExceptionsTest extends DirSuiteLike {
  val basedir = Paths.get("tests")


  runDirSuiteTestCases(basedir, Glob("cli/ex/ConfigurationException-*.exec")) { args: Array[String] =>
    assertThrows[ConfigurationException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/NoSuchFileException-*.exec")) { args: Array[String] =>
    assertThrows[NoSuchFileException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/TxnException-*.exec")) { args: Array[String] =>
    assertThrows[TxnException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/AccountException-*.exec")) { args: Array[String] =>
    assertThrows[AccountException] {
      TacklerCli.runExceptions(args)
    }
  }

  ignoreDirSuiteTestCases(basedir, Glob("cli/ex/RequiredOptionNotFound-*.exec")) { args: Array[String] =>
    assertThrows[RequiredOptionNotFound] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/ValidationFailure-*.exec")) { args: Array[String] =>
    assertThrows[ValidationFailure] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/ExcessArguments-*.exec")) { args: Array[String] =>
    assertThrows[ExcessArguments] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/UnknownOption-*.exec")) { args: Array[String] =>
    assertThrows[UnknownOption] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/PatternSyntaxException-*.exec")) { args: Array[String] =>
    assertThrows[PatternSyntaxException] {
      TacklerCli.runExceptions(args)
    }
  }
}


/**
 * Commodity
 */
class DirsuiteCommodityTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  runDirSuiteTestCases(basedir, Glob("commodity/ex/CommodityException-*.exec")) { args: Array[String] =>
    assertThrows[CommodityException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("commodity/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}

/**
 * Audit
 */
class DirsuiteAuditTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  runDirSuiteTestCases(basedir, Glob("audit/ex/TacklerException-*.exec")) { args: Array[String] =>
    assertThrows[TacklerException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("audit/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}


/**
 * Core
 */
class DirsuiteCoreTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  runDirSuiteTestCases(basedir, Glob("core/ex/TxnException-*.exec")) { args: Array[String] =>
    assertThrows[TxnException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("core/ex/NoSuchFileException-*.exec")) { args: Array[String] =>
    assertThrows[NoSuchFileException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("core/ex/TacklerException-*.exec")) { args: Array[String] =>
    assertThrows[TacklerException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("core/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}

/**
  * Location
  */
class DirsuiteLocationTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  runDirSuiteTestCases(basedir, Glob("location/ex/TacklerParseException-*.exec")) { args: Array[String] =>
    assertThrows[TacklerParseException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("location/ex/TacklerException-*.exec")) { args: Array[String] =>
    assertThrows[TacklerException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("location/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}

/**
 * Tags
 */
class DirsuiteTagsTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  runDirSuiteTestCases(basedir, Glob("tags/ex/TagsException-*.exec")) { args: Array[String] =>
    assertThrows[TagsException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("tags/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}


/**
 * Parser
 */
class DirsuiteParserTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  runDirSuiteTestCases(basedir, Glob("parser/ex/*.exec")) { args: Array[String] =>
    assertThrows[TacklerParseException] {
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("parser/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}


/**
 * Accumulator
 */
class DirsuiteAccumulatorTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  runDirSuiteTestCases(basedir, Glob("accumulator/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}


/**
 * Reporting
 */
class DirsuiteReportingTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  runDirSuiteTestCases(basedir, Glob("reporting/ex/ReportException-*.exec")) { args: Array[String] =>
    assertThrows[ReportException]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("reporting/ex/GroupByException-*.exec")) { args: Array[String] =>
    assertThrows[GroupByException]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("reporting/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("reporting/group-by/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("reporting/time-and-zones/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}

/**
 * Compatibility tests
 */
class DirsuiteCompatibilityTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  ignoreDirSuiteTestCases(basedir, Glob("compat/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}
