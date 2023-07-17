# 自定义下载设置

以下设置项均可选，可以根据自己的项目需要进行设置

## 事件监听

通过`eventListenerFactory`方法设置下载事件监听，如下是设置上报的一个示例

```kotlin
val downloader = Downloader.Builder()
    .eventListenerFactory { ReporterEventListener() }
    .build()
```

ReporterEventListener

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

## 监听任务空闲

通过`idleCallback`设置任务空闲监听

```kotlin
val downloader = Downloader.Builder()
    .idleCallback { // download pool idle }
    .build()
```

## 自定义 OkHttpClient

通过`okhttpClientFactory`设置自定义 OkHttpClient

```kotlin
val downloader = Downloader.Builder()
    .okHttpClientFactory { buildOkHttpClient() }
    .build()

private fun buildOKHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .cache(null)
        .build()
}
```

## 设置默认重试次数

默认重试次数为 3 次

```kotlin
val downloader = Downloader.Builder()
    .defaultMaxRetry(10)
    .build()
```

## 设置任务执行线程池

调用`executorService`设置异步下载任务执行线程池

```kotlin
val downloader = Downloader.Builder()
    .executorService(CustomExecutorService())
    .build()
```

