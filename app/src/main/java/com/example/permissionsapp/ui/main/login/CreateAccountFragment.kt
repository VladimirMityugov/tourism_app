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
import com.example.tourismApp.databinding.FragmentCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest


private const val TAG = "CREATE_ACCOUNT"

class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var createAccountButton: AppCompatButton
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: AppCompatButton
    private lateinit var username: String
    private lateinit var authentification: FirebaseAuth
    private var isEmailEntered: Boolean = false
    private var isPasswordEntered: Boolean = false
    private var isNameEntered: Boolean = false

    private val viewModel: MyViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authentification = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createAccountButton = binding.createAccount
        name = binding.userName
        email = binding.email
        password = binding.password
        loginButton = binding.goToLoginButton

        loginButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentRegistrationLayout, SignInFragment.newInstance())
                .commit()
        }

        name.doOnTextChanged { text, _, _, _ ->
            if (text?.trim()?.count()!! >= 3) {
                isNameEntered = true
                viewModel.switchNameStatus(isNameEntered)
            } else {
                isNameEntered = false
                viewModel.switchNameStatus(isNameEntered)
            }
        }

        email.doOnTextChanged { text, _, _, _ ->
            if (text?.trim()?.count()!! >= 6) {
                isEmailEntered = true
                viewModel.switchRegistrationEmailStatus(isEmailEntered)
            } else {
                isEmailEntered = false
                viewModel.switchRegistrationEmailStatus(isEmailEntered)
            }
        }

        password.doOnTextChanged { text, _, _, _ ->
            if (text?.trim()?.count()!! >= 1) {
                isPasswordEntered = true
                viewModel.switchRegistrationPasswordStatus(isPasswordEntered)
            } else {
                isPasswordEntered = false
                viewModel.switchRegistrationPasswordStatus(isPasswordEntered)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isNewEmailEntered.collectLatest { isEmailEntered ->
                viewModel.isNewPasswordEntered.collectLatest { isPasswordEntered ->
                    viewModel.isNameEntered.collectLatest { isNameEntered ->
                        if (isEmailEntered && isPasswordEntered && isNameEntered) {
                            createAccountButton.isEnabled = true
                            createAccountButton.isActivated = true
                            createAccountButton.isClickable = true
                        } else {
                            createAccountButton.isEnabled = false
                            createAccountButton.isActivated = false
                            createAccountButton.isClickable = false
                        }
                    }
                }
            }
        }

        createAccountButton.setOnClickListener {
            when {
                TextUtils.isEmpty(name.text.toString().trim { it <= ' ' }) ->
                    Toast.makeText(
                        requireContext(),
                        "Please, enter name",
                        Toast.LENGTH_SHORT
                    ).show()
                TextUtils.isEmpty(email.text.toString().trim { it <= ' ' }) ->
                    Toast.makeText(
                        requireContext(),
                        "Please, enter email",
                        Toast.LENGTH_SHORT
                    ).show()
                TextUtils.isEmpty(password.text.toString().trim { it <= ' ' }) ->
                    Toast.makeText(
                        requireContext(),
                        "Please, enter password",
                        Toast.LENGTH_SHORT
                    ).show()
                else -> {
                    username = name.text.toString().trim { it <= ' ' }
                    val email: String = email.text.toString().trim { it <= ' ' }
                    val password: String = password.text.toString().trim { it <= ' ' }
                    createAccount(email, password)
                }
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        authentification.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                                    val user = authentification.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = username
                        viewModel.setUserName(username)
                    }

                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                            }
                        }
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                               startActivity(intent)
                    requireActivity().finish()

                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    companion object {
        fun newInstance() = CreateAccountFragment()
    }
}