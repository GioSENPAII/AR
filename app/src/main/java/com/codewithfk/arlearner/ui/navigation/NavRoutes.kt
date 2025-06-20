package com.codewithfk.arlearner.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeScreen

@Serializable
data class ARScreen(val model: String)

@Serializable
data class DisplayScreen(val selectedModel: String)