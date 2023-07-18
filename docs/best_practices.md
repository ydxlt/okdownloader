Best Practices
==============

Managing Downloader Objects
---------------------------

Build and maintain a singleton downloader object to facilitate centralized management of download tasks. Different instances of the downloader have different download pools.

In Android 🫴Hilt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideDownloader(@ApplicationContext context: Context): Downloader {
        return Downloader.Builder()
            .addInterceptor(CopyOnExistsInterceptor(context, 1000))
            .addInterceptor(NetworkInterceptor(context))
            .build()
    }
}
```

If you want to manage tasks for different business purposes within your app, you can initialize a downloader and create new downloaders using the `newBuilder` method of the downloader. This allows the new downloaders to have different download pools while reusing thread pools and other resources.

```kotlin
val downloader = Downloader.Builder().build()
```

Business 1

```kotlin
val downloaderForBiz1 = downloader.newBuilder().build()
```

Business 2

```kotlin
val downloaderForBiz2 = downloader.newBuilder().build()
```

Using in Coroutines
-------------------

Executing the call in the IO dispatcher

```kotlin
withContext(Dispatchers.IO) {
    val request = Download.Request.Builder()
        .url(url)
        .into(file)
        .build()
    val response = downloader.newCall(request).execute()
}
```

By using coroutines with the IO dispatcher, you can perform the download operation asynchronously without blocking the main thread.
