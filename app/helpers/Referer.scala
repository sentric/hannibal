package helpers

import play.api.mvc.Request
import java.net.URL

object Referer {
  def getReferer(implicit request: Request[_]) = request.headers.get("Referer").map { headerValue => new URL(headerValue) }

  def isInternal(implicit request: Request[_]) = getReferer(request).map { referer => referer.getAuthority().equals(request.host) }.getOrElse(false)

  def isFromPath(path: String)(implicit request: Request[_]): Boolean = getReferer(request).map { referer => referer.getPath().equals(path) }.getOrElse(false)

  def isFromPath(reverseRoute: play.api.mvc.Call)(implicit request: Request[_]): Boolean = isFromPath(reverseRoute.url)(request)

}