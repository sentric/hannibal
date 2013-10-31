package helpers

object Utils {
  //tod remove after migration to Play 2.1 - use "@helper.urlEncode"
  def urlEncode(value: String) = java.net.URLEncoder.encode(value, "UTF-8")
}
