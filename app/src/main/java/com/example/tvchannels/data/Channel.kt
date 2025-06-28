package com.example.tvchannels.data

data class Channel(
    val id: String,
    val name: String,
    val category: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val description: String? = null,
    val isFavorite: Boolean = false,
    val channelNumber: Int = 0,
    val language: String = "ru",
    val quality: String = "HD"
) 