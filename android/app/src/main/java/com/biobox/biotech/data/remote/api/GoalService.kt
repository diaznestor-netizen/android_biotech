package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface GoalService {
    @GET("goals")
    suspend fun getGoals(): Response<List<GoalDto>>

    @GET("goals/{id}")
    suspend fun getGoalById(@Path("id") id: Int): Response<GoalDto>

    @POST("goals")
    suspend fun createGoal(@Body request: GoalRequest): Response<GoalDto>

    @PUT("goals/{id}")
    suspend fun updateGoal(@Path("id") id: Int, @Body request: GoalRequest): Response<GoalDto>

    @DELETE("goals/{id}")
    suspend fun deleteGoal(@Path("id") id: Int): Response<Unit>
}
