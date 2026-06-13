package com.example.tiny_computer

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.Window
import android.view.View
import java.util.WeakHashMap
import java.lang.ref.WeakReference

object DesktopShortcutManager {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val unhandledListenerViews = WeakHashMap<View, Boolean>()
    private var mainActivityRef: WeakReference<MainActivity>? = null
    private var activeActivityRef: WeakReference<Activity>? = null
    private var desktopActivityRef: WeakReference<Activity>? = null

    fun onActivityResumed(activity: Activity) {
        activeActivityRef = WeakReference(activity)
        if (activity is MainActivity) {
            mainActivityRef = WeakReference(activity)
        } else if (isVncDesktopActivity(activity)) {
            desktopActivityRef = WeakReference(activity)
        }
        installAggressively(activity)
    }

    fun onActivityDestroyed(activity: Activity) {
        if (mainActivityRef?.get() === activity) {
            mainActivityRef = null
        }
        if (activeActivityRef?.get() === activity) {
            activeActivityRef = null
        }
        if (desktopActivityRef?.get() === activity) {
            desktopActivityRef = null
        }
    }

    fun handleShortcutFrom(activity: Activity): Boolean {
        if (isVncDesktopActivity(activity)) {
            activity.finish()
            return true
        }

        val desktopActivity = desktopActivityRef?.get()
        if (desktopActivity != null && !desktopActivity.isFinishing && !desktopActivity.isDestroyed) {
            desktopActivity.finish()
            return true
        }

        val mainActivity = when (activity) {
            is MainActivity -> activity
            else -> mainActivityRef?.get()
        }
        mainActivity?.handleDesktopShortcut()
        return mainActivity != null
    }

    fun isDesktopShortcut(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_F11) return true
        return event.isCtrlPressed &&
            event.isAltPressed &&
            event.keyCode == KeyEvent.KEYCODE_D
    }

    fun shouldConsume(event: KeyEvent): Boolean {
        return event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 && isDesktopShortcut(event)
    }

    private fun isVncDesktopActivity(activity: Activity): Boolean {
        val name = activity.javaClass.name.lowercase()
        return name.contains(".vnc.") || name.endsWith(".vncactivity") || name.contains("vncactivity")
    }

    private fun installAggressively(activity: Activity) {
        val delays = longArrayOf(
            0, 50, 100, 200, 300, 500, 800, 1200, 1800, 2500,
            3500, 5000, 7500, 10000, 15000, 20000, 30000, 45000, 60000,
        )
        delays.forEach { delay ->
            mainHandler.postDelayed({
                installShortcutCallback(activity)
                installDecorShortcutListener(activity)
            }, delay)
        }
    }

    private fun installShortcutCallback(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return
        val window = activity.window ?: return
        val current = window.callback ?: return
        if (current is DesktopShortcutWindowCallback) return
        window.callback = DesktopShortcutWindowCallback(activity, current)
    }

    private fun installDecorShortcutListener(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return
        val decor = activity.window?.decorView ?: return
        decor.isFocusableInTouchMode = true
        decor.setOnKeyListener(DesktopShortcutViewKeyListener(activity))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && unhandledListenerViews[decor] != true) {
            decor.addOnUnhandledKeyEventListener(DesktopShortcutUnhandledKeyListener(activity))
            unhandledListenerViews[decor] = true
        }
    }

    private class DesktopShortcutWindowCallback(
        private val activity: Activity,
        private val base: Window.Callback,
    ) : Window.Callback by base {
        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (shouldConsume(event)) {
                handleShortcutFrom(activity)
                return true
            }
            return base.dispatchKeyEvent(event)
        }
    }

    private class DesktopShortcutViewKeyListener(
        private val activity: Activity,
    ) : View.OnKeyListener {
        override fun onKey(view: View?, keyCode: Int, event: KeyEvent): Boolean {
            if (shouldConsume(event)) {
                handleShortcutFrom(activity)
                return true
            }
            return false
        }
    }

    private class DesktopShortcutUnhandledKeyListener(
        private val activity: Activity,
    ) : View.OnUnhandledKeyEventListener {
        override fun onUnhandledKeyEvent(view: View?, event: KeyEvent): Boolean {
            if (shouldConsume(event)) {
                handleShortcutFrom(activity)
                return true
            }
            return false
        }
    }
}
