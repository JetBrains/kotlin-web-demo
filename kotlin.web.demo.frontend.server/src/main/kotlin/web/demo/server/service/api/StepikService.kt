package web.demo.server.service.api

import web.demo.server.dtos.stepic.ProgressDto

/**
 * @author Alexander Prendota on 2/20/18 JetBrains.
 */
interface StepikService {
    fun getCourseProgress(courseId: String, tokenValue: String): List<ProgressDto>
}