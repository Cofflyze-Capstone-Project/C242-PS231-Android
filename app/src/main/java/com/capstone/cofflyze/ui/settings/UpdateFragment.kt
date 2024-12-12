package com.capstone.cofflyze.ui.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.capstone.cofflyze.R
import com.capstone.cofflyze.data.model.ApiClient
import com.capstone.cofflyze.data.model.ApiService
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

class UpdateFragment : Fragment() {

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var gender: Spinner
    private lateinit var location: EditText
    private lateinit var saveButton: Button
    private lateinit var profileImage: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView

    private var selectedImageUri: Uri? = null
    private var uid: String? = null  // Menyimpan UID Firebase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_update, container, false)

        // Inisialisasi views
        firstName = binding.findViewById(R.id.first_name)
        lastName = binding.findViewById(R.id.last_name)
        phoneNumber = binding.findViewById(R.id.phone_number)
        gender = binding.findViewById(R.id.gender)
        location = binding.findViewById(R.id.location)
        saveButton = binding.findViewById(R.id.save_button)
        profileImage = binding.findViewById(R.id.profileImage)
        selectImageButton = binding.findViewById(R.id.uploadphoto)
        titleText = binding.findViewById(R.id.title_text)
        subtitleText = binding.findViewById(R.id.subtitle_text)

        // Set spinner gender
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gender.adapter = adapter

        // Dapatkan UID pengguna Firebase
        getUserUID()

        // Select image button
        selectImageButton.setOnClickListener {
            selectImage()
        }

        // Save button click
        saveButton.setOnClickListener {
            if (uid != null) {
                saveProfileData()
            } else {
                Toast.makeText(requireContext(), "UID tidak tersedia. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding
    }

    private fun getUserUID() {
        // Mendapatkan UID Firebase pengguna yang sedang login
        val user = FirebaseAuth.getInstance().currentUser
        uid = user?.uid
        if (uid == null) {
            Log.e("Firebase", "User UID tidak ditemukan!")
            Toast.makeText(requireContext(), "UID tidak ditemukan!", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("Firebase", "User UID: $uid")
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data

            // Menggunakan UCrop untuk memotong gambar
            selectedImageUri?.let { uri ->
                val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image.jpg"))
                UCrop.of(uri, destinationUri)
                    .withAspectRatio(1f, 1f) // Set ratio square untuk gambar profile
                    .withMaxResultSize(500, 500) // Set ukuran maksimal gambar yang diinginkan
                    .start(requireContext(), this) // Mulai UCrop
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!) // Mendapatkan URI gambar yang sudah dipotong
            resultUri?.let {
                profileImage.setImageURI(resultUri) // Menampilkan gambar hasil crop ke ImageView
                selectedImageUri = resultUri // Menyimpan URI gambar cropped
            }
        } else if (requestCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(requireContext(), "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveProfileData() {
        val firstNameInput = firstName.text.toString().trim()
        val lastNameInput = lastName.text.toString().trim()
        val phoneNumberInput = phoneNumber.text.toString().trim()
        val genderInput = gender.selectedItem.toString()
        val locationInput = location.text.toString().trim()

        // Validasi input
        if (firstNameInput.isEmpty() || lastNameInput.isEmpty() || phoneNumberInput.isEmpty() ||
            genderInput == "Select Gender" || locationInput.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a profile image", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert image to Bitmap
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("fotoProfile", "profile.jpg", requestFile)

        // Pastikan UID ada
        val apiService = ApiClient.getApiClient().create(ApiService::class.java)

        // Ganti firebaseToken dengan UID
        val call = apiService.updateUserProfileByToken(
            uid ?: "",  // Gunakan UID
            "$firstNameInput $lastNameInput".toRequestBody("text/plain".toMediaTypeOrNull()),
            phoneNumberInput.toRequestBody("text/plain".toMediaTypeOrNull()),
            locationInput.toRequestBody("text/plain".toMediaTypeOrNull()),
            genderInput.toRequestBody("text/plain".toMediaTypeOrNull()),
            body // Foto profile sebagai multipart
        )

        call.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                    // Menggunakan NavController untuk menavigasi
                    val navController = findNavController()
                    navController.navigate(R.id.navigation_setting) // Gantilah dengan fragment tujuan yang sesuai
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.e("API Error", "Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private const val IMAGE_REQUEST_CODE = 100
    }
}
