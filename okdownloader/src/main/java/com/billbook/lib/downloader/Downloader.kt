package com.billbook.lib.downloader

import com.billbook.lib.downloader.internal.core.DefaultDownloadCall
import com.billbook.lib.downloader.internal.core.Dispatcher
import com.billbook.lib.downloader.internal.util.DefaultOkhttpClient
import com.billbook.lib.downloader.internal.util.asFactory
import okhttp3.OkHttpClient
import okhttp3.internal.toImmutableList
import java.util.*

class Downloader internal constructor(
    builder: Builder
) : Download.Call.Factory, Download.Subjection {

    @get:JvmName("eventListenerFactory")
    val eventListenerFactory: EventListener.Factory = builder.eventListenerFactory

    @get:JvmName("defaultMaxRetry")
    val defaultMaxRetry: Int = builder.defaultMaxRetry

    @get:JvmName("okHttpClientFactory")
    val okHttpClientFactory: Factory<OkHttpClient> = builder.okHttpClientFactory

    @get:JvmName("downloadPool")
    val downloadPool: DownloadPool = builder.downloadPool ?: DownloadPool()

    @get:JvmName("interceptors")
    val interceptors: List<Interceptor> = builder.interceptors.toImmutableList()

    internal val okhttpClient by lazy { builder.okHttpClientFactory.create() }

    private val dispatcher: Dispatcher = Dispatcher()

    fun newBuilder(): Builder {
        return Builder(this)
    }

    override fun newCall(request: Download.Request): Download.Call {
        return CallWrapper(DefaultDownloadCall(this, request), dispatcher)
    }

    override fun subscribe(subscriber: Download.Subscriber) {
        dispatcher.subscribe(subscriber)
    }

    override fun subscribe(url: String, subscriber: Download.Subscriber) {
        dispatcher.subscribe(url, subscriber)
    }

    override fun unsubscribe(subscriber: Download.Subscriber) {
        dispatcher.unsubscribe(subscriber)
    }

    override fun unsubscribe(url: String, subscriber: Download.Subscriber) {
        dispatcher.unsubscribe(url, subscriber)
    }

    fun cancelAll() {
        downloadPool.cancelAll()
    }

    fun cancelAllSafely() {
        downloadPool.cancelAllSafely()
    }

    class Builder {
        internal var eventListenerFactory: EventListener.Factory = EventListener.NONE.asFactory()
        internal var defaultMaxRetry: Int = 3
        internal var okHttpClientFactory: Factory<OkHttpClient> = DefaultOkhttpClient.asFactory()
        internal var downloadPool: DownloadPool? = null
        internal var interceptors: MutableList<Interceptor> = mutableListOf()

        constructor()

        internal constructor(downloader: Downloader) {
            this.eventListenerFactory = downloader.eventListenerFactory
            this.defaultMaxRetry = downloader.defaultMaxRetry
            this.okHttpClientFactory = downloader.okHttpClientFactory
            this.downloadPool = downloader.downloadPool.copy()
            this.interceptors = downloader.interceptors.toMutableList()
        }

        fun eventListenerFactory(factory: EventListener.Factory): Builder = apply {
            this.eventListenerFactory = factory
        }

        fun defaultMaxRetry(retry: Int): Builder = apply {
            this.defaultMaxRetry = retry
        }

        fun okHttpClientFactory(factory: Factory<OkHttpClient>): Builder = apply {
            this.okHttpClientFactory = factory
        }

        fun downloadPool(downloadPool: DownloadPool): Builder = apply {
            this.downloadPool = downloadPool
        }

        fun addInterceptor(interceptor: Interceptor): Builder = apply {
            this.interceptors += interceptor
        }

        fun build(): Downloader {
            return Downloader(this)
        }
    }

    fun interface Factory<T> {
        fun create(): T
    }

    private class CallWrapper(
        private val call: Download.Call,
        private val subscriber: Download.Subscriber
    ) : Download.Call by call {

        override fun execute(callback: Download.Callback): Download.Response {
            return call.execute(CallbackWrapper(callback, subscriber))
        }

        override fun enqueue(callback: Download.Callback) {
            call.enqueue(CallbackWrapper(callback, subscriber))
        }
    }

    private class CallbackWrapper(
        private val callback: Download.Callback,
        private val subscriber: Download.Subscriber,
    ) : Download.Callback by callback {

        override fun onSuccess(call: Download.Call, response: Download.Response) {
            callback.onSuccess(call, response)
            subscriber.onSuccess(call, response)
        }

        override fun onFailure(call: Download.Call, response: Download.Response) {
            callback.onFailure(call, response)
            subscriber.onFailure(call, response)
        }
    }
}
