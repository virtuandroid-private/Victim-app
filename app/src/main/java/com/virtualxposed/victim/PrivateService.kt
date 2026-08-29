package com.virtualxposed.victim

import android.app.Service
import android.content.Intent
import android.os.IBinder

class PrivateService : Service() {
    private val binder = object : IPrivateService.Stub() {
        override fun sendMessage(message: String): String {
            println("Got message: $message")
            return "Server received: '$message'"
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Starting service: ${this.javaClass.name}")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}