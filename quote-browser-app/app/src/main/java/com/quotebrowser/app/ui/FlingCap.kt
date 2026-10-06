package com.quotebrowser.app.ui

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * v1.88.37: ceiling for fling velocity (px/s) before the platform fling
 * animation runs. Touch-sensor noise on lift-off can make VelocityTracker
 * report 20,000+ px/s from a near-stationary finger, teleporting the list;
 * deliberate flicks live well under this ceiling, so fast scrolling still
 * feels fast. Delegates to the platform default otherwise.
 */
const val MaxFlingVelocityPxPerSec = 12_000f

@Composable
fun rememberCappedFlingBehavior(
    maxVelocityPxPerSec: Float = MaxFlingVelocityPxPerSec
): FlingBehavior {
    val default = ScrollableDefaults.flingBehavior()
    return remember(default, maxVelocityPxPerSec) {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                val clamped = initialVelocity.coerceIn(-maxVelocityPxPerSec, maxVelocityPxPerSec)
                return with(default) { performFling(clamped) }
            }
        }
    }
}
