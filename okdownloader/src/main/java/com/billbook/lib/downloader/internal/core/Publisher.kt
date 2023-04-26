package com.billbook.lib.downloader.internal.core

import com.billbook.lib.downloader.Download
import java.util.concurrent.CopyOnWriteArrayList

internal class Publisher : Download.Subjection, Download.Subscriber {

    private val observers: MutableMap<String, MutableSet<Download.Subscriber>> = HashMap()
    private val globalObservers = CopyOnWriteArrayList<Download.Subscriber>()

    override fun subscribe(subscriber: Download.Subscriber) {
        globalObservers += subscriber
    }

    override fun subscribe(url: String, subscriber: Download.Subscriber) {
        synchronized(observers) {
            observers.getOrPut(url) { mutableSetOf() } += subscriber
        }
    }

    override fun unsubscribe(subscriber: Download.Subscriber) {
        globalObservers -= subscriber
    }

    override fun unsubscribe(url: String, subscriber: Download.Subscriber) {
        synchronized(observers) {
            observers.getOrPut(url) { mutableSetOf() } -= subscriber
        }
    }

    private fun Download.Call.dispatch(block: Download.Subscriber.() -> Unit) {
        synchronized(observers) {
            observers[request.url]?.forEach(block)
        }
        globalObservers.forEach(block)
    }

    override fun onStart(call: Download.Call) {
        call.dispatch { onStart(call) }
    }

    override fun onLoading(call: Download.Call, current: Long, total: Long) {
        call.dispatch { onLoading(call, current, total) }
    }

    override fun onChecking(call: Download.Call) {
        call.dispatch { onChecking(call) }
    }

    override fun onRetrying(call: Download.Call) {
        call.dispatch { onRetrying(call) }
    }

    override fun onSuccess(call: Download.Call, response: Download.Response) {
        call.dispatch { onSuccess(call, response) }
    }

    override fun onFailure(call: Download.Call, response: Download.Response) {
        call.dispatch { onFailure(call, response) }
    }

    override fun onCancel(call: Download.Call) {
        call.dispatch { onCancel(call) }
    }
}