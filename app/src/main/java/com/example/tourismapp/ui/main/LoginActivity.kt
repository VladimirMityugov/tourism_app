package com.example.tourismapp.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tourismapp.R
import com.example.tourismapp.ui.main.login.LoginFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentRegistrationLayout, LoginFragment.newInstance())
                .commitNow()
        }
    }
}
