/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package utils

object Console {
   def startApp() = play.api.Play.start(new play.api.Application(new java.io.File("."), classOf[play.core.StaticApplication].getClassLoader, None, play.api.Mode.Test))
   def stopApp() = play.api.Play.stop()
}
