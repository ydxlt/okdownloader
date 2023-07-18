
FAQ
===

How to pause a download
-----------------------

In OkDownloader, there is no specific `pause` method. Instead, you can use the `cancel` method, which cancels the download without deleting the already downloaded files, effectively pausing the download.

```kotlin
call.cancel()
```

> The `cancelSafely()` method will deletes the downloaded temporary files.

How to resume a download
------------------------

In OkDownloader, there is no dedicated `resume` method. Download tasks in OkDownloader are treated as one-time operations. If a task is canceled and you want to resume it, you need to create a new `call` object and execute it again.

```kotlin
downloader.newCall(request).execute() // or enqueue()
```

Please note that this approach creates a new download task starting from the beginning. If you want to implement resumable downloads with support for partial downloads, you would need to handle the logic yourself, such as saving the progress and resuming from where it left off.

Please refer to the specific documentation of the downloader library you are using for more detailed information on pausing and resuming download tasks.
