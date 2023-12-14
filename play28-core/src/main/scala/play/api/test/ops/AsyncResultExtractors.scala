package play.api.test.ops

import play.api.test.ops.ActorTypes._
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Similar to play.api.test.ResultExtractors except that it is designed to work better
  * for org.scalatest.AsyncSuite in ScalaTest 3.0.
  *
  * All methods return either the expected value or a [[Future]] and does not perform
  * any awaits or timeouts. This improves the total time spent on tests and cuts
  * down on processor usage.
  */
trait AsyncResultExtractors {

  /**
    * Extracts the Content-Type of this Result value.
    */
  def contentType(result: Result): Option[String] = {
    result.body.contentType.map(_.split(";").take(1).mkString.trim)
  }

  /**
    * Extracts the Charset of this Result value.
    */
  def charset(result: Result): Option[String] = {
    result.body.contentType match {
      case Some(s) if s.contains("charset=") => Some(s.split("; *charset=").drop(1).mkString.trim)
      case _ => None
    }
  }

  /**
    * Extracts the content as String.
    */
  def contentAsString(result: Result)(implicit ec: ExecutionContext, mat: Materializer = NoMaterializer): Future[String] = {
    contentAsBytes(result).map(_.decodeString(charset(result).getOrElse("utf-8")))
  }

  /**
    * Extracts the content as bytes.
    */
  def contentAsBytes(result: Result)(implicit mat: Materializer = NoMaterializer): Future[ByteString] = {
    result.body.consumeData
  }

  /**
    * Extracts the content as Json.
    */
  def contentAsJson(result: Result)(implicit ec: ExecutionContext, mat: Materializer = NoMaterializer): Future[JsValue] = {
    contentAsString(result).map(Json.parse)
  }

  /**
    * Extracts the Status code of this Result value.
    */
  def status(result: Result): Int = result.header.status

  /**
    * Gets the Cookies associated with this Result value. Note that this only extracts the "new" cookies added to
    * this result (e.g. through withCookies), not including the Session or Flash. The final set of cookies may be
    * different because the Play server automatically adds those cookies and merges the headers.
    */
  def cookies(result: Result): Cookies = Cookies(result.newCookies)

  /**
    * Extracts the Flash values set by this Result value.
    */
  def flash(result: Result): Flash = result.newFlash.getOrElse(new Flash())

  /**
    * Extracts the Session values set by this Result value.
    */
  def session(result: Result): Session = result.newSession.getOrElse(new Session())

  /**
    * Extracts the Location header of this Result value if this Result is a Redirect.
    */
  def redirectLocation(result: Result): Option[String] = result.header match {
    case ResponseHeader(FOUND, headers, _) => headers.get(LOCATION)
    case ResponseHeader(SEE_OTHER, headers, _) => headers.get(LOCATION)
    case ResponseHeader(TEMPORARY_REDIRECT, headers, _) => headers.get(LOCATION)
    case ResponseHeader(MOVED_PERMANENTLY, headers, _) => headers.get(LOCATION)
    case ResponseHeader(_, _, _) => None
  }

  /**
    * Extracts an Header value of this Result value.
    */
  def header(header: String, result: Result): Option[String] = result.header.headers.get(header)

  /**
    * Extracts all Headers of this Result value.
    */
  def headers(result: Result): Map[String, String] = result.header.headers
}
