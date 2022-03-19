package com.kodimstudio.dota2stats.api.model

data class MakeRequestResponse(
    val job: JobResponse
) {
    data class JobResponse(
        val jobId: Long
    )
}
