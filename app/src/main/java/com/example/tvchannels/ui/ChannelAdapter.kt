package com.example.tvchannels.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tvchannels.R
import com.example.tvchannels.data.Channel

class ChannelAdapter(
    private val channels: List<Channel>,
    private val onChannelClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val channelLogo: ImageView = itemView.findViewById(R.id.channel_logo)
        val channelName: TextView = itemView.findViewById(R.id.channel_name)
        val channelNumber: TextView = itemView.findViewById(R.id.channel_number)
        val currentProgram: TextView = itemView.findViewById(R.id.current_program)
        val favoriteIndicator: ImageView = itemView.findViewById(R.id.favorite_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        
        holder.channelName.text = channel.name
        holder.channelNumber.text = "Канал ${channel.channelNumber}"
        holder.currentProgram.text = "Текущая программа"
        
        // Load channel logo
        if (channel.logoUrl != null) {
            Glide.with(holder.itemView.context)
                .load(channel.logoUrl)
                .placeholder(R.drawable.ic_tv)
                .error(R.drawable.ic_tv)
                .into(holder.channelLogo)
        } else {
            holder.channelLogo.setImageResource(R.drawable.ic_tv)
        }
        
        // Show favorite indicator
        holder.favoriteIndicator.visibility = if (channel.isFavorite) View.VISIBLE else View.GONE
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onChannelClick(channel)
        }
        
        // Set focus change listener for TV navigation
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                holder.itemView.alpha = 1.0f
                holder.itemView.scaleX = 1.1f
                holder.itemView.scaleY = 1.1f
            } else {
                holder.itemView.alpha = 0.8f
                holder.itemView.scaleX = 1.0f
                holder.itemView.scaleY = 1.0f
            }
        }
    }

    override fun getItemCount(): Int = channels.size
} 