package com.example.tiny_computer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.Window
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
            installShortcutCallback(activity)
            Handler(Looper.getMainLooper()).postDelayed({ installShortcutCallback(activity) }, 250)
            Handler(Looper.getMainLooper()).postDelayed({ installShortcutCallback(activity) }, 1000)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}

        private fun installShortcutCallback(activity: Activity) {
            if (activity.isFinishing || activity.isDestroyed) return
            val window = activity.window ?: return
            val current = window.callback ?: return
            if (current is DesktopShortcutWindowCallback) return
            window.callback = DesktopShortcutWindowCallback(activity, current)
        }
    }

    private class DesktopShortcutWindowCallback(
        private val activity: Activity,
        private val base: Window.Callback,
    ) : Window.Callback by base {
        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_DOWN && isDesktopShortcut(event)) {
                if (activity is MainActivity) {
                    activity.handleDesktopShortcut()
                } else {
                    activity.finish()
                }
                return true
            }
            return base.dispatchKeyEvent(event)
        }

        private fun isDesktopShortcut(event: KeyEvent): Boolean {
            if (event.keyCode == KeyEvent.KEYCODE_F11) return true
            return event.isCtrlPressed &&
                event.isAltPressed &&
                event.keyCode == KeyEvent.KEYCODE_D
        }
    }
}
