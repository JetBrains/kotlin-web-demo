package web.demo.server.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Alexander Prendota on 2/28/18 JetBrains.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Validation exception")
class ValidationException(override var message: String) : RuntimeException(message)