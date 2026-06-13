package com.example.tiny_computer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import com.google.android.material.color.DynamicColors
import io.flutter.app.FlutterApplication
import me.weishu.reflection.Reflection

class MainApplication : FlutterApplication() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this@MainApplication)
        registerActivityLifecycleCallbacks(F12ActivityCallbacks())
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }

    private class F12ActivityCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            val window = activity.window ?: return
            val current = window.callback ?: return
            if (current is F12WindowCallback) return
            window.callback = F12WindowCallback(activity, current)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    private class F12WindowCallback(
        private val activity: Activity,
        private val base: Window.Callback,
    ) : Window.Callback by base {
        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_F12) {
                if (activity is MainActivity) {
                    activity.handleF12Key()
                } else {
                    activity.finish()
                }
                return true
            }
            return base.dispatchKeyEvent(event)
        }
    }
}
