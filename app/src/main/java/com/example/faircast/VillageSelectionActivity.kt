package com.example.faircast

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class VillageSelectionActivity : AppCompatActivity() {

    private val villages = listOf(
        "Rampur", "Kartarpur", "Punch", "Sahebganj", "Baheri",
        "Maheru", "Cheharu", "Bharathi", "Simri", "Darbhanga",
        "Muzaffarpur", "Begusarai", "Madhubani", "Saharsa", "Katihar",
        "Kishanganj", "Munger", "Lakhisarai", "Sheikhpura", "Nawada"
    )

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var scrollView: NestedScrollView
    private lateinit var container: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var resetSearch: TextView
    private lateinit var noResultsText: TextView
    private var selectedVillage: String? = null

    private val positions = listOf("Mukhiya", "Sarpanch", "WardMember")

    // 3 candidate sets per role (3 names per section)
    private val candidateNames = listOf(
        "Kotlin Sahab", "XMl Sahiba ", "Nikhil Kumar Singh", "Parth Madrewar",
        "Lovely Singh", "Rani Patel", "Abhinav Tyagi", "Ayush Singh", "Kumar Mangalam"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_village_selection)

        initializeViews()
        setupScrollView()
        setupSearchFunctionality()
        populateVillages(villages.sorted())
        addScrollToTopButton()
    }

    private fun initializeViews() {
        scrollView = findViewById(R.id.scrollView)
        container = findViewById(R.id.villageListContainer)
        searchInput = findViewById(R.id.searchInput)
        resetSearch = findViewById(R.id.resetSearch)
        noResultsText = findViewById(R.id.noResultsText)
    }

    private fun setupScrollView() {
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                animateHeaderOnScroll(scrollY)
            }
        }
    }

    private fun animateHeaderOnScroll(scrollY: Int) {
        val header = findViewById<TextView>(R.id.headerText)
        val alpha = kotlin.math.max(0.3f, 1.0f - (scrollY / 500f))
        header.alpha = alpha
    }

    private fun setupSearchFunctionality() {
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim().lowercase()
                val filtered = villages.filter { it.lowercase().contains(query) }.sorted()

                container.removeAllViews()
                noResultsText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE

                populateVillages(filtered)

                if (filtered.isNotEmpty()) {
                    container.postDelayed({
                        scrollView.smoothScrollTo(0, container.getChildAt(0).top)
                    }, 200)
                }

                addScrollToTopButton()
            }
        })

        resetSearch.setOnClickListener {
            searchInput.text.clear()
            noResultsText.visibility = View.GONE
            container.removeAllViews()
            populateVillages(villages.sorted())
            addScrollToTopButton()
        }
    }

    private fun populateVillages(list: List<String>) {
        list.forEachIndexed { index, villageName ->
            createFirestoreStructureIfNeeded(villageName)
            val card = createInteractiveVillageCard(villageName, index)
            container.addView(card)
            animateCardEntrance(card, index)
        }
    }

    private fun createFirestoreStructureIfNeeded(villageName: String) {
        val villageDoc = firestore.collection("BiharPanchayat").document(villageName)

        villageDoc.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                positions.forEachIndexed { index, position ->
                    val roleCollection = villageDoc.collection(position)

                    val startIndex = index * 3
                    val endIndex = startIndex + 3
                    val roleCandidates = candidateNames.subList(startIndex, endIndex)
                    val a = 0

                    roleCandidates.forEach { name ->
                        val candidate = mapOf(
                            "name" to name,
                            "votes" to a
                        )
                        roleCollection.document(name).set(candidate, SetOptions.merge())
                    }
                }
            }
        }
    }

    private fun createInteractiveVillageCard(villageName: String, index: Int): View {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 24, 20, 24)
            background = ContextCompat.getDrawable(this@VillageSelectionActivity, R.drawable.village_card_bg)
            elevation = 4f

            setOnClickListener { view ->
                handleVillageSelection(villageName, view)
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> animateCardPress(view, true)
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> animateCardPress(view, false)
                }
                false
            }
        }

        val emojiView = TextView(this).apply {
            text = getVillageEmoji(index)
            textSize = 24f
            setPadding(0, 0, 16, 0)
        }

        val nameView = TextView(this).apply {
            text = villageName
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@VillageSelectionActivity, android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val arrowView = TextView(this).apply {
            text = "→"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@VillageSelectionActivity, R.color.primary))
        }

        cardLayout.addView(emojiView)
        cardLayout.addView(nameView)
        cardLayout.addView(arrowView)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 12)
        }

        cardLayout.layoutParams = params
        return cardLayout
    }

    private fun getVillageEmoji(index: Int): String {
        val emojis = listOf(
            "🏘️", "🏡", "🏠", "🏞️", "🌾", "🌿", "🌳", "🌲", "🏕️", "🏛️",
            "🏗️", "🏘️", "🏡", "🏠", "🏞️", "🌾", "🌿", "🌳", "🌲", "🏕️"
        )
        return emojis[index % emojis.size]
    }

    private fun animateCardEntrance(card: View, index: Int) {
        card.alpha = 0f
        card.translationY = 50f

        card.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay((index * 50).toLong())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun animateCardPress(view: View, isPressed: Boolean) {
        val scale = if (isPressed) 0.95f else 1.0f
        val alpha = if (isPressed) 0.7f else 1.0f

        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .alpha(alpha)
            .setDuration(100)
            .start()
    }

    private fun handleVillageSelection(villageName: String, view: View) {
        selectedVillage = villageName
        highlightSelectedVillage(view)
        view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)

        view.postDelayed({
            val intent = Intent(this, VoteActivity::class.java)
            intent.putExtra("villageName", villageName)
            startActivity(intent)
        }, 200)
    }

    private fun highlightSelectedVillage(selectedView: View) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            child.background = ContextCompat.getDrawable(this, R.drawable.village_card_bg)
        }

        selectedView.background = ContextCompat.getDrawable(this, R.drawable.village_card_selected_bg)
    }

    private fun addScrollToTopButton() {
        val scrollToTopButton = TextView(this).apply {
            text = "⬆️ Top"
            textSize = 14f
            setPadding(16, 8, 16, 8)
            setTextColor(ContextCompat.getColor(this@VillageSelectionActivity, android.R.color.white))
            background = ContextCompat.getDrawable(this@VillageSelectionActivity, R.drawable.scroll_to_top_bg)
            alpha = 0f

            setOnClickListener {
                scrollView.smoothScrollTo(0, 0)
            }
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 16, 0, 16)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        scrollToTopButton.layoutParams = layoutParams
        container.addView(scrollToTopButton)

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            animateHeaderOnScroll(scrollY)

            val shouldShow = scrollY > 300
            val targetAlpha = if (shouldShow) 1f else 0f

            if (scrollToTopButton.alpha != targetAlpha) {
                scrollToTopButton.animate()
                    .alpha(targetAlpha)
                    .setDuration(200)
                    .start()
            }
        }
    }
}
