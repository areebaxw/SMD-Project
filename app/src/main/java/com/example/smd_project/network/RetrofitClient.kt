package com.example.smd_project.network

import com.example.smd_project.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private const val BASE_URL = "https://campusino-backend-production.up.railway.app/api/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC // Changed from BODY to BASIC to avoid binary data
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
                }
                
                requestBuilder.method(original.method, original.body)
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    fun getApiService(sessionManager: SessionManager): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient(sessionManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return retrofit.create(ApiService::class.java)
    }
}
