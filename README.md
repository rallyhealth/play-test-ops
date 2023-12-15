[![Build Status](https://travis-ci.org/rallyhealth/play-test-ops.svg?branch=master)](https://travis-ci.org/rallyhealth/play-test-ops)

# Introduction

Adds Play Framework test code helpers that augment the `com.typesafe` %% `play-test` library
with the following features:

- AsyncResultExtractors: Instead of blocking for a result, extract the content into Futures
  and let the testing framework handle it for you.

Published to [Maven Central](https://search.maven.org/artifact/com.rallyhealth/play28-test-ops-core_2.13). 
Example dependency in sbt on the play28-specific artifact:
```scala
libraryDependencies += "com.rallyhealth" %% "play28-test-ops-core" % "version"
```

Scala Versions supported:

| Artifact             |      Scala 2.11       |      Scala 2.12      |      Scala 2.13      |       Scala 3        |
|----------------------|:---------------------:|:--------------------:|:--------------------:|:--------------------:|
| play25-test-ops-core |  :white_check_mark:   |                      |                      |                      |
| play26-test-ops-core |  :white_check_mark:   |  :white_check_mark:  |                      |                      |
| play27-test-ops-core |  :white_check_mark:   |  :white_check_mark:  |  :white_check_mark:  |                      |
| play28-test-ops-core |                       |  :white_check_mark:  |  :white_check_mark:  |                      |
| play29-test-ops-core |                       |                      |  :white_check_mark:  |  :white_check_mark:  |
| play30-test-ops-core |                       |                      |  :white_check_mark:  |  :white_check_mark:  |

# Usage

The following example uses the `AsyncTestSuite` in ScalaTest 3.x along with the
`play.api.test.EssentialActionCaller` from the `play-test` project:

```scala
class MyTest extends AsyncWordSpec
  with AsyncResultExtractors
  with EssentialActionCaller {

  "my test should complete asynchronously" in {
    val ctrl = new MyTestController
    val testJson = Json.obj("expected" -> "json")
    val request = FakeRequest("POST", "/test/json").withJsonBody(testJson)
    for {
      result <- call(ctrl.testAction, request)
      resultBody <- contentAsJson(result)
    } yield {
      assertResult(testJson) {
        resultBody
      }
    }
  }
}
```
