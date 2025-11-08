package com.example.faircast

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class VoteActivity : AppCompatActivity() {

    private lateinit var candidateContainer: LinearLayout
    private lateinit var mainScrollView: ScrollView
    private lateinit var progressBar: ProgressBar
    private lateinit var headerTitle: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var villageName: String
    private val candidateListeners = mutableListOf<ListenerRegistration>()
    private val sections = listOf("Mukhiya", "Sarpanch", "WardMember")
    private val userId = FirebaseAuth.getInstance().uid ?: "anonymous"
    private var votedSections = mutableSetOf<String>()
    private val sectionLayouts = mutableMapOf<String, LinearLayout>()
    private val sectionTitles = mutableMapOf<String, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote)

        villageName = intent.getStringExtra("villageName") ?: return
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupInteractiveElements()
        loadAllSections()
    }

    private fun initializeViews() {
        mainScrollView = findViewById(R.id.mainScrollView)
        candidateContainer = findViewById(R.id.candidateListContainer)
        progressBar = findViewById(R.id.votingProgressBar)
        headerTitle = findViewById(R.id.headerTitle)

        // Set village name in header
        headerTitle.text = "🏛️ Voting in $villageName"

        // Setup progress bar
        progressBar.max = sections.size
        progressBar.progress = 0

        // Add smooth scrolling
        mainScrollView.isScrollbarFadingEnabled = true
        mainScrollView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    }

    private fun setupInteractiveElements() {
        // Add floating action button for scroll to top
        val fabScrollTop = findViewById<ImageButton>(R.id.fabScrollTop)
        fabScrollTop.setOnClickListener {
            animateScrollToTop()
        }

        // Show/hide FAB based on scroll position
        mainScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 300) {
                showFAB(fabScrollTop)
            } else {
                hideFAB(fabScrollTop)
            }
        }
    }

    private fun loadAllSections() {
        candidateContainer.removeAllViews()
        addVotingInstructions()

        sections.forEachIndexed { index, position ->
            Handler(Looper.getMainLooper()).postDelayed({
                loadSection(position)
            }, index * 200L) // Stagger loading for smooth effect
        }
    }

    private fun addVotingInstructions() {
        val instructionsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            background = ContextCompat.getDrawable(this@VoteActivity, R.drawable.village_card_bg)
            elevation = 6f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 32)
            }
        }

        val instructionsTitle = TextView(this).apply {
            text = "📋 Voting Instructions"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@VoteActivity, R.color.primary))
            gravity = Gravity.CENTER
        }

        val instructionsText = TextView(this).apply {
            text = "• Tap on section headers to expand\n• Vote for one candidate per section\n• Confirm your choice carefully\n• Complete all sections to finish"
            textSize = 14f
            setPadding(0, 16, 0, 0)
            setTextColor(ContextCompat.getColor(this@VoteActivity, android.R.color.black))
        }

        instructionsCard.addView(instructionsTitle)
        instructionsCard.addView(instructionsText)
        candidateContainer.addView(instructionsCard)

        // Animate instructions card
        animateCardEntrance(instructionsCard)
    }

    private fun loadSection(position: String) {
        val sectionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 24, 0, 12)
            }
        }

        val sectionTitle = TextView(this).apply {
            text = "🔸 $position (Tap to expand)"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@VoteActivity, R.color.primary))
            setPadding(24, 20, 24, 20)
            background = ContextCompat.getDrawable(this@VoteActivity, R.drawable.expandable_header_bg)
            elevation = 4f
            setOnClickListener {
                toggleSectionVisibility(position, sectionLayout, this)
            }
        }

        sectionLayout.visibility = View.GONE
        sectionLayouts[position] = sectionLayout
        sectionTitles[position] = sectionTitle

        candidateContainer.addView(sectionTitle)
        candidateContainer.addView(sectionLayout)

        // Animate section appearance
        animateCardEntrance(sectionTitle)

        checkUserVoted(position) { hasVoted ->
            if (hasVoted) {
                votedSections.add(position)
                updateSectionTitle(position, true)
                updateProgressBar()
            }

            val collectionRef = db.collection("BiharPanchayat")
                .document(villageName)
                .collection(position)

            val listener = collectionRef.addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    sectionLayout.removeAllViews()
                    val sortedCandidates = snapshot.documents.sortedByDescending {
                        it.getLong("votes") ?: 0
                    }

                    sortedCandidates.forEachIndexed { index, doc ->
                        val name = doc.getString("name") ?: return@forEachIndexed
                        val votes = doc.getLong("votes")?.toInt() ?: 0
                        val card = createInteractiveCandidateCard(position, name, votes, !hasVoted, index == 0)
                        sectionLayout.addView(card)

                        // Stagger card animations
                        Handler(Looper.getMainLooper()).postDelayed({
                            animateCardEntrance(card)
                        }, index * 100L)
                    }
                }
            }
            candidateListeners.add(listener)
        }
    }

    private fun toggleSectionVisibility(position: String, sectionLayout: LinearLayout, titleView: TextView) {
        val isVisible = sectionLayout.visibility == View.VISIBLE

        if (isVisible) {
            // Collapse animation
            val collapseAnimator = ValueAnimator.ofFloat(1f, 0f)
            collapseAnimator.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                sectionLayout.alpha = value
                sectionLayout.scaleY = value
            }
            collapseAnimator.duration = 300
            collapseAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    sectionLayout.visibility = View.GONE
                    titleView.text = "🔸 $position (Tap to expand)"
                }
            })
            collapseAnimator.start()
        } else {
            // Expand animation
            sectionLayout.visibility = View.VISIBLE
            sectionLayout.alpha = 0f
            sectionLayout.scaleY = 0f

            val expandAnimator = ValueAnimator.ofFloat(0f, 1f)
            expandAnimator.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                sectionLayout.alpha = value
                sectionLayout.scaleY = value
            }
            expandAnimator.duration = 300
            expandAnimator.interpolator = OvershootInterpolator()
            expandAnimator.start()

            titleView.text = "🔹 $position (Tap to collapse)"

            // Auto-scroll to section
            Handler(Looper.getMainLooper()).postDelayed({
                animateScrollToView(titleView)
            }, 100)
        }
    }

    private fun updateSectionTitle(position: String, hasVoted: Boolean) {
        sectionTitles[position]?.let { titleView ->
            if (hasVoted) {
                titleView.text = "✅ $position (Completed)"
                titleView.setTextColor(ContextCompat.getColor(this, R.color.status_completed))
            }
        }
    }

    private fun updateProgressBar() {
        val animator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, votedSections.size)
        animator.duration = 500
        animator.interpolator = BounceInterpolator()
        animator.start()
    }

    private fun checkUserVoted(position: String, callback: (Boolean) -> Unit) {
        db.collection("Votes")
            .document(userId)
            .get()
            .addOnSuccessListener {
                val voted = it.getString(position)
                callback(voted != null)
                if (voted != null) {
                    votedSections.add(position)
                }
                checkAllVotesCast()
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    private fun createInteractiveCandidateCard(
        position: String,
        name: String,
        votes: Int,
        isVotingAllowed: Boolean,
        isLeading: Boolean
    ): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            background = ContextCompat.getDrawable(
                this@VoteActivity,
                if (isLeading) R.drawable.leading_candidate_bg else R.drawable.village_card_bg
            )
            elevation = if (isLeading) 8f else 4f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        // Add leading indicator
        if (isLeading) {
            val leadingIndicator = TextView(this).apply {
                text = "🏆 Leading"
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(this@VoteActivity, R.color.gold))
                gravity = Gravity.END
            }
            layout.addView(leadingIndicator)
        }

        val nameView = TextView(this).apply {
            text = "👤 $name"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ContextCompat.getColor(this@VoteActivity,
                if (isLeading) R.color.primary else android.R.color.black))
        }

        val voteCount = TextView(this).apply {
            text = "🗳️ Votes: $votes"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@VoteActivity, R.color.primary))
            setPadding(0, 8, 0, 8)
        }

        // Interactive vote button with enhanced styling
        val voteBtn = Button(this).apply {
            text = if (isVotingAllowed) "Cast Vote" else "Vote Casted"
            isEnabled = isVotingAllowed
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setPadding(32, 16, 32, 16)

            if (isVotingAllowed) {
                background = ContextCompat.getDrawable(this@VoteActivity, R.drawable.vote_button_bg)
                setTextColor(ContextCompat.getColor(this@VoteActivity, android.R.color.white))
                setOnClickListener {
                    animateButtonPress(this) {
                        showInteractiveConfirmationDialog(position, name, this)
                    }
                    it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                }
            } else {
                setBackgroundColor(ContextCompat.getColor(this@VoteActivity, R.color.status_completed))
                setTextColor(ContextCompat.getColor(this@VoteActivity, android.R.color.white))
            }
        }

        layout.addView(nameView)
        layout.addView(voteCount)
        layout.addView(voteBtn)

        if (!isVotingAllowed) {
            val votedLabel = TextView(this).apply {
                text = "✅ You have voted in this section"
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(this@VoteActivity, R.color.status_completed))
                gravity = Gravity.CENTER
                setPadding(0, 12, 0, 0)
            }
            layout.addView(votedLabel)
        }

        return layout
    }

    private fun animateButtonPress(button: Button, onComplete: () -> Unit) {
        val scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f)
        val scaleDown2 = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f)
        val scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f)
        val scaleUp2 = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleDown).with(scaleDown2)
        animatorSet.play(scaleUp).with(scaleUp2).after(scaleDown)
        animatorSet.duration = 150
        animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                onComplete()
            }
        })
        animatorSet.start()
    }

    private fun showInteractiveConfirmationDialog(position: String, candidateName: String, button: Button) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("🗳️ Confirm Vote")
        builder.setMessage("Are you sure you want to vote for:\n\n👤 $candidateName\n🏛️ as $position\n\nThis action cannot be undone.")
        builder.setIcon(R.drawable.ic_vote_logo)

        builder.setPositiveButton("Yes, Cast Vote") { dialog, _ ->
            dialog.dismiss()
            castVoteWithAnimation(position, candidateName, button)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        // Animate dialog appearance
        dialog.window?.decorView?.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator())
                .start()
        }
    }

    private fun castVoteWithAnimation(position: String, candidateName: String, button: Button) {
        // Show loading animation
        button.text = "Casting Vote..."
        button.isEnabled = false

        val candidateRef = db.collection("BiharPanchayat")
            .document(villageName)
            .collection(position)
            .document(candidateName)

        val userVoteRef = db.collection("Votes").document(userId)

        db.runTransaction { transaction ->
            val voteSnapshot = transaction.get(userVoteRef)
            val hasVoted = voteSnapshot.getString(position) != null
            if (hasVoted) throw Exception("Already voted for $position")

            val candidateSnapshot = transaction.get(candidateRef)
            val currentVotes = candidateSnapshot.getLong("votes") ?: 0
            transaction.update(candidateRef, "votes", currentVotes + 1)
            transaction.set(userVoteRef, mapOf(position to candidateName), SetOptions.merge())
        }.addOnSuccessListener {
            // Success animation
            animateVoteSuccess(button) {
                button.text = "✅ Vote Casted"
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.status_completed))
                votedSections.add(position)
                updateSectionTitle(position, true)
                updateProgressBar()
                checkAllVotesCast()
            }

            showSuccessToast("Vote successfully casted for $candidateName! 🎉")

        }.addOnFailureListener {
            // Error animation
            animateVoteError(button) {
                button.text = "Cast Vote"
                button.isEnabled = true
            }
            Toast.makeText(this, "You've already voted for $position.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateVoteSuccess(button: Button, onComplete: () -> Unit) {
        val successAnimator = ObjectAnimator.ofFloat(button, "rotation", 0f, 360f)
        successAnimator.duration = 500
        successAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                onComplete()
            }
        })
        successAnimator.start()
    }

    private fun animateVoteError(button: Button, onComplete: () -> Unit) {
        val shakeAnimator = ObjectAnimator.ofFloat(button, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shakeAnimator.duration = 500
        shakeAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                onComplete()
            }
        })
        shakeAnimator.start()
    }

    private fun showSuccessToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    private fun checkAllVotesCast() {
        db.collection("Votes")
            .document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (sections.all { snapshot.contains(it) }) {
                    // All votes cast - show completion animation
                    showCompletionAnimation()

                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this, ThankYouActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }, 2000)
                }
            }
    }

    private fun showCompletionAnimation() {
        val completionOverlay = findViewById<LinearLayout>(R.id.completionOverlay)
        completionOverlay.visibility = View.VISIBLE

        val checkmark = findViewById<TextView>(R.id.completionCheckmark)
        val message = findViewById<TextView>(R.id.completionMessage)

        // Animate checkmark
        checkmark.scaleX = 0f
        checkmark.scaleY = 0f
        checkmark.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(BounceInterpolator())
            .start()

        // Animate message
        message.alpha = 0f
        message.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(300)
            .start()
    }

    private fun animateScrollToView(view: View) {
        val scrollY = view.top - mainScrollView.height / 2
        val currentScrollY = mainScrollView.scrollY

        val animator = ValueAnimator.ofInt(currentScrollY, scrollY)
        animator.addUpdateListener { animation ->
            mainScrollView.scrollTo(0, animation.animatedValue as Int)
        }
        animator.duration = 500
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun animateScrollToTop() {
        val animator = ValueAnimator.ofInt(mainScrollView.scrollY, 0)
        animator.addUpdateListener { animation ->
            mainScrollView.scrollTo(0, animation.animatedValue as Int)
        }
        animator.duration = 800
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun showFAB(fab: ImageButton) {
        if (fab.visibility != View.VISIBLE) {
            fab.visibility = View.VISIBLE
            fab.scaleX = 0f
            fab.scaleY = 0f
            fab.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator())
                .start()
        }
    }

    private fun hideFAB(fab: ImageButton) {
        if (fab.visibility == View.VISIBLE) {
            fab.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(300)
                .withEndAction {
                    fab.visibility = View.GONE
                }
                .start()
        }
    }

    private fun animateCardEntrance(view: View) {
        view.alpha = 0f
        view.translationY = 60f
        view.scaleX = 0.9f
        view.scaleY = 0.9f

        view.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        candidateListeners.forEach { it.remove() }
    }
}