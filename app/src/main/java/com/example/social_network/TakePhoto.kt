package com.example.social_network

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream
import java.lang.Long

class TakePhoto : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    val db = Firebase.firestore
    private val storage = Firebase.storage
    private var storageRef = storage.reference
    var postsRef: StorageReference? = storageRef.child("posts")

    private var byteArray: ByteArray? = null
    private var bmp: Bitmap? = null
    private var userName: String? = null

    private var imagePreview: ImageView? = null
    private var userTextView: TextView? = null
    private var edtPostContent: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_take_photo)
        auth = Firebase.auth
        userName = "${auth.currentUser?.displayName}"
        findViews()
        getBmp(intent)
        showImage()
    }

    private fun getBmp(intent: Intent){
        val extras: Bundle? = intent.extras
        byteArray = extras?.getByteArray("image")
        bmp = byteArray?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    private fun findViews(){
        imagePreview = findViewById(R.id.ImgPreview)
        userTextView = findViewById(R.id.TxtUserPreview)
        edtPostContent = findViewById(R.id.TxtImageDescription)
    }

    private fun showImage(){
        imagePreview?.setImageBitmap(bmp)
        userTextView?.text = userName
    }

    fun btnCancelClicked(view: View) {
        finish()
    }
    fun btnSaveClicked(view: View) {
        // Create a file in the Pictures/HelloAR album.
        val out = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/SoChat", "Img" + Long.toHexString(System.currentTimeMillis()) + ".png")
        // Make sure the directory exists
        if (!out.parentFile?.exists()!!) {out.parentFile?.mkdirs()}
        // Save the bitmap to disk.
        val fos = FileOutputStream(out)
        bmp?.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show()
        finish()
    }
    fun btnPublishClicked(view: View) {
        val userImgRefName = "${auth.currentUser?.uid}/${Long.toHexString(System.currentTimeMillis())}.png"
        val userImgRef = postsRef?.child(userImgRefName)
        val postContent = edtPostContent?.text.toString()
        val uploadTask = userImgRef?.putBytes(byteArray!!)
        uploadTask
            ?.addOnFailureListener { Toast.makeText(this, "Upload failed", Toast.LENGTH_LONG).show() }
            ?.addOnSuccessListener { taskSnapshot ->
                userImgRef.downloadUrl.addOnSuccessListener { uri ->
                    val newPost = hashMapOf(
                        "user" to userName,
                        "content" to postContent,
                        "image" to uri,
                        "date" to System.currentTimeMillis(),
                        "likes" to 0
                    )
                    db.collection("posts").add(newPost)
                    Toast.makeText(this, "Upload successful", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
    }
}