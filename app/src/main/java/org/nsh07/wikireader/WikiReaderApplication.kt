package org.nsh07.wikireader

import android.app.Application
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import okhttp3.OkHttpClient
import org.nsh07.wikireader.data.AppContainer
import org.nsh07.wikireader.data.DefaultAppContainer

class WikiReaderApplication : Application(), SingletonImageLoader.Factory {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        OkHttpClient.Builder()
                            .addNetworkInterceptor { chain ->
                                chain.proceed(
                                    chain.request()
                                        .newBuilder()
                                        .header(
                                            "User-Agent",
                                            container.userAgentString
                                        )
                                        .build()
                                )
                            }
                            .build()
                    )
                )
                add(SvgDecoder.Factory(scaleToDensity = true))
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}