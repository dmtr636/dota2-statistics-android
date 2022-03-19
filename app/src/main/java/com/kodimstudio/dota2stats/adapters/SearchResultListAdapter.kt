package com.kodimstudio.dota2stats.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.model.Player
import com.squareup.picasso.Picasso



class SearchResultListAdapter(
    private val dataSet: MutableList<Player>,
    private val onPlayerClickListener: OnPlayerClickListener
) : RecyclerView.Adapter<SearchResultListAdapter.ViewHolder>() {

    interface OnPlayerClickListener{
        fun onPlayerClick(player: Player, position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nickname: TextView = view.findViewById(R.id.nickname)
        val avatar: ImageView = view.findViewById(R.id.avatar)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.search_result_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val player = dataSet[position]
        viewHolder.nickname.text = player.nickname
        Picasso.get().load(player.avatarUrl).into(viewHolder.avatar)
        viewHolder.itemView.setOnClickListener {
            onPlayerClickListener.onPlayerClick(player, position)
        }
    }

    override fun getItemCount() = dataSet.size
}