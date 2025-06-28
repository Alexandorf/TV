package com.example.tvchannels

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.example.tvchannels.data.Channel
import com.example.tvchannels.data.ChannelRepository
import com.example.tvchannels.ui.ChannelAdapter
import com.example.tvchannels.ui.ChannelPresenter
import com.example.tvchannels.ui.SearchFragment
import com.example.tvchannels.utils.PlaylistUpdater

class MainActivity : FragmentActivity() {
    
    private lateinit var browseFragment: BrowseSupportFragment
    private lateinit var channelRepository: ChannelRepository
    private lateinit var playlistUpdater: PlaylistUpdater
    private val channelAdapter = ChannelAdapter()
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        channelRepository = ChannelRepository(this)
        playlistUpdater = PlaylistUpdater(this)
        
        setupBrowseFragment()
        
        // Проверяем необходимость обновления плейлиста
        if (playlistUpdater.shouldUpdatePlaylist()) {
            updatePlaylist()
        } else {
            loadChannels()
        }
    }
    
    private fun setupBrowseFragment() {
        browseFragment = BrowseSupportFragment()
        browseFragment.setHeadersState(BrowseSupportFragment.HEADERS_ENABLED)
        browseFragment.setHeadersTransitionOnBackEnabled(true)
        browseFragment.setTitle(getString(R.string.app_name))
        browseFragment.setBadgeDrawable(getDrawable(R.drawable.ic_tv))
        
        // Настройка поиска
        browseFragment.setOnSearchClickedListener {
            val searchFragment = SearchFragment()
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, searchFragment)
                .addToBackStack(null)
                .commit()
        }
        
        // Настройка настроек
        browseFragment.setOnBrowseItemClickListener { itemViewHolder, item ->
            when (item.id) {
                R.id.action_update_playlist -> {
                    updatePlaylist()
                }
                R.id.action_clear_cache -> {
                    playlistUpdater.clearCache()
                    loadChannels()
                }
                R.id.action_about -> {
                    // Показать информацию о приложении
                }
            }
        }
        
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, browseFragment)
            .commit()
    }
    
    private fun updatePlaylist() {
        Log.d(TAG, "Updating playlist...")
        playlistUpdater.updatePlaylist { success, error ->
            runOnUiThread {
                if (success) {
                    Log.d(TAG, "Playlist updated successfully")
                    loadChannels()
                } else {
                    Log.e(TAG, "Failed to update playlist: $error")
                    loadChannels() // Загружаем кэшированные данные
                }
            }
        }
    }
    
    private fun loadChannels() {
        channelRepository.getChannels { channels, error ->
            runOnUiThread {
                if (error != null) {
                    Log.w(TAG, "Error loading channels: $error")
                    // Показываем уведомление об ошибке
                }
                
                if (channels.isNotEmpty()) {
                    setupChannelRows(channels)
                }
            }
        }
    }
    
    private fun setupChannelRows(channels: List<Channel>) {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        
        // Группируем каналы по категориям
        val channelsByCategory = channels.groupBy { it.category }
        
        channelsByCategory.forEach { (category, categoryChannels) ->
            val listRowAdapter = ArrayObjectAdapter(ChannelPresenter())
            categoryChannels.forEach { channel ->
                listRowAdapter.add(channel)
            }
            
            val header = HeaderItem(category)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
        
        browseFragment.adapter = rowsAdapter
        
        // Настройка обработчика кликов
        browseFragment.onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is Channel) {
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("channel_id", item.id)
                    putExtra("channel_name", item.name)
                    putExtra("stream_url", item.streamUrl)
                    putExtra("logo_url", item.logoUrl)
                }
                startActivity(intent)
            }
        }
        
        // Настройка обработчика долгих нажатий для избранного
        browseFragment.onItemViewLongClickedListener = OnItemViewLongClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is Channel) {
                toggleFavorite(item)
                true
            } else {
                false
            }
        }
    }
    
    private fun toggleFavorite(channel: Channel) {
        val favorites = channelRepository.getFavorites()
        val isFavorite = favorites.any { it.id == channel.id }
        
        if (isFavorite) {
            channelRepository.removeFromFavorites(channel.id)
            Log.d(TAG, "Removed ${channel.name} from favorites")
        } else {
            channelRepository.addToFavorites(channel.id)
            Log.d(TAG, "Added ${channel.name} to favorites")
        }
    }
} 