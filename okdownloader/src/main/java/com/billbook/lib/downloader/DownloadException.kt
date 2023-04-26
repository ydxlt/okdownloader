package com.billbook.lib.downloader

class DownloadException(val code: Int, message: String? = null, cause: Throwable? = null) :
    IllegalStateException(message, cause) {

    override fun toString(): String {
        return "DownloadException(code=$code, message = $message, cause = $cause)"
    }
}