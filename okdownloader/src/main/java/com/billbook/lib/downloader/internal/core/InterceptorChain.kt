package com.billbook.lib.downloader.internal.core

import com.billbook.lib.downloader.Download
import com.billbook.lib.downloader.Interceptor

internal class DefaultInterceptorChain(
    private val index: Int,
    private val request: Download.Request,
    private val call: Download.Call,
    private val callback: Download.Callback,
    private val interceptors: List<Interceptor>
) : Interceptor.Chain {

    private fun copy(index: Int) = DefaultInterceptorChain(index, request, call, callback, interceptors)

    override fun request(): Download.Request {
        return this.request
    }

    override fun call(): Download.Call {
        return this.call
    }

    override fun callback(): Download.Callback {
        return this.callback
    }

    override fun proceed(request: Download.Request): Download.Response {
        check(index < interceptors.size)
        return interceptors[index].intercept(copy(index + 1))
    }
}