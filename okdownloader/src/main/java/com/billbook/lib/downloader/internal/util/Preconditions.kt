package com.billbook.lib.downloader.internal.util

import java.lang.IllegalArgumentException

inline fun requireNotNullOrEmpty(value: String?, lazyMessage: () -> Any): String {
    if (value.isNullOrEmpty()) {
        val message = lazyMessage()
        throw IllegalArgumentException(message.toString())
    } else {
        return value
    }
}

