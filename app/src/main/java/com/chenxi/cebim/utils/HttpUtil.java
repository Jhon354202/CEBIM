package com.chenxi.cebim.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * 网络请求工具，有保持cookie功能
 */
public class HttpUtil {

    private static OkHttpClient singleton;

    //非常有必要，要不此类还是可以被new，但是无法避免反射
    private HttpUtil() {

    }

    public static OkHttpClient getInstance() {
        if (singleton == null) {
            synchronized (HttpUtil.class) {
                if (singleton == null) {

                    singleton = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .cookieJar(new CookieJar() {
                                private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                                @Override
                                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                                    cookieStore.put(url.host(), cookies);
                                }

                                @Override
                                public List<Cookie> loadForRequest(HttpUrl url) {
                                    List<Cookie> cookies = cookieStore.get(url.host());
                                    return cookies != null ? cookies : new ArrayList<Cookie>();
                                }
                            }).build();
                }
            }
        }
        return singleton;
    }

}
