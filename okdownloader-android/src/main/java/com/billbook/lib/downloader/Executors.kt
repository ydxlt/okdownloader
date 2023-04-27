package com.billbook.lib.downloader

import android.os.Handler
import android.os.Looper

/**
 * @author xluotong@gmail.com
 */
private val MainThreadExecutor: CallbackExecutor = object : CallbackExecutor() {

    override val name: String get() = "Main"

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        mainHandler.post(command)
    }
}

val CallbackExecutor.Companion.Main: CallbackExecutor get() = MainThreadExecutor