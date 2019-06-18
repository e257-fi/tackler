/*
 * Copyright 2019 E257.FI
 *
 * Scala12to13 is based on following examples:
 * url: https://github.com/scala/scala-parallel-collections/issues/22
 * url: https://github.com/scala/scala-collection-compat/issues/208
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
package fi.e257.tackler

/**
 * author: https://github.com/sjrd
 * url: https://github.com/scala/scala-parallel-collections/issues/22#issuecomment-288389306
 */
private[tackler] object Scala12to13 {
  val Converters = {
    import Compat._

    {
      import scala.collection.parallel._

      CollectionConverters
    }
  }

  object Compat {
    object CollectionConverters
  }
}

// TODO: Remove when this is released:
// url: https://github.com/scala/scala-collection-compat/pull/217
/**
 * author: https://github.com/sjrd
 * code: https://github.com/scala/scala-collection-compat/issues/208#issuecomment-497735669
 * explanation: https://github.com/scala/scala-collection-compat/issues/208#issuecomment-497785065
 *
 * Magic to get cross-compiling access to `scala.jdk.CollectionConverters`
 *  with a fallback on `scala.collection.JavaConverters`, without deprecation
 *  warning in any Scala version.
 */
object JDKCollectionConvertersCompat {
  object Scope1 {
    object jdk {
      type CollectionConverters = Int
    }
  }
  import Scope1._

  object Scope2 {
    import scala.collection.{JavaConverters => CollectionConverters}
    object Inner {
      import scala._
      import jdk.CollectionConverters
      val Converters = CollectionConverters
    }
  }

  val Converters = Scope2.Inner.Converters
}
