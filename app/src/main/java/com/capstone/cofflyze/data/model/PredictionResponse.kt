package com.capstone.cofflyze.data.model

data class PredictionResponse(
    val confidence: String,
    val deskripsi: String,
    val faktor_risiko: String,
    val gejala: String,
    val image_url: String,
    val penanganan: String,
    val pencegahan: String,
    val penyakit: String,
    val penyebab: String,
    val tanggal: String
)