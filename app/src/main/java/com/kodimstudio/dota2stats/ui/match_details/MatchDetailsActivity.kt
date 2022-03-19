package com.kodimstudio.dota2stats.ui.match_details

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.api.model.MatchDetails
import com.kodimstudio.dota2stats.databinding.ActivityMatchDetailsBinding
import com.kodimstudio.dota2stats.model.Player
import com.kodimstudio.dota2stats.ui.MainActivity
import com.kodimstudio.dota2stats.ui.stats.StatsActivity
import java.text.SimpleDateFormat
import java.util.*

class MatchDetailsActivity : AppCompatActivity() {

    companion object {
        var matchId: Long = 0
        lateinit var radiantPlayersList: List<MatchDetails.MatchDetailsPlayer>
        lateinit var direPlayersList: List<MatchDetails.MatchDetailsPlayer>
        lateinit var matchInfo: MatchDetails
    }

    private lateinit var binding: ActivityMatchDetailsBinding
    private val viewModel: MatchDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val matchId = intent.getLongExtra("matchId", 0)
        val player = intent.getSerializableExtra("player") as Player?


        viewModel.matchId = matchId

        binding = ActivityMatchDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = MainActivity.db
        if (player != null) {
            if (db.playerDAO().getById(player.steamId!!).isEmpty()) {
                binding.inFavourite.setImageResource(R.drawable.star_off)
            } else {
                binding.inFavourite.setImageResource(R.drawable.star_copy)
            }

            binding.inFavourite.setOnClickListener {
                if (db.playerDAO().getById(player.steamId).isEmpty()) {
                    db.playerDAO().insertAll(Player(
                        steamId = player.steamId,
                        nickname = player.nickname,
                        avatarUrl = player.avatarUrl
                    ))
                    binding.inFavourite.setImageResource(R.drawable.star_copy)
                } else {
                    db.playerDAO().deleteById(player.steamId)
                    binding.inFavourite.setImageResource(R.drawable.star_off)
                }
            }

            binding.nickName.text = player.nickname
        }
        try {

        } catch (e: Exception) {

        }

        viewModel.errorStatus.observe(this) {
            if (it == -1) {
                Snackbar.make(binding.root, R.string.snackbar_error, Snackbar.LENGTH_LONG).show()
            }
        }

        binding.progressBar2.visibility = ProgressBar.VISIBLE
        viewModel.loadMatchDetails()
        viewModel.getMatchDetails().observe(this) { matchDetails ->
            if (binding.progressBar2.visibility == View.VISIBLE) {
                binding.progressBar2.visibility = ProgressBar.INVISIBLE
                loadMatchDetails(matchDetails)
            }
        }
    }

    private fun loadMatchDetails(matchDetails: MatchDetails) {
        matchInfo = matchDetails

        binding.matchNumber.text = matchDetails.match_id.toString()

        if (matchDetails.radiant_win) {
            binding.matchResult.text = getString(R.string.radiant_victory)
            binding.matchResult.setTextColor(
                ContextCompat.getColor(
                    this@MatchDetailsActivity,
                    R.color.secondaryGreen
                )
            )
        } else {
            binding.matchResult.text = getString(R.string.dire_victory)
            binding.matchResult.setTextColor(
                ContextCompat.getColor(
                    this@MatchDetailsActivity,
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
        binding.matchDuration.text = duration

        if (matchDetails.lobby_type in 5..7) {
            binding.matchIsRanking.text = getString(R.string.ranked)
        } else {
            binding.matchIsRanking.text = getString(R.string.unranked)
        }

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

        radiantPlayersList = matchDetails.players.filter {
            it.isRadiant
        }
        direPlayersList = matchDetails.players.filter {
            !it.isRadiant
        }

        var radiantTowerStatus = Integer.toBinaryString(matchDetails.tower_status_radiant)
        radiantTowerStatus = "0".repeat(11 - radiantTowerStatus.length) + radiantTowerStatus
        var direTowerStatus = Integer.toBinaryString(matchDetails.tower_status_dire)
        direTowerStatus = "0".repeat(11 - direTowerStatus.length) + direTowerStatus
        var radiantBarracksStatus = Integer.toBinaryString(matchDetails.barracks_status_radiant)
        radiantBarracksStatus = "0".repeat(6 - radiantBarracksStatus.length) + radiantBarracksStatus
        var direBarracksStatus = Integer.toBinaryString(matchDetails.barracks_status_dire)
        direBarracksStatus = "0".repeat(6 - direBarracksStatus.length) + direBarracksStatus

        binding.let {
            it.radiantTower42.visibility =
                if (radiantTowerStatus[0] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTower41.visibility =
                if (radiantTowerStatus[1] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTowerBot3.visibility =
                if (radiantTowerStatus[2] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTowerBot2.visibility =
                if (radiantTowerStatus[3] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTowerBot1.visibility =
                if (radiantTowerStatus[4] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTowerMid3.visibility =
                if (radiantTowerStatus[5] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTowerMid2.visibility =
                if (radiantTowerStatus[6] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTowerMid1.visibility =
                if (radiantTowerStatus[7] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTowerTop3.visibility =
                if (radiantTowerStatus[8] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTowerTop2.visibility =
                if (radiantTowerStatus[9] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantTowerTop1.visibility =
                if (radiantTowerStatus[10] == '1') View.VISIBLE else View.INVISIBLE

            it.radiantRaxTop1.visibility =
                if (radiantBarracksStatus[5] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantRaxTop2.visibility =
                if (radiantBarracksStatus[4] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantRaxMid1.visibility =
                if (radiantBarracksStatus[3] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantRaxMid2.visibility =
                if (radiantBarracksStatus[2] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantRaxBot1.visibility =
                if (radiantBarracksStatus[1] == '1') View.VISIBLE else View.INVISIBLE
            it.radiantRaxBot2.visibility =
                if (radiantBarracksStatus[0] == '1') View.VISIBLE else View.INVISIBLE

            it.direTower42.visibility =
                if (direTowerStatus[0] == '1') View.VISIBLE else View.INVISIBLE
            it.direTower41.visibility =
                if (direTowerStatus[1] == '1') View.VISIBLE else View.INVISIBLE
            it.direTowerBot3.visibility =
                if (direTowerStatus[2] == '1') View.VISIBLE else View.INVISIBLE
            it.direTowerBot2.visibility =
                if (direTowerStatus[3] == '1') View.VISIBLE else View.INVISIBLE
            it.direTowerBot1.visibility =
                if (direTowerStatus[4] == '1') View.VISIBLE else View.INVISIBLE
            it.direTowerMid3.visibility =
                if (direTowerStatus[5] == '1') View.VISIBLE else View.INVISIBLE
            it.direTowerMid2.visibility =
                if (direTowerStatus[6] == '1') View.VISIBLE else View.INVISIBLE
            it.direTowerMid1.visibility =
                if (direTowerStatus[7] == '1') View.VISIBLE else View.INVISIBLE
            it.direTowerTop3.visibility =
                if (direTowerStatus[8] == '1') View.VISIBLE else View.INVISIBLE
            it.direTowerTop2.visibility =
                if (direTowerStatus[9] == '1') View.VISIBLE else View.INVISIBLE
            it.direTowerTop1.visibility =
                if (direTowerStatus[10] == '1') View.VISIBLE else View.INVISIBLE

            it.direRaxTop1.visibility =
                if (direBarracksStatus[5] == '1') View.VISIBLE else View.INVISIBLE
            it.direRaxTop2.visibility =
                if (direBarracksStatus[4] == '1') View.VISIBLE else View.INVISIBLE
            it.direRaxMid1.visibility =
                if (direBarracksStatus[3] == '1') View.VISIBLE else View.INVISIBLE
            it.direRaxMid2.visibility =
                if (direBarracksStatus[2] == '1') View.VISIBLE else View.INVISIBLE
            it.direRaxBot1.visibility =
                if (direBarracksStatus[1] == '1') View.VISIBLE else View.INVISIBLE
            it.direRaxBot2.visibility =
                if (direBarracksStatus[0] == '1') View.VISIBLE else View.INVISIBLE

            it.radiantFort.visibility =
                if (matchDetails.radiant_win) View.VISIBLE else View.INVISIBLE
            it.direFort.visibility = if (matchDetails.radiant_win) View.INVISIBLE else View.VISIBLE
        }


        val matchInfoAdapter = MatchInfoAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.adapter = matchInfoAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.setText(
                when (pos) {
                    0 -> R.string.match_tab_text_1
                    1 -> R.string.match_tab_text_2
                    2 -> R.string.match_tab_text_3
                    3 -> R.string.match_tab_text_4

                    else -> R.string.match_tab_text_1
                }
            )
        }.attach()
    }
}