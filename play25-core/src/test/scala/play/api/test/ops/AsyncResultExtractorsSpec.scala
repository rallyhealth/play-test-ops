package play.api.test.ops

import org.scalatest.{Args, FutureOutcome, fixture, Status => TestStatus}
import play.api.http.{MimeTypes, Status}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test._
import play.api.{Application, Play}

class AsyncResultExtractorsSpec extends fixture.AsyncFreeSpec
  with EssentialActionCaller
  with Writeables {

  implicit lazy val app: Application = FakeApplication()
  import app.materializer

  class FixtureParam {
    object extractors extends AsyncResultExtractors
  }

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = test(new FixtureParam)

  protected override def runTests(testName: Option[String], args: Args): TestStatus = {
    // Use a single application for all the suites
    Play.start(app)
    val resultStatus = super.runTests(testName, args)
    resultStatus.whenCompleted(_ => Play.stop(app))
    resultStatus
  }

  class TestEchoController extends Controller {

    def echoTextBody: EssentialAction = Action { request =>
      Ok(request.body.asText.getOrElse("Missing body"))
    }

    def echoJsonBody: Action[JsValue] = Action(parse.json) { request =>
      Ok(request.body)
    }

    def echoJsonInHeader: EssentialAction = Action(parse.json) { request =>
      val name = (request.body \ "name").as[String]
      val value = (request.body \ "value").as[String]
      Ok.withHeaders(name -> value)
    }

    def echoJsonInCookie: EssentialAction = Action(parse.json) { request =>
      val name = (request.body \ "name").as[String]
      val value = (request.body \ "value").as[String]
      Ok.withCookies(Cookie(name, value))
    }

    def echoJsonInSession: EssentialAction = Action(parse.json) { request =>
      val sessionData = request.body.as[Map[String, String]]
      Ok.withSession(sessionData.toSeq: _*)
    }

    def echoJsonInFlash: EssentialAction = Action(parse.json) { request =>
      val flashData = request.body.as[Map[String, String]]
      Ok.flashing(flashData.toSeq: _*)
    }

    def redirectToBody: EssentialAction = Action(parse.json) { request =>
      val url = (request.body \ "url").as[String]
      val status = (request.body \ "status").as[Int]
      Redirect(url, status)
    }
  }

  protected def testSubject(methodName: String) = s"play25.AsyncResultExtrators.$methodName"

  s"${testSubject(methodName = "status")} should return the status code" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val request = FakeRequest("POST", "/test/status")
    for {
      result <- call(ctrl.echoTextBody, request)
    } yield {
      assertResult(Status.OK) {
        status(result)
      }
    }
  }

  s"${testSubject(methodName = "contentType")} should extract the expected content type" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val testJson = Json.obj()
    val request = FakeRequest("POST", "/test/contentType").withJsonBody(testJson)
    for {
      result <- call(ctrl.echoJsonBody, request)
    } yield {
      assertResult(Some(MimeTypes.JSON)) {
        contentType(result)
      }
    }
  }

  s"${testSubject(methodName = "charset")} should extract the expected charset" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val testString = "test"
    val request = FakeRequest("POST", "/test/charset").withTextBody(testString)
    for {
      result <- call(ctrl.echoTextBody, request)
    } yield {
      assertResult(Some(Codec.utf_8.charset)) {
        charset(result)
      }
    }
  }

  s"${testSubject(methodName = "contentAsString")} should extract the expected text" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val testString = "test"
    val request = FakeRequest("POST", "/test/text").withTextBody(testString)
    for {
      result <- call(ctrl.echoTextBody, request)
      resultBody <- contentAsString(result)
    } yield {
      assertResult(testString) {
        resultBody
      }
    }
  }

  s"${testSubject(methodName = "contentAsJson")} should extract the expected json" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val testJson = Json.obj("expected" -> "json")
    val request = FakeRequest("POST", "/test/json").withJsonBody(testJson)
    for {
      result <- call(ctrl.echoJsonBody, request)
      resultBody <- contentAsJson(result)
    } yield {
      assertResult(testJson) {
        resultBody
      }
    }
  }

  s"${testSubject(methodName = "header")} should extract the expected header" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val expectedHeaderName = "expected"
    val expectedHeaderValue = "value"
    val request = FakeRequest("POST", "/test/header").withJsonBody(Json.obj(
      "name" ->  expectedHeaderName,
      "value" -> expectedHeaderValue
    ))
    for {
      result <- call(ctrl.echoJsonInHeader, request)
    } yield {
      assertResult(Some(expectedHeaderValue)) {
        header(expectedHeaderName, result)
      }
    }
  }

  s"${testSubject(methodName = "cookies")} should extract the expected cookie" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val expectedCookie = Cookie("expected", "cookie")
    val request = FakeRequest("POST", "/test/cookie").withJsonBody(Json.obj(
      "name" -> expectedCookie.name,
      "value" -> expectedCookie.value
    ))
    for {
      result <- call(ctrl.echoJsonInCookie, request)
    } yield {
      assertResult(Some(expectedCookie)) {
        cookies(result).get(expectedCookie.name)
      }
    }
  }

  s"${testSubject(methodName = "session")} should extract the expected session data" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val expectedSession = Session(Map("k1" -> "v1", "k2" -> "v2"))
    val request = FakeRequest("POST", "/test/session").withJsonBody(Json.toJson(expectedSession.data))
    for {
      result <- call(ctrl.echoJsonInSession, request)
    } yield {
      assertResult(expectedSession) {
        session(result)
      }
    }
  }

  s"${testSubject(methodName = "flash")} should extract the expected flash data" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val expectedFlash = Flash(Map("k1" -> "v1", "k2" -> "v2"))
    val request = FakeRequest("POST", "/test/flash").withJsonBody(Json.toJson(expectedFlash.data))
    for {
      result <- call(ctrl.echoJsonInFlash, request)
    } yield {
      assertResult(expectedFlash) {
        flash(result)
      }
    }
  }

  s"${testSubject(methodName = "redirectLocation")} should extract the expected redirect url from 301" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val redirectUrl = "test redirect"
    val request = FakeRequest("POST", "/test/redirect").withJsonBody(Json.obj(
      "url" -> redirectUrl,
      "status" -> Status.MOVED_PERMANENTLY
    ))
    for {
      result <- call(ctrl.redirectToBody, request)
    } yield {
      assertResult(Some(redirectUrl)) {
        redirectLocation(result)
      }
    }
  }

  s"${testSubject(methodName = "redirectLocation")} should extract the expected redirect url from 302" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val redirectUrl = "test redirect"
    val request = FakeRequest("POST", "/test/redirect").withJsonBody(Json.obj(
      "url" -> redirectUrl,
      "status" -> Status.FOUND
    ))
    for {
      result <- call(ctrl.redirectToBody, request)
    } yield {
      assertResult(Some(redirectUrl)) {
        redirectLocation(result)
      }
    }
  }

  s"${testSubject(methodName = "redirectLocation")} should extract the expected redirect url from 303" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val redirectUrl = "test redirect"
    val request = FakeRequest("POST", "/test/redirect").withJsonBody(Json.obj(
      "url" -> redirectUrl,
      "status" -> Status.SEE_OTHER
    ))
    for {
      result <- call(ctrl.redirectToBody, request)
    } yield {
      assertResult(Some(redirectUrl)) {
        redirectLocation(result)
      }
    }
  }

  s"${testSubject(methodName = "redirectLocation")} should extract the expected redirect url from 307" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val redirectUrl = "test redirect"
    val request = FakeRequest("POST", "/test/redirect").withJsonBody(Json.obj(
      "url" -> redirectUrl,
      "status" -> Status.TEMPORARY_REDIRECT
    ))
    for {
      result <- call(ctrl.redirectToBody, request)
    } yield {
      assertResult(Some(redirectUrl)) {
        redirectLocation(result)
      }
    }
  }

  s"${testSubject(methodName = "redirectLocation")} should NOT extract a redirect url from a 400" in { fixture =>
    import fixture.extractors._
    val ctrl = new TestEchoController
    val redirectUrl = "test redirect"
    val request = FakeRequest("POST", "/test/redirect").withJsonBody(Json.obj(
      "url" -> redirectUrl,
      "status" -> Status.BAD_REQUEST
    ))
    for {
      result <- call(ctrl.redirectToBody, request)
    } yield {
      assertResult(None) {
        redirectLocation(result)
      }
    }
  }
}

