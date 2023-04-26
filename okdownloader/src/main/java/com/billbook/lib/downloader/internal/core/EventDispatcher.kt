package com.billbook.lib.downloader.internal.core

import com.billbook.lib.downloader.Download

internal class EventDispatcher(
    private val callback: Download.Callback,
    private val subscriber: Download.Subscriber,
) : Download.Callback {

    override fun onStart(call: Download.Call) {
        call.request.callbackExecutor.execute {
            callback.onStart(call)
        }
        subscriber.onStart(call)
    }

    override fun onLoading(call: Download.Call, currentLength: Long, total: Long) {
        call.request.callbackExecutor.execute {
            callback.onLoading(call, currentLength, total)
        }
        subscriber.onLoading(call, currentLength, total)
    }

    override fun onChecking(call: Download.Call) {
        call.request.callbackExecutor.execute {
            callback.onChecking(call)
        }
        subscriber.onChecking(call)
    }

    override fun onRetrying(call: Download.Call) {
        call.request.callbackExecutor.execute {
            callback.onRetrying(call)
        }
        subscriber.onRetrying(call)
    }

    override fun onCancel(call: Download.Call) {
        call.request.callbackExecutor.execute {
            callback.onCancel(call)
        }
        subscriber.onCancel(call)
    }

    override fun onSuccess(call: Download.Call, response: Download.Response) {
        call.request.callbackExecutor.execute {
            callback.onSuccess(call, response)
        }
        subscriber.onSuccess(call, response)
    }

    override fun onFailure(call: Download.Call, response: Download.Response) {
        call.request.callbackExecutor.execute {
            callback.onFailure(call, response)
        }
        subscriber.onFailure(call, response)
    }
}

// Android Main dispatcher