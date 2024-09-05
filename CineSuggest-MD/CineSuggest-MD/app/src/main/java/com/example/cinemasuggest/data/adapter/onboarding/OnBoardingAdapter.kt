package com.example.cinemasuggest.data.adapter.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cinemasuggest.view.onboarding.OnBoarding1Fragment

class OnBoardingAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 1 //current fragment
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> OnBoarding1Fragment()
            else -> OnBoarding1Fragment() //change if have more fragment
        }
    }
}
