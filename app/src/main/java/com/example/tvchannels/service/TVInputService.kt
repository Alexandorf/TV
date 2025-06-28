package com.example.tvchannels.service

import android.content.Intent
import android.media.tv.TvInputService
import android.media.tv.TvInputService.Session
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import com.example.tvchannels.data.Channel
import com.example.tvchannels.data.ChannelRepository

class TVInputService : TvInputService() {

    override fun onCreateSession(inputId: String): Session {
        return TVSession(this, inputId)
    }

    private class TVSession(
        private val context: TVInputService,
        private val inputId: String
    ) : Session(context) {

        private var currentChannel: Channel? = null
        private val channelRepository = ChannelRepository()

        override fun onRelease() {
            // Cleanup resources
        }

        override fun onSetSurface(surface: Surface?) {
            // Set video surface
        }

        override fun onSetStreamVolume(volume: Float) {
            // Set audio volume
        }

        override fun onTune(uri: Uri?) {
            // Tune to channel
            uri?.let { channelUri ->
                val channelId = channelUri.lastPathSegment
                if (channelId != null) {
                    channelRepository.getChannels { channels, error ->
                        if (error == null) {
                            currentChannel = channels.find { it.id == channelId }
                            currentChannel?.let { channel ->
                                // Start playing the channel
                                notifyChannelRetuned(channelUri)
                            }
                        }
                    }
                }
            }
        }

        override fun onSetCaptionEnabled(enabled: Boolean) {
            // Enable/disable captions
        }

        override fun onUnblockContent(unblockedRating: TvContentRating?) {
            // Unblock content
        }
    }
} 