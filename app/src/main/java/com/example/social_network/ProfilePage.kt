package com.example.social_network

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import app.com.kotlinapp.OnSwipeTouchListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfilePage : AppCompatActivity() {

    private lateinit var gestureDetectorLayout: RelativeLayout

    private lateinit var auth: FirebaseAuth

    //TextViews
    private var userNameTextView: TextView? = null
    private var userEmailTextView: TextView? = null

    //Images
    private var userProfileImage: ImageView? = null

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
    }

    private fun findViews() {
        userNameTextView = findViewById(R.id.ProfilePageActivityUsername)
        userEmailTextView = findViewById(R.id.ProfilePageActivityEmail)
        userProfileImage = findViewById(R.id.ProfilePageActivityProfilePicture)
        gestureDetectorLayout = this.findViewById(R.id.ProfilePageActivityGestureDetectorLayout)
    }

    private fun setViews() {
        userNameTextView?.text = auth.currentUser?.displayName
        userEmailTextView?.text = auth.currentUser?.email
        val profileImageUrl = auth.currentUser?.photoUrl
        if (profileImageUrl != null){ userProfileImage?.setImageURI(profileImageUrl) }else { userProfileImage?.setImageResource(R.drawable.logo_circle) }
    }

}