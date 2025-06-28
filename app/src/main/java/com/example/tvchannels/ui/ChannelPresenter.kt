package com.example.tvchannels.ui

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.example.tvchannels.R
import com.example.tvchannels.data.Channel

class ChannelPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(200, 150)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val channel = item as Channel
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = channel.name
        cardView.contentText = channel.description ?: ""

        // Load channel logo
        if (channel.logoUrl != null) {
            Glide.with(cardView.context)
                .load(channel.logoUrl)
                .placeholder(R.drawable.ic_tv)
                .error(R.drawable.ic_tv)
                .into(cardView.mainImageView)
        } else {
            cardView.mainImageView.setImageResource(R.drawable.ic_tv)
        }

        // Show favorite indicator
        if (channel.isFavorite) {
            cardView.badgeImage = cardView.context.getDrawable(R.drawable.ic_favorite)
        } else {
            cardView.badgeImage = null
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
} 