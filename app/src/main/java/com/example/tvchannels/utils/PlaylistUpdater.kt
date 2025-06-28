package com.example.tvchannels.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.tvchannels.data.Channel
import com.example.tvchannels.data.ChannelRepository
import com.example.tvchannels.data.M3UParser
import com.google.gson.Gson
import java.util.concurrent.Executors

class PlaylistUpdater(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("tv_channels", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val m3uParser = M3UParser()
    private val executor = Executors.newSingleThreadExecutor()
    
    companion object {
        private const val TAG = "PlaylistUpdater"
        private const val LAST_UPDATE_KEY = "last_playlist_update"
        private const val UPDATE_INTERVAL = 24 * 60 * 60 * 1000 // 24 часа
    }
    
    fun shouldUpdatePlaylist(): Boolean {
        val lastUpdate = prefs.getLong(LAST_UPDATE_KEY, 0)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastUpdate) > UPDATE_INTERVAL
    }
    
    fun updatePlaylist(callback: (Boolean, String?) -> Unit) {
        executor.execute {
            try {
                Log.d(TAG, "Starting playlist update")
                
                m3uParser.loadChannelsFromM3U { channels, error ->
                    if (error == null && channels.isNotEmpty()) {
                        // Сохраняем обновленные каналы
                        val repository = ChannelRepository(context)
                        repository.cacheChannels(channels)
                        
                        // Обновляем время последнего обновления
                        prefs.edit().putLong(LAST_UPDATE_KEY, System.currentTimeMillis()).apply()
                        
                        Log.d(TAG, "Playlist updated successfully: ${channels.size} channels")
                        callback(true, null)
                    } else {
                        Log.e(TAG, "Failed to update playlist: $error")
                        callback(false, error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating playlist", e)
                callback(false, e.message)
            }
        }
    }
    
    fun getLastUpdateTime(): Long {
        return prefs.getLong(LAST_UPDATE_KEY, 0)
    }
    
    fun getLastUpdateTimeString(): String {
        val lastUpdate = getLastUpdateTime()
        if (lastUpdate == 0L) {
            return "Никогда"
        }
        
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - lastUpdate
        
        return when {
            diff < 60 * 1000 -> "Только что"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} мин назад"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} ч назад"
            else -> "${diff / (24 * 60 * 60 * 1000)} дн назад"
        }
    }
    
    fun clearCache() {
        prefs.edit().remove("cached_channels").apply()
        Log.d(TAG, "Cache cleared")
    }
    
    fun getCacheSize(): Int {
        val cachedJson = prefs.getString("cached_channels", "[]") ?: "[]"
        return try {
            val type = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, Channel::class.java).type
            val channels: List<Channel> = gson.fromJson(cachedJson, type) ?: emptyList()
            channels.size
        } catch (e: Exception) {
            0
        }
    }
} 