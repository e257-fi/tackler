/*
 * Copyright 2017-2018 E257.FI
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
inThisBuild(
  List(
    sonatypeProfileName := "fi.e257",
    publishMavenStyle := true,
    licenses := Seq(
      ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
    ),
    homepage := Some(
      url("https://gitlab.com/e257/accounting/tackler")
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://gitlab.com/e257/accounting/tackler"),
        "scm:git:https://gitlab.com/e257/accounting/tackler.git"
      )
    ),
    developers := List(
      Developer(id = "e257", name = "E257", email = "dev-x64ae53@e257.fi", url = url("https://gitlab.com/e257"))
    )
  )
)
