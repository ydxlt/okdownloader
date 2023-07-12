package com.billbook.lib.downloader

import java.security.MessageDigest

/**
 * @author xluotong@gmail.com
 */
inline fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

inline fun String.md5() = MessageDigest.getInstance("md5").digest(toByteArray()).toHex()