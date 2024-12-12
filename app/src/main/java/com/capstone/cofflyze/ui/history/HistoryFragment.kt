package com.capstone.cofflyze.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.cofflyze.R
import com.capstone.cofflyze.data.model.ApiClient
import com.capstone.cofflyze.data.model.ApiService
import com.capstone.cofflyze.data.model.HistoryResponse
import com.capstone.cofflyze.ui.home.HistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            val tokenFirebase = currentUser.uid
            fetchHistoryData(tokenFirebase)
        } else {
            showToast("User not logged in")
            Log.e(TAG, "FirebaseAuth: User not logged in")
        }

        return view
    }

    private fun fetchHistoryData(tokenFirebase: String) {
        val apiService = ApiClient.getApiClient().create(ApiService::class.java)
        apiService.getHistoryByTokenFirebase(tokenFirebase).enqueue(object : Callback<List<HistoryResponse>> {
            override fun onResponse(
                call: Call<List<HistoryResponse>>,
                response: Response<List<HistoryResponse>>
            ) {
                if (response.isSuccessful) {
                    val historyList = response.body() ?: emptyList()
                    if (historyList.isNotEmpty()) {
                        setupRecyclerView(historyList)
                    } else {
                        showToast("No history found")
                        Log.w(TAG, "Response successful but history list is empty")
                    }
                } else {
                    showToast("Failed to load history data")
                    Log.e(TAG, "Response failed: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<HistoryResponse>>, t: Throwable) {
                showToast("Error: ${t.message}")
                Log.e(TAG, "Failed to fetch history data: ${t.message}")
            }
        })
    }

    private fun setupRecyclerView(historyList: List<HistoryResponse>) {
        val historyAdapter = HistoryAdapter(historyList, requireContext()) { selectedHistory ->
            // When an item is clicked, navigate to the detail fragment with the selected history
            navigateToDetail(selectedHistory)
        }
        recyclerView.adapter = historyAdapter
    }

    private fun navigateToDetail(selectedHistory: HistoryResponse) {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.fragment_fade_in)
            .setExitAnim(R.anim.fragment_fade_out)
            .setPopEnterAnim(R.anim.fragment_fade_in)
            .setPopExitAnim(R.anim.fragment_fade_out)
            .build()

        val idHistory = selectedHistory.id_history.toString() // Convert id_history to String
        if (idHistory.isNotEmpty()) {
            val bundle = Bundle().apply {
                putParcelable("historyDetail", selectedHistory)
            }

            findNavController().navigate(R.id.historyDetailFragment, bundle)

        } else {
            showToast("Invalid history ID")
            Log.e(TAG, "navigateToDetail: id_history is null or empty")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "HistoryFragment"
    }
}
