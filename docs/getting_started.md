Getting Started
===============

Asynchronous Download
---------------------

`Asynchronous download` means executing in an asynchronous thread.

```kotlin
val request = Download.Request.Builder()
    .url(url)
    .into(file)
    .build()
val call = downloader.newCall(request)
call.enqueue()
```

Add callback listeners:

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

Synchronous Download
--------------------

`Synchronous download` means executing in the current thread, blocking the calling thread.

```kotlin
val request = Download.Request.Builder()
    .url(url)
    .into(file)
    .build()
val call = downloader.newCall(request)
val response = call.execute()
```

Add callback listeners

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

Usually, synchronous download is used in coroutines:

```kotlin
withContext(Dispatchers.IO) {
    val request = Download.Request.Builder()
        .url(url)
        .into(file)
        .build()
    val response = downloader.newCall(request).execute()
}
```

Canceling Download
------------------

```kotlin
call.cancel()
```

or

```kotlin
call.cancelSafely()
```

The difference between `cancel` and `cancelSafely` is that `cancelSafely()` will delete the downloaded temporary file.

File Verification
-----------------

Set the `MD5` value to perform MD5 verification upon download completion

```kotlin
val request = Download.Request.Builder()
    // ..
    .md5(md5)
    .build()
```

Set the `size` of the file to verify the file size upon download completion:

```kotlin
val request = Download.Request.Builder()
    // ..
    .size(size)
    .build()
```

Setting Retries
---------------

Set the number of retries. The default number of retries is 3:

```kotlin
val request = Download.Request.Builder()
    // ..
    .retry(5)
    .build()
```

Setting Priority
----------------

Supports three priority levels: High, Middle, and Low. The default priority is Middle

```kotlin
val request = Download.Request.Builder()
    // ..
    .priority(Download.Priority.HIGH)
    .build()
```

Setting Tags
------------

Tags are used to label tasks and can be used to differentiate different tasks within the app for reporting purposes:

```kotlin
val request = Download.Request.Builder()
    // ..
    .tag(tag)
    .build()
```

Task Subscription
-----------------

In addition to task callbacks, task subscription is supported:

```kotlin
val subscriber = object : Download.Subscriber {
    override fun onSuccess(call: Download.Call, response: Download.Response) {
        // Do your job
    }

    override fun onFailure(call: Download.Call, response: Download.Response) {
        // Do your job
    }
}
downloader.subscribe(subscriber)
```

Unsubscribe:

```kotlin
downloader.unsubscribe(subscriber)
```
