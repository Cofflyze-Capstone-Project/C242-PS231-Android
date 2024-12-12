package com.capstone.cofflyze.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.capstone.cofflyze.R

class ArticleDetailFragment : Fragment() {

    private var imageUrl: String? = null
    private var title: String? = null
    private var description: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_article_detail, container, false)

        // Retrieve arguments safely using the 'arguments' property
        arguments?.let {
            imageUrl = it.getString("imageUrl")
            title = it.getString("title")
            description = it.getString("description")
        }

        // Initialize views
        val imageView: ImageView = view.findViewById(R.id.articleDetailImage)
        val titleView: TextView = view.findViewById(R.id.articleDetailTitle)
        val descriptionView: TextView = view.findViewById(R.id.articleDetailDescription)

        // Set data to views with null checks
        titleView.text = title ?: "No Title Available"
        descriptionView.text = description ?: "No Description Available"

        // Load image with Glide, checking if the URL is valid
        Glide.with(requireContext())
            .load(imageUrl ?: "") // Gambar atau URL
            .placeholder(R.drawable.ic_profile_placeholder)  // Placeholder
            .error(R.drawable.ic_profile_placeholder)  // Gambar error
            .transform(RoundedCorners(30))  // Apply rounded corners transformation
            .into(imageView)


        return view
    }

    companion object {
        // Factory method to create a new instance of ArticleDetailFragment with data
        fun newInstance(imageUrl: String, title: String, description: String): ArticleDetailFragment {
            val fragment = ArticleDetailFragment()
            val bundle = Bundle().apply {
                putString("imageUrl", imageUrl)
                putString("title", title)
                putString("description", description)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
