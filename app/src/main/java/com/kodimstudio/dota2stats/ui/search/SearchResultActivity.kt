package com.kodimstudio.dota2stats.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.api.model.MatchDetails
import com.kodimstudio.dota2stats.databinding.ActivitySearchResultBinding
import com.kodimstudio.dota2stats.model.Player
import com.kodimstudio.dota2stats.adapters.SearchResultListAdapter
import com.kodimstudio.dota2stats.ui.match_details.MatchDetailsActivity
import com.kodimstudio.dota2stats.ui.stats.StatsActivity
import java.text.SimpleDateFormat
import java.util.*

class SearchResultActivity : AppCompatActivity() {

    private val onPlayerClickListener = object : SearchResultListAdapter.OnPlayerClickListener{
        override fun onPlayerClick(player: Player, position: Int) {
            val intent = Intent(this@SearchResultActivity, StatsActivity::class.java)
            intent.putExtra("player", player)
            startActivity(intent)
        }
    }

    private lateinit var binding: ActivitySearchResultBinding
    private val playersList: MutableList<Player> = mutableListOf()
    private val adapter: SearchResultListAdapter = SearchResultListAdapter(playersList, onPlayerClickListener)
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        val searchQuery = intent.getStringExtra("steamUrl")

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.errorStatus.observe(this) {status ->
            if (status == -1) {
                Snackbar.make(binding.root, R.string.snackbar_error, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.matchDetails.observe(this) { matchDetails ->
            loadMatchDetails(matchDetails)
            binding.matchDetailsLayout.visibility = View.VISIBLE

            binding.matchDetailsLayout.setOnClickListener {
                val intent = Intent(this@SearchResultActivity, MatchDetailsActivity::class.java)
                intent.putExtra("matchId", matchDetails.match_id)
                startActivity(intent)
            }
        }

        if (searchQuery != null && playersList.isEmpty()) {
            binding.progressBar.visibility = ProgressBar.VISIBLE
            viewModel.playersList.observe(this) { playersList ->

            }
            if (searchQuery.length == 17 && searchQuery.isDigitsOnly()) {
                viewModel.taskCounter.value = viewModel.taskCounter.value?.plus(1)
                viewModel.getPlayerSummaries(searchQuery.toLong())
            }
            if (searchQuery.isDigitsOnly() && searchQuery.length < 17) {
                viewModel.loadMatchDetails(searchQuery.toLong())
            }

            viewModel.resolveVanityUrl(searchQuery)
            viewModel.searchPlayers(searchQuery)

            viewModel.taskCounter.observe(this) { counter ->
                if (counter == 0) {
                    binding.progressBar.visibility = ProgressBar.INVISIBLE
                    viewModel.playersList.value?.let {
                        this.playersList.addAll(it)
                        adapter.notifyItemRangeInserted(0, playersList.size)
                    }
                }
            }
        }
    }

    private fun loadMatchDetails(matchDetails: MatchDetails) {
        MatchDetailsActivity.matchInfo = matchDetails

        binding.matchNumber.text = matchDetails.match_id.toString()

        if (matchDetails.radiant_win) {
            binding.matchResult.text = getString(R.string.radiant_victory)
            binding.matchResult.setTextColor(
                ContextCompat.getColor(
                    this@SearchResultActivity,
                    R.color.secondaryGreen
                )
            )
        } else {
            binding.matchResult.text = getString(R.string.dire_victory)
            binding.matchResult.setTextColor(
                ContextCompat.getColor(
                    this@SearchResultActivity,
                    R.color.secondaryRed
                )
            )
        }

        if (matchDetails.radiant_score != null) {
            binding.radiantKills.text = matchDetails.radiant_score.toString()
        } else {
            binding.radiantKills.text = matchDetails.players
                .filter { it.isRadiant }
                .map { player -> player.kills }
                .reduce { acc, kills -> acc + kills }
                .toString()
        }

        if (matchDetails.dire_score != null) {
            binding.direKills.text = matchDetails.dire_score.toString()
        } else {
            binding.direKills.text = matchDetails.players
                .filter { !it.isRadiant }
                .map { player -> player.kills }
                .reduce { acc, kills -> acc + kills }
                .toString()
        }

        binding.matchDate.text = SimpleDateFormat(getString(R.string.date_format_dots), Locale.getDefault())
            .format(Date(matchDetails.start_time * 1000L))

        val minutes = matchDetails.duration.div(60)
        val seconds = matchDetails.duration.mod(60)
        val duration = "${minutes}:${seconds}"

        binding.matchGameMode.text = when (matchDetails.game_mode) {
            0 -> "Unknown mode"
            1, 22 -> "All Pick"
            2, 8 -> "Captains Mode"
            3 -> "Random Draft"
            4 -> "Single Draft"
            5 -> "All Random"
            11 -> "Mid Only"
            12 -> "Least Played"
            13 -> "Limited Heroes"
            15 -> "Custom"
            16 -> "Captains Draft"
            18 -> "Ability Draft"
            19 -> "Event"
            20 -> "All Random DM"
            21 -> "1v1 Mid"
            23 -> "Turbo"

            else -> "Unknown mode"
        }
    }
}
