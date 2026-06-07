package com.example.focusquest.data.repository

import com.example.focusquest.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LeaderboardRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun syncUserScore(user: User) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("xp", user.xp, "level", user.level)
    }
}
