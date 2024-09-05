package by.marcel.apps_lab.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.marcel.apps_lab.onboardingPage.OnBoardPageFragment

class OnBoardingAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 1 //current fragment
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> OnBoardPageFragment()
            else -> OnBoardPageFragment() //change if have more fragment
        }
    }
}