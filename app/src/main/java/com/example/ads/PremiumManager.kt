package com.example.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PremiumManager {
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    fun isPremiumUser(): Boolean = _isPremium.value

    fun premiumFeatureEnabled(featureId: String): Boolean {
        // In V1, core features are accessible, future pro features are gated or unlockable via reward
        return true
    }

    fun setPremium(enabled: Boolean) {
        _isPremium.value = enabled
    }
}
