package com.example.tiny_computer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.android.material.color.DynamicColors
import io.flutter.app.FlutterApplication
import me.weishu.reflection.Reflection

class MainApplication : FlutterApplication() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this@MainApplication)
        registerActivityLifecycleCallbacks(DesktopShortcutActivityCallbacks())
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }

    private class DesktopShortcutActivityCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            DesktopShortcutManager.onActivityResumed(activity)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {
            DesktopShortcutManager.onActivityDestroyed(activity)
        }
    }
}
