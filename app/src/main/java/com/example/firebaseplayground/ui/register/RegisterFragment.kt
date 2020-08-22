package com.example.firebaseplayground.ui.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.firebaseplayground.R
import com.example.firebaseplayground.models.User
import com.example.firebaseplayground.ui.dialogs.LoadingDialog
import com.example.firebaseplayground.ui.dialogs.VerifyEmail
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {
    private lateinit var viewModel: RegisterViewModel
    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.register_fragment, container, false)

        view.findViewById<Button>(R.id.btn_register).setOnClickListener {
            handleClick(view)
        }

        return view
    }

    private fun handleClick(view: View) {
        val email = view.findViewById<TextInputEditText>(R.id.input_email).text.toString()
        val password = view.findViewById<TextInputEditText>(R.id.input_password).text.toString()
        val confirmPassword =
            view.findViewById<TextInputEditText>(R.id.input_confirm_password).text.toString()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
        } else {
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT)
                    .show()
            } else {
                register(email, password)
            }
        }
    }

    private fun register(email: String, password: String) {
        val loading = LoadingDialog(requireActivity())
        loading.start()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) sendVerificationEmail {

                    val value = User(
                        uid = user.uid,
                        name = email.substring(0, email.indexOf('@')),
                        phone = "",
                        photoUrl = "",
                        securityLevel = "1"
                    )

                    val database = Firebase.database.reference
                    database.child(resources.getString(R.string.dbnode_users))
                        .child(user.uid)
                        .setValue(value)
                        .addOnCompleteListener { task ->
                            loading.stop()
                            if (task.isSuccessful) {
                                VerifyEmail(requireActivity(), user) {
                                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                                    Snackbar.make(
                                        requireView(),
                                        "Email verified!",
                                        Snackbar.LENGTH_SHORT
                                    )
                                        .setBackgroundTint(
                                            resources.getColor(
                                                R.color.green,
                                                context?.theme
                                            )
                                        )
                                        .setTextColor(
                                            resources.getColor(
                                                R.color.white,
                                                context?.theme
                                            )
                                        ).show()
                                }.show()
                            }
                        }

                }
                else loading.stop()
            } else {
                Snackbar.make(
                    requireView(),
                    task.exception?.message.toString(),
                    Snackbar.LENGTH_SHORT
                )
                    .setBackgroundTint(resources.getColor(R.color.red, context?.theme))
                    .setTextColor(resources.getColor(R.color.white, context?.theme)).show()
                loading.stop()
            }
        }
    }

    private fun sendVerificationEmail(callback: (success: Boolean) -> Unit) {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        // TODO: Use the ViewModel
    }

}