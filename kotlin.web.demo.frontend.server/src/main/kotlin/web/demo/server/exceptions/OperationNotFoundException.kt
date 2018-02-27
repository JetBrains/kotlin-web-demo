package web.demo.server.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Alexander Prendota on 2/26/18 JetBrains.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Operation is not supported")
class OperationNotFoundException(override var message: String) : RuntimeException(message)