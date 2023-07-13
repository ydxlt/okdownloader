package com.billbook.lib.downloader

import okhttp3.internal.notifyAll
import okhttp3.internal.wait
import org.junit.Assert
import org.junit.Test
import java.lang.IllegalStateException
import java.nio.file.Files
import java.util.UUID

/**
 * @author xluotong@gmail.com
 */
class DownloadUnitTest {

    private val downloader by lazy { Downloader.Builder().build() }
    private val downloader2 by lazy { downloader.newBuilder().build() }

    @Test
    fun argument_is_correct() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            Download.Request.Builder().build()
        }
        Assert.assertThrows(IllegalArgumentException::class.java) {
            Download.Request.Builder().url("")
                .build()
        }
        Assert.assertThrows(IllegalArgumentException::class.java) {
            Download.Request.Builder().url("xxx")
                .build()
        }
        Assert.assertThrows(IllegalArgumentException::class.java) {
            Download.Request.Builder().into("").build()
        }
        val request = Download.Request.Builder()
            .url("xxx")
            .into("xxx")
            .build()
        val response = downloader.newCall(request).execute()
        Assert.assertFalse(response.isSuccessful())
    }

    @Test
    fun execute_is_correct() {
        val request = buildRequest()
        val call = downloader.newCall(request)
        call.execute()
        Assert.assertThrows(IllegalStateException::class.java) {
            call.execute()
        }
    }

    private fun buildRequest(): Download.Request {
        return Download.Request.Builder()
            .url(FakeData.resources[0].url)
            .into(Files.createTempFile(UUID.randomUUID().toString(), ".apk").toFile())
            .build()
    }

    @Test
    fun source_file_exists() {
        val request = buildRequest()
        val call = downloader.newCall(request)
        call.enqueue(object : Download.Callback {
            override fun onLoading(tmp: Download.Call, current: Long, total: Long) {
                super.onLoading(tmp, current, total)
                synchronized(call) { call.notifyAll() }
            }
        })
        synchronized(call) { call.wait() }
        Assert.assertTrue(request.sourceFile().exists())
    }

    @Test
    fun cancel_is_correct() {
        val request = buildRequest()
        val call = downloader.newCall(request)
        call.enqueue(object : Download.Callback {
            override fun onLoading(tmp: Download.Call, current: Long, total: Long) {
                super.onLoading(tmp, current, total)
                synchronized(call) { call.notifyAll() }
            }
        })
        synchronized(call) { call.wait() }
        call.cancel()
        Thread.sleep(1000)
        Assert.assertTrue(request.sourceFile().exists())
    }

    @Test
    fun cancelSafely_is_correct() {
        val request = buildRequest()
        val call = downloader.newCall(request)
        call.enqueue(object : Download.Callback {
            override fun onLoading(tmp: Download.Call, current: Long, total: Long) {
                super.onLoading(tmp, current, total)
                synchronized(call) { call.notifyAll() }
            }
        })
        synchronized(call) { call.wait() }
        call.cancelSafely()
        Assert.assertTrue(call.isCanceled())
        Thread.sleep(1000)
        Assert.assertFalse(request.sourceFile().exists())
    }

    @Test
    fun cancelAll_is_correct() {
        val request = buildRequest()
        val call = downloader.newCall(request)
        call.enqueue(object : Download.Callback {
            override fun onLoading(tmp: Download.Call, current: Long, total: Long) {
                super.onLoading(tmp, current, total)
                synchronized(call) { call.notifyAll() }
            }
        })
        synchronized(call) { call.wait() }
        downloader.cancelAll()
        Thread.sleep(1000)
        Assert.assertTrue(call.isCanceled())
        Assert.assertTrue(request.sourceFile().exists())
    }

    @Test
    fun cancelAllSafely_is_correct() {
        val request = buildRequest()
        val call = downloader.newCall(request)
        call.enqueue(object : Download.Callback {
            override fun onLoading(tmp: Download.Call, current: Long, total: Long) {
                super.onLoading(tmp, current, total)
                synchronized(call) { call.notifyAll() }
            }
        })
        synchronized(call) { call.wait() }
        downloader.cancelAllSafely()
        Thread.sleep(1000)
        Assert.assertTrue(call.isCanceled())
        Assert.assertFalse(request.sourceFile().exists())
    }

    @Test
    fun downloadPool_is_correct() {
        val request = buildRequest()
        val call = downloader.newCall(request)
        call.enqueue(object : Download.Callback {
            override fun onLoading(tmp: Download.Call, current: Long, total: Long) {
                super.onLoading(tmp, current, total)
                synchronized(call) { call.notifyAll() }
            }
        })
        synchronized(call) { call.wait() }
        downloader2.cancelAll()
        Thread.sleep(1000)
        Assert.assertFalse(call.isCanceled())
        Assert.assertTrue(request.sourceFile().exists())
    }
}