package com.example.tvchannels.data

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.Executors

class M3UParser {
    
    companion object {
        private const val TAG = "M3UParser"
        private const val M3U_URL = "https://gitlab.com/iptv135435/iptvshared/raw/main/IPTV_SHARED.m3u"
    }
    
    fun loadChannelsFromM3U(callback: (List<Channel>, String?) -> Unit) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val channels = parseM3UFromUrl(M3U_URL)
                callback(channels, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading M3U: ${e.message}")
                callback(emptyList(), "Ошибка загрузки плейлиста: ${e.message}")
            }
        }
    }
    
    private fun parseM3UFromUrl(url: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        var currentChannel: MutableMap<String, String>? = null
        var channelId = 1
        
        try {
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line = line?.trim()
                    if (line.isNullOrEmpty()) continue
                    
                    if (line!!.startsWith("#EXTINF:")) {
                        // Парсим информацию о канале
                        currentChannel = parseExtInf(line)
                    } else if (!line.startsWith("#") && currentChannel != null) {
                        // Это URL стрима
                        val channel = createChannel(currentChannel!!, line, channelId++)
                        if (channel != null) {
                            channels.add(channel)
                        }
                        currentChannel = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing M3U: ${e.message}")
            throw e
        }
        
        Log.d(TAG, "Loaded ${channels.size} channels from M3U")
        return channels
    }
    
    private fun parseExtInf(line: String): MutableMap<String, String> {
        val channelInfo = mutableMapOf<String, String>()
        
        try {
            // Извлекаем название канала
            val nameMatch = Regex(",(.+)$").find(line)
            if (nameMatch != null) {
                channelInfo["name"] = nameMatch.groupValues[1].trim()
            }
            
            // Извлекаем tvg-id
            val tvgIdMatch = Regex("tvg-id=\"([^\"]*)\"").find(line)
            if (tvgIdMatch != null) {
                channelInfo["tvg-id"] = tvgIdMatch.groupValues[1]
            }
            
            // Извлекаем tvg-logo
            val tvgLogoMatch = Regex("tvg-logo=\"([^\"]*)\"").find(line)
            if (tvgLogoMatch != null) {
                channelInfo["tvg-logo"] = tvgLogoMatch.groupValues[1]
            }
            
            // Извлекаем group-title
            val groupTitleMatch = Regex("group-title=\"([^\"]*)\"").find(line)
            if (groupTitleMatch != null) {
                channelInfo["group-title"] = groupTitleMatch.groupValues[1]
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing EXTINF: $line", e)
        }
        
        return channelInfo
    }
    
    private fun createChannel(channelInfo: Map<String, String>, streamUrl: String, channelId: Int): Channel? {
        val name = channelInfo["name"] ?: return null
        val logoUrl = channelInfo["tvg-logo"] ?: ""
        val groupTitle = channelInfo["group-title"] ?: "Другие"
        
        // Фильтруем информационные каналы
        if (groupTitle.contains("ИНФО", ignoreCase = true) || 
            name.contains("ОБНОВЛЕНИЕ", ignoreCase = true) ||
            name.contains("ПОДДЕРЖКА", ignoreCase = true)) {
            return null
        }
        
        // Определяем категорию на основе group-title
        val category = when {
            groupTitle.contains("Эфирные", ignoreCase = true) -> "Общие"
            groupTitle.contains("Развлекательные", ignoreCase = true) -> "Развлекательные"
            groupTitle.contains("Спорт", ignoreCase = true) -> "Спорт"
            groupTitle.contains("Детские", ignoreCase = true) -> "Детские"
            groupTitle.contains("Культура", ignoreCase = true) -> "Культура"
            groupTitle.contains("Новости", ignoreCase = true) -> "Новости"
            groupTitle.contains("Радио", ignoreCase = true) -> "Радио"
            else -> "Другие"
        }
        
        return Channel(
            id = channelId.toString(),
            name = name,
            category = category,
            streamUrl = streamUrl,
            logoUrl = if (logoUrl.isNotEmpty()) logoUrl else null,
            description = "Канал из плейлиста IPTV",
            channelNumber = channelId
        )
    }
} 