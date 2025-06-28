package com.example.tvchannels.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.tvchannels.api.ApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChannelRepository(private val context: Context? = null) {
    
    private val gson = Gson()
    private val prefs: SharedPreferences? = context?.getSharedPreferences("tv_channels", Context.MODE_PRIVATE)
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private val m3uParser = M3UParser()
    
    companion object {
        private const val TAG = "ChannelRepository"
    }
    
    fun getChannels(callback: (List<Channel>, String?) -> Unit) {
        // Сначала пытаемся загрузить из кэша
        val cachedChannels = getCachedChannels()
        if (cachedChannels.isNotEmpty()) {
            Log.d(TAG, "Loading ${cachedChannels.size} channels from cache")
            callback(cachedChannels, null)
        }
        
        // Затем загружаем свежие данные из M3U плейлиста
        loadChannelsFromM3U { channels, error ->
            if (error == null && channels.isNotEmpty()) {
                Log.d(TAG, "Loaded ${channels.size} channels from M3U")
                cacheChannels(channels)
                callback(channels, null)
            } else {
                // Если M3U недоступен, используем локальные данные
                val localChannels = getMockChannels()
                Log.d(TAG, "Using ${localChannels.size} local channels")
                callback(localChannels, error ?: "Используются локальные данные")
            }
        }
    }
    
    fun searchChannels(query: String, callback: (List<Channel>, String?) -> Unit) {
        // Сначала пытаемся API поиск
        ApiClient.channelApiService.searchChannels(query).enqueue(object : Callback<List<Channel>> {
            override fun onResponse(call: Call<List<Channel>>, response: Response<List<Channel>>) {
                if (response.isSuccessful) {
                    response.body()?.let { channels ->
                        callback(channels, null)
                    } ?: callback(emptyList(), "Пустой ответ от сервера")
                } else {
                    // Локальный поиск если API недоступен
                    val localChannels = getCachedChannels()
                    val searchResults = localChannels.filter { 
                        it.name.contains(query, ignoreCase = true) || 
                        it.description?.contains(query, ignoreCase = true) == true 
                    }
                    callback(searchResults, "API недоступен, используется локальный поиск")
                }
            }
            
            override fun onFailure(call: Call<List<Channel>>, t: Throwable) {
                // Локальный поиск при ошибке сети
                val localChannels = getCachedChannels()
                val searchResults = localChannels.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    it.description?.contains(query, ignoreCase = true) == true 
                }
                callback(searchResults, "Ошибка сети: ${t.message}")
            }
        })
    }
    
    fun getChannelsByCategory(category: String, callback: (List<Channel>, String?) -> Unit) {
        // Сначала пытаемся API запрос
        ApiClient.channelApiService.getChannelsByCategory(category).enqueue(object : Callback<List<Channel>> {
            override fun onResponse(call: Call<List<Channel>>, response: Response<List<Channel>>) {
                if (response.isSuccessful) {
                    response.body()?.let { channels ->
                        callback(channels, null)
                    } ?: callback(emptyList(), "Пустой ответ от сервера")
                } else {
                    // Локальная фильтрация если API недоступен
                    val localChannels = getCachedChannels()
                    val categoryChannels = localChannels.filter { it.category == category }
                    callback(categoryChannels, "API недоступен, используется локальная фильтрация")
                }
            }
            
            override fun onFailure(call: Call<List<Channel>>, t: Throwable) {
                // Локальная фильтрация при ошибке сети
                val localChannels = getCachedChannels()
                val categoryChannels = localChannels.filter { it.category == category }
                callback(categoryChannels, "Ошибка сети: ${t.message}")
            }
        })
    }
    
    fun getFavorites(): List<Channel> {
        val favoritesJson = prefs?.getString("favorites", "[]") ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        val favoriteIds: List<String> = gson.fromJson(favoritesJson, type)
        
        val allChannels = getCachedChannels()
        return allChannels.filter { it.id in favoriteIds }
    }
    
    fun addToFavorites(channelId: String) {
        val favorites = getFavoriteIds().toMutableList()
        if (!favorites.contains(channelId)) {
            favorites.add(channelId)
            saveFavorites(favorites)
        }
    }
    
    fun removeFromFavorites(channelId: String) {
        val favorites = getFavoriteIds().toMutableList()
        favorites.remove(channelId)
        saveFavorites(favorites)
    }
    
    // Публичный метод для кэширования каналов (используется PlaylistUpdater)
    fun cacheChannels(channels: List<Channel>) {
        try {
            val channelsJson = gson.toJson(channels)
            prefs?.edit()?.putString("cached_channels", channelsJson)?.apply()
            Log.d(TAG, "Cached ${channels.size} channels")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching channels", e)
        }
    }
    
    private fun loadChannelsFromM3U(callback: (List<Channel>, String?) -> Unit) {
        m3uParser.loadChannelsFromM3U { channels, error ->
            if (error == null && channels.isNotEmpty()) {
                callback(channels, null)
            } else {
                // При ошибке M3U используем локальные данные
                val localChannels = getMockChannels()
                callback(localChannels, error ?: "Ошибка загрузки M3U")
            }
        }
    }
    
    private fun getCachedChannels(): List<Channel> {
        val cachedJson = prefs?.getString("cached_channels", "[]") ?: "[]"
        val type = object : TypeToken<List<Channel>>() {}.type
        return try {
            gson.fromJson(cachedJson, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing cached channels", e)
            emptyList()
        }
    }
    
    private fun getFavoriteIds(): List<String> {
        val favoritesJson = prefs?.getString("favorites", "[]") ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(favoritesJson, type)
    }
    
    private fun saveFavorites(favoriteIds: List<String>) {
        val favoritesJson = gson.toJson(favoriteIds)
        prefs?.edit()?.putString("favorites", favoritesJson)?.apply()
    }
    
    private fun getMockChannels(): List<Channel> {
        return listOf(
            Channel(
                id = "1",
                name = "Первый канал",
                category = "Общие",
                streamUrl = TestStreams.getTestStream("1"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Logo_of_Channel_One_Russia.svg/200px-Logo_of_Channel_One_Russia.svg.png",
                description = "Первый канал России - главный телеканал страны",
                channelNumber = 1
            ),
            Channel(
                id = "2",
                name = "Россия 1",
                category = "Общие",
                streamUrl = TestStreams.getTestStream("2"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Russia-1_logo.svg/200px-Russia-1_logo.svg.png",
                description = "Всероссийская государственная телевизионная и радиовещательная компания",
                channelNumber = 2
            ),
            Channel(
                id = "3",
                name = "НТВ",
                category = "Общие",
                streamUrl = TestStreams.getTestStream("3"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/NTV_logo.svg/200px-NTV_logo.svg.png",
                description = "НТВ - новости, ток-шоу, сериалы",
                channelNumber = 3
            ),
            Channel(
                id = "4",
                name = "ТНТ",
                category = "Развлекательные",
                streamUrl = TestStreams.getTestStream("4"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/TNT_logo.svg/200px-TNT_logo.svg.png",
                description = "ТНТ - развлекательные программы и комедии",
                channelNumber = 4
            ),
            Channel(
                id = "5",
                name = "СТС",
                category = "Развлекательные",
                streamUrl = TestStreams.getTestStream("5"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/CTC_logo.svg/200px-CTC_logo.svg.png",
                description = "СТС - комедии, сериалы и развлекательные шоу",
                channelNumber = 5
            ),
            Channel(
                id = "6",
                name = "Рен ТВ",
                category = "Общие",
                streamUrl = TestStreams.getTestStream("6"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/REN_TV_logo.svg/200px-REN_TV_logo.svg.png",
                description = "Рен ТВ - новости, аналитика, документальные фильмы",
                channelNumber = 6
            ),
            Channel(
                id = "7",
                name = "ТВ Центр",
                category = "Общие",
                streamUrl = TestStreams.getTestStream("7"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/TVC_logo.svg/200px-TVC_logo.svg.png",
                description = "ТВ Центр - московский канал с общероссийским вещанием",
                channelNumber = 7
            ),
            Channel(
                id = "8",
                name = "Культура",
                category = "Культура",
                streamUrl = TestStreams.getTestStream("8"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Russia_K_logo.svg/200px-Russia_K_logo.svg.png",
                description = "Россия К - канал культуры и искусства",
                channelNumber = 8
            ),
            Channel(
                id = "9",
                name = "Матч ТВ",
                category = "Спорт",
                streamUrl = TestStreams.getTestStream("9"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Match_TV_logo.svg/200px-Match_TV_logo.svg.png",
                description = "Матч ТВ - спортивный канал",
                channelNumber = 9
            ),
            Channel(
                id = "10",
                name = "Карусель",
                category = "Детские",
                streamUrl = TestStreams.getTestStream("10"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Karusel_logo.svg/200px-Karusel_logo.svg.png",
                description = "Карусель - детский канал",
                channelNumber = 10
            ),
            Channel(
                id = "11",
                name = "Мульт",
                category = "Детские",
                streamUrl = TestStreams.getTestStream("11"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Mult_logo.svg/200px-Mult_logo.svg.png",
                description = "Мульт - канал мультфильмов",
                channelNumber = 11
            ),
            Channel(
                id = "12",
                name = "Россия 24",
                category = "Новости",
                streamUrl = TestStreams.getTestStream("12"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Russia_24_logo.svg/200px-Russia_24_logo.svg.png",
                description = "Россия 24 - новостной канал",
                channelNumber = 12
            ),
            Channel(
                id = "13",
                name = "Домашний",
                category = "Развлекательные",
                streamUrl = TestStreams.getTestStream("13"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Domashny_logo.svg/200px-Domashny_logo.svg.png",
                description = "Домашний - канал для всей семьи",
                channelNumber = 13
            ),
            Channel(
                id = "14",
                name = "Пятница!",
                category = "Развлекательные",
                streamUrl = TestStreams.getTestStream("14"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Friday_logo.svg/200px-Friday_logo.svg.png",
                description = "Пятница! - развлекательный канал",
                channelNumber = 14
            ),
            Channel(
                id = "15",
                name = "Звезда",
                category = "Общие",
                streamUrl = TestStreams.getTestStream("15"),
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Zvezda_logo.svg/200px-Zvezda_logo.svg.png",
                description = "Звезда - патриотический канал",
                channelNumber = 15
            )
        )
    }
} 