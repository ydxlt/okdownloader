package com.billbook.lib.downloader

import com.billbook.lib.downloader.internal.util.deleteIfExists
import org.junit.Assert
import org.junit.Test
import java.lang.IllegalStateException
import java.nio.file.Files


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
        val request = Download.Request.Builder()
            .url(FakeData.resources[0].url)
            .into("xxx")
            .build()
        val call = downloader.newCall(request)
        call.execute()
        Assert.assertThrows(IllegalStateException::class.java) {
            call.execute()
        }
    }

    @Test
    fun cancel_is_correct() {
        val file = Files.createTempFile("test", ".apk").toFile()
        val request = Download.Request.Builder()
            .url(FakeData.resources[0].url)
            .into(file)
            .build()
        val call = downloader.newCall(request)
        call.enqueue()
        Thread.sleep(2000)
        Assert.assertTrue(request.sourceFile().exists())
        Thread.sleep(2000)
        call.cancel()
        Assert.assertTrue(request.sourceFile().exists())
    }

    @Test
    fun cancelSafely_is_correct() {
        val request = Download.Request.Builder()
            .url(FakeData.resources[0].url)
            .into(Files.createTempFile("test", ".apk").toFile())
            .build()
        val call = downloader.newCall(request)
        call.enqueue()
        Thread.sleep(2000)
        Assert.assertTrue(request.sourceFile().exists())
        call.cancelSafely()
        Assert.assertTrue(call.isCanceled())
        Thread.sleep(1000)
        Assert.assertFalse(request.sourceFile().exists())
    }

    @Test
    fun cancelAll_is_correct() {
        val file = Files.createTempFile("test", ".apk").toFile()
        val request = Download.Request.Builder()
            .url(FakeData.resources[0].url)
            .into(file)
            .build()
        val call = downloader.newCall(request)
        call.enqueue()
        Assert.assertTrue(file.exists())
        Thread.sleep(2000)
        downloader.cancelAll()
        Thread.sleep(1000)
        Assert.assertTrue(call.isCanceled())
        Assert.assertTrue(file.exists())
    }

    @Test
    fun cancelAllSafely_is_correct() {
        val file = Files.createTempFile("test", ".apk").toFile()
        val request = Download.Request.Builder()
            .url(FakeData.resources[0].url)
            .into(file)
            .build()
        val call = downloader.newCall(request)
        call.enqueue()
        Assert.assertTrue(file.exists())
        Thread.sleep(2000)
        downloader.cancelAllSafely()
        Thread.sleep(1000)
        Assert.assertTrue(call.isCanceled())
        Assert.assertFalse(file.exists())
    }

    @Test
    fun downloadPool_is_correct() {
        val file = Files.createTempFile("test", ".apk").toFile()
        val request = Download.Request.Builder()
            .url(FakeData.resources[0].url)
            .into(file)
            .build()
        val call = downloader.newCall(request)
        call.enqueue()
        Assert.assertTrue(file.exists())
        Thread.sleep(2000)
        downloader2.cancelAll()
        Thread.sleep(1000)
        Assert.assertFalse(call.isCanceled())
        Assert.assertTrue(file.exists())
    }
}