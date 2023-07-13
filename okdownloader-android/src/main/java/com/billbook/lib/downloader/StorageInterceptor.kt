package com.billbook.lib.downloader

import android.os.Environment
import android.os.StatFs

/**
 * @author xluotong@gmail.com
 */
class StorageInterceptor(
    private val availableThreshold: Long = DEFAULT_MIN_AVAILABLE_SIZE
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Download.Response {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val availableSpace = statFs.blockSizeLong * statFs.availableBlocksLong
            if (availableSpace <= availableThreshold) {
                throw DownloadException(ErrorCode.IO_STORAGE_FULL)
            }
        }
        return chain.proceed(chain.request())
    }

    companion object {
        private const val DEFAULT_MIN_AVAILABLE_SIZE = 20L * 1024 * 1024 // 20MB
    }
}