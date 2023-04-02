package com.example.tourismapp.ui.main


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tourismapp.R
import com.example.tourismapp.databinding.ActivityMainBinding
import com.example.tourismapp.presentation.services.LocationService
import com.example.tourismapp.presentation.view_models.MainViewModel
import com.example.tourismapp.presentation.utility.Constants
import com.example.tourismapp.presentation.utility.Constants.REQUIRED_LOCATION_PERMISSIONS
import com.example.tourismapp.presentation.utility.permissions.hasLocationPermission
import com.example.tourismapp.presentation.utility.permissions.*
import com.example.tourismapp.presentation.view_models.PermissionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private val viewModel: MainViewModel by viewModels()
    private val permissionViewModel: PermissionsViewModel by viewModels()


    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it.keys.forEach { permission ->
                permissionViewModel.onPermissionResult(
                    permission, it[permission] == true
                )
                if(it[permission] == true){
                    permissionViewModel.dismissDialog(permission)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        //set transparent background
        val navigationView = binding.bottomNavView
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

        val newRouteButton = binding.newRouteButton
        newRouteButton.setOnClickListener {
            if (applicationContext.hasLocationPermission()) {
                if (!LocationService.isTracking.value && !LocationService.isOnRoute.value) {
                    viewModel.selectRouteName(null)
                }
                navController.navigate(R.id.action_global_mapsFragment)
            } else {
                permissionLauncher.launch(REQUIRED_LOCATION_PERMISSIONS)
            }
        }


        lifecycleScope.launch {
            permissionViewModel.permissionsList.collectLatest {
                it
                    .reversed()
                    .forEach { permission ->
                        providePermissionDialog(
                            this@MainActivity,
                            permissionDialogTextProvider = when (permission) {
                                android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                    AccessCoarseLocationPermission()
                                }
                                android.Manifest.permission.ACCESS_FINE_LOCATION -> {
                                    AccessFineLocationPermission()
                                }
                                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                                    AccessBackgroundLocationPermission()
                                }
                                else -> {
                                    return@forEach
                                }
                            },
                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(permission),
                            onOkClick = {
                                permissionViewModel.dismissDialog(permission)
                                permissionLauncher.launch(arrayOf(permission))
                            },
                            onDismissClick = { permissionViewModel.dismissDialog(permission) },
                            onGoToAppSettingsCLick = { this@MainActivity.openAppSettings() }
                        )
                    }
            }
        }

        //Navigation buttons click handler
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
            lifecycleScope.launch {
                val allRoutes = viewModel.getAllRoutes().first()
                viewModel.selectLastRouteName(allRoutes)
            }
            navController.navigate(R.id.action_global_mapsFragment)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}

