package play.api.test.ops

import akka.stream.Materializer
import akka.util.ByteString
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.Accumulator
import play.api.mvc._
import play.twirl.api.Content

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
    * Extracts the Content-Type of this Content value.
    */
  def contentType(of: Content): String = of.contentType

  /**
    * Extracts the content as String.
    */
  def contentAsString(of: Content): String = of.body

  /**
    * Extracts the content as bytes.
    */
  def contentAsBytes(of: Content): Array[Byte] = of.body.getBytes

  /**
    * Extracts the content as Json.
    */
  def contentAsJson(of: Content): JsValue = Json.parse(of.body)

  /**
    * Extracts the Content-Type of this Result value.
    */
  def contentType(result: Result): Option[String] = {
    result.body.contentType.map(_.split(";").take(1).mkString.trim)
  }

  /**
    * Extracts the Content-Type of this Result value.
    */
  def contentType(result: Accumulator[ByteString, Result])(
    implicit ec: ExecutionContext, mat: Materializer): Future[Option[String]] = {
    result.run() map contentType
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
  def contentAsString(result: Result)(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {
    contentAsBytes(result).map(_.decodeString(charset(result).getOrElse("utf-8")))
  }

  /**
    * Extracts the content as bytes.
    */
  def contentAsBytes(result: Result)(implicit mat: Materializer): Future[ByteString] = {
    result.body.consumeData
  }

  /**
    * Extracts the content as Json.
    */
  def contentAsJson(result: Result)(implicit ec: ExecutionContext, mat: Materializer): Future[JsValue] = {
    contentAsString(result).map(Json.parse)
  }

  /**
    * Extracts the Status code of this Result value.
    */
  def status(result: Result): Int = result.header.status

  /**
    * Extracts the Cookies of this Result value.
    */
  def cookies(result: Result): Cookies = Cookies.fromSetCookieHeader(header(SET_COOKIE, result))

  /**
    * Extracts the Flash values of this Result value.
    */
  def flash(result: Result): Flash = Flash.decodeFromCookie(cookies(result).get(Flash.COOKIE_NAME))

  /**
    * Extracts the Session of this Result value.
    * Extracts the Session from this Result value.
    */
  def session(result: Result): Session = Session.decodeFromCookie(cookies(result).get(Session.COOKIE_NAME))

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
