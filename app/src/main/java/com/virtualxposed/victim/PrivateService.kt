package com.virtualxposed.victim

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

class PrivateService : Service() {
    private val binder = object : IPrivateService.Stub() {
        override fun sendMessage(message: String): String {
            println("Got message: $message")
            return "Server received: '$message'"
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startMessage = intent?.getStringExtra("StartMessage")
        if (startMessage != null) {
            Toast.makeText(baseContext, startMessage, Toast.LENGTH_LONG).show()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}