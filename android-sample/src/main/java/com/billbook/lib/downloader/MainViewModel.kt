package com.billbook.lib.downloader

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject

private val Context.downloadDir: File get() = File(this.filesDir, "download")
private const val TAG = "MainViewModel"

/**
 * @author xluotong@gmail.com
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloader: Downloader
) : ViewModel() {

    private val _states: MutableStateFlow<Map<String, DownloadState>> = MutableStateFlow(emptyMap())
    val states: StateFlow<Map<String, DownloadState>> = _states
    private val calls = mutableMapOf<String, Download.Call>()
    private val mutex = Mutex()

    fun cancel(bean: ResourceBean) = viewModelScope.launch(Dispatchers.IO) {
        mutex.withLock {
            calls[bean.url]?.let {
                it.cancelSafely()
            }
        }
    }

    fun redownload(bean: ResourceBean) = viewModelScope.launch(Dispatchers.IO) {
        download(bean, true)
    }

    fun pause(bean: ResourceBean) = viewModelScope.launch(Dispatchers.IO) {
        mutex.withLock { calls[bean.url]?.cancel() }
    }

    fun download(bean: ResourceBean, force: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            val call = mutex.withLock {
                if (calls[bean.url] != null) return@launch
                val request = Download.Request.Builder()
                    .url(bean.url)
                    .into(File(context.downloadDir, bean.url.md5()))
                    .apply { bean.md5?.let { md5(it) } }
                    .build()
                downloader.newCall(request).also { calls[bean.url] = it }
            }
            if (force) {
                call.request.sourceFile().delete()
                call.request.destFile().delete()
            }
            call.execute(object : Download.Callback {

                private var lastProgress = 0f

                override fun onStart(call: Download.Call) {
                    Log.i(TAG, "onStart")
                    updateState(bean, DownloadState.DOWNLOADING(lastProgress))
                }

                override fun onLoading(call: Download.Call, current: Long, total: Long) {
                    val progress = current * 1f / total
                    if (progress == lastProgress) return
                    lastProgress = progress
                    updateState(bean, DownloadState.DOWNLOADING(progress))
                }

                override fun onCancel(call: Download.Call) {
                    updateState(bean, DownloadState.IDLE)
                }

                override fun onChecking(call: Download.Call) {
                    updateState(bean, DownloadState.CHECKING)
                }

                override fun onRetrying(call: Download.Call) {
                    updateState(bean, DownloadState.RETRYING)
                }

                override fun onSuccess(call: Download.Call, response: Download.Response) {
                    Log.i(TAG, "onSuccess response = $response")
                    updateState(bean, DownloadState.FINISH)
                }

                override fun onFailure(call: Download.Call, response: Download.Response) {
                    Log.e(TAG, "onFailure response = $response")
                    updateState(bean, DownloadState.ERROR(response.code))
                }
            })
            mutex.withLock { calls -= bean.url }
        }

    private fun updateState(bean: ResourceBean, state: DownloadState) {
        val map = _states.value.toMutableMap()
        map[bean.url] = state
        _states.update { map }
    }

    override fun onCleared() {
        downloader.cancelAll()
    }
}

sealed interface DownloadState {

    object IDLE : DownloadState
    object WAIT : DownloadState
    class DOWNLOADING(val progress: Float) : DownloadState
    object CHECKING : DownloadState
    object RETRYING : DownloadState
    object FINISH : DownloadState
    class ERROR(val code: Int) : DownloadState
}
