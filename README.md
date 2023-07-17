OkDownloader
============

A downloader base on OkHttp for Java and Android.

- Easy to use: API like OkHttp
- Many features: support many scenes, such as synchronous/asynchronous download, you can use it easy in coroutines
- Modern: OkDownloader is Kotlin-first and base on OkHttp
- Easy to expand: support SPI and dynomic Interceptor to expand

Features
--------

| Feature                  | Supported |
| ------------------------ | --------- |
| Synchronous download     | ✅        |
| Asynchronous download    | ✅        |
| Breakpoint download      | ✅        |
| Multi-threaded download | ✅        |
| Priority                 | ✅        |
| Retry                    | ✅        |
| Event subscribe          | ✅        |
| Subscrible API           | ✅        |
| Multi-downloader         | ✅        |

Download
--------

OkDownloader is available on `mavenCentral()`.

```kotlin
implementation("com.billbook.lib:downloader:1.0.0")
```

Quick Start
-----------

Build a downloader just like build OkHttpClient

```kotlin
val downloader = Downloader.Builder().build()
```

Start download

```kotlin
val request = Download.Request.Builder()
    .url(url)
    .into(path) // or into(file)
    .build()
val call = downloader.newCall(request)
val response = call.execute() // synchronous download
// use response here
```

or

```kotlin
call.enqueue() // Asynchronous download
```

with callback

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

Cancel download

```kotlin
call.cancel() // or call.cancelSafely()
```

Checkout OkDownloader's full documentation here

How to expand
-------------

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

R8/Proguard
-----------

OkDownloader is fully compatible with R8 out of the box and doesn't require adding any extra rules.
If you use Proguard, you may need to add rules for [OkHttp](https://github.com/square/okhttp/blob/master/okhttp/src/jvmMain/resources/META-INF/proguard/okhttp3.pro) and [Okio](https://github.com/square/okio/blob/master/okio/src/jvmMain/resources/META-INF/proguard/okio.pro).

License
=======

    Copyright 2023 Billbook, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
