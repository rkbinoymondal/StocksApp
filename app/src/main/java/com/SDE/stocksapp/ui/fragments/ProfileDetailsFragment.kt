package com.SDE.stocksapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.SDE.stocksapp.R
import com.SDE.stocksapp.databinding.FragmentProfileDetailsBinding
import com.SDE.stocksapp.ui.auth.LoginActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class ProfileDetailsFragment : Fragment(R.layout.fragment_profile_details) {

    private var _binding: FragmentProfileDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileDetailsBinding.bind(view)
        mAuth = FirebaseAuth.getInstance()

        val currentUser = mAuth.currentUser
        currentUser?.let { user ->
            binding.tvUserName.text = user.displayName ?: "No Name"
            binding.tvUserEmail.text = user.email ?: "No Email"
            
            user.photoUrl?.let {
                Glide.with(this).load(it).into(binding.ivProfilePic)
            }
        }

        binding.btnLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}