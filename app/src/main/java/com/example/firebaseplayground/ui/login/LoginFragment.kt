package com.example.firebaseplayground.ui.login

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
import com.example.firebaseplayground.ui.dialogs.LoadingDialog
import com.example.firebaseplayground.ui.dialogs.VerifyEmail
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
    private lateinit var viewModel: LoginViewModel
    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.login_fragment, container, false)

        view.findViewById<Button>(R.id.btn_register).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        view.findViewById<Button>(R.id.btn_login).setOnClickListener {
            handleClick(view)
        }

        return view
    }

    private fun handleClick(view: View) {
        val email = view.findViewById<TextInputEditText>(R.id.input_email).text.toString()
        val password = view.findViewById<TextInputEditText>(R.id.input_password).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
        } else {
            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        val loading = LoadingDialog(requireActivity())
        loading.start()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    if (user.isEmailVerified) {
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else {
                        VerifyEmail(requireActivity(), user) {
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        }.show()
                    }
                }
                loading.stop()
            } else {
                Snackbar.make(
                    requireView(), task.exception?.message.toString(), Snackbar.LENGTH_SHORT
                )
                    .setBackgroundTint(resources.getColor(R.color.red, context?.theme))
                    .setTextColor(resources.getColor(R.color.white, context?.theme)).show()
                loading.stop()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        // TODO: Use the ViewModel
    }
}
