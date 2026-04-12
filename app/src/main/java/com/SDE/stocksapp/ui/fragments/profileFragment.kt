package com.SDE.stocksapp.ui.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.SDE.stocksapp.R
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.common.api.ApiException

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    companion object {
        private const val RC_SIGN_IN = 120
    }


    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginBtn = view.findViewById<View>(R.id.loginBtn)
        val g = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), g)
        mAuth = FirebaseAuth.getInstance()
        loginBtn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)
                    Log.d(TAG, "Google sign in successful, authenticating with Firebase")
                    fireBaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.e(TAG, "Google sign in failed", e)
                    Toast.makeText(requireContext(), "Google Sign In Failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e(TAG, "Google sign in task failed: ${task.exception?.message}")
                Toast.makeText(requireContext(), "Google Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun fireBaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase auth successful, navigating to home")
                    Toast.makeText(requireContext(), "Authentication Successful", Toast.LENGTH_SHORT).show()
                    val user = mAuth.currentUser
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)

                } else {
                    Log.e(TAG, "Firebase signInWithCredential:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

}