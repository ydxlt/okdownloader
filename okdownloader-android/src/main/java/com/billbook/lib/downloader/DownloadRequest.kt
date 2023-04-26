package com.billbook.lib.downloader

import com.billbook.lib.downloader.internal.util.requireNotNullOrEmpty
import java.io.File

/**
 * @author xluotong@gmail.com
 */
open class DownloadRequest protected constructor(
    url: String,
    path: String,
    md5: String?,
    tag: String?,
    size: Long?,
    retry: Int?,
    callbackExecutor: CallbackExecutor,
    priority: Download.Priority,
    @get:JvmName("network") internal val network: Int,
) : Download.Request(url, path, md5, tag, size, retry, callbackExecutor, priority) {

    override fun newBuilder(): Download.Request.Builder {
        return Builder(this)
    }

    override fun toString(): String {
        return "DownloadRequest(network=$network) ${super.toString()}"
    }

    open class Builder : Download.Request.Builder {

        protected var network: Int = NETWORK_WIFI or NETWORK_DATA

        constructor()

        constructor(request: DownloadRequest) : super(request) {
            this.network = request.network
        }

        override fun url(url: String): Builder = apply {
            this.url = url
        }

        override fun path(path: String): Builder = apply {
            this.path = path
        }

        override fun md5(md5: String): Builder = apply {
            this.md5 = md5
        }

        open fun networkOn(network: Int): Builder = apply {
            this.network = network
        }

        override fun tag(tag: String): Builder = apply {
            this.tag = tag
        }

        override fun size(size: Long): Builder = apply {
            this.size = size
        }

        override fun retry(retry: Int): Builder = apply {
            this.retry = retry
        }

        override fun priority(priority: Download.Priority): Builder  = apply{
            this.priority = priority
        }

        override fun callbackOn(executor: CallbackExecutor): Builder = apply {
            this.callbackExecutor = executor
        }

        override fun build(): DownloadRequest {
            return DownloadRequest(
                url = requireNotNullOrEmpty(url) { "Missing url!" },
                path = requireNotNullOrEmpty(path) { "Missing path!" },
                md5 = md5,
                size = size,
                retry = retry,
                callbackExecutor = callbackExecutor,
                tag = tag,
                priority = priority,
                network = network
            )
        }
    }

    companion object {
        const val NETWORK_WIFI = 0x00000001
        const val NETWORK_DATA = 0x00000002
    }
}