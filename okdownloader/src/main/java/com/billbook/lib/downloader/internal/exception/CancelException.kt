package com.billbook.lib.downloader.internal.exception

internal class CancelException(override val message: String?) : RuntimeException(message)

internal class PauseException(override val message: String?) : RuntimeException(message)