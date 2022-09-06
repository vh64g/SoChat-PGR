package com.example.social_network

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import app.com.kotlinapp.OnSwipeTouchListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.random.Random

class watchPosts : AppCompatActivity() {

    private lateinit var gestureDetectorLayout: RelativeLayout

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var viewMode: String = "normal"

    private var posts: MutableList<DocumentSnapshot>? = null

    private var imageView: ImageView? = null

    private var usernameTextView: TextView? = null
    private var postTextView: TextView? = null
    private var likesTextView: TextView? = null
    private var likeIndicator: TextView? = null

    private var deleteBtn: Button? = null

    private var currentPost: DocumentSnapshot? = null

    private var lastSlide: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_watch_posts)
        auth = Firebase.auth
        getExtras(intent)
        findViews()
        gestureDetector()
        getPosts()
    }

    private fun getExtras(intent: Intent){
        val mode: String?
        val extras: Bundle? = intent.extras
        mode = extras?.getString("mode")
        if (mode != null) {
            viewMode = mode
        }
    }

    private fun getPosts(){
        if (viewMode == "normal") {
            db.collection("posts").get().addOnSuccessListener { documents ->
                posts = documents.toMutableList()
                if (posts != null && posts!!.size > 0) {
                    if(auth.currentUser?.uid == posts?.get(0)?.get("uid").toString() || auth.currentUser?.uid.toString() == "27FHEnkMGUeUOlrsmBg6EdXgsHC3"){ deleteBtn?.visibility = Button.VISIBLE }
                    else { deleteBtn?.visibility = Button.GONE }
                }
                showNewPost()
            }
        } else if (viewMode == "user") {
            db.collection("posts").whereEqualTo("authorUid", auth.currentUser?.uid).get().addOnSuccessListener { documents ->
                posts = documents.toMutableList()
                deleteBtn?.visibility = Button.VISIBLE
                showNewPost()
            }
        }
    }

    private fun findViews(){
        //ImageView
        imageView = findViewById(R.id.ImgView)
        //TextView
        usernameTextView = findViewById(R.id.TxtUser)
        postTextView = findViewById(R.id.PostTextView)
        likesTextView = findViewById(R.id.likesCount)
        likeIndicator = findViewById(R.id.WatchPostsActivityLikeIndicator)
        //Gesture Relative Layout
        gestureDetectorLayout = findViewById(R.id.gestureDetectorLayoutWatchPostsActivity)
        //Button
        deleteBtn = findViewById(R.id.WatchPostActivityDeleteButton)
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
                showNewPost()
            }
            override fun onSwipeDown() {
                super.onSwipeDown()
                finish()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                likePost()
            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    private fun showNewPost(){

        if(lastSlide){finish()}

        if(posts==null || posts!!.size<=0){
            val msg = "You're all caught up!"
            usernameTextView?.text = msg
            imageView?.setImageResource(R.drawable.logo_circle)
            postTextView?.text = ""
            likesTextView?.text = "∞"
            deleteBtn?.visibility = Button.GONE
            currentPost = null
            lastSlide = true
            return
        }

        lastSlide = false

        val postIndex = Random.nextInt(posts!!.size)
        val post = posts?.get(postIndex)
        val username = post?.data?.get("user")
        val imageUrl = post?.data?.get("image")
        val postText = post?.data?.get("content")
        val likes = post?.data?.get("likes")
        val likedBy: MutableList<Any?> = post?.data?.get("likedBy") as MutableList<Any?>

        currentPost = post
        posts?.removeAt(postIndex)

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
        likesTextView?.text = likes.toString()
        if(likedBy.contains(auth.currentUser?.uid)){likeIndicator?.visibility = View.VISIBLE} else{likeIndicator?.visibility = View.GONE}
    }

    @Suppress("UNCHECKED_CAST")
    private fun likePost(){

        val post = currentPost ?: return
        var likes = post.data?.get("likes").toString().toInt()
        val likedBy: MutableList<Any?> = post.data?.get("likedBy") as MutableList<Any?>

        if(!likedBy.contains(auth.currentUser?.uid)){

            likedBy.add(auth.currentUser?.uid)
            likes ++

            db.collection("posts").document(post.id).update("likes", likes)
            db.collection("posts").document(post.id).update("likedBy", likedBy)
            db.collection("posts").document(post.id).get().addOnSuccessListener { document -> currentPost = document }

            likesTextView?.text = "$likes"
            likeIndicator?.visibility = View.VISIBLE
        } else {
            likedBy.remove(auth.currentUser?.uid)
            likes --

            db.collection("posts").document(post.id).update("likes", likes)
            db.collection("posts").document(post.id).update("likedBy", likedBy)
            db.collection("posts").document(post.id).get().addOnSuccessListener { document -> currentPost = document }

            likesTextView?.text = "$likes"
            likeIndicator?.visibility = View.GONE
        }
    }

    fun deletePost(view: View){
        val post = currentPost ?: return
        val imageUrl = post.data?.get("image")
        val storageRef = storage.getReferenceFromUrl(imageUrl.toString())
        storageRef.delete().addOnSuccessListener {
            db.collection("posts").document(post.id).delete().addOnSuccessListener {
                Toast.makeText(this, "deleted", Toast.LENGTH_SHORT).show()
                showNewPost()
            }
        }
    }

}