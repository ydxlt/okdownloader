# How to Expand

Dynamically add interceptors

```java
val downloader = Downloader.Builder()
    .addInterceptor(CustomInterceptor())
    .build()
```

or

Declare your interceptors using SPI,In META-INF/services/com.billbook.lib.Interceptor

```java
com.example.CustomInterceptor1
com.example.CustomInterceptor2
com.example.CustomInterceptor3
```
