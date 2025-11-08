package com.example.faircast.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.faircast.CollegeSelectionActivity
import com.example.faircast.R
import com.example.faircast.VillageSelectionActivity
import java.text.SimpleDateFormat
import java.util.*

class ElectionFragment : Fragment() {

    private var ongoingExpanded = true
    private var upcomingExpanded = true
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_election, container, false)
        setupViews(view)
        startPeriodicUpdates()
        return view
    }

    private fun setupViews(view: View) {
        val ongoingContainer = view.findViewById<LinearLayout>(R.id.ongoingElectionsContainer)
        val upcomingContainer = view.findViewById<LinearLayout>(R.id.upcomingElectionsContainer)
        val ongoingToggle = view.findViewById<TextView>(R.id.ongoingToggle)
        val upcomingToggle = view.findViewById<TextView>(R.id.upcomingToggle)
        val lastUpdatedTime = view.findViewById<TextView>(R.id.lastUpdatedTime)

        val ongoingElections = listOf(
            Triple("Lok Sabha By-Elections", "5 states", "15 Oct 2024 - 20 Oct 2024"),
            Triple("Bihar Panchayat Elections", "Phase 3", "12 Oct 2025 - 18 Oct 2025"),
            Triple("Karnataka Municipal Elections", "25 cities", "20 Oct 2025 - 22 Oct 2025")
        )

        val upcomingElections = listOf(
            Triple("General Elections 2024", "All India", "April 2024"),
            Triple("Maharashtra Vidhan Sabha", "Statewide", "October 2024"),
            Triple("Delhi Nagar Nigam", "Municipal", "December 2023"),
            Triple("West Bengal Panchayat", "Rural areas", "February 2024"),
            Triple("Rajasthan Assembly", "Statewide", "November 2023")
        )

        ongoingElections.forEachIndexed { index, election ->
            val card = createInteractiveElectionCard(election.first, election.second, election.third, R.color.ongoing_election_bg, true)
            ongoingContainer.addView(card)
            animateStagger(card, index)
        }

        upcomingElections.forEachIndexed { index, election ->
            val card = createInteractiveElectionCard(election.first, election.second, election.third, R.color.upcoming_election_bg, false)
            upcomingContainer.addView(card)
            animateStagger(card, index)
        }

        ongoingToggle.setOnClickListener {
            toggleSection(ongoingContainer, ongoingToggle, ongoingExpanded) {
                ongoingExpanded = !ongoingExpanded
            }
        }

        upcomingToggle.setOnClickListener {
            toggleSection(upcomingContainer, upcomingToggle, upcomingExpanded) {
                upcomingExpanded = !upcomingExpanded
            }
        }

        setupElectionTypeCards(view)
        updateLastUpdatedTime(lastUpdatedTime)
    }

    private fun animateStagger(view: View, index: Int) {
        view.alpha = 0f
        view.translationY = 50f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay((index * 100).toLong())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun createInteractiveElectionCard(
        title: String,
        scope: String,
        date: String,
        bgColorRes: Int,
        isOngoing: Boolean
    ): View {
        val context = requireContext()

        val cardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            background = ContextCompat.getDrawable(context, R.drawable.election_card_bg)
            isClickable = true
            isFocusable = true
            foreground = ContextCompat.getDrawable(context, android.R.drawable.list_selector_background)
        }

        val statusLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val statusDot = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(12, 12).apply {
                setMargins(0, 0, 16, 0)
            }
            background = ContextCompat.getDrawable(context,
                if (isOngoing) R.drawable.red_dot else R.drawable.green_dot
            )
        }

        val statusText = TextView(context).apply {
            text = if (isOngoing) "LIVE" else "SCHEDULED"
            textSize = 12f
            setTextColor(ContextCompat.getColor(context,
                if (isOngoing) R.color.ongoing_election_text else R.color.upcoming_election_text
            ))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        statusLayout.addView(statusDot)
        statusLayout.addView(statusText)

        val titleText = TextView(context).apply {
            text = title
            textSize = 18f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 12, 0, 8)
        }

        val infoLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 8)
        }

        val scopeText = TextView(context).apply {
            text = "📍 $scope"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val dateText = TextView(context).apply {
            text = "📅 $date"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        }

        infoLayout.addView(scopeText)
        infoLayout.addView(dateText)

        cardLayout.addView(statusLayout)
        cardLayout.addView(titleText)
        cardLayout.addView(infoLayout)

        if (isOngoing) {
            val progressBar = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    8
                ).apply {
                    setMargins(0, 8, 0, 0)
                }
                background = ContextCompat.getDrawable(context, R.drawable.progress_bar_bg)
            }
            cardLayout.addView(progressBar)
            animateProgressBar(progressBar)
            startPulseAnimation(statusDot)
        }

        cardLayout.setOnClickListener {
            if (title == "Bihar Panchayat Elections") {
                val intent = Intent(requireContext(), VillageSelectionActivity::class.java)
                startActivity(intent)
            } else {
                showElectionDetails(title, "Details about $title")
            }
        }

        return cardLayout
    }

    private fun animateProgressBar(progressBar: View) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                progressBar.scaleX = it.animatedValue as Float
            }
            start()
        }
    }

    private fun startPulseAnimation(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f, 1.0f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }

        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f, 1.0f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }

        val alpha = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.6f, 1.0f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }

        // Start animations individually
        scaleX.start()
        scaleY.start()
        alpha.start()
    }


    private fun animateCardClick(view: View, action: () -> Unit) {
        val down = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f)
            )
            duration = 100
        }

        val up = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f)
            )
            duration = 100
            interpolator = BounceInterpolator()
        }

        down.start()
        down.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                up.start()
                action()
            }
        })
    }

    private fun toggleSection(container: LinearLayout, toggle: TextView, isExpanded: Boolean, callback: () -> Unit) {
        val rotation = if (isExpanded) 0f else 180f
        toggle.animate()
            .rotation(rotation)
            .setDuration(300)
            .start()

        if (isExpanded) {
            container.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    container.visibility = View.GONE
                    callback()
                }
                .start()
        } else {
            container.visibility = View.VISIBLE
            container.alpha = 0f
            container.animate()
                .alpha(1f)
                .setDuration(200)
                .withEndAction { callback() }
                .start()
        }
    }

    private fun setupElectionTypeCards(view: View) {
        val cards = listOf(
            Pair(view.findViewById<LinearLayout>(R.id.lokSabhaCard), "Lok Sabha"),
            Pair(view.findViewById<LinearLayout>(R.id.vidhanSabhaCard), "Vidhan Sabha"),
            Pair(view.findViewById<LinearLayout>(R.id.nagarNigamCard), "Nagar Nigam"),
            Pair(view.findViewById<LinearLayout>(R.id.panchayatCard), "Panchayat"),
            Pair(view.findViewById<LinearLayout>(R.id.collegesCard), "Colleges")
        )

        cards.forEach { (card, name) ->
            card.setOnClickListener {
                animateCardClick(card) {
                    if (name == "Colleges") {
                        startActivity(Intent(requireContext(), CollegeSelectionActivity::class.java))
                    } else {
                        showElectionDetails(name, getElectionTypeDescription(name))
                    }
                }
            }

            card.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start()
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                }
                false
            }
        }
    }

    private fun getElectionTypeDescription(type: String): String {
        return when (type) {
            "Lok Sabha" -> "National elections for MPs. Lower house of Parliament."
            "Vidhan Sabha" -> "State elections for MLAs."
            "Nagar Nigam" -> "Municipal body elections for cities."
            "Panchayat" -> "Rural village-level elections."
            "Colleges" -> "Student elections or academic council elections."
            else -> "Info not available."
        }
    }

    private fun showElectionDetails(title: String, details: String) {
        Toast.makeText(requireContext(), "$title\n$details", Toast.LENGTH_LONG).show()
    }

    private fun updateLastUpdatedTime(textView: TextView) {
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        textView.text = currentTime
        textView.animate().alpha(0.5f).setDuration(200).withEndAction {
            textView.animate().alpha(1f).setDuration(200).start()
        }.start()
    }

    private fun startPeriodicUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                view?.findViewById<TextView>(R.id.lastUpdatedTime)?.let {
                    updateLastUpdatedTime(it)
                }
                handler.postDelayed(this, 30000)
            }
        }
        handler.postDelayed(updateRunnable!!, 30000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onResume() {
        super.onResume()
        view?.let {
            it.alpha = 0f
            it.translationY = 100f
            it.animate().alpha(1f).translationY(0f).setDuration(500)
                .setInterpolator(AccelerateDecelerateInterpolator()).start()
        }
    }
}
