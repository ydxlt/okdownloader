# How to Expand

Add interceptors through code

```kotlin
val downloader = Downloader.Builder()
    .addInterceptor(CustomInterceptor())
    .build()
```

or

Declare your interceptors in `META-INF/services/com.billbook.lib.Interceptor` using the `SPI` mechanism.

```kotlin
com.example.CustomInterceptor1
com.example.CustomInterceptor2
com.example.CustomInterceptor3
```
