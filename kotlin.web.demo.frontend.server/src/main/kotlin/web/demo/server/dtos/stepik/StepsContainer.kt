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
data class StepsContainer(var steps: List<Steps>)

data class Steps(var id: String,
                 var block: Block)

data class Block(var options: Options)

data class Options(
        var title: String,
        var test: List<Text>,
        var files: List<Task>,
        var text: List<Text>)

data class Text(var name: String, var text: String)

data class Task(var name: String,
                var text: String,
                var placeholders: List<Placeholders>)

data class Placeholders(var subtask_infos: List<SubtaskInfos>)

data class SubtaskInfos(var possible_answer: String, var placeholder_text: String)
