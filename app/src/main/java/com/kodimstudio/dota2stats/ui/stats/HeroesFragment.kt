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
import com.kodimstudio.dota2stats.databinding.FragmentHeroesBinding
import com.kodimstudio.dota2stats.model.HeroStat
import com.kodimstudio.dota2stats.adapters.HeroesStatsListAdapter

class HeroesFragment: Fragment() {
    private lateinit var binding: FragmentHeroesBinding
    private lateinit var heroesStatsList: MutableList<HeroStat>
    private lateinit var adapter: HeroesStatsListAdapter

    private lateinit var dividers: Array<View>
    private lateinit var headers: Array<View>
    private var sortType = 1
    private var sortOrder = -1

    private val viewModel by activityViewModels<StatsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHeroesBinding.inflate(inflater, container, false)
        val root = binding.root

        binding.heroesStatsList.layoutManager = LinearLayoutManager(context)

        viewModel.loadHeroesStatsStatus.observe(viewLifecycleOwner) {status ->
            if (status == RequestStatus.ERROR) {
                Snackbar.make(binding.root, R.string.snackbar_error, Snackbar.LENGTH_LONG).show()
            }
        }

        binding.progressBar.visibility = ProgressBar.VISIBLE
        viewModel.getHeroesStats().observe(viewLifecycleOwner) { heroesStats ->
            binding.progressBar.visibility = ProgressBar.INVISIBLE
            heroesStatsList = heroesStats.toMutableList()
            adapter = HeroesStatsListAdapter(heroesStatsList)
            binding.heroesStatsList.adapter = adapter
        }
        viewModel.loadHeroesStats(viewModel.player.accountId!!)

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
                        SORT_HERO -> heroesStatsList.sortByDescending { it.heroId }
                        SORT_MATCHES -> heroesStatsList.sortBy { it.matches }
                        SORT_WINRATE -> heroesStatsList.sortBy { it.winrate }
                        SORT_KDA -> heroesStatsList.sortBy { it.kda }
                    }
                } else {
                    when (index) {
                        SORT_HERO -> heroesStatsList.sortBy { it.heroId }
                        SORT_MATCHES -> heroesStatsList.sortByDescending { it.matches }
                        SORT_WINRATE -> heroesStatsList.sortByDescending { it.winrate }
                        SORT_KDA -> heroesStatsList.sortByDescending { it.kda }
                    }
                }
                adapter.notifyItemRangeChanged(0, heroesStatsList.size)
            }
        }

    }

    companion object {
        private const val SORT_HERO = 0
        private const val SORT_MATCHES = 1
        private const val SORT_WINRATE = 2
        private const val SORT_KDA = 3
    }
}