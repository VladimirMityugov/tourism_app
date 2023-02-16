package com.example.permissionsapp.ui.main.main_screen

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.permissionsapp.presentation.MyViewModel
import com.example.permissionsapp.ui.main.LoginActivity
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentMainScreenBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


private const val TAG = "MAIN_SCREEN"

@AndroidEntryPoint
class MainScreenFragment : Fragment() {

    private var _binding: FragmentMainScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var username: TextView
    private lateinit var signOut: AppCompatButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var routesRecyclerView: RecyclerView
    private lateinit var firstRouteButton: AppCompatImageButton
    private lateinit var infoField: AppCompatTextView
    private lateinit var scrollView: ScrollView

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

        signOut.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        firstRouteButton.setOnClickListener {
            Log.d(TAG, "Button is pushed")
            findNavController().navigate(R.id.action_main_to_maps)
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
                    .setDuration(1000L)
                    .interpolator = BounceInterpolator()
            }
            .start()




    }

    private fun animateFirstRouteButton() {
        val scaleX = ObjectAnimator.ofFloat(firstRouteButton, "scaleX", 2.0F).setDuration(1000L)
        val scaleY = ObjectAnimator.ofFloat(firstRouteButton, "scaleY", 2.0F).setDuration(1000L)

        val scaleX2 = ObjectAnimator.ofFloat(firstRouteButton, "scaleX", 2.0F).setDuration(1000L)
        val scaleY2 = ObjectAnimator.ofFloat(firstRouteButton, "scaleY", 2.0F).setDuration(1000L)


        AnimatorSet().apply {
            play(scaleX).with(scaleY)
            play(scaleX2).with(scaleY2)
            play(scaleX2).after(1000L)
            interpolator = BounceInterpolator()
            startDelay = 1500L
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}