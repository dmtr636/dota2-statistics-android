package com.kodimstudio.dota2stats.ui.stats

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
import com.kodimstudio.dota2stats.api.model.RecentMatch
import com.kodimstudio.dota2stats.databinding.FragmentMatchesBinding
import com.kodimstudio.dota2stats.adapters.RecentMatchesListAdapter
import com.kodimstudio.dota2stats.ui.match_details.MatchDetailsActivity


class MatchesFragment: Fragment(), View.OnClickListener {

    private val model: StatsViewModel by activityViewModels()

    private lateinit var binding: FragmentMatchesBinding
    private val matchesListPage = mutableListOf<RecentMatch>()
    private val matchesList = mutableListOf<RecentMatch>()
    private lateinit var adapter: RecentMatchesListAdapter
    private var start = 0
    private val count = 100

    val onMatchClickListener = object : RecentMatchesListAdapter.OnMatchClickListener {
        override fun onMatchClick(match: RecentMatch, position: Int) {
            val intent = Intent(context, MatchDetailsActivity::class.java)
            intent.putExtra("matchId", match.match_id)
            intent.putExtra("player", model.player)
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMatchesBinding.inflate(inflater, container, false)
        val root = binding.root

        adapter = RecentMatchesListAdapter(matchesListPage, onMatchClickListener, requireContext())

        binding.fragmentMatchesHistoryBtnNext.setOnClickListener(this)
        binding.fragmentMatchesHistoryBtnPrev.setOnClickListener(this)
        binding.fragmentMatchesHistoryBtnToEnd.setOnClickListener(this)
        binding.fragmentMatchesHistoryBtnToStart.setOnClickListener(this)

        binding.matchesList.adapter = adapter
        binding.matchesList.layoutManager = LinearLayoutManager(context)

        model.errorStatus.observe(viewLifecycleOwner) {status ->
            if (status == -1) {
                Snackbar.make(binding.root, R.string.snackbar_error, Snackbar.LENGTH_LONG).show()
            }
        }

        if (matchesList.isEmpty()) {
            binding.progressBar.visibility = ProgressBar.VISIBLE
            model.getMatches().observe(viewLifecycleOwner) { matchesList ->
                binding.progressBar.visibility = ProgressBar.INVISIBLE
                this.matchesList.addAll(matchesList)
                matchesListPage.addAll(matchesList.take(count))
                adapter.notifyItemRangeInserted(0, matchesListPage.size)
            }
        }
//                    Snackbar.make(binding.root, "Произошла ошибка. Попробуйте снова", Snackbar.LENGTH_SHORT).show()
        return root
    }


    override fun onClick(v: View) {
        val matchesCount: Int = matchesList.size
        when (v.id) {
            R.id.fragment_matches_history_btn_to_start -> start = 0
            R.id.fragment_matches_history_btn_to_end -> {
                start = matchesCount - 100
                if (start < 0) {
                    start = 0
                }
            }
            R.id.fragment_matches_history_btn_prev -> {
                start -= 100
                if (start < 0) {
                    start = 0
                }
            }
            R.id.fragment_matches_history_btn_next -> if (start + 100 < matchesCount) {
                start += 100
            }
        }
        binding.fragmentMatchesHistoryStartLabel.text = (start + 1).toString()
        if (start + count > matchesCount) {
            binding.fragmentMatchesHistoryEndLabel.text = matchesCount.toString()
        } else {
            binding.fragmentMatchesHistoryEndLabel.text = (start + count).toString()
        }
        matchesListPage.clear()
        for (i in start until start+count) {
            matchesListPage.add(matchesList[i])
        }
        adapter.notifyItemRangeChanged(0, count)
    }
}