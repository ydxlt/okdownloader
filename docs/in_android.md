In Android
==========

In Android, you can add the following dependency

```kotlin
implementation("io.github.ydxlt:okdownloader-android:1.0.0")
```

To add a copy interceptor that records download tasks and prioritizes copying for subsequent downloads of the same resource to `prevent duplicate downloads`, use the `CopyOnExists` mechanism:

```kotlin
val downloader = Downloader.Builder()
    .addInterceptor(CopyOnExistsInterceptor(context, 1000))
    .build()
```

> Note: The task records use Google's modern [Room](https://developer.android.com/jetpack/androidx/releases/room) database.

To add a network interceptor that supports network restrictions

```kotlin
val downloader = Downloader.Builder()
    .addInterceptor(NetworkInterceptor(context))
    .build()
```

Afterward, you can set network restrictions

```kotlin
val request = DownloadRequest.Builder()
    .networkOn(DownloadRequest.NETWORK_WIFI or DownloadRequest.NETWORK_DATA)
    .build()
```

To add a storage interceptor that checks for insufficient disk space and avoids ineffective downloads

```kotlin
val downloader = Downloader.Builder()
    .addInterceptor(StorageInterceptor(100 * 1024 * 1024)) // 100MB
    .build()
```
