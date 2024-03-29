/*
 * Copyright 2016-2018 E257.FI
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
import better.files._
import com.typesafe.config.ConfigFactory
import sbt._

object TacklerTests {

  /**
   * Prepare Tackler's test (e.g. clean up old output files)
   *
   * @param tests is path to test directory
   * @param log logger of Setup process (info and warning messages)
   */
  def setup(tests: String, log: Logger) = {
    val testsDir = File(tests)
    val autoCleanConfFile = testsDir / "dirsuite.conf"

    if (autoCleanConfFile.exists) {
      val cfg = ConfigFactory.parseFile(autoCleanConfFile.toJava)
      val autoClean: Boolean = cfg.getBoolean("auto-clean")
      val outGlob: String = cfg.getString("out-glob")

      if (autoClean) {
        val outFiles = testsDir.glob(outGlob)
          .filter(f => f.isRegularFile)
          .toSeq

        outFiles.foreach { output =>
          output.delete(true)
        }
        log.info("DirSuite clean-up: Removed " + outFiles.size + " files")
      } else {
        log.info("DirSuite clean-up: Disabled")
      }
    } else {
      log.warn("DirSuite: Missing configuration file: " + autoCleanConfFile)
    }
  }
}
