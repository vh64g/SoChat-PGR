package com.example.social_network

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import app.com.kotlinapp.OnSwipeTouchListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.random.Random

class watchPosts : AppCompatActivity() {

    private lateinit var gestureDetectorLayout: RelativeLayout

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var posts: MutableList<DocumentSnapshot>? = null

    private var imageView: ImageView? = null

    private var usernameTextView: TextView? = null
    private var postTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_watch_posts)
        findViews()
        gestureDetector()
        getPosts()
    }

    private fun getPosts(){
        db.collection("posts").get()
            .addOnSuccessListener { result ->
                posts = result.documents
                if (posts!!.size > 0){
                    showNewPost()
                } else {
                    Toast.makeText(this, "No posts to show", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun findViews(){
        //ImageView
        imageView = findViewById(R.id.ImgView)
        //TextView
        usernameTextView = findViewById(R.id.TxtUser)
        postTextView = findViewById(R.id.PostTextView)
        //Gesture Relative Layout
        gestureDetectorLayout = findViewById(R.id.gestureDetectorLayoutWatchPostsActivity)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun gestureDetector(){
        gestureDetectorLayout.setOnTouchListener(object : OnSwipeTouchListener(this@watchPosts) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
            }
            override fun onSwipeRight() {
                super.onSwipeRight()
            }
            override fun onSwipeUp() {
                super.onSwipeUp()
                getPosts()
            }
            override fun onSwipeDown() {
                super.onSwipeDown()
                finish()
            }
        })
    }

    private fun showNewPost(){
        val post = posts?.get(Random.nextInt(posts!!.size))
        val username = post?.data?.get("user")
        val imageUrl = post?.data?.get("image")
        val postText = post?.data?.get("content")

        var imageBmp: Bitmap? = null

        val storageRef = storage.getReferenceFromUrl(imageUrl.toString())
        val ONE_MEGABYTE: Long = 1024 * 1024
        storageRef.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener {
                imageBmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                imageView?.setImageBitmap(imageBmp)
            }
            .addOnFailureListener {Toast.makeText(this, "Img download failed", Toast.LENGTH_SHORT).show()}

        usernameTextView?.text = username.toString()
        postTextView?.text = postText.toString()
    }

}