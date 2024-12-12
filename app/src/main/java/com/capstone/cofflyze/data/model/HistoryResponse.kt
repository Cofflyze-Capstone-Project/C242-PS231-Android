package com.capstone.cofflyze.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryResponse(
    val id_history: Int,
    val gambar: String,
    val akurasi: String,
    val tanggal: String,
    val nama_penyakit: String,
    val deskripsi: String,
    val penyebab: String,
    val gejala: String,
    val faktor_risiko: String,
    val penanganan: String,
    val pencegahan: String,
    val tokenFirebase: String
) : Parcelable

data class HistoryRequest(
    val gambar: String,
    val akurasi: String,
    val nama_penyakit: String,
    val deskripsi: String,
    val penyebab: String,
    val gejala: String,
    val faktor_risiko: String,
    val penanganan: String,
    val pencegahan: String,
    val tokenFirebase: String
)
