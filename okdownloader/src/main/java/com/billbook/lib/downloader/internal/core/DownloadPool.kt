package com.billbook.lib.downloader.internal.core

import com.billbook.lib.downloader.Download
import com.billbook.lib.downloader.EventListener
import java.util.*
import java.util.concurrent.ExecutorService

internal class DownloadPool(private val executorService: ExecutorService, private val idleCallback: Runnable?) {

    private val readyAsyncCalls = PriorityQueue<DefaultDownloadCall.AsyncCall>()
    private val runningAsyncCalls = ArrayDeque<DefaultDownloadCall.AsyncCall>()
    private val runningSyncCalls = ArrayDeque<Download.Call>()

    @get:Synchronized
    var maxRequests = 64
        set(maxRequests) {
            require(maxRequests >= 1) { "max < 1: $maxRequests" }
            synchronized(this) {
                field = maxRequests
            }
            promoteAndExecute()
        }

    @Synchronized
    internal fun executed(call: Download.Call, eventListener: EventListener) {
        if (findHitCall(call) != null) {
            eventListener.callHit(call)
        }
        runningSyncCalls.add(call)
    }

    fun enqueue(call: DefaultDownloadCall.AsyncCall, eventListener: EventListener) {
        if (findHitCall(call.call) != null) {
            eventListener.callHit(call.call)
        }
        synchronized(this) {
            readyAsyncCalls.add(call)
        }
        promoteAndExecute()
    }

    private fun findHitCall(call: Download.Call): Download.Call? {
        synchronized(this) {
            val hitCall = readyAsyncCalls.find { it.call.request.url == call.request.url }?.call
                ?: runningSyncCalls.find { it.request.url == call.request.url }
                ?: runningAsyncCalls.find { it.call.request.url == call.request.url }?.call
            if (hitCall != null && hitCall != call) {
                return hitCall
            }
        }
        return null
    }

    private fun promoteAndExecute(): Boolean {
        val executableCalls = mutableListOf<DefaultDownloadCall.AsyncCall>()
        val isRunning: Boolean
        synchronized(this) {
            while (!readyAsyncCalls.isEmpty()) {
                if (runningAsyncCalls.size >= this.maxRequests) break // Max capacity.
                val asyncCall = readyAsyncCalls.poll()
                executableCalls.add(asyncCall)
                runningAsyncCalls.add(asyncCall)
            }
            isRunning = runningCallsCount() > 0
        }
        for (i in 0 until executableCalls.size) {
            val asyncCall = executableCalls[i]
            asyncCall.executeOn(executorService)
        }

        return isRunning
    }

    @Synchronized
    fun queuedCalls(): List<Download.Call> {
        return Collections.unmodifiableList(readyAsyncCalls.map { it.call })
    }

    /** Returns a snapshot of the calls currently being executed. */
    @Synchronized
    fun runningCalls(): List<Download.Call> {
        return Collections.unmodifiableList(runningSyncCalls + runningAsyncCalls.map { it.call })
    }

    @Synchronized
    fun queuedCallsCount(): Int = readyAsyncCalls.size

    @Synchronized
    fun runningCallsCount(): Int = runningAsyncCalls.size + runningSyncCalls.size

    fun finished(call: Download.Call) {
        finished(runningSyncCalls, call)
    }

    fun finished(call: DefaultDownloadCall.AsyncCall) {
        finished(runningAsyncCalls, call)
    }

    private fun <T> finished(calls: Deque<T>, call: T) {
        val idleCallback: Runnable?
        synchronized(this) {
            if (!calls.remove(call)) throw AssertionError("Call wasn't in-flight!")
            idleCallback = this.idleCallback
        }

        val isRunning = promoteAndExecute()

        if (!isRunning && idleCallback != null) {
            idleCallback.run()
        }
    }
}