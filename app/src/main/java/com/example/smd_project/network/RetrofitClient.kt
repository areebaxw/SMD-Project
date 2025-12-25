package com.example.smd_project.network

import com.example.smd_project.utils.SessionManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://web-production-763f.up.railway.app/api/"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("DEBUG_HTTP", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY // Logs full request & response
    }

    private fun getOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()

                // Add auth token if available
                sessionManager.getToken()?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                    Log.d("DEBUG_TOKEN", "Adding token to header: $token")
                }

                val request = requestBuilder.build()

                // Log full request details
                Log.d("DEBUG_REQUEST", "Request URL: ${request.url}")
                Log.d("DEBUG_REQUEST", "Request method: ${request.method}")
                Log.d("DEBUG_REQUEST", "Request headers: ${request.headers}")

                val response = chain.proceed(request)

                // Log response info
                Log.d("DEBUG_RESPONSE", "Response code: ${response.code}")
                Log.d("DEBUG_RESPONSE", "Response message: ${response.message}")

                response
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getApiService(sessionManager: SessionManager): ApiService {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient(sessionManager))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
