package com.SDE.stocksapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.SDE.stocksapp.ui.StocksActivity
import com.SDE.stocksapp.util.Resource

class splashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = (activity as StocksActivity).viewModel

        var isNavigated = false

        viewModel.topGainersLosers.observe(viewLifecycleOwner){ response ->

            when (response) {
                is Resource.Loading -> {

                }
                is Resource.Success -> {
                    if (!isNavigated){
                        isNavigated = true
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                }
                is Resource.Error -> {
                    if (!isNavigated){
                        isNavigated = true
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                }
            }
        }


    }

}