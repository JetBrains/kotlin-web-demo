package web.demo.server.dtos.stepik

/**
 * BFR like BFS.
 *
 * POJO for mapping stepik response.
 *
 * @see <a href="https://www.neowin.net/news/elon-musks-plans-for-the-big-fucking-rocket-mars-moon-and-earth">PFR</a>
 * @see <a href="https://stepik.org/api/docs/#!/steps/Step_list">Stepik API</a>
 *
 * @author Alexander Prendota on 3/12/18 JetBrains.
 */
data class StepikCourseContainer(var courses: List<StepikCourse>)

data class StepikCourse(
        var id: String = "",
        var title: String = "",
        var lessons: List<StepikLesson> = emptyList())

data class StepikLesson(var id: String,
                        var steps: List<String>,
                        var title: String,
                        var task: List<StepikOptions> = emptyList())

data class StepikStepsContainer(var steps: List<StepikSteps>)

data class StepikSteps(var id: String,
                       var block: StepikBlock)

data class StepikBlock(var options: StepikOptions)

data class StepikOptions(
        var title: String,
        var test: List<StepikText>,
        var files: List<StepikTask>,
        var text: List<StepikText>)

open class StepikText(var name: String, var text: String)

class StepikTask(name: String,
                 text: String,
                 var placeholders: List<StepikPlaceholders>) : StepikText(name, text)

data class StepikPlaceholders(var subtask_infos: List<StepikSubtaskInfos>)

data class StepikSubtaskInfos(var possible_answer: String, var placeholder_text: String)