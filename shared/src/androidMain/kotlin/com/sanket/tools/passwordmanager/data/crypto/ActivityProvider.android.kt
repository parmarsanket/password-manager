package com.sanket.tools.passwordmanager.data.crypto

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

class ActivityProvider {
    private var activityRef: WeakReference<FragmentActivity>? = null

    fun setCurrentActivity(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
    }

    fun clearActivity(activity: FragmentActivity) {
        if (activityRef?.get() === activity) {
            activityRef = null
        }
    }

    fun currentActivity(): FragmentActivity? = activityRef?.get()
}
