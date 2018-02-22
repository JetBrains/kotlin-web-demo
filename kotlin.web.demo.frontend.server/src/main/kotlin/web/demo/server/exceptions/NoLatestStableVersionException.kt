package web.demo.server.exceptions

/**
 * @author Alexander Prendota on 2/6/18 JetBrains.
 */
class NoLatestStableVersionException(override var message: String) : Exception(message)