Optional Settings
========================

The following settings are optional and can be customized according to your project needs.

Event Listeners
---------------

Set download event listeners using the `eventListenerFactory` method. For example:

```kotlin
val downloader = Downloader.Builder()
    .eventListenerFactory { ReporterEventListener() }
    .build()
```

ReporterEventListener:

```kotlin
class ReporterEventListener : EventListener() {
    override fun callSuccess(call: Download.Call, response: Download.Response) {
        // do your job
    }

    override fun callFailed(call: Download.Call, response: Download.Response) {
        // do your job
    }
}
```

Idle Task Callback
------------------

Set idle task callback using the `idleCallback` method

```kotlin
val downloader = Downloader.Builder()
    .idleCallback { // handle download pool idle }
    .build()
```

Custom OkHttpClient
-------------------

Set a custom OkHttpClient using the `okHttpClientFactory` method

```kotlin
val downloader = Downloader.Builder()
    .okHttpClientFactory { buildOkHttpClient() }
    .build()

private fun buildOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .cache(null)
        .build()
}
```

Set Default Retry Count
-----------------------

Set the default retry count. The default is 3

```kotlin
val downloader = Downloader.Builder()
    .defaultMaxRetry(10)
    .build()
```

Set Task Execution Thread Pool
------------------------------

Set a custom asynchronous download task execution thread pool using the `executorService` method

```kotlin
val downloader = Downloader.Builder()
    .executorService(CustomExecutorService())
    .build()
```