package com.example.firebaseplayground.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var uid: String? = "",
    var name: String? = "",
    var phone: String? = "",
    var photoUrl: String? = "",
    var securityLevel: String? = ""
)