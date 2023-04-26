okdownloader
========

A downloader for Android and Java.

Features
--------

Synchronous download
-------------
```java
val downloader = Downloader.Builder().build()
val request = Download.Request.Builder()
    .url(url)
    .path(path)
    .build()
downloader.newCall(request).execute()
```

Asynchronous download
---------------------
```java
val downloader = Downloader.Builder().build()
val request = Download.Request.Builder()
    .url(url)
    .path(path)
    .build()
downloader.newCall(request).enqueue()
```

Callback
--------
```java
downloader.newCall(request).enqueue(object : Download.Callback {
    // ...
    override fun onSuccess(call: Download.Call, response: Download.Response) {
    // do your job
    }

    override fun onFailure(call: Download.Call, response: Download.Response) {
        // do your job
    }
})
```

Priority
--------
```java
val request = Download.Request.Builder()
    .priority(Download.Priority.HIGH)
    .url(url)
    .path(path)
    .build()
```

Retry
-----
```java
val request = Download.Request.Builder()
    .retry(3)
    .url(url)
    .path(path)
    .build()
```

Global callback
-----
```java
val downloader = Downloader.Builder().build()
val subscriber = object : Download.Subscriber {
    // ...
    override fun onSuccess(call: Download.Call, response: Download.Response) {
    // do your job
    }

    override fun onFailure(call: Download.Call, response: Download.Response) {
    // do your job
    }
}
downloader.subscribe(subscriber)
downloader.unsubscribe(subscriber)
```

Global callback with url
------------------------
```java
val downloader = Downloader.Builder().build()
val subscriber = object : Download.Subscriber {
    // ...
    override fun onSuccess(call: Download.Call, response: Download.Response) {
        // do your job
    }

    override fun onFailure(call: Download.Call, response: Download.Response) {
        // do your job
    }
}
downloader.subscribe(url, subscriber)
downloader.unsubscribe(url, subscriber)
```

Specifies thread callback
-----------------------------
```java
val request = Download.Request.Builder()
    .callbackOn(CallbackExecutor.SERIAL)
    .url(url)
    .path(path)
    .build()
```

Android main thread callback
-----------------------------
```java
val request = Download.Request.Builder()
    .callbackOn(CallbackExecutor.Main)
    .url(url)
    .path(path)
    .build()
```

Android specifies network
-------------------------
```java
val request = DownloadRequest.Builder()
    .networkOn(DownloadRequest.NETWORK_WIFI or DownloadRequest.NETWORK_DATA)
    .url(url)
    .path(path)
    .build()
```

How to expand
-------------
```java
val downloader = Downloader.Builder()
    .addInterceptor(CustomInterceptor())
    .build()
```
or 

Declare your interceptor using SPI,In META-INF/services/com.billbook.lib.Interceptor

```java
com.example.CustomInterceptor1
com.example.CustomInterceptor2
com.example.CustomInterceptor3
```

License
=======

    Copyright 2013 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

