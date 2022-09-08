package com.example.social_network

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import app.com.kotlinapp.OnSwipeTouchListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class ProfilePage : AppCompatActivity() {

    private lateinit var gestureDetectorLayout: RelativeLayout

    private lateinit var auth: FirebaseAuth

    //TextViews
    private var userNameTextView: EditText? = null
    private var userEmailTextView: TextView? = null
    private var userPasswordTextView: EditText? = null

    //Images
    private var userProfileImage: ImageView? = null

    //Buttons
    private var edtProfileButton: Button? = null
    private var cancelEdtBtn: Button? = null
    private var saveEdtBtn: Button? = null

    //Layouts
    private var pwInfoLayout : LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_profile_page)
        auth = Firebase.auth
        findViews()
        setViews()
        gestureDetector()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun gestureDetector(){
        gestureDetectorLayout.setOnTouchListener(object : OnSwipeTouchListener(this@ProfilePage) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
            }
            override fun onSwipeRight() {
                super.onSwipeRight()
                finish()
            }
            override fun onSwipeUp() {
                super.onSwipeUp()
                intent = Intent(this@ProfilePage, watchPosts::class.java)
                intent.putExtra("mode", "user")
                startActivity(intent)
            }
            override fun onSwipeDown() {
                super.onSwipeDown()
            }
        })
        userProfileImage?.setOnTouchListener(object : OnSwipeTouchListener(this@ProfilePage) {
            override fun onDoubleClick() {
                super.onDoubleClick()
                Toast.makeText(this@ProfilePage, "Double Clicked", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun findViews() {
        userNameTextView = findViewById(R.id.ProfilePageActivityUsername)
        userEmailTextView = findViewById(R.id.ProfilePageActivityEmail)
        userPasswordTextView = findViewById(R.id.ProfilePageActivityPw)
        userProfileImage = findViewById(R.id.ProfilePageActivityProfilePicture)
        gestureDetectorLayout = this.findViewById(R.id.ProfilePageActivityGestureDetectorLayout)
        edtProfileButton = findViewById(R.id.ProfilePageActivityEdtProfileBtn)
        cancelEdtBtn = findViewById(R.id.ProfilePageActivityCancelEdtProfileBtn)
        saveEdtBtn = findViewById(R.id.ProfilePageActivitySaveEdtProfileBtn)
        pwInfoLayout = findViewById(R.id.ProfilePageActivityPwInfoLayout)
    }

    private fun setViews() {
        userNameTextView?.setText(auth.currentUser?.displayName)
        userEmailTextView?.text = auth.currentUser?.email
        val profileImageUrl = auth.currentUser?.photoUrl
        if (profileImageUrl != null){ userProfileImage?.setImageURI(profileImageUrl) }else { userProfileImage?.setImageResource(R.drawable.logo_circle) }
    }

    fun onEdtProfileBtnClick(view: View) {
        updateViews(true)
    }

    fun onSaveEdtProfileBtnClick(view: View) {
        val user = auth.currentUser
        val newEmail = userEmailTextView?.text.toString()
        val profileUpdates = userProfileChangeRequest {
            displayName = userNameTextView?.text.toString()
            photoUri = null
        }
        user?.updateProfile(profileUpdates)
            ?.addOnSuccessListener {
                if (newEmail != user.email){
                    if (pwInfoLayout?.visibility == View.VISIBLE){
                        val credential = EmailAuthProvider.getCredential(user.email!!, userPasswordTextView?.text.toString())
                        user.reauthenticate(credential)
                            .addOnCompleteListener {
                                Toast.makeText(this, "Reauthenticated", Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { Toast.makeText(this, "Reauthentication failed", Toast.LENGTH_SHORT).show() }
                    }
                    user.updateEmail(newEmail)
                        .addOnSuccessListener {
                            user.sendEmailVerification()
                                .addOnSuccessListener {
                                    updateViews(false)
                                    auth.signOut()
                                    intent = Intent(this, login::class.java)
                                    startActivity(intent)
                                    finish()}
                                .addOnFailureListener { Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show() } }
                        .addOnFailureListener { e ->
                            when ((e as FirebaseAuthException).errorCode) {
                                "ERROR_INVALID_EMAIL" -> Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
                                "ERROR_EMAIL_ALREADY_IN_USE" -> Toast.makeText(this, "Email already in use", Toast.LENGTH_SHORT).show()
                                "ERROR_REQUIRES_RECENT_LOGIN" -> {
                                    Toast.makeText(this, "Reauthentication required", Toast.LENGTH_SHORT).show()
                                    pwInfoLayout?.visibility = View.VISIBLE
                                }
                                else -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                }else{
                    updateViews(false)
                } }
            ?.addOnFailureListener { Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show() }
    }
    fun onCancelEdtProfileBtnClick(view: View) {
        updateViews(false)
    }

    private fun updateViews(edit: Boolean){
        if (edit){
            gestureDetectorLayout.visibility = View.GONE
            edtProfileButton?.visibility = View.GONE

            cancelEdtBtn?.visibility = View.VISIBLE
            saveEdtBtn?.visibility = View.VISIBLE
        } else {
            userNameTextView?.clearFocus()
            userEmailTextView?.clearFocus()

            gestureDetectorLayout.visibility = View.VISIBLE
            edtProfileButton?.visibility = View.VISIBLE

            cancelEdtBtn?.visibility = View.GONE
            saveEdtBtn?.visibility = View.GONE
            pwInfoLayout?.visibility = View.GONE
        }
    }

    fun onLogoutBtnClick(view: View) {
        auth.signOut()
        intent = Intent(this, login::class.java)
        startActivity(intent)
        finish()
    }
}