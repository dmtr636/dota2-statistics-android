package com.kodimstudio.dota2stats.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.api.RequestStatus
import com.kodimstudio.dota2stats.databinding.FragmentItemsBinding
import com.kodimstudio.dota2stats.model.ItemStat
import com.kodimstudio.dota2stats.adapters.ItemsStatsListAdapter

class ItemsFragment: Fragment() {
    private lateinit var binding: FragmentItemsBinding
    private lateinit var itemsStatsList: MutableList<ItemStat>
    private lateinit var adapter: ItemsStatsListAdapter

    private lateinit var dividers: Array<View>
    private lateinit var headers: Array<View>
    private var sortType = 1
    private var sortOrder = -1

    private val viewModel: StatsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {

        binding = FragmentItemsBinding.inflate(inflater, container, false)
        val root = binding.root

        itemsStatsList = mutableListOf()
        adapter = ItemsStatsListAdapter(itemsStatsList)
        binding.heroesStatsList.adapter = adapter
        binding.heroesStatsList.layoutManager = LinearLayoutManager(context)

        viewModel.loadItemsStatsStatus.observe(viewLifecycleOwner) {status ->
            if (status == RequestStatus.ERROR) {
                Snackbar.make(binding.root, R.string.snackbar_error, Snackbar.LENGTH_LONG).show()
            }
        }

        binding.progressBar.visibility = ProgressBar.VISIBLE
        viewModel.getItemsStats().observe(viewLifecycleOwner) { itemsStats ->
            binding.progressBar.visibility = ProgressBar.INVISIBLE
            this.itemsStatsList.addAll(itemsStats)
            adapter.notifyItemRangeInserted(0, itemsStatsList.size)
        }
        viewModel.loadItemsStats(viewModel.player.accountId!!)

        setDividersListeners()

        return root
    }

    private fun setDividersListeners() {
        dividers = arrayOf(
            binding.dividerHero,
            binding.dividerMatchesCount,
            binding.dividerWinsPercentage,
            binding.dividerKda)
        headers = arrayOf(
            binding.fragmentHeroStatsHero,
            binding.fragmentHeroStatsMatchesCount,
            binding.fragmentHeroStatsWinsPercentage,
            binding.fragmentHeroStatsKda)

        headers.forEachIndexed { index, view ->
            view.setOnClickListener {
                if (dividers[index].isVisible) {
                    sortOrder = -sortOrder
                } else {
                    dividers[sortType].visibility = View.INVISIBLE
                    dividers[index].visibility = View.VISIBLE
                    sortType = index
                }

                if (sortOrder == 1) {
                    when (index) {
                        SORT_Item -> itemsStatsList.sortByDescending { it.itemId }
                        SORT_MATCHES -> itemsStatsList.sortBy { it.matches }
                        SORT_WINRATE -> itemsStatsList.sortBy { it.winrate }
                        SORT_KDA -> itemsStatsList.sortBy { it.kda }
                    }
                } else {
                    when (index) {
                        SORT_Item -> itemsStatsList.sortBy { it.itemId }
                        SORT_MATCHES -> itemsStatsList.sortByDescending { it.matches }
                        SORT_WINRATE -> itemsStatsList.sortByDescending { it.winrate }
                        SORT_KDA -> itemsStatsList.sortByDescending { it.kda }
                    }
                }
                adapter.notifyItemRangeChanged(0, itemsStatsList.size)
            }
        }

    }

    companion object {
        private const val SORT_Item = 0
        private const val SORT_MATCHES = 1
        private const val SORT_WINRATE = 2
        private const val SORT_KDA = 3
    }
}