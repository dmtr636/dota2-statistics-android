package com.kodimstudio.dota2stats.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.api.model.MatchDetails
import com.kodimstudio.dota2stats.data.Data

class MatchFarmListAdapter(
    private val dataSet: List<MatchDetails.MatchDetailsPlayer>,
    private val listHeight: Int,
    private val context: Context
) : RecyclerView.Adapter<MatchFarmListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val heroIcon: ImageView = view.findViewById(R.id.heroIcon)
        val playerName: TextView = view.findViewById(R.id.playerName)
        val kda: TextView = view.findViewById(R.id.playerKda)
        val creepStats: TextView = view.findViewById(R.id.creepStats)
        val gpmAndXpm: TextView = view.findViewById(R.id.gpmAndXpm)
        val netWorth: TextView = view.findViewById(R.id.netWorth)
        val layout: ConstraintLayout = view.findViewById(R.id.layout)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.match_farm_list_item, viewGroup, false)

        view.layoutParams.height = listHeight / 5

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val playerStat = dataSet[position]

        viewHolder.heroIcon.setImageDrawable(Data.HEROES[playerStat.hero_id]?.icon)
        var personaName = playerStat.personaname ?: context.getString(R.string.anonymous)
        if (personaName.length > 13) {
            personaName = personaName.subSequence(0, 12).toString() + "..."
        }
        viewHolder.playerName.text = personaName
        viewHolder.kda.text = "${playerStat.kills} / ${playerStat.deaths} / ${playerStat.assists}"
        viewHolder.creepStats.text = "${playerStat.last_hits} / ${playerStat.denies}"
        viewHolder.gpmAndXpm.text = "${playerStat.gold_per_min} / ${playerStat.xp_per_min}"
        val netWorth = playerStat.net_worth ?: playerStat.total_gold
        viewHolder.netWorth.text = String.format("%.1f K", netWorth/1000f)
        if (position % 2 == 0) {
            viewHolder.layout.setBackgroundColor(Color.parseColor("#242f39"))
        }
    }

    override fun getItemCount() = dataSet.size
}