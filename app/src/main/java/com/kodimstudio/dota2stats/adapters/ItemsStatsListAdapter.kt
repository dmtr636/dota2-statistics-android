package com.kodimstudio.dota2stats.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.data.Data
import com.kodimstudio.dota2stats.model.ItemStat

class ItemsStatsListAdapter(
    private val dataSet: MutableList<ItemStat>
) : RecyclerView.Adapter<ItemsStatsListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val heroIcon: ImageView = view.findViewById(R.id.hero_stats_list_item_hero_icon)
        val heroName: TextView = view.findViewById(R.id.hero_stats_list_item_hero_name)
        val matches: TextView = view.findViewById(R.id.hero_stats_list_item_matches_count)
        val winrate: TextView = view.findViewById(R.id.hero_stats_list_item_wins_percentage)
        val kda: TextView = view.findViewById(R.id.hero_stats_list_item_kda)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_stats_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val heroStat = dataSet[position]
        viewHolder.heroIcon.setImageDrawable(Data.ITEMS_S[heroStat.itemId]?.icon)
        viewHolder.heroName.text = Data.ITEMS_S[heroStat.itemId]?.name
        viewHolder.matches.text = heroStat.matches.toString()
        viewHolder.winrate.text = String.format("%.2f%%", heroStat.winrate)
        viewHolder.kda.text = String.format("%.2f", heroStat.kda)
    }

    override fun getItemCount() = dataSet.size
}