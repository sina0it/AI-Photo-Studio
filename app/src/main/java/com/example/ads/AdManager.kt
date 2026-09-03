package com.example.ads

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AdManager {
    val isRewardedReady: StateFlow<Boolean>
    fun isRewardedAdReady(): Boolean
    suspend fun showRewardedAd(activity: Activity?, onRewardEarned: () -> Unit, onAdClosed: () -> Unit)
    suspend fun showInterstitialAd(activity: Activity?, onAdClosed: () -> Unit)
}

class TapsellAdManager(private val context: Context) : AdManager {

    private val _isRewardedReady = MutableStateFlow(true)
    override val isRewardedReady: StateFlow<Boolean> = _isRewardedReady.asStateFlow()

    // Tapsell zone IDs can be configured via build/env without hardcoding
    var tapsellZoneId: String? = null

    override fun isRewardedAdReady(): Boolean = _isRewardedReady.value

    override suspend fun showRewardedAd(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit
    ) {
        // Non-intrusive safe simulation layer that completes gracefully
        _isRewardedReady.value = false
        delay(1200) // Brief mock presentation
        onRewardEarned()
        onAdClosed()
        _isRewardedReady.value = true
    }

    override suspend fun showInterstitialAd(activity: Activity?, onAdClosed: () -> Unit) {
        delay(800)
        onAdClosed()
    }
}
