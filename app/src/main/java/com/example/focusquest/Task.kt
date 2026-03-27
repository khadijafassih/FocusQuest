package com.example.focusquest

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    val id: Long = System.currentTimeMillis(),
    var title: String,
    var category: String,
    var isCompleted: Boolean = false,
    val xpReward: Int = 10
) : Parcelable
