package com.example.firebaseplayground.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser


class VerifyEmail(private val activity: Activity, private val user: FirebaseUser, private val done: () -> Unit) {
    private lateinit var dialog: AlertDialog

    fun show() {
        val builder = AlertDialog.Builder(activity)
            .setMessage(
                """
                    A verification link has been sent to ${user.email}, click on the link to verify your email.
                     
                    Click the verify button once your email has been verified or resend to receive a new link
                """.trimIndent()
            )
            .setNegativeButton("Close") { d, _ -> d.dismiss() }
            .setNeutralButton("Resend") { _, _ -> resend() }
            .setPositiveButton("Verify") { _, _ -> verify() }
        dialog = builder.create()
        dialog.show()
    }

    private fun verify() {
        user.reload()
        if (user.isEmailVerified) {
            Toast.makeText(activity, "Email address verified", Toast.LENGTH_SHORT).show()
            dismiss()
            done()
        } else {
            Toast.makeText(activity, "Email address not verified", Toast.LENGTH_SHORT).show()
            show()
        }
    }

    private fun resend() {
        dismiss()
        val loading = LoadingDialog(activity)
        loading.start()
        user.sendEmailVerification().addOnCompleteListener { task ->
            loading.stop()
            if (task.isSuccessful) {
                Toast.makeText(activity, "Verification link sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, task.exception?.message.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
            show()
        }
    }

    private fun dismiss() {
        dialog.dismiss()
    }
}