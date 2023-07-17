# Getting Started

## Asynchronous Download

`Asynchronous Download`意味着在异步线程中执行

```kotlin
val request = Download.Request.Builder()
    .url(url)
    .into(file)
    .build()
val call = downloader.newCall(request)
call.enqueue()
```

添加回调监听

```kotlin
call.enqueue(object : Download.Callback {
    // ...
    override fun onSuccess(call: Download.Call, response: Download.Response) {
        // do your job
    }

    override fun onFailure(call: Download.Call, response: Download.Response) {
        // do your job
    }
})
```

## Synchronous Download

`Synchronous Download`意味着在当前线程中执行，即阻塞调用线程执行

```kotlin
val request = Download.Request.Builder()
    .url(url)
    .into(file)
    .build()
val call = downloader.newCall(request)
call.execute()
```

添加回调监听

```kotlin
call.execute(object : Download.Callback {
    // ...
    override fun onSuccess(call: Download.Call, response: Download.Response) {
        // do your job
    }

    override fun onFailure(call: Download.Call, response: Download.Response) {
        // do your job
    }
})
```


