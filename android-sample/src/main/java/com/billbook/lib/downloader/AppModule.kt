package com.billbook.lib.downloader

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author xluotong@gmail.com
 */
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