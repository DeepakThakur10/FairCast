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

class CollegeSelectionActivity : AppCompatActivity() {

    private val colleges = listOf(
        "IIT Delhi", "IIT Bombay", "IIT Madras", "IIT Kanpur", "IIT Kharagpur",
        "IIT Roorkee", "IIT Guwahati", "IIT Hyderabad", "NIT Trichy", "NIT Warangal",
        "NIT Surathkal", "NIT Calicut", "BITS Pilani", "BITS Goa", "BITS Hyderabad",
        "IIIT Hyderabad", "IIIT Bangalore", "DTU Delhi", "NSUT Delhi", "VIT Vellore"
    )

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var scrollView: NestedScrollView
    private lateinit var container: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var resetSearch: TextView
    private lateinit var noResultsText: TextView
    private var selectedCollege: String? = null

    private val positions = listOf("President", "VicePresident", "Secretary", "Treasurer", "CulturalHead")

    // College student candidate names with branches and years
    private val candidateData = listOf(
        mapOf("name" to "Arjun Sharma", "branch" to "Computer Science", "year" to "3rd"),
        mapOf("name" to "Priya Patel", "branch" to "Electronics", "year" to "4th"),
        mapOf("name" to "Rahul Singh", "branch" to "Mechanical", "year" to "2nd"),
        mapOf("name" to "Ananya Gupta", "branch" to "Civil", "year" to "3rd"),
        mapOf("name" to "Vikram Kumar", "branch" to "Computer Science", "year" to "4th"),
        mapOf("name" to "Sneha Reddy", "branch" to "Electronics", "year" to "2nd"),
        mapOf("name" to "Karan Mehta", "branch" to "Mechanical", "year" to "3rd"),
        mapOf("name" to "Divya Joshi", "branch" to "Civil", "year" to "4th"),
        mapOf("name" to "Amit Verma", "branch" to "Computer Science", "year" to "2nd"),
        mapOf("name" to "Pooja Agarwal", "branch" to "Electronics", "year" to "3rd"),
        mapOf("name" to "Rohit Bansal", "branch" to "Mechanical", "year" to "4th"),
        mapOf("name" to "Kavya Nair", "branch" to "Civil", "year" to "2nd"),
        mapOf("name" to "Saurav Das", "branch" to "Computer Science", "year" to "3rd"),
        mapOf("name" to "Riya Jain", "branch" to "Electronics", "year" to "4th"),
        mapOf("name" to "Deepak Yadav", "branch" to "Mechanical", "year" to "2nd")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_college_selection)

        initializeViews()
        setupScrollView()
        setupSearchFunctionality()
        populateColleges(colleges.sorted())
        addScrollToTopButton()
    }

    private fun initializeViews() {
        scrollView = findViewById(R.id.scrollView)
        container = findViewById(R.id.collegeListContainer)
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

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    resetSearch.visibility = View.VISIBLE
                    val filteredColleges = colleges.filter {
                        it.contains(query, ignoreCase = true)
                    }.sorted()
                    populateColleges(filteredColleges)
                } else {
                    resetSearch.visibility = View.GONE
                    populateColleges(colleges.sorted())
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        resetSearch.setOnClickListener {
            searchInput.text.clear()
            resetSearch.visibility = View.GONE
            populateColleges(colleges.sorted())
        }
    }

    private fun populateColleges(collegeList: List<String>) {
        container.removeAllViews()

        if (collegeList.isEmpty()) {
            noResultsText.visibility = View.VISIBLE
            return
        } else {
            noResultsText.visibility = View.GONE
        }

        collegeList.forEach { college ->
            val collegeView = createCollegeView(college)
            container.addView(collegeView)
        }
    }

    private fun createCollegeView(college: String): View {
        val collegeCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 16, 32, 16)
            }
            setPadding(40, 32, 40, 32)
            background = ContextCompat.getDrawable(this@CollegeSelectionActivity, R.drawable.village_card_bg)
            elevation = 8f
            isClickable = true
            isFocusable = true
        }

        val collegeNameText = TextView(this).apply {
            text = college
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@CollegeSelectionActivity, R.color.primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val subtitleText = TextView(this).apply {
            text = "Select to view elections"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@CollegeSelectionActivity, R.color.secondary))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }

        collegeCard.addView(collegeNameText)
        collegeCard.addView(subtitleText)

        collegeCard.setOnClickListener {
            selectedCollege = college
            animateCardSelection(collegeCard)
            navigateToElections(college)
        }

        return collegeCard
    }

    private fun animateCardSelection(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun navigateToElections(college: String) {
        // Create election data for the selected college
        createElectionData(college)

        // Navigate to election activity
        val intent = Intent(this, CollegeVoteActivity::class.java)
        intent.putExtra("COLLEGE_NAME", college)
        startActivity(intent)
    }

    private fun createElectionData(college: String) {
        val collegeRef = firestore.collection("colleges").document(college)

        // Create college document
        val collegeData = mapOf(
            "name" to college,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "totalPositions" to positions.size
        )

        collegeRef.set(collegeData, SetOptions.merge())

        // Create positions and assign random candidates
        positions.forEach { position ->
            val positionRef = collegeRef.collection("positions").document(position)

            // Randomly assign 2-3 candidates for each position
            val shuffledCandidates = candidateData.shuffled()
            val candidatesForPosition = shuffledCandidates.take((2..3).random())

            val positionData = mapOf(
                "title" to position,
                "candidates" to candidatesForPosition.map { candidate ->
                    mapOf(
                        "name" to candidate["name"],
                        "branch" to candidate["branch"],
                        "year" to candidate["year"],
                        "votes" to 0
                    )
                },
                "totalVotes" to 0,
                "isActive" to true
            )

            positionRef.set(positionData, SetOptions.merge())
        }
    }

    private fun addScrollToTopButton() {
        val scrollToTopButton = TextView(this).apply {
            text = "↑"
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@CollegeSelectionActivity, android.R.color.white))
            background = ContextCompat.getDrawable(this@CollegeSelectionActivity, R.drawable.scroll_to_top_bg)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                setMargins(0, 0, 32, 32)
            }
            elevation = 12f
            visibility = View.GONE
        }

        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        rootLayout.addView(scrollToTopButton)

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            animateHeaderOnScroll(scrollY)
            if (scrollY > 300) {
                scrollToTopButton.visibility = View.VISIBLE
            } else {
                scrollToTopButton.visibility = View.GONE
            }
        }

        scrollToTopButton.setOnClickListener {
            scrollView.smoothScrollTo(0, 0)
        }
    }
}