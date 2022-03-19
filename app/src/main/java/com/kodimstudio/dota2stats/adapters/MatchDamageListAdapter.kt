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

class MatchDamageListAdapter(
    private val dataSet: List<MatchDetails.MatchDetailsPlayer>,
    private val listHeight: Int,
    private val context: Context
) : RecyclerView.Adapter<MatchDamageListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val heroIcon: ImageView = view.findViewById(R.id.heroIcon)
        val playerName: TextView = view.findViewById(R.id.playerName)
        val kda: TextView = view.findViewById(R.id.playerKda)
        val heroDamage: TextView = view.findViewById(R.id.heroDamage)
        val heal: TextView = view.findViewById(R.id.heal)
        val towerDamage: TextView = view.findViewById(R.id.towerDamage)
        val layout: ConstraintLayout = view.findViewById(R.id.layout)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.match_damage_list_item, viewGroup, false)

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
        viewHolder.heroDamage.text = String.format("%.1f K", playerStat.hero_damage/1000f)
        if (playerStat.hero_healing == 0) {
            viewHolder.heal.text = "0"
        } else {
            viewHolder.heal.text = String.format("%.1f K", playerStat.hero_healing/1000f)
        }
        if (playerStat.tower_damage == 0) {
            viewHolder.towerDamage.text = "0"
        } else {
            viewHolder.towerDamage.text = String.format("%.1f K", playerStat.tower_damage/1000f)
        }
        if (position % 2 == 0) {
            viewHolder.layout.setBackgroundColor(Color.parseColor("#242f39"))
        }
    }

    override fun getItemCount() = dataSet.size
}