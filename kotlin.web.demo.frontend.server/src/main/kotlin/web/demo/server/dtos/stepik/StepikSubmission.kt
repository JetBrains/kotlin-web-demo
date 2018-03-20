package web.demo.server.dtos.stepik

/**
 * @author Alexander Prendota on 3/20/18 JetBrains.
 */
data class StepikSubmissionContainer(var submissions: List<StepikSubmission>)

data class StepikSubmission(var reply: StepikReply)

data class StepikReply(var solution: List<StepikSolution>)

data class StepikSolution(var name: String,
                          var text: String)