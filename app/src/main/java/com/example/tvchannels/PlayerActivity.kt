package com.example.tvchannels

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvchannels.data.Channel
import com.example.tvchannels.data.ChannelRepository
import com.example.tvchannels.ui.ChannelAdapter

class PlayerActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playerControls: View
    private lateinit var channelName: TextView
    private lateinit var channelNumber: TextView
    private lateinit var programInfo: TextView
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var channelList: RecyclerView
    private lateinit var channelAdapter: ChannelAdapter
    private lateinit var channelRepository: ChannelRepository
    
    private var currentChannel: Channel? = null
    private var channels: List<Channel> = emptyList()
    private var currentChannelIndex = 0
    private var isControlsVisible = false
    private var controlsHandler = Handler(Looper.getMainLooper())
    private var controlsRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        
        channelRepository = ChannelRepository(this)
        
        initializeViews()
        setupMediaPlayer()
        loadChannels()
        setupControls()
        
        // Get channel info from intent
        val channelId = intent.getStringExtra("channel_id")
        val channelNameStr = intent.getStringExtra("channel_name")
        val streamUrl = intent.getStringExtra("stream_url")
        
        if (channelId != null && streamUrl != null) {
            playChannel(channelId, channelNameStr ?: "", streamUrl)
        }
    }

    private fun initializeViews() {
        surfaceView = findViewById(R.id.video_surface)
        playerControls = findViewById(R.id.player_controls)
        channelName = findViewById(R.id.channel_name)
        channelNumber = findViewById(R.id.channel_number)
        programInfo = findViewById(R.id.program_info)
        volumeSeekBar = findViewById(R.id.volume_seekbar)
        channelList = findViewById(R.id.channel_list)
        
        surfaceView.holder.addCallback(this)
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }
        mediaPlayer.setOnErrorListener { _, what, extra ->
            Toast.makeText(this, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun loadChannels() {
        channelRepository.getChannels { channels, error ->
            runOnUiThread {
                if (error == null || channels.isNotEmpty()) {
                    this.channels = channels
                    setupChannelList()
                } else {
                    Toast.makeText(this, "Ошибка загрузки каналов: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupChannelList() {
        channelAdapter = ChannelAdapter(channels) { channel ->
            playChannel(channel.id, channel.name, channel.streamUrl)
            hideChannelList()
        }
        
        channelList.layoutManager = LinearLayoutManager(this)
        channelList.adapter = channelAdapter
    }

    private fun setupControls() {
        // Volume control
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress / 100f
                    mediaPlayer.setVolume(volume, volume)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Navigation buttons
        findViewById<ImageButton>(R.id.btn_previous_channel).setOnClickListener {
            previousChannel()
        }
        
        findViewById<ImageButton>(R.id.btn_next_channel).setOnClickListener {
            nextChannel()
        }
        
        findViewById<ImageButton>(R.id.btn_exit).setOnClickListener {
            finish()
        }
        
        findViewById<ImageButton>(R.id.btn_favorite).setOnClickListener {
            toggleFavorite()
        }
        
        findViewById<ImageButton>(R.id.btn_settings).setOnClickListener {
            // TODO: Show settings
        }
        
        findViewById<ImageButton>(R.id.btn_mute).setOnClickListener {
            toggleMute()
        }
    }

    private fun playChannel(channelId: String, channelNameStr: String, streamUrl: String) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, Uri.parse(streamUrl))
            mediaPlayer.prepareAsync()
            
            channelName.text = channelNameStr
            currentChannel = channels.find { it.id == channelId }
            currentChannelIndex = channels.indexOfFirst { it.id == channelId }
            
            if (currentChannelIndex >= 0) {
                channelNumber.text = getString(R.string.channel_number, currentChannelIndex + 1)
            }
            
            updateProgramInfo()
            showControls()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка загрузки канала", Toast.LENGTH_SHORT).show()
        }
    }

    private fun previousChannel() {
        if (channels.isNotEmpty()) {
            val newIndex = if (currentChannelIndex > 0) currentChannelIndex - 1 else channels.size - 1
            val channel = channels[newIndex]
            playChannel(channel.id, channel.name, channel.streamUrl)
        }
    }

    private fun nextChannel() {
        if (channels.isNotEmpty()) {
            val newIndex = if (currentChannelIndex < channels.size - 1) currentChannelIndex + 1 else 0
            val channel = channels[newIndex]
            playChannel(channel.id, channel.name, channel.streamUrl)
        }
    }

    private fun toggleFavorite() {
        currentChannel?.let { channel ->
            if (channel.isFavorite) {
                channelRepository.removeFromFavorites(channel.id)
                Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show()
            } else {
                channelRepository.addToFavorites(channel.id)
                Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleMute() {
        val isMuted = volumeSeekBar.progress == 0
        volumeSeekBar.progress = if (isMuted) 80 else 0
    }

    private fun showControls() {
        isControlsVisible = true
        playerControls.visibility = View.VISIBLE
        
        controlsRunnable?.let { controlsHandler.removeCallbacks(it) }
        controlsRunnable = Runnable { hideControls() }
        controlsHandler.postDelayed(controlsRunnable!!, 5000)
    }

    private fun hideControls() {
        isControlsVisible = false
        playerControls.visibility = View.GONE
    }

    private fun showChannelList() {
        channelList.visibility = View.VISIBLE
        channelList.requestFocus()
    }

    private fun hideChannelList() {
        channelList.visibility = View.GONE
        surfaceView.requestFocus()
    }

    private fun updateProgramInfo() {
        // TODO: Get current program info from EPG
        programInfo.text = "Текущая программа"
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (isControlsVisible) {
                    hideControls()
                } else {
                    showControls()
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                previousChannel()
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                nextChannel()
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                showChannelList()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (channelList.visibility == View.VISIBLE) {
                    hideChannelList()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mediaPlayer.setDisplay(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Surface changed
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Surface destroyed
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        controlsRunnable?.let { controlsHandler.removeCallbacks(it) }
    }
} 