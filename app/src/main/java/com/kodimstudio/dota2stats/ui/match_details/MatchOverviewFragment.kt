package com.kodimstudio.dota2stats.ui.match_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kodimstudio.dota2stats.databinding.FragmentMatchOverviewBinding
import com.kodimstudio.dota2stats.adapters.MatchOverviewListAdapter


class MatchOverviewFragment : Fragment() {

    private lateinit var binding: FragmentMatchOverviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMatchOverviewBinding.inflate(inflater, container, false)

        val displayMetrics = requireContext().resources.displayMetrics
        val pxHeight = requireContext().resources.displayMetrics.heightPixels
        val height = (pxHeight - 305 * displayMetrics.density - getStatusBarHeight()) / 2

        val radiantListAdapter = MatchOverviewListAdapter(
            MatchDetailsActivity.radiantPlayersList,
            height.toInt(),
            requireContext()
        )
        val direListAdapter = MatchOverviewListAdapter(
            MatchDetailsActivity.direPlayersList,
            height.toInt(),
            requireContext()
        )

        binding.radiantInfoList.adapter = radiantListAdapter
        binding.direInfoList.adapter = direListAdapter

        binding.radiantInfoList.layoutManager = LinearLayoutManager(context)
        binding.direInfoList.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}