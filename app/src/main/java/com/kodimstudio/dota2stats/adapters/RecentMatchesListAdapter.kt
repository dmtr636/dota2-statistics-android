package com.kodimstudio.dota2stats.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.api.model.RecentMatch
import com.kodimstudio.dota2stats.data.Data
import java.text.SimpleDateFormat
import java.util.*

class RecentMatchesListAdapter(
    private val dataSet: MutableList<RecentMatch>,
    private val onMatchClickListener: OnMatchClickListener,
    private val context: Context
) : RecyclerView.Adapter<RecentMatchesListAdapter.ViewHolder>() {

    interface OnMatchClickListener{
        fun onMatchClick(match: RecentMatch, position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val heroIcon: ImageView = view.findViewById(R.id.match_list_item_hero_icon)
        val heroName: TextView = view.findViewById(R.id.match_list_item_hero_name)
        val date: TextView = view.findViewById(R.id.match_list_item_date)
        val result: TextView = view.findViewById(R.id.match_list_item_result)
        val kills: TextView = view.findViewById(R.id.match_list_item_kills)
        val deaths: TextView = view.findViewById(R.id.match_list_item_deaths)
        val assists: TextView = view.findViewById(R.id.match_list_item_assists)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recent_matches_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val match = dataSet[position]
        viewHolder.heroIcon.setImageDrawable(Data.HEROES[match.hero_id]?.icon)
        viewHolder.heroName.text = Data.HEROES[match.hero_id]?.name
        viewHolder.kills.text = match.kills.toString()
        viewHolder.deaths.text = match.deaths.toString()
        viewHolder.assists.text = match.assists.toString()

        if ((match.radiant_win && match.player_slot < 128)
            || (!match.radiant_win && match.player_slot >= 128)) {
            viewHolder.result.text = context.getString(R.string.win)
            viewHolder.result.setTextColor(ContextCompat.getColor(context, R.color.primaryGreen))
        } else {
            viewHolder.result.text = context.getString(R.string.lose)
            viewHolder.result.setTextColor(ContextCompat.getColor(context, R.color.primaryRed))
        }

        val dateValue = Date(match.start_time * 1000L)
        val simpleDateFormat = SimpleDateFormat(context.getString(R.string.date_format), Locale.getDefault())
        viewHolder.date.text = simpleDateFormat.format(dateValue)

        viewHolder.itemView.setOnClickListener {
            onMatchClickListener.onMatchClick(match, position)
        }
    }

    override fun getItemCount() = dataSet.size
}