package com.example.tvchannels.data

object TestStreams {
    
    // Тестовые стримы для демонстрации
    // В реальном приложении замените на реальные URL
    
    val TEST_STREAMS = mapOf(
        "1" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", // Тестовый стрим Big Buck Bunny
        "2" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "3" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "4" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "5" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "6" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "7" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "8" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "9" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "10" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "11" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "12" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "13" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "14" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "15" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    )
    
    // Альтернативные тестовые стримы
    val ALTERNATIVE_STREAMS = mapOf(
        "1" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "2" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "3" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        "4" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        "5" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
        "6" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
        "7" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
        "8" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        "9" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
        "10" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        "11" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
        "12" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
        "13" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "14" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "15" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    )
    
    fun getTestStream(channelId: String): String {
        return TEST_STREAMS[channelId] ?: ALTERNATIVE_STREAMS[channelId] ?: ""
    }
} 