[![Build Status](https://travis-ci.org/rallyhealth/play-test-ops.svg?branch=master)](https://travis-ci.org/rallyhealth/play-test-ops)
[![codecov](https://codecov.io/gh/rallyhealth/play-test-ops/branch/master/graph/badge.svg)](https://codecov.io/gh/rallyhealth/play-test-ops)

| play23-test-ops-core | play25-test-ops-core | play27-test-ops-core |
| :------------------: | :------------------: | :------------------: |
| [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play25-test-ops-core/images/download.svg) ](https://bintray.com/rallyhealth/maven/play25-test-ops-core/_latestVersion) | [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play26-test-ops-core/images/download.svg) ](https://bintray.com/rallyhealth/maven/play26-test-ops-core/_latestVersion) | [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play27-test-ops-core/images/download.svg) ](https://bintray.com/rallyhealth/maven/play27-test-ops-core/_latestVersion) |

# Introduction

Adds Play Framework test code helpers that augment the `com.typesafe` %% `play-test` library
with the following features:

- AsyncResultExtractors: Instead of blocking for a result, extract the content into Futures
  and let the testing framework handle it for you.

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
