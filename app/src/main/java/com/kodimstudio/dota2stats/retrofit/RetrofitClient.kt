package com.kodimstudio.dota2stats.retrofit

import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun getClient(baseUrl: String): Retrofit {
        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 5

        val okHttpClient =
            OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .build()


        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
}