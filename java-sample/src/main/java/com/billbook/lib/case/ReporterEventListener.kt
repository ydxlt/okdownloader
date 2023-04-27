package com.billbook.lib.case

import com.billbook.lib.downloader.Download
import com.billbook.lib.downloader.EventListener

/**
 * @author xluotong@gmail.com
 */
class ReporterEventListener : EventListener() {
    override fun callSuccess(call: Download.Call, response: Download.Response) {
        println("DownloadReporter: Download successful: call = $call, response = $response")
    }

    override fun callFailed(call: Download.Call, response: Download.Response) {
        println("DownloadReporter: Download failed: call = $call, response = $response")
    }
}