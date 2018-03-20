package web.demo.server.model.stepik

/**
 * @author Alexander Prendota on 3/20/18 JetBrains.
 */
data class StepikSubmissionContainer(var submissions: List<StepikSubmission>)

data class StepikSubmission(var reply: StepikReply)

data class StepikReply(var solution: List<StepikSolution>)

data class StepikSolution(var name: String,
                          var text: String)

data class StepikAttemptContainer(var attempts: List<StepikAttempt>)

data class StepikAttempt(var id: String)
