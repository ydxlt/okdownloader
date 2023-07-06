package com.billbook.lib.downloader.internal.core

import com.billbook.lib.downloader.Download
import com.billbook.lib.downloader.Downloader
import com.billbook.lib.downloader.EventListener
import com.billbook.lib.downloader.Interceptor
import java.util.ServiceLoader
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

internal class DefaultDownloadCall(
    private val client: Downloader,
    private val originalRequest: Download.Request
) : Download.Call {

    private val eventListener: EventListener = client.eventListenerFactory.create(this)
    private val canceled = AtomicBoolean(false)
    private val executed = AtomicBoolean(false)

    override val request: Download.Request get() = originalRequest

    override fun execute(): Download.Response {
        return execute(Download.Callback.NOOP)
    }

    override fun execute(callback: Download.Callback): Download.Response {
        check(executed.compareAndSet(false, true)) { "Already Executed" }
        callback.onStart(this)
        eventListener.callStart(this)
        try {
            client.downloadPool.executed(this, eventListener)
            val response = getResponseWithInterceptorChain(callback)
            if (response.isSuccessful()) {
                callback.onSuccess(this, response)
                eventListener.callSuccess(this, response)
            } else {
                callback.onFailure(this, response)
                eventListener.callFailed(this, response)
            }
            return response
        } finally {
            eventListener.callEnd(this)
            client.downloadPool.finished(this)
        }
    }

    private fun getResponseWithInterceptorChain(callback: Download.Callback): Download.Response {
        val interceptors = mutableListOf<Interceptor>()
        interceptors += RetryInterceptor(client)
        interceptors += LocalExistsInterceptor()
        interceptors += SynchronousInterceptor()
        interceptors += ExceptionInterceptor()
        interceptors += client.interceptors
        interceptors += extInterceptors
        interceptors += VerifierInterceptor()
        interceptors += ExchangeInterceptor(client)
        val chain = DefaultInterceptorChain(0, originalRequest, this, callback, interceptors)
        return chain.proceed(originalRequest)
    }

    override fun enqueue() {
        enqueue(Download.Callback.NOOP)
    }

    override fun enqueue(callback: Download.Callback) {
        check(executed.compareAndSet(false, true)) { "Already Executed" }
        client.downloadPool.enqueue(AsyncCall(callback, request.priority), eventListener)
    }

    override fun cancel() {
        if (isCanceled()) return
        this.canceled.getAndSet(true)
        this.eventListener.callCanceled(this)
    }

    override fun isExecuted(): Boolean {
        return this.executed.get()
    }

    override fun isCanceled(): Boolean {
        return this.canceled.get()
    }

    override fun toString(): String {
        return "DefaultDownloadCall(request=$request)"
    }

    internal inner class AsyncCall(
        private val callback: Download.Callback,
        private val priority: Download.Priority
    ) : Runnable, Comparable<Download.Priority> {

        val call: Download.Call
            get() = this@DefaultDownloadCall

        override fun run() {
            callback.onStart(call)
            eventListener.callStart(call)
            try {
                val response = getResponseWithInterceptorChain(callback)
                if (response.isSuccessful()) {
                    callback.onSuccess(call, response)
                    eventListener.callSuccess(call, response)
                } else {
                    callback.onFailure(call, response)
                    eventListener.callFailed(call, response)
                }
            } finally {
                eventListener.callEnd(call)
                client.downloadPool.finished(this)
            }
        }

        fun executeOn(executorService: ExecutorService) {
            var success = false
            try {
                executorService.execute(this)
                success = true
            } finally {
                if (!success) {
                    client.downloadPool.finished(this)
                }
            }
        }

        override fun compareTo(other: Download.Priority): Int {
            return this.priority.ordinal - other.ordinal
        }
    }

    companion object {
        private val extInterceptors: List<Interceptor> by lazy(LazyThreadSafetyMode.NONE) {
            ServiceLoader.load(Interceptor::class.java).toList()
        }
    }


}

