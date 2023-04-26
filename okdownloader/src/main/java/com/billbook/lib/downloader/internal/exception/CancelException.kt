package com.billbook.lib.downloader.internal.exception

internal class CancelException(override val message: String?) : RuntimeException(message)