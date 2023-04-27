package com.billbook.lib.case

import com.billbook.lib.downloader.Download
import com.billbook.lib.downloader.ErrorCode
import com.billbook.lib.downloader.Interceptor

/**
 * @author xluotong@gmail.com
 */
class FileExistsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Download.Response {
        if (chain.request().destFile().exists()) {
            return Download.Response.Builder()
                .code(ErrorCode.EXISTS_SUCCESS)
                .message("File already exists")
                .build()
        }
        return chain.proceed(chain.request())
    }
}