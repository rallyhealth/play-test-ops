package play.api.test.ops

import play.api.test.ops.ActorTypes._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.BeforeAndAfterAll
import play.api.http.{MimeTypes, Status}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._

class AsyncResultExtractorsSpec extends AsyncFreeSpec
  with BeforeAndAfterAll
  with AsyncResultExtractors
  with EssentialActionCaller
  with Writeables {

  private lazy val sys: ActorSystem = ActorSystem(getClass.getSimpleName)
  implicit private lazy val mat: Materializer = Materializer(sys)

  override protected def afterAll(): Unit = {
    Await.ready(sys.terminate(), 1.second)
  }

  class TestEchoController extends AbstractController(Helpers.stubControllerComponents(
    playBodyParsers = Helpers.stubPlayBodyParsers(mat),
    executionContext = executionContext
  )) {

    // you must specify a parser for test actions otherwise Play will assume AnyContentAsEmpty
    def echoTextBody: EssentialAction = Action(parse.tolerantText) { request =>
      Ok(request.body)
    }

    def echoJsonBody: Action[JsValue] = Action(parse.json) { request =>
      Ok(request.body)
    }

    def echoJsonInHeader: Action[JsValue] = Action(parse.json) { request =>
      val name = (request.body \ "name").as[String]
      val value = (request.body \ "value").as[String]
      Ok.withHeaders(name -> value)
    }

    def echoJsonInCookie: Action[JsValue] = Action(parse.json) { request =>
      val name = (request.body \ "name").as[String]
      val value = (request.body \ "value").as[String]
      Ok.withCookies(Cookie(name, value))
    }

    def echoJsonInSession: Action[JsValue] = Action(parse.json) { request =>
      val sessionData = request.body.as[Map[String, String]]
      Ok.withSession(sessionData.toSeq: _*)
    }

    def echoJsonInFlash: Action[JsValue] = Action(parse.json) { request =>
      val flashData = request.body.as[Map[String, String]]
      Ok.flashing(flashData.toSeq: _*)
    }

    def redirectToBody: Action[JsValue] = Action(parse.json) { request =>
      val url = (request.body \ "url").as[String]
      val status = (request.body \ "status").as[Int]
      Redirect(url, status)
    }
  }

  protected def method(name: String) = s"play28+.AsyncResultExtractors.$name"

  behave like parsesContentUsing("ActorMaterializer", mat)
  behave like parsesContentUsing("NoMaterializer", NoMaterializer)

  protected def parsesContentUsing(materializerName: String, contentMaterializer: Materializer): Unit = {

    s"${method("contentAsString")}($materializerName) should extract the expected text" in {
      val ctrl = new TestEchoController
      val testString = "test"
      val request = FakeRequest("POST", s"/test/contentAsString?mat=$materializerName").withTextBody(testString)
      for {
        result <- call(ctrl.echoTextBody, request)
        resultBody <- contentAsString(result)(implicitly, contentMaterializer)
      } yield {
        assertResult(testString) {
          resultBody
        }
      }
    }

    s"${method("contentAsJson")}($materializerName) should extract the expected json" in {
      val ctrl = new TestEchoController
      val testJson = Json.obj("expected" -> "json")
      val request = FakeRequest("POST", s"/test/contentAsJson?mat=$materializerName").withJsonBody(testJson)
      for {
        result <- call(ctrl.echoJsonBody, request)
        resultBody <- contentAsJson(result)(implicitly, contentMaterializer)
      } yield {
        assertResult(testJson) {
          resultBody
        }
      }
    }
  }

  s"${method("status")} should return the status code" in {
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

  s"${method("contentType")} should extract the expected content type from the full result" in {
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

  s"${method("charset")} should extract the expected charset" in {
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

  s"${method("header")} should extract the expected header" in {
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

  s"${method("cookies")} should extract the expected cookie" in {
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

  s"${method("session")} should extract the expected session data" in {
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

  s"${method("flash")} should extract the expected flash data" in {
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

  s"${method("redirectLocation")} should extract the expected redirect url from 301" in {
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

  s"${method("redirectLocation")} should extract the expected redirect url from 302" in {
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

  s"${method("redirectLocation")} should extract the expected redirect url from 303" in {
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

  s"${method("redirectLocation")} should extract the expected redirect url from 307" in {
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

  s"${method("redirectLocation")} should NOT extract a redirect url from a 400" in {
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

