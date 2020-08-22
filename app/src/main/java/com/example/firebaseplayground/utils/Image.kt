package com.example.firebaseplayground.utils

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class Image {
    fun upload(
        context: Context,
        ref: StorageReference,
        bitmap: Bitmap,
        done: (url: String) -> Unit
    ) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = ref.putBytes(data)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val url = task.result.toString()
                done(url)
            }
        }
    }
}