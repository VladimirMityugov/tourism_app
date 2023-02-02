package com.example.permissionsapp.ui.main.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.permissionsapp.presentation.MyViewModel
import com.example.permissionsapp.ui.main.MainActivity
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentSignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "SIGN_IN"

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private lateinit var loginButton: AppCompatButton
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var createAccountButton: AppCompatButton
    private lateinit var authentification: FirebaseAuth
    private var isEmailEntered: Boolean = false
    private var isPasswordEntered: Boolean = false

    private val viewModel: MyViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authentification = Firebase.auth
        val user = Firebase.auth.currentUser
        if (user != null) {
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
        _binding = FragmentSignInBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton = binding.login
        email = binding.email
        password = binding.password
        createAccountButton = binding.goToCreateAccountButton

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
                    val email: String = email.text.toString().trim { it <= ' ' }.lowercase()
                    val password: String = password.text.toString().trim { it <= ' ' }
                    signIn(email, password)
                }
            }
        }
    }

    private fun signIn(email: String, password: String) {

        authentification.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
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
        fun newInstance() = SignInFragment()
    }

}