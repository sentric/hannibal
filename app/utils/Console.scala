package utils

/**
 * Created by IntelliJ IDEA.
 * User: nkuebler
 * Date: 27.09.12
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */

object Console {
   def startApp() = play.api.Play.start(new play.api.Application(new java.io.File("."), classOf[play.core.StaticApplication].getClassLoader, None, play.api.Mode.Test))
}