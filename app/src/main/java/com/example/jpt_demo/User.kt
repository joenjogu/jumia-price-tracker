package com.example.jpt_demo

import com.google.firebase.database.Exclude

data class User (
    @get: Exclude
    var userid : String? = null,
    @get: Exclude
    var isLoggedIn : Boolean = false
)