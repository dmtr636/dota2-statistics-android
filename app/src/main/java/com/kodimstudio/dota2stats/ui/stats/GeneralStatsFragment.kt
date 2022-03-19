package com.kodimstudio.dota2stats.ui.stats

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.api.RequestStatus
import com.kodimstudio.dota2stats.api.model.RecentMatch
import com.kodimstudio.dota2stats.api.model.StatsSummariesResponse
import com.kodimstudio.dota2stats.databinding.FragmentGeneralStatsBinding
import com.kodimstudio.dota2stats.adapters.RecentMatchesListAdapter
import com.kodimstudio.dota2stats.ui.match_details.MatchDetailsActivity
import com.squareup.picasso.Picasso

class GeneralStatsFragment: Fragment() {

    private lateinit var binding: FragmentGeneralStatsBinding
    private val model by activityViewModels<StatsViewModel>()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentGeneralStatsBinding.inflate(inflater, container, false)
        val root = binding.root

        val player = model.player
        Picasso.get().load(player.avatarUrl).into(binding.avatar)

        val onMatchClickListener = object : RecentMatchesListAdapter.OnMatchClickListener {
            override fun onMatchClick(match: RecentMatch, position: Int) {
                val intent = Intent(context, MatchDetailsActivity::class.java)
                intent.putExtra("matchId", match.match_id)
                intent.putExtra("player", player)
                startActivity(intent)
            }
        }

        binding.matchesList.layoutManager = LinearLayoutManager(context)

        binding.progressBar.visibility = ProgressBar.VISIBLE
        model.getRecentMatches().observe(viewLifecycleOwner) { recentMatchesList ->
            binding.progressBar.visibility = ProgressBar.INVISIBLE
            binding.matchesList.adapter = RecentMatchesListAdapter(
                recentMatchesList.toMutableList(),
                onMatchClickListener,
                requireContext())
        }

        model.loadGeneralStatsStatus.observe(viewLifecycleOwner) {status ->
            if (status == RequestStatus.ERROR) {
                Snackbar.make(binding.root, R.string.snackbar_error, Snackbar.LENGTH_LONG).show()
            }
        }

        model.getGeneralStats().observe(viewLifecycleOwner) { stats ->
            binding.gamesCount.text = stats.gamesCount.toString()
            binding.wins.text = stats.wins.toString()
            binding.loses.text = stats.loses.toString()
            binding.winrate.text = String.format("%.2f %%", stats.winrate)
            //binding.timeSpent.text = stats.timeSpent.toString() + " " + getString(R.string.abbreviated_hour)
            binding.avgKda.text = String.format("%.2f", stats.avgKda)
        }

        model.getStatsSummaries().observe(viewLifecycleOwner) { statsSummaries ->
            setStatsSummaries(statsSummaries)
        }

        model.playtime.observe(viewLifecycleOwner) {
            binding.timeSpent.text = it.toString() + " " + getString(R.string.abbreviated_hour)
        }

        model.loadGeneralStats(player.steamId!!, player.accountId!!)

        return root
    }

    private fun setStatsSummaries(statsSummaries: StatsSummariesResponse) {
        var resource = R.drawable.seasonalrank1_1
        resource = when (statsSummaries.rank_tier) {
            11 -> R.drawable.seasonalrank1_1
            12 -> R.drawable.seasonalrank1_2
            13 -> R.drawable.seasonalrank1_3
            14 -> R.drawable.seasonalrank1_4
            15 -> R.drawable.seasonalrank1_5

            21 -> R.drawable.seasonalrank2_1
            22 -> R.drawable.seasonalrank2_2
            23 -> R.drawable.seasonalrank2_3
            24 -> R.drawable.seasonalrank2_4
            25 -> R.drawable.seasonalrank2_5

            31 -> R.drawable.seasonalrank3_1
            32 -> R.drawable.seasonalrank3_2
            33 -> R.drawable.seasonalrank3_3
            34 -> R.drawable.seasonalrank3_4
            35 -> R.drawable.seasonalrank3_5

            41 -> R.drawable.seasonalrank4_1
            42 -> R.drawable.seasonalrank4_2
            43 -> R.drawable.seasonalrank4_3
            44 -> R.drawable.seasonalrank4_4
            45 -> R.drawable.seasonalrank4_5

            51 -> R.drawable.seasonalrank5_1
            52 -> R.drawable.seasonalrank5_2
            53 -> R.drawable.seasonalrank5_3
            54 -> R.drawable.seasonalrank5_4
            55 -> R.drawable.seasonalrank5_5

            61 -> R.drawable.seasonalrank6_1
            62 -> R.drawable.seasonalrank6_2
            63 -> R.drawable.seasonalrank6_3
            64 -> R.drawable.seasonalrank6_4
            65 -> R.drawable.seasonalrank6_5

            71 -> R.drawable.seasonalrank7_1
            72 -> R.drawable.seasonalrank7_2
            73 -> R.drawable.seasonalrank7_3
            74 -> R.drawable.seasonalrank7_4
            75 -> R.drawable.seasonalrank7_5

            80 -> when (statsSummaries.leaderboard_rank) {
                in 101..1000 -> R.drawable.seasonalranktop1
                in 0..100 -> R.drawable.seasonalranktop2
                else -> R.drawable.seasonalranktop0
            }

            else -> R.drawable.seasonalrank1_1
        }
        if (statsSummaries.leaderboard_rank != null) {
            binding.rankScore.text = statsSummaries.leaderboard_rank.toString()
        } else {
            binding.rankScore.text = ""
        }

        binding.rankIcon.setImageResource(resource)
    }
}