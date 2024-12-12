package com.capstone.cofflyze.data.model

import com.capstone.cofflyze.ui.home.Article
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("users")
    fun createUserWithImage(
        @Part("namaLengkap") namaLengkap: RequestBody,
        @Part("nomorHp") nomorHp: RequestBody,
        @Part("alamat") alamat: RequestBody,
        @Part("jenisKelamin") jenisKelamin: RequestBody,
        @Part("tokenFirebase") tokenFirebase: RequestBody,
        @Part fotoProfile: MultipartBody.Part // Foto profil wajib
    ): Call<ProfileResponse>


    @Multipart
    @PUT("users/{tokenFirebase}")
    fun updateUserProfileByToken(
        @Path("tokenFirebase") token: String,
        @Part("namaLengkap") name: RequestBody,
        @Part("nomorHp") phone: RequestBody,
        @Part("alamat") address: RequestBody,
        @Part("jenisKelamin") gender: RequestBody,
        @Part fotoProfile: MultipartBody.Part? = null
    ): Call<ProfileResponse>


    @GET("articles")
    fun getArticles(): Call<List<Article>>

    @GET("users")
    fun getAllUsers(): Call<List<UserProfile>>

    @Multipart
    @POST("predict")
    fun predictDisease(
        @Part image: okhttp3.MultipartBody.Part
    ): Call<PredictionResponse>

    @POST("history")
    fun saveHistory(@Body historyRequest: HistoryRequest): Call<Void>

    @GET("history/{tokenFirebase}")
    fun getHistoryByTokenFirebase(@Path("tokenFirebase") tokenFirebase: String): Call<List<HistoryResponse>>

    @GET("history/{tokenFirebase}/{id_history}")
    fun getHistoryDetail(
        @Path("tokenFirebase") tokenFirebase: String,
        @Path("id_history") idHistory: Int
    ): Call<List<HistoryResponse>>

}
