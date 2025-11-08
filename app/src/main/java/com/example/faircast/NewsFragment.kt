package com.example.faircast.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.faircast.R

class NewsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)

        val readFeaturedButton: Button = view.findViewById(R.id.readFeaturedButton)
        val news1: LinearLayout = view.findViewById(R.id.news1)
        val news2: LinearLayout = view.findViewById(R.id.news2)
        val news3: LinearLayout = view.findViewById(R.id.news3)
        val upcoming1: LinearLayout = view.findViewById(R.id.upcoming1)
        val upcoming2: LinearLayout = view.findViewById(R.id.upcoming2)
        val subscribeButton: Button = view.findViewById(R.id.subscribeButton)

        readFeaturedButton.setOnClickListener {
            openArticle("https://example.com/article/2024-us-election")
        }

        news1.setOnClickListener {
            openArticle("https://example.com/article/european-elections")
        }

        news2.setOnClickListener {
            openArticle("https://example.com/article/india-2024-election")
        }

        news3.setOnClickListener {
            openArticle("https://example.com/article/global-turnout")
        }

        upcoming1.setOnClickListener {
            openArticle("https://example.com/elections/uk-general")
        }

        upcoming2.setOnClickListener {
            openArticle("https://example.com/elections/mexico-presidential")
        }

        subscribeButton.setOnClickListener {
            showSubscribeDialog()
        }

        return view
    }

    private fun openArticle(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showSubscribeDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Subscribe to Election Alerts")
            .setMessage("You'll receive breaking election news and analysis to your email.")
            .setPositiveButton("Confirm") { _, _ ->
                Toast.makeText(requireContext(), "Subscription confirmed!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}
