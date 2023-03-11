package com.example.permissionsapp.ui.main


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.permissionsapp.data.user_preferences.UserPreferences
import com.example.permissionsapp.presentation.view_models.MainViewModel
import com.example.permissionsapp.presentation.services.LocationService
import com.example.permissionsapp.presentation.utility.Constants
import com.example.tourismApp.R
import com.example.tourismApp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MAIN_ACTIVITY"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationView: BottomNavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var newRouteButton: FloatingActionButton

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        //set transparent background
        navigationView = binding.bottomNavView
        navigationView.background = null

        //set navHostFragment and navController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        //set top level destinations IDs
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.main, R.id.profile)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        lifecycleScope.launch {
            if (viewModel.getDataStore().first().isFirstLaunch) {
                Log.d(TAG, "Is first launch true")
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_profile, true)
                    .build()
                navController.navigate(
                    R.id.action_global_profileFragment,
                    savedInstanceState,
                    navOptions = navOptions
                )
            }
        }



        newRouteButton = binding.newRouteButton
        newRouteButton.setOnClickListener {
            if (!LocationService.isTracking.value && !LocationService.isOnRoute.value) {
                viewModel.selectRouteName(null)
            }
            navController.navigate(R.id.action_global_mapsFragment)
        }

        navigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main -> navController.navigate(R.id.action_global_mainScreenFragment)
                R.id.profile -> navController.navigate(R.id.action_global_profileFragment)
            }
            true
        }

        //Navigate to maps fragment by click on location notification
        navigateToMapsIfNeeded(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //Navigate to maps fragment by click on location notification
        navigateToMapsIfNeeded(intent)
    }

    private fun navigateToMapsIfNeeded(intent: Intent?) {
        if (intent?.action == Constants.ACTION_SHOW_MAPS_FRAGMENT) {
            viewModel.selectLastRouteName()
            navController.navigate(R.id.action_global_mapsFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}

