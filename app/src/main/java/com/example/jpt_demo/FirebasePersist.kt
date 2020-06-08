package com.example.jpt_demo

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class FirebasePersist : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}