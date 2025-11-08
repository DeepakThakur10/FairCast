package com.example.faircast.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.faircast.LoginActivity
import com.example.faircast.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etVoterId: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button
    private lateinit var btnChangeImage: Button
    private lateinit var quoteTextView: TextView
    private var isEditing = false

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadImageToFirebase(it) }
    }

    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val bitmap = result.data!!.extras?.get("data") as? Bitmap
            bitmap?.let {
                val uri = getImageUri(it)
                uploadImageToFirebase(uri)
            }
        }
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver, bitmap, "profile", null
        )
        return Uri.parse(path)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        imageView = view.findViewById(R.id.profileImageView)
        etFullName = view.findViewById(R.id.etFullName)
        etEmail = view.findViewById(R.id.etEmail)
        etVoterId = view.findViewById(R.id.etVoterId)
        etPhone = view.findViewById(R.id.etPhone)
        etAddress = view.findViewById(R.id.etAddress)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnChangeImage = view.findViewById(R.id.btnChangeImage)
        quoteTextView = view.findViewById(R.id.quoteText)

        loadUserData()

        btnEdit.setOnClickListener {
            if (isEditing) {
                saveAddressUpdate()
                enableFields(false)
                btnEdit.text = "Edit"
            } else {
                enableFields(true)
                btnEdit.text = "Save"
            }
            isEditing = !isEditing
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        btnChangeImage.setOnClickListener {
            showImageOptions()
        }

        showMotivationalQuote()

        return view
    }

    private fun loadUserData() {
        val fullName = auth.currentUser?.displayName ?: return
        db.collection("user").document(fullName)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    etFullName.setText(snapshot.getString("fullName"))
                    etEmail.setText(snapshot.getString("email"))
                    etVoterId.setText(snapshot.getString("voterId"))
                    etPhone.setText(snapshot.getString("phone"))
                    etAddress.setText(snapshot.getString("address") ?: "")
                    val imageUrl = snapshot.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).into(imageView)
                    }
                }
            }
    }

    private fun saveAddressUpdate() {
        val fullName = auth.currentUser?.displayName ?: return
        val address = etAddress.text.toString().trim()

        db.collection("user").document(fullName)
            .update("address", address)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            }
    }

    private fun enableFields(enable: Boolean) {
        etFullName.isEnabled = false
        etEmail.isEnabled = false
        etVoterId.isEnabled = false
        etPhone.isEnabled = false
        etAddress.isEnabled = enable
    }

    private fun showImageOptions() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image From")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        captureImage.launch(intent)
                    }
                    1 -> pickImage.launch("image/*")
                }
            }
            .show()
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val fullName = auth.currentUser?.displayName ?: return
        val ref = storage.reference.child("profile_images/$uid.jpg")

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    db.collection("user").document(fullName)
                        .update("imageUrl", uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showMotivationalQuote() {
        val quotes = listOf(
            "Believe you can and you're halfway there.",
            "Stay positive, work hard, make it happen.",
            "Push yourself, because no one else is going to do it for you.",
            "Your only limit is your mind.",
            "Great things never come from comfort zones."
        )
        quoteTextView.text = quotes.random()
    }
}
