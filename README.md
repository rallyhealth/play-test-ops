<a href='https://travis-ci.org/jeffmay/play-test-ops'>
  <img src='https://travis-ci.org/jeffmay/play-test-ops.svg' alt='Build Status' />
</a>
<a href='https://coveralls.io/github/jeffmay/play-test-ops?branch=master'>
  <img src='https://coveralls.io/repos/jeffmay/play-test-ops/badge.svg?branch=master&service=github' alt='Coverage Status' />
</a>
<table>
  <tr>
    <th>play23-test-ops-core</th>
    <th>play25-test-ops-core</th>
  </tr>
  <tr>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play23-test-ops-core/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play23-test-ops-core/images/download.svg'>
      </a>
    </td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play25-test-ops-core/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play25-test-ops-core/images/download.svg'>
      </a>
    </td>
  </tr>
</table>

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
