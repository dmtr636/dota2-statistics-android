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

class MatchOverviewListAdapter(
    private val dataSet: List<MatchDetails.MatchDetailsPlayer>,
    private val listHeight: Int,
    private val context: Context
) : RecyclerView.Adapter<MatchOverviewListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val heroIcon: ImageView = view.findViewById(R.id.heroIcon)
        val playerName: TextView = view.findViewById(R.id.playerName)
        val kda: TextView = view.findViewById(R.id.playerKda)
        val itemNeutral: ImageView = view.findViewById(R.id.itemNeutral)
        val item0: ImageView = view.findViewById(R.id.item0)
        val item1: ImageView = view.findViewById(R.id.item1)
        val item2: ImageView = view.findViewById(R.id.item2)
        val item3: ImageView = view.findViewById(R.id.item3)
        val item4: ImageView = view.findViewById(R.id.item4)
        val item5: ImageView = view.findViewById(R.id.item5)
        val backpack0: ImageView = view.findViewById(R.id.backpack0)
        val backpack1: ImageView = view.findViewById(R.id.backpack1)
        val backpack2: ImageView = view.findViewById(R.id.backpack2)
        val level: TextView = view.findViewById(R.id.level)
        val layout: ConstraintLayout = view.findViewById(R.id.layout)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.match_overview_list_item, viewGroup, false)

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
        viewHolder.level.text = playerStat.level.toString()
        Data.ITEMS[playerStat.item_neutral]?.icon?.let { viewHolder.itemNeutral.setImageDrawable(it) }
        Data.ITEMS[playerStat.item_0]?.icon?.let { viewHolder.item0.setImageDrawable(it) }
        Data.ITEMS[playerStat.item_1]?.icon?.let { viewHolder.item1.setImageDrawable(it) }
        Data.ITEMS[playerStat.item_2]?.icon?.let { viewHolder.item2.setImageDrawable(it) }
        Data.ITEMS[playerStat.item_3]?.icon?.let { viewHolder.item3.setImageDrawable(it) }
        Data.ITEMS[playerStat.item_4]?.icon?.let { viewHolder.item4.setImageDrawable(it) }
        Data.ITEMS[playerStat.item_5]?.icon?.let { viewHolder.item5.setImageDrawable(it) }
        Data.ITEMS[playerStat.backpack_0]?.icon?.let { viewHolder.backpack0.setImageDrawable(it) }
        Data.ITEMS[playerStat.backpack_1]?.icon?.let { viewHolder.backpack1.setImageDrawable(it) }
        Data.ITEMS[playerStat.backpack_2]?.icon?.let { viewHolder.backpack2.setImageDrawable(it) }
        if (position % 2 == 0) {
            viewHolder.layout.setBackgroundColor(Color.parseColor("#242f39"))
        }
    }

    override fun getItemCount() = dataSet.size
}