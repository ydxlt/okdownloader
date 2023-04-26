package com.billbook.lib.downloader.internal.util

inline fun requireNotNullOrEmpty(value: String?, lazyMessage: () -> Any): String {
    if (value.isNullOrEmpty()) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    } else {
        return value
    }
}

