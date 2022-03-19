package com.kodimstudio.dota2stats.ui.stats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.api.RequestStatus
import com.kodimstudio.dota2stats.api.model.RecentMatch
import com.kodimstudio.dota2stats.databinding.FragmentRecordsBinding
import com.kodimstudio.dota2stats.model.Record
import com.kodimstudio.dota2stats.adapters.RecordsListAdapter
import com.kodimstudio.dota2stats.ui.match_details.MatchDetailsActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class RecordsFragment: Fragment() {

    private lateinit var binding: FragmentRecordsBinding
    private lateinit var matchesMap: HashMap<Long, RecentMatch>
    private lateinit var recordsList: MutableList<Record>
    private lateinit var adapter: RecordsListAdapter

    private val viewModel: StatsViewModel by activityViewModels()

    private val onMatchClickListener = object : RecordsListAdapter.OnMatchClickListener {
        override fun onMatchClick(match: RecentMatch, position: Int) {
            val intent = Intent(context, MatchDetailsActivity::class.java)
            intent.putExtra("matchId", match.match_id)
            intent.putExtra("player", viewModel.player)
            startActivity(intent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {

        binding = FragmentRecordsBinding.inflate(inflater, container, false)
        val root = binding.root

        matchesMap = hashMapOf<Long, RecentMatch>()
        recordsList = mutableListOf<Record>()

        binding.recordsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        viewModel.errorStatus.observe(viewLifecycleOwner) {status ->
            if (status == -1) {
                Snackbar.make(binding.root, R.string.snackbar_error, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.loadRecordsStatsStatus.observe(viewLifecycleOwner) {status ->
            if (status == RequestStatus.ERROR) {
                Snackbar.make(binding.root, R.string.snackbar_error, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.getMatchesMap().observe(viewLifecycleOwner) { matchesMap ->
            this.matchesMap.putAll(matchesMap)
        }

        viewModel.getRecords().observe(viewLifecycleOwner) { records ->
            recordsList.addAll(records)
        }

        viewModel.loadRecords(viewModel.player.accountId!!)

        binding.progressBar.visibility = ProgressBar.VISIBLE

        lifecycleScope.launch {
            while (matchesMap.isEmpty() || recordsList.isEmpty()) {
                delay(50)
            }
            binding.progressBar.visibility = ProgressBar.INVISIBLE
            adapter = RecordsListAdapter(matchesMap, recordsList, onMatchClickListener, requireContext())
            binding.recordsList.adapter = adapter
            adapter.notifyItemRangeInserted(0, recordsList.size)
        }

        return root
    }
}