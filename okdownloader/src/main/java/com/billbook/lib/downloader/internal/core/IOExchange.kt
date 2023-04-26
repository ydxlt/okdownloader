package com.billbook.lib.downloader.internal.core

import okio.*
import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

internal class IOExchange {

    @Throws(IOException::class)
    fun exchange(file: File, source: BufferedSource, onRead: (Long) -> Unit) {
        file.appendingSink().buffer().use {
            it.writeAll(object : ForwardingSource(source) {
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytes = super.read(sink, byteCount)
                    onRead(bytes)
                    return bytes
                }
            })
        }
    }
}