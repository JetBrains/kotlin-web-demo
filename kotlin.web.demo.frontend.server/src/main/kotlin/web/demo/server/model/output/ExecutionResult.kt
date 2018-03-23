package web.demo.server.model.output

/**
 * Structure from Backend-server.
 *  - [JUnitExecutionResult]  - for running run with `junit` type
 *  - [KotlinExecutionResult] - plain running code on JVM of JavaScript
 *
 * @author Alexander Prendota on 3/20/18 JetBrains.
 */
open class ExecutionResult(var errors: Map<String, List<ErrorDescriptor>> = emptyMap())


class JUnitExecutionResult(var testResults: MutableMap<String, List<TestRunInfo>>?,
                           errors: Map<String, List<ErrorDescriptor>>) : ExecutionResult(errors)

class KotlinExecutionResult(var text: String, var exception: ExceptionDescriptor?,
                            errors: Map<String, List<ErrorDescriptor>>) : ExecutionResult(errors)

class TestRunInfo(var output: String,
                  var sourceFileName: String?,
                  var className: String,
                  var methodName: String,
                  var executionTime: Long,
                  var exception: ExceptionDescriptor?,
                  var comparisonFailure: ComparisonFailureDescriptor?,
                  var methodPosition: Int = 0,
                  var status: Status)

open class ExceptionDescriptor(var message: String,
                               var fullName: String,
                               var stackTrace: List<StackTraceElement>,
                               var cause: ExceptionDescriptor?)

class ComparisonFailureDescriptor(message: String,
                                  fullName: String,
                                  stackTrace: List<StackTraceElement>,
                                  cause: ExceptionDescriptor?,
                                  var expected: String?,
                                  var actual: String?) : ExceptionDescriptor(message, fullName, stackTrace, cause)

class ErrorDescriptor(var intervar: TextInterval?,
                      var message: String?,
                      var severity: Severity,
                      var className: String? = null)

class TextInterval(var start: TextPosition?,
                   var end: TextPosition?)

class TextPosition(var line: Int?,
                   var ch: Int?)

enum class Status {
    OK,
    FAIL,
    ERROR
}

enum class Severity {
    INFO,
    ERROR,
    WARNING
}

