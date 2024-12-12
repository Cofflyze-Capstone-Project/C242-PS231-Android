package com.capstone.cofflyze.ui.login

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("CofflyzePrefs", Context.MODE_PRIVATE)

    // Untuk menyimpan data
    fun saveUserUid(firebaseUid: String) {
        val editor = sharedPreferences.edit()
        editor.putString("USER_UID", firebaseUid)
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.apply()  
    }

    // Untuk mengambil data
    fun getUserUid(): String? {
        return sharedPreferences.getString("USER_UID", null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("IS_LOGGED_IN", false)
    }

    // Untuk menghapus data atau logout
    fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()  // Menghapus semua data
        editor.apply()
    }
}
