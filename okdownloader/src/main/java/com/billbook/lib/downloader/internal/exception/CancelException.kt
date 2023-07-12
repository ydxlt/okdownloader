package com.billbook.lib.downloader.internal.exception

internal open class TerminalException(override val message: String?) : RuntimeException(message)

internal class CancelException(override val message: String?) : TerminalException(message)
