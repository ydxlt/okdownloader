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

## 资源校验

可以给 Request 设置 md5，这样会进行MD5 校验

```kotlin
val request = Download.Request.Builder()
    // ..
    .md5(md5)
    .build()
```
还可以设置文件大小，这样在下载完成时会校验文件大小

```kotlin
val request = Download.Request.Builder()
    // ..
    .size(size)
    .build()
```

## 设置重试

设置重试次数，默认重试次数为 3 次

```kotlin
val request = Download.Request.Builder()
    // ..
    .retry(5)
    .build()
```

## 设置优先级

OkDownloader支持 High，MIddle，low 三种优先级，默认优先级为 Middle

```kotlin
val request = Download.Request.Builder()
    // ..
    .priority(Download.Priority.HIGH)
    .build()
```

## 设置tag

用来标记任务

```kotlin
val request = Download.Request.Builder()
    // ..
    .tag(tag)
    .build()
```

## 任务订阅

OkDownloader除了任务回调之外，还支持任务订阅，如下将会订阅该 Downloader所有的下载任务结果

```kotlin
val subscriber = object : Download.Subscriber {
    override fun onSuccess(call: Download.Call, response: Download.Response) {
        // Do you job
    }

    override fun onFailure(call: Download.Call, response: Download.Response) {
        // Do you job
    }
}
downloader.subscribe(subscriber)
```

取消订阅

```kotlin
downloader.unsubscribe(subscriber)
```

