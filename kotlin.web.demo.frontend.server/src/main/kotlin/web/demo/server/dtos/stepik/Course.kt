package web.demo.server.dtos.stepik

/**
 *
 * DTO - objects like object from Stepik structure
 *
 * @author Alexander Prendota on 2/21/18 JetBrains.
 */
data class Course(
        var id: String = "",
        var lessons: List<Lesson>)

data class Lesson(var id: String,
                  var steps: List<String>,
                  var title: String,
                  var task: List<Options> = emptyList())
