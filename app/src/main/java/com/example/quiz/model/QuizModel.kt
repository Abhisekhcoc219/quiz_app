package com.example.quiz.model

import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
data class QuizModel(val question:String,val option:List<String>,val ans:String)
