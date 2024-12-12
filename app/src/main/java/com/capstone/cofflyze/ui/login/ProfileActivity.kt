package com.capstone.cofflyze.ui.login

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.capstone.cofflyze.MainActivity
import com.capstone.cofflyze.R
import com.capstone.cofflyze.data.model.ApiClient
import com.capstone.cofflyze.data.model.ApiService
import com.capstone.cofflyze.data.model.ProfileRequest
import com.capstone.cofflyze.data.model.ProfileResponse
import com.google.firebase.auth.FirebaseAuth
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var gender: Spinner
    private lateinit var location: EditText
    private lateinit var saveButton: Button
    private lateinit var profileImage: ImageView
    private lateinit var selectImageButton: Button

    private var firebaseUid: String? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize views
        firstName = findViewById(R.id.first_name)
        lastName = findViewById(R.id.last_name)
        phoneNumber = findViewById(R.id.phone_number)
        gender = findViewById(R.id.gender)
        location = findViewById(R.id.location)
        saveButton = findViewById(R.id.save_button)
        profileImage = findViewById(R.id.profileImage)
        selectImageButton = findViewById(R.id.uploadphoto)

        // Gender Spinner setup
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gender.adapter = adapter

        // Firebase UID
        val currentUser = FirebaseAuth.getInstance().currentUser
        firebaseUid = currentUser?.uid

        if (firebaseUid == null) {
            Toast.makeText(this, "User belum login. Silakan login terlebih dahulu.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Select image button click
        selectImageButton.setOnClickListener {
            selectImage()
        }


        // Save button click
        saveButton.setOnClickListener {
            if (firebaseUid != null) {
                saveProfileData()
            } else {
                Toast.makeText(this, "UID tidak tersedia. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val sourceUri = data.data
            if (sourceUri != null) {
                val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
                UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1f, 1f) // Crop 1:1
                    .withMaxResultSize(1000, 1000) // Resolusi maksimum
                    .start(this)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            if (resultUri != null) {
                selectedImageUri = resultUri
                profileImage.setImageURI(resultUri) // Tampilkan gambar yang telah di-crop
            }
        } else if (requestCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveProfileData() {
        val firstNameInput = firstName.text.toString().trim()
        val lastNameInput = lastName.text.toString().trim()
        val phoneNumberInput = phoneNumber.text.toString().trim()
        val genderInput = gender.selectedItem.toString()
        val locationInput = location.text.toString().trim()

        // Validate input
        if (firstNameInput.isEmpty() || lastNameInput.isEmpty() || phoneNumberInput.isEmpty() ||
            genderInput == "Select Gender" || locationInput.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert image to Bitmap
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("fotoProfile", "profile.jpg", requestFile)

        // Create ProfileRequest object (tanpa fotoProfile)
        val profileRequest = ProfileRequest(
            namaLengkap = "$firstNameInput $lastNameInput",
            nomorHp = phoneNumberInput,
            alamat = locationInput,
            jenisKelamin = genderInput,
            tokenFirebase = firebaseUid ?: "",
            fotoProfile = null // Tidak perlu mengisi fotoProfile karena akan dikirim sebagai MultipartBody.Part
        )

        // Send profile data to API
        val apiService = ApiClient.getApiClient().create(ApiService::class.java)
        val call = apiService.createUserWithImage(
            "$firstNameInput $lastNameInput".toRequestBody("text/plain".toMediaTypeOrNull()),
            phoneNumberInput.toRequestBody("text/plain".toMediaTypeOrNull()),
            locationInput.toRequestBody("text/plain".toMediaTypeOrNull()),
            genderInput.toRequestBody("text/plain".toMediaTypeOrNull()),
            (firebaseUid ?: "").toRequestBody("text/plain".toMediaTypeOrNull()),
            body // Send image as MultipartBody.Part
        )

        call.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ProfileActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                    finish()
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to save profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.e("API Error", "Error: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private const val IMAGE_REQUEST_CODE = 100
    }
}
