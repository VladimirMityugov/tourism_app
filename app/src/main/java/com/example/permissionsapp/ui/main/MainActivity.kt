package com.example.permissionsapp.ui.main


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.permissionsapp.ui.main.photos.ListPhotosFragment
import com.example.tourismApp.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("username")
        val bundle = Bundle()
        bundle.putString("username", username)

        val listPhotosFragment = ListPhotosFragment.newInstance()
        listPhotosFragment.arguments = bundle

        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentLayout, listPhotosFragment)
                .commitNow()
        }
    }
}

