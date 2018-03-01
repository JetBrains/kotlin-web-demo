package web.demo.server.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Alexander Prendota on 2/28/18 JetBrains.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Source not found")
class SourceNotFoundException(override var message: String) : Exception(message)