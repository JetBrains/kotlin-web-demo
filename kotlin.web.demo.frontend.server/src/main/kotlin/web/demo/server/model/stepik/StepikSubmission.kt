package web.demo.server.model.stepik

/**
 * Stepik structure for operation with Attempts ans Submissions
 *
 * @see <a href="https://stepik.org/api/docs/#!/attempts">Stepik Attempts</a>
 * @see <a href="https://stepik.org/api/docs/#!/submissions">Stepik Submissions</a>
 *
 * @author Alexander Prendota on 3/20/18 JetBrains.
 */
data class StepikSubmissionContainer(var submissions: List<StepikSubmission>)

data class StepikSubmission(var reply: StepikReply,
                            var attempt: String)

data class SubmissionWrapper(var submission: StepikSubmission)

data class StepikReply(var solution: List<StepikSolution>,
                       var score: String)

data class StepikSolution(var name: String,
                          var text: String)

data class StepikAttemptContainer(var attempts: List<StepikAttempt>)

data class AttemptWrapper(var attempt: StepikAttempt)

data class StepikAttempt(var id: String,
                         var step: String)
