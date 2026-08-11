package com.example.calleridapp

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

/**
 * Draws a small floating card on top of the incoming-call screen,
 * showing the identified name/spam-status. Requires the user to have
 * granted "Display over other apps" permission.
 */
class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val number = intent?.getStringExtra("number") ?: ""
        val name = intent?.getStringExtra("name") ?: "Unknown"
        val isSpam = intent?.getBooleanExtra("isSpam", false) ?: false

        showOverlay(number, name, isSpam)
        return START_NOT_STICKY
    }

    private fun showOverlay(number: String, name: String, isSpam: Boolean) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_caller_card, null)

        val nameText = overlayView!!.findViewById<TextView>(R.id.overlayName)
        val numberText = overlayView!!.findViewById<TextView>(R.id.overlayNumber)

        nameText.text = if (isSpam) "$name (Likely Spam)" else name
        nameText.setTextColor(if (isSpam) Color.RED else Color.BLACK)
        numberText.text = number

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 100

        windowManager?.addView(overlayView, params)

        // Auto-remove after 15 seconds (call usually answered/rejected by then)
        overlayView?.postDelayed({ removeOverlay() }, 15000)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}
