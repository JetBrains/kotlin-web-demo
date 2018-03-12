package web.demo.server.dtos.stepik

/**
 *
 * DTO - objects like object from Stepik structure
 *
 * @author Alexander Prendota on 2/21/18 JetBrains.
 */
data class CourseDto(var lessons: List<LessonDto>)

data class LessonDto(var id: String,
                     var steps: List<String>,
                     var title: String)
