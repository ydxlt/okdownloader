package com.billbook.lib.downloader

import com.billbook.lib.downloader.internal.util.requireNotNullOrEmpty
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface Download {

    open class Request protected constructor(
        @get:JvmName("url") val url: String,
        @get:JvmName("path") val path: String,
        @get:JvmName("md5") val md5: String?,
        @get:JvmName("tag") val tag: String?,
        @get:JvmName("size") val size: Long?,
        @get:JvmName("retry") val retry: Int?,
        @get:JvmName("callbackExecutor") val callbackExecutor: CallbackExecutor,
        @get:JvmName("priority") val priority: Priority,
    ) {

        open fun newBuilder(): Builder = Builder(this)

        open fun sourceFile(): File = File("$path.tmp")

        open fun destFile(): File = File(path)

        override fun toString(): String {
            return "Request(url='$url', path='$path', md5=$md5, tag=$tag, size=$size, retry=$retry, callbackExecutor=$callbackExecutor, priority=$priority)"
        }

        open class Builder {
            protected var url: String? = null
            protected var path: String? = null
            protected var md5: String? = null
            protected var tag: String? = null
            protected var size: Long? = null
            protected var retry: Int? = null
            protected var callbackExecutor: CallbackExecutor = CallbackExecutor.UNCONFINED
            protected var priority: Priority = Priority.MIDDLE

            constructor()

            constructor(request: Request) {
                this.url = request.url
                this.path = request.path
                this.md5 = request.md5
                this.tag = request.tag
                this.size = request.size
                this.callbackExecutor = request.callbackExecutor
                this.priority = request.priority
            }

            open fun url(url: String): Builder = apply {
                this.url = url
            }

            open fun md5(md5: String): Builder = apply {
                this.md5 = md5
            }

            open fun tag(tag: String): Builder = apply {
                this.tag = tag
            }

            open fun size(size: Long): Builder = apply {
                this.size = size
            }

            open fun path(path: String): Builder = apply {
                this.path = path
            }

            open fun retry(retry: Int): Builder = apply {
                this.retry = retry
            }

            open fun priority(priority: Priority): Builder = apply {
                this.priority = priority
            }

            open fun callbackOn(executor: CallbackExecutor): Builder = apply {
                this.callbackExecutor = callbackExecutor
            }

            open fun build(): Request {
                return Request(
                    url = requireNotNullOrEmpty(url) { "Missing url!" },
                    path = requireNotNullOrEmpty(path) { "Missing path!" },
                    md5 = md5,
                    size = size,
                    retry = retry,
                    callbackExecutor = callbackExecutor,
                    tag = tag,
                    priority = priority
                )
            }
        }
    }

    class Response internal constructor(
        @get:JvmName("code") val code: Int,
        @get:JvmName("message") val message: String?,
        @get:JvmName("output") val output: File?,
        @get:JvmName("retryCount") val retryCount: Int,
        @get:JvmName("downloadLength") val downloadLength: Long,
        @get:JvmName("totalSize") val totalSize: Long,
    ) {
        fun isSuccessful(): Boolean = this.code in ErrorCode.SUCCESS..ErrorCode.EXISTS_SUCCESS

        fun newBuilder(): Builder = Builder(this)

        fun isBreakpoint(): Boolean = this.code == ErrorCode.APPEND_SUCCESS

        override fun toString(): String {
            return "Response(code=$code, message=$message, output=$output, retryCount=$retryCount, downloadLength=$downloadLength, totalSize=$totalSize)"
        }

        class Builder {
            private var code: Int = ErrorCode.SUCCESS
            private var message: String? = null
            private var output: File? = null
            private var downloadLength: Long = 0L
            private var retryCount: Int = 0
            private var totalSize: Long = 0

            constructor()

            internal constructor(response: Response) {
                this.code = response.code
                this.message = response.message
                this.output = response.output
                this.downloadLength = response.downloadLength
                this.retryCount = response.retryCount
                this.totalSize = response.totalSize
            }

            fun code(code: Int): Builder = apply {
                this.code = code
            }

            fun message(message: String): Builder = apply {
                this.message = message
            }

            fun output(output: File): Builder = apply {
                this.output = output
            }

            fun downloadLength(downloadLength: Long): Builder = apply {
                this.downloadLength = downloadLength
            }

            fun retryCount(retryCount: Int): Builder = apply {
                this.retryCount = retryCount
            }

            fun totalSize(totalSize: Long): Builder = apply {
                this.totalSize = totalSize
            }

            fun build(): Response {
                return Response(
                    code = this.code,
                    message = this.message,
                    output = this.output,
                    downloadLength = this.downloadLength,
                    retryCount = this.retryCount,
                    totalSize = this.totalSize,
                )
            }
        }
    }

    interface Call {
        val request: Request

        fun execute(): Response

        fun execute(callback: Callback): Response

        fun enqueue()

        fun enqueue(callback: Callback)

        fun cancel()

        fun isExecuted(): Boolean

        fun isCanceled(): Boolean

        fun interface Factory {
            fun newCall(request: Request): Call
        }
    }

    interface Callback {
        fun onStart(call: Call) {}
        fun onLoading(call: Call, current: Long, total: Long) {}
        fun onCancel(call: Call) {}
        fun onChecking(call: Call) {}
        fun onRetrying(call: Call) {}
        fun onSuccess(call: Call, response: Response) {}
        fun onFailure(call: Call, response: Response) {}

        companion object {
            @JvmField
            val NOOP: Callback = object : Callback {}
        }
    }

    enum class Priority {
        LOW_LOW, LOW, MIDDLE, HIGH, HIGH_HIGH
    }

    interface Subjection {
        fun subscribe(subscriber: Subscriber)
        fun unsubscribe(subscriber: Subscriber)
        fun subscribe(url: String, subscriber: Subscriber)
        fun unsubscribe(url: String, subscriber: Subscriber)
    }

    interface Subscriber : Callback
}

interface Named {
    val name: String
}

abstract class CallbackExecutor : Executor, Named {

    companion object {
        val UNCONFINED = object : CallbackExecutor() {
            override val name: String get() = "UNCONFINED"

            override fun execute(command: Runnable) {
                command.run()
            }
        }

        val SERIAL = object : CallbackExecutor() {
            override val name: String get() = "SERIAL"

            private val executor by lazy { Executors.newSingleThreadExecutor() }

            override fun execute(command: Runnable) {
                executor.execute(command)
            }
        }
    }

    override fun toString(): String {
        return "CallbackExecutor(name='$name')"
    }
}