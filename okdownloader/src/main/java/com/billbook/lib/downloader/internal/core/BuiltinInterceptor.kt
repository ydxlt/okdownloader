package com.billbook.lib.downloader.internal.core

import com.billbook.lib.downloader.Download
import com.billbook.lib.downloader.DownloadException
import com.billbook.lib.downloader.Downloader
import com.billbook.lib.downloader.ErrorCode
import com.billbook.lib.downloader.Interceptor
import com.billbook.lib.downloader.internal.exception.CancelException
import com.billbook.lib.downloader.internal.exception.TerminalException
import com.billbook.lib.downloader.internal.util.contentLength
import com.billbook.lib.downloader.internal.util.deleteIfExists
import com.billbook.lib.downloader.internal.util.makeNewFile
import com.billbook.lib.downloader.internal.util.md5
import com.billbook.lib.downloader.internal.util.ofRangeStart
import com.billbook.lib.downloader.internal.util.renameToTarget
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http2.StreamResetException
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.MalformedURLException

internal class RetryInterceptor(private val client: Downloader) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Download.Response {
        var retry = chain.request().retry ?: client.defaultMaxRetry
        var retryCount = 0
        while (true) {
            val response = chain.proceed(chain.request())
            if (response.isSuccessful() || retry-- < 1 || chain.call().isCanceled()) {
                return response.newBuilder()
                    .retryCount(retryCount)
                    .build()
            }
            try {
                Thread.sleep(3000)
            } catch (ex: InterruptedException) {
                // ignore
            }
            chain.callback().onRetrying(chain.call())
            retryCount++
        }
    }
}

internal fun Interceptor.Chain.checkTerminal() {
    val call = this.call()
    if (call is InternalCall && call.isCancelSafely()) {
        throw InterruptedException("Call terminal!")
    }
    if (call.isCanceled()) {
        throw CancelException("Call canceled!")
    }
}

internal class VerifierInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Download.Response {
        val request = chain.request()
        val destFile = request.destFile()
        val response = chain.proceed(request)
        if (!response.isSuccessful()) return response
        chain.callback().onChecking(chain.call())
        if (destFile.exists().not()) {
            throw DownloadException(ErrorCode.VERIFY_FILE_NOT_EXISTS, "File not exists")
        }
        if (destFile.isFile.not()) {
            throw DownloadException(ErrorCode.VERIFY_FILE_NOT_FILE, "Not file")
        }
        if (!request.md5.isNullOrEmpty() && destFile.md5() != request.md5) {
            throw DownloadException(ErrorCode.VERIFY_MD5_NOT_MATCHED, "MD5 not matched")
        }
        if (request.size != null && destFile.length() != request.size) {
            throw DownloadException(ErrorCode.VERIFY_SIZE_NOT_MATCHED, "Size not matched")
        }
        return response
    }
}

internal class SynchronousInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Download.Response {
        val request = chain.request()
        val (resLock, fileLock) = synchronized(SynchronousInterceptor::class.java) {
            sResLocks.getOrPut(request.url) { Any() } to sFileLock.getOrPut(request.path) { Any() }
        }
        return synchronized(fileLock) {
            synchronized(resLock) {
                chain.proceed(chain.request())
            }
        }.also {
            synchronized(SynchronousInterceptor::class.java) {
                sResLocks -= request.url
                sFileLock -= request.path
            }
        }
    }

    companion object {
        private val sResLocks = mutableMapOf<String, Any>()
        private val sFileLock = mutableMapOf<String, Any>()
    }
}

internal class LocalExistsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Download.Response {
        val destFile = chain.request().destFile()
        val md5 = chain.request().md5
        if (md5 != null && destFile.exists() && destFile.md5() == md5) {
            return Download.Response.Builder()
                .code(ErrorCode.EXISTS_SUCCESS)
                .totalSize(destFile.length())
                .message("File exists and MD5 matched, don`t need download!")
                .build()
        }
        return chain.proceed(chain.request())
    }
}

internal class ExceptionInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Download.Response {
        return try {
            chain.proceed(chain.request())
        } catch (ex: CancelException) {
            chain.callback().onCancel(chain.call())
            Download.Response.Builder().code(ErrorCode.CANCEL)
                .messageWith(ex)
                .build()
        } catch (ex: TerminalException) {
            chain.callback().onCancel(chain.call())
            chain.request().sourceFile().deleteIfExists()
            Download.Response.Builder().code(ErrorCode.CANCEL)
                .messageWith(ex)
                .build()
        } catch (ex: StreamResetException) {
            Download.Response.Builder().code(ErrorCode.NET_STREAM_RESET)
                .messageWith(ex)
                .build()
        } catch (ex: InterruptedIOException) {
            Download.Response.Builder().code(ErrorCode.IO_INTERRUPTED)
                .messageWith(ex)
                .build()
        } catch (ex: InterruptedException) {
            Download.Response.Builder().code(ErrorCode.INTERRUPTED)
                .messageWith(ex)
                .build()
        } catch (ex: IllegalArgumentException) {
            Download.Response.Builder().code(ErrorCode.ARGUMENT_EXCEPTION)
                .messageWith(ex)
                .build()
        } catch (ex: MalformedURLException) {
            Download.Response.Builder().code(ErrorCode.MALFORMED_URL)
                .messageWith(ex)
                .build()
        } catch (ex: FileNotFoundException) {
            Download.Response.Builder().code(ErrorCode.FILE_NOT_FOUND)
                .messageWith(ex)
                .build()
        } catch (ex: IOException) {
            Download.Response.Builder().code(ErrorCode.IO_EXCEPTION)
                .messageWith(ex)
                .build()
        } catch (ex: DownloadException) {
            Download.Response.Builder().code(ex.code)
                .messageWith(ex)
                .build()
        } catch (t: Throwable) {
            Download.Response.Builder().code(ErrorCode.UNKNOWN)
                .messageWith(t)
                .build()
        }
    }

    private inline fun Download.Response.Builder.messageWith(t: Throwable): Download.Response.Builder {
        return message(t.toString())
    }
}

internal class ExchangeInterceptor(private val client: Downloader) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Download.Response {
        val downloadRequest = chain.request()
        val sourceFile = downloadRequest.sourceFile()
        if (!sourceFile.exists()) sourceFile.makeNewFile()
        chain.checkTerminal()
        val netResponse = getRemoteResponse(chain, downloadRequest)
        chain.checkTerminal()
        if (!netResponse.isSuccessful) throw DownloadException(
            ErrorCode.REMOTE_CONNECT_ERROR,
            "Remote connect failed, message = ${netResponse.message}, httpCode = ${netResponse.code}"
        )
        val body = netResponse.body
            ?: throw DownloadException(ErrorCode.REMOTE_CONTENT_EMPTY, "Remote source body is null")
        var downloadLength = 0L
        val startLength = sourceFile.length()
        val contentLength = startLength + netResponse.contentLength()
        IOExchange().exchange(sourceFile, body.source()) {
            chain.checkTerminal()
            if (it > 0) {
                downloadLength += it
            }
            chain.callback().onLoading(chain.call(), startLength + downloadLength, contentLength)
        }
        sourceFile.renameToTarget(downloadRequest.destFile())
        return Download.Response.Builder()
            .code(if (startLength > 0) ErrorCode.APPEND_SUCCESS else ErrorCode.SUCCESS)
            .downloadLength(downloadLength)
            .totalSize(contentLength)
            .output(downloadRequest.destFile())
            .message("Success")
            .build()
    }

    private fun getRemoteResponse(
        chain: Interceptor.Chain,
        downloadRequest: Download.Request
    ): Response {
        val request = Request.Builder().url(downloadRequest.url)
            .ofRangeStart(downloadRequest.sourceFile().length())
            .get()
            .build()
        return try {
            client.okHttpClientFactory.create().newCall(request).also {
                val call = chain.call()
                if (call is InternalCall) {
                    call.httpCall = it
                }
            }.execute()
        } catch (e: IOException) {
            throw DownloadException(ErrorCode.REMOTE_CONNECT_ERROR, "Could not connect: $e", e)
        }
    }
}