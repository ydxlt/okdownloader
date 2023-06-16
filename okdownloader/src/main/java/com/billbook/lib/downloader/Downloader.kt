package com.billbook.lib.downloader

import com.billbook.lib.downloader.internal.core.DefaultDownloadCall
import com.billbook.lib.downloader.internal.core.DownloadPool
import com.billbook.lib.downloader.internal.core.EventDispatcher
import com.billbook.lib.downloader.internal.core.Publisher
import com.billbook.lib.downloader.internal.util.CPU_COUNT
import com.billbook.lib.downloader.internal.util.DefaultOkhttpClient
import com.billbook.lib.downloader.internal.util.asFactory
import okhttp3.OkHttpClient
import okhttp3.internal.toImmutableList
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class Downloader internal constructor(
    builder: Builder
) : Download.Call.Factory, Download.Subjection {

    @get:JvmName("eventListenerFactory")
    val eventListenerFactory: EventListener.Factory = builder.eventListenerFactory

    @get:JvmName("defaultMaxRetry")
    val defaultMaxRetry: Int = builder.defaultMaxRetry

    @get:JvmName("okHttpClientFactory")
    val okHttpClientFactory: Factory<OkHttpClient> = builder.okHttpClientFactory

    @get:JvmName("executorService")
    val executorService: ExecutorService = builder.executorService

    @get:JvmName("executorService")
    val idleCallback: Runnable? = builder.idleCallback

    @get:JvmName("interceptors")
    val interceptors: List<Interceptor> = builder.interceptors.toImmutableList()

    internal val downloadPool = DownloadPool(executorService, idleCallback)

    private val publisher: Publisher = Publisher()

    fun newBuilder(): Builder {
        return Builder(this)
    }

    override fun newCall(request: Download.Request): Download.Call {
        return CallWrapper(DefaultDownloadCall(this, request), publisher)
    }

    override fun subscribe(subscriber: Download.Subscriber) {
        publisher.subscribe(subscriber)
    }

    override fun subscribe(url: String, subscriber: Download.Subscriber) {
        publisher.subscribe(url, subscriber)
    }

    override fun unsubscribe(subscriber: Download.Subscriber) {
        publisher.unsubscribe(subscriber)
    }

    override fun unsubscribe(url: String, subscriber: Download.Subscriber) {
        publisher.unsubscribe(url, subscriber)
    }

    class Builder {
        internal var eventListenerFactory: EventListener.Factory = EventListener.NONE.asFactory()
        internal var defaultMaxRetry: Int = 3
        internal var okHttpClientFactory: Factory<OkHttpClient> = DefaultOkhttpClient.asFactory()
        internal var executorService: ExecutorService = ThreadPoolExecutor(
            CPU_COUNT, CPU_COUNT * 2, 5, TimeUnit.SECONDS,
            LinkedBlockingQueue()
        )
        internal var idleCallback: Runnable? = null
        internal var interceptors: MutableList<Interceptor> = mutableListOf()

        constructor()

        internal constructor(downloader: Downloader) {
            this.eventListenerFactory = downloader.eventListenerFactory
            this.defaultMaxRetry = downloader.defaultMaxRetry
            this.okHttpClientFactory = downloader.okHttpClientFactory
            this.executorService = downloader.executorService
            this.idleCallback = downloader.idleCallback
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

        fun executorService(executorService: ExecutorService): Builder = apply {
            this.executorService = executorService
        }

        fun idleCallback(idleCallback: Runnable): Builder = apply {
            this.idleCallback = idleCallback
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
            return call.execute(EventDispatcher(callback, subscriber))
        }

        override fun enqueue(callback: Download.Callback) {
            call.enqueue(EventDispatcher(callback, subscriber))
        }
    }
}
