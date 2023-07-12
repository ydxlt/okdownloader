package com.billbook.lib.case

import com.billbook.lib.downloader.Download
import com.billbook.lib.downloader.Downloader

fun main() {
    val downloader = Downloader.Builder()
        .addInterceptor(FileExistsInterceptor())
        .eventListenerFactory { ReporterEventListener() }
        .idleCallback { println("DownloadPool idle!") }
        .build()
    downloader.subscribe(object : Download.Subscriber {
        override fun onSuccess(call: Download.Call, response: Download.Response) {
            super.onSuccess(call, response)
            println("GlobalSubscriber: onSuccess call = $call, response = $response")
        }

        override fun onFailure(call: Download.Call, response: Download.Response) {
            super.onFailure(call, response)
            println("GlobalSubscriber: onFailure call = $call, response = $response")
        }
    })
    val url = "https://wap.pp.cn/app/dl/fs08/2023/03/21/2/110_083a6016054e988728d7b6b36f1fdb4b.apk"
    downloader.download(url, System.getProperty("user.dir") + "/downloads/test.apk")
}

private fun Downloader.download(url: String, path: String, md5: String? = null) {
    val request = Download.Request.Builder()
        .url(url)
        .apply { md5?.let { md5(it) } }
        .into(path)
        .build()
    subscribe(url, object : Download.Subscriber {
        override fun onSuccess(call: Download.Call, response: Download.Response) {
            super.onSuccess(call, response)
            println("UrlSubscriber: onSuccess call = $call, response = $response")
        }

        override fun onFailure(call: Download.Call, response: Download.Response) {
            super.onFailure(call, response)
            println("UrlSubscriber: onFailure call = $call, response = $response")
        }
    })
    newCall(request).enqueue(object : Download.Callback {

        override fun onLoading(call: Download.Call, current: Long, total: Long) {
            super.onLoading(call, current, total)
            println("Callback: onLoading call = $call, current = $current, total = $total")
        }

        override fun onRetrying(call: Download.Call) {
            println("Callback: onRetrying call = $call")
        }

        override fun onSuccess(call: Download.Call, response: Download.Response) {
            super.onSuccess(call, response)
            println("Callback: onSuccess call = $call, response = $response")
        }

        override fun onFailure(call: Download.Call, response: Download.Response) {
            super.onFailure(call, response)
            println("Callback: onFailure call =$call, response = $response")
        }
    })
}