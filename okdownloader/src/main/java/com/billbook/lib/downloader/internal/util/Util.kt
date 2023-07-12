package com.billbook.lib.downloader.internal.util

import com.billbook.lib.downloader.DownloadException
import com.billbook.lib.downloader.ErrorCode
import org.apache.commons.codec.digest.DigestUtils
import java.io.File

internal val CPU_COUNT = Runtime.getRuntime().availableProcessors()

@Throws(DownloadException::class)
internal fun File.makeNewFile(): Boolean {
    if (this.exists()) return true
    this.parentFile?.mkdirs()
    if (this.parentFile?.exists() == false || this.parentFile?.isDirectory == false) {
        throw DownloadException(ErrorCode.IO_CREATE_DIRECTORY_ERROR, "Directory could not be created")
    }
    return this.createNewFile()
}

@Throws(DownloadException::class)
internal fun File.renameToTarget(target: File): Boolean {
    val result = this.renameTo(target)
    if (!result) {
        target.deleteIfExists()
        this.copyTo(target, true)
        return true
    }
    return true
}

internal inline fun File.deleteIfExists() {
    try {
        if (this.exists()) this.delete()
    } catch (e: Throwable) {
    }
}

internal fun File.md5(): String {
    return DigestUtils.md5Hex(this.readBytes())
}

