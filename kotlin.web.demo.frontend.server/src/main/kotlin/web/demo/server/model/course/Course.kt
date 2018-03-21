package web.demo.server.model.course

/**
 * @author Alexander Prendota on 3/14/18 JetBrains.
 */
data class Course(
        var id: String,
        var title: String,
        var lessons: List<Chapter>)

data class Chapter(var id: String,
                   var title: String,
                   var task: List<Lesson>)

data class Lesson(
        var id: String,
        var title: String,
        var text: String,
        var type: String,
        var confType: String,
        var args: String,
        var compilerVersion: String,
        var expectedOutput: Boolean,
        var readOnlyFileNames: List<String>,
        var courseFiles: List<CourseFile>)

open class CourseFile(var name: String,
                      var modifyAble: Boolean,
                      var text: String,
                      var hidden: Boolean)

class TaskFile(name: String,
               modifyAble: Boolean,
               text: String,
               var placeholderAnswers: List<PlaceholderAnswer>,
               hidden: Boolean) : CourseFile(name, modifyAble, text, hidden)

data class PlaceholderAnswer(
        var text: String,
        var answer: String)