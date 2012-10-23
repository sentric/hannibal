package helpers

import play.api.mvc.Request
import java.net.URL

object Referer {
  def getReferer(implicit request: Request[Any]) = {
    request.headers.get("Referer").map { headerValue => new URL(headerValue) }
  }

  def isInternal(implicit request: Request[Any]) = {
    getReferer(request).map { referer => referer.getAuthority().equals(request.host) }.getOrElse(false)
  }

  def isFromPath(path: String)(implicit request: Request[Any]): Boolean = {
	getReferer(request).map { referer => referer.getPath().equals(path) }.getOrElse(false)
  }
  
  def isFromPath(reverseRoute: play.api.mvc.Call)(implicit request: Request[Any]): Boolean = {
    isFromPath(reverseRoute.url)(request)
  }
}