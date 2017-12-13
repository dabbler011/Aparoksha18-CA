package org.aparoksha.app18.ca.models

/**
 * Created by akshat on 2/12/17.
 */
data class Data (
        var count: Long = 0,
        var revealedCount: Long = 0,
        var totalPoints: Long = 0,
        var identifier: String = "",
        var collegeName: String = "",
        var userName: String = "",
        var fullName: String = "",
        var gender: String = "",
        var images: List<Image>? = null,
        var accountVerified: Boolean = false
)
