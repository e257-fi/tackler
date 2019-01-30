/*
 * Copyright 2016-2019 E257.FI
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

import java.nio.file.NoSuchFileException

import org.rogach.scallop.exceptions.{UnknownOption, ValidationFailure}
import org.scalatest.FunSpec

class TacklerCliArgsTest extends FunSpec {

  describe("Tackler with cli-args") {

    it("use default config") {
      /**
       * This is an implicit way to test the use of default config.
       * Default config is:
       *  - basedir=./
       *  - input.fs.dir=txns
       *
       * Let's assert that these path components are used to build basedir,
       * which obviously won't be found under cli/target/scala-2.12
       * so we intercept and inspect NoSuchFileException
       */
      val ex = intercept[NoSuchFileException] {
        TacklerCli.runExceptions(Array[String]())
      }
      assert(ex.getMessage.endsWith("/txns"), ex.getMessage)
    }

    it("support --help") {
      assertThrows[org.rogach.scallop.exceptions.Help] {
        TacklerCli.runExceptions(Array[String]("--help"))
      }
    }

    it("support --version") {
      val lifeIsGood = try {
        TacklerCli.runExceptions(Array[String]("--version"))
        false
      } catch {
        case org.rogach.scallop.exceptions.Version =>
          true
        case _: Exception =>
          false
      }
      assert(lifeIsGood)
    }

    it("check return result of --help") {
      assertResult(TacklerCli.FAILURE) {
        TacklerCli.runReturnValue(Array[String]("--help"))
      }
    }

    it("check return result of --version") {
      assertResult(TacklerCli.FAILURE) {
        TacklerCli.runReturnValue(Array[String]("--version"))
      }
    }

    it("reject unknown args") {
      assertThrows[UnknownOption] {
        TacklerCli.runExceptions(Array[String]("--not-an-argument"))
      }
    }
  }
  describe("git") {

    /**
     * test: a2ca374a-1323-413b-aaff-64bc3c8d4d30
     */
    it("err: git.ref + git.commit") {
      assertThrows[ValidationFailure] {
        TacklerCli.runExceptions(
          Array[String]("--input.git.ref", "ref", "--input.git.commit", "id"))
      }
    }
  }
  describe("input.file") {

    /**
     * test: 1822f1b2-f749-4f63-be44-fa29c58c4fe2
     */
    it("err: git.ref") {
      assertThrows[ValidationFailure] {
        TacklerCli.runExceptions(
          Array[String]("--input.file", "filename", "--input.git.ref", "ref"))
      }
    }

    /**
     * test: 97bf542e-55b5-437f-9878-7f436f50c428
     */
    it("err: git.commit") {
      assertThrows[ValidationFailure] {
        TacklerCli.runExceptions(
          Array[String]("--input.file", "filename", "--input.git.commit", "id"))
      }
    }

    /**
     * test: 8afb22ac-8a52-4cba-9443-e6375e6fcf75
     */
    it("err: git.dir") {
      assertThrows[ValidationFailure] {
        TacklerCli.runExceptions(
          Array[String]("--input.file", "filename", "--input.git.dir", "path/to/dir"))
      }
    }
  }
  describe("fs.dir") {

    /**
     * test: 3eba26fe-821d-4d36-94cb-09427b1c004f
     */
    it("err: git.ref") {
      assertThrows[ValidationFailure] {
        TacklerCli.runExceptions(
          Array[String]("--input.fs.dir", "txns", "--input.git.ref", "ref"))
      }
    }

    /**
     * test: 400bd1e9-6f7a-4e0c-913c-45401ee73181
     */
    it("err: git.commit") {
      assertThrows[ValidationFailure] {
        TacklerCli.runExceptions(
          Array[String]("--input.fs.dir", "txns", "--input.git.commit", "id"))
      }
    }

    /**
     * test: f74a2252-d826-4176-945a-8895d4c7f1f7
     */
    it("err: git.dir") {
      assertThrows[ValidationFailure] {
        TacklerCli.runExceptions(
          Array[String]("--input.fs.dir", "txns", "--input.git.dir", "path/to/dir"))
      }
    }
  }
}
