package com.capstone.cofflyze.data.model

import com.google.gson.annotations.SerializedName


data class UserProfile(
    val idUser: Int,           // idUser sebagai Integer
    val namaLengkap: String,
    val jenisKelamin: String,
    val fotoProfile: String,   // URL gambar profil
    val nomorHp: String,
    val alamat: String,
    val tokenFirebase: String
)

data class ProfileResponse(
    val idUser: String,
    val tokenFirebase: String,
    val status: String,
    val message: String,
    val data: ProfileData?, // Hanya satu data profil yang dibutuhkan
    val imageUrl: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: String,
    val location: String,
    val profileImageUrl: String

)

data class ProfileData(
    val namaLengkap: String,
    val nomorHp: String,
    val alamat: String,
    val tokenFirebase: String,
    val jenisKelamin: String,
    val fotoProfile: String?
)

data class ProfileRequest(
    val namaLengkap: String,
    val nomorHp: String,
    val alamat: String,
    val jenisKelamin : String,
    val tokenFirebase: String,
    val fotoProfile: String?
)

