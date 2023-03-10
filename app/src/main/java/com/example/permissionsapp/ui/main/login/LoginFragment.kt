package com.example.permissionsapp.ui.main.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.permissionsapp.presentation.view_models.LoginViewModel
import com.example.permissionsapp.ui.main.MainActivity
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "SIGN_IN"

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authentification: FirebaseAuth

    private var isEmailEntered: Boolean = false
    private var isPasswordEntered: Boolean = false

    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authentification = Firebase.auth
        val user = authentification.currentUser
        if (user != null) {
            viewModel.updateLaunchStatus(false)
            Log.d(TAG, "SET LAUNCH STATUS TO FALSE")
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = binding.login
        val email = binding.email
        val password = binding.password
        val createAccountButton = binding.goToCreateAccountButton

        createAccountButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentRegistrationLayout, CreateAccountFragment.newInstance())
                .addToBackStack("SignInFrag")
                .commit()
        }

        email.doOnTextChanged { text, _, _, _ ->
            if (text?.trim()?.count()!! >= 6) {
                isEmailEntered = true
                viewModel.switchEmailStatus(isEmailEntered)
            } else {
                isEmailEntered = false
                viewModel.switchEmailStatus(isEmailEntered)
            }
        }

        password.doOnTextChanged { text, _, _, _ ->
            if (text?.trim()?.count()!! >= 1) {
                isPasswordEntered = true
                viewModel.switchPasswordStatus(isPasswordEntered)
            } else {
                isPasswordEntered = false
                viewModel.switchPasswordStatus(isPasswordEntered)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isEmailEntered.collectLatest { isEmailEntered ->
                viewModel.isPasswordEntered.collectLatest { isPasswordEntered ->
                    if (isEmailEntered && isPasswordEntered) {
                        loginButton.isEnabled = true
                        loginButton.isActivated = true
                        loginButton.isClickable = true
                    } else {
                        loginButton.isEnabled = false
                        loginButton.isActivated = false
                        loginButton.isClickable = false
                    }
                }
            }
        }

        loginButton.setOnClickListener {
            when {
                TextUtils.isEmpty(email.text.toString().trim { it <= ' ' }) ->
                    Toast.makeText(
                        requireContext(),
                        "Please, fill in email field",
                        Toast.LENGTH_SHORT
                    ).show()
                TextUtils.isEmpty(password.text.toString().trim { it <= ' ' }) ->
                    Toast.makeText(
                        requireContext(),
                        "Please, fill in password field",
                        Toast.LENGTH_SHORT
                    ).show()
                else -> {
                    val emailFormatted: String = email.text.toString().trim { it <= ' ' }.lowercase()
                    val passwordFormatted: String = password.text.toString().trim { it <= ' ' }
                    signIn(emailFormatted, passwordFormatted)
                }
            }
        }
    }

    private fun signIn(email: String, password: String) {

        authentification.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModel.updateLaunchStatus(true)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(
                        requireContext(), "Please check introduced data or create an account",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    companion object {
        fun newInstance() = LoginFragment()
    }

}