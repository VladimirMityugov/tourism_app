package com.example.permissionsapp.ui.main.main_screen


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.presentation.MyViewModel
import com.example.permissionsapp.presentation.RoutesAdapter
import com.example.permissionsapp.presentation.utility.Constants.RATIONALE_FOR_LOCATION
import com.example.permissionsapp.presentation.utility.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.permissionsapp.presentation.utility.hasLocationPermission
import com.example.permissionsapp.ui.main.LoginActivity
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentMainScreenBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


private const val TAG = "MAIN_SCREEN"

@AndroidEntryPoint
class MainScreenFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentMainScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var username: TextView
    private lateinit var signOut: AppCompatButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var routesRecyclerView: RecyclerView
    private lateinit var firstRouteButton: AppCompatImageButton
    private lateinit var infoField: AppCompatTextView
    private lateinit var scrollView: ScrollView

    private val routesAdapter = RoutesAdapter(
        onItemClick = { route -> onRouteClick(route) }
    )

    private val viewModel: MyViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainScreenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        username = binding.userTitle
        signOut = binding.signOutButton
        firebaseAuth = FirebaseAuth.getInstance()
        routesRecyclerView = binding.routesRecyclerView
        firstRouteButton = binding.firstRouteButton
        infoField = binding.infoField
        scrollView = binding.scrollView

        routesRecyclerView.adapter = routesAdapter

        signOut.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getRoutesList().collectLatest {
                if (it.isNotEmpty()) {
                    routesRecyclerView.visibility = View.VISIBLE
                    scrollView.visibility = View.VISIBLE
                    infoField.visibility = View.INVISIBLE
                    firstRouteButton.visibility = View.INVISIBLE
                    Log.d(TAG, "routes are : $it")
                    routesAdapter.submitList(it)
                } else {
                    routesRecyclerView.visibility = View.GONE
                    scrollView.visibility = View.GONE
                    infoField.visibility = View.VISIBLE
                    firstRouteButton.visibility = View.VISIBLE
                }

            }
        }


        firstRouteButton.setOnClickListener {
            if (requireContext().hasLocationPermission()) {
                findNavController().navigate(R.id.action_main_to_maps)
            } else requestPermissions()
        }


        if (firebaseAuth.currentUser?.displayName == null) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.currentUserName.collectLatest { currentName ->

                    if (currentName != null) {
                        username.isVisible = true
                        username.text = currentName
                    }
                    username.isVisible = false
                }
            }
        } else {
            username.text = firebaseAuth.currentUser?.displayName.toString()
        }

        firstRouteButton
            .animate()
            .translationY(150F)
            .setStartDelay(1000L)
            .setDuration(500L)
            .withEndAction {
                firstRouteButton
                    .animate()
                    .translationY(0F)
                    .setDuration(800L)
                    .interpolator = BounceInterpolator()
            }
            .start()

    }


    private fun onRouteClick(route: PhotoData) {
        viewModel.selectRouteName(route.routeName)
        viewModel.getPhotosByRouteName(route.routeName)
        findNavController().navigate(R.id.action_main_to_routeFragment)
    }

    private fun requestPermissions() {
        if (requireContext().hasLocationPermission()) {
            Log.d(TAG, "has permissions = true")
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Log.d(TAG, "Version is below Q, show dialog")
            EasyPermissions.requestPermissions(
                this,
                RATIONALE_FOR_LOCATION,
                REQUEST_CODE_LOCATION_PERMISSION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            Log.d(TAG, "Version is above Q, show dialog")
            EasyPermissions.requestPermissions(
                this,
                RATIONALE_FOR_LOCATION,
                REQUEST_CODE_LOCATION_PERMISSION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            Log.d(TAG, "Values : ${it.entries}")
        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "PERMISSIONS ARE GRANTED")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .build()
                .show()
        } else {
            requestPermissions()
        }
    }
}

//private fun createAlertDialog() {
//    MaterialAlertDialogBuilder(requireContext())
//        .setTitle("Location permission dialog")
//        .setMessage("To provide you better experience, please accept location permission")
//        .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
//            launcher.launch(MapsFragment.REQUIRED_PERMISSIONS)
//        })
//        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
//            dialog.dismiss()
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        })
//        .create()
//        .show()
//}

//private fun checkPermissions() {
//    val allGranted = REQUIRED_PERMISSIONS.all { permission ->
//        ContextCompat.checkSelfPermission(
//            requireContext(),
//            permission
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//    if (allGranted) {
//        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//        mapFragment?.getMapAsync(onMapReadyCallback)
//        Log.d(com.example.permissionsapp.ui.main.maps.TAG, "ALL PERMISSIONS ARE GRANTED. 1")
////            setMapSettings()
////            setLocationSource()
//    } else {
//        if (shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0])) {
//            createAlertDialog()
//        } else {
//            launcher.launch(REQUIRED_PERMISSIONS)
//        }
//    }
//}