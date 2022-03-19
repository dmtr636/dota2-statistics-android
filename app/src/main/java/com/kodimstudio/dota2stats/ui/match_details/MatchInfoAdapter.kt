package com.kodimstudio.dota2stats.ui.match_details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class MatchInfoAdapter(fm: FragmentManager, l: Lifecycle) : FragmentStateAdapter(fm, l) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        val fragment = when(position) {
            0 -> MatchOverviewFragment()
            1 -> MatchFarmFragment()
            2 -> MatchDamageFragment()
            3 -> MatchChartsFragment()
            else -> MatchOverviewFragment()
        }

        return fragment
    }
}