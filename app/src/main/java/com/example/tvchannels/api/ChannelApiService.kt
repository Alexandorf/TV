package com.example.tvchannels.api

import com.example.tvchannels.data.Channel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ChannelApiService {
    
    @GET("channels")
    fun getChannels(): Call<List<Channel>>
    
    @GET("channels/search")
    fun searchChannels(@Query("q") query: String): Call<List<Channel>>
    
    @GET("channels/category")
    fun getChannelsByCategory(@Query("category") category: String): Call<List<Channel>>
    
    @GET("channels/favorites")
    fun getFavoriteChannels(): Call<List<Channel>>
} 