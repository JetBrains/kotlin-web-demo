package web.demo.server.model.stepik

/**
 *
 * DTO - objects like object from Stepik structure
 *
 * @author Alexander Prendota on 2/20/18 JetBrains.
 */
data class ProgressContainerDto(var progresses: List<ProgressDto>)

data class ProgressDto(var id: String,
                       var is_passed: Boolean)