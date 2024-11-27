
/*
  Code taken from https://gist.github.com/swankjesse/8571a8207a5815cca1fb
*/

package org.nsh07.wikireader.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * An interceptor that allows runtime changes to the URL hostname.
 */
public final class HostSelectionInterceptor implements Interceptor {
    private volatile String host;

    public static void main(String[] args) throws Exception {
        HostSelectionInterceptor interceptor = new HostSelectionInterceptor();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Request request = new Request.Builder()
                .url("http://www.coca-cola.com/robots.txt")
                .build();

        okhttp3.Call call1 = okHttpClient.newCall(request);
        okhttp3.Response response1 = call1.execute();
        System.out.println("RESPONSE FROM: " + response1.request().url());
        System.out.println(response1.body().string());

        interceptor.setHost("www.pepsi.com");

        okhttp3.Call call2 = okHttpClient.newCall(request);
        okhttp3.Response response2 = call2.execute();
        System.out.println("RESPONSE FROM: " + response2.request().url());
        System.out.println(response2.body().string());
    }

    public void setHost(String host) {
        this.host = host;
    }

    @NonNull
    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String host = this.host;
        if (host != null) {
            HttpUrl newUrl = request.url().newBuilder()
                    .host(host)
                    .build();
            request = request.newBuilder()
                    .url(newUrl)
                    .build();
        }
        return chain.proceed(request);
    }
}