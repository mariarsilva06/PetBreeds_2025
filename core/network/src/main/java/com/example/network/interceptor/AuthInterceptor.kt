package com.example.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val apiKey: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request =
            original
                .newBuilder()
                .header("x-api-key", apiKey)
                .build()
        return chain.proceed(request)
    }
}
