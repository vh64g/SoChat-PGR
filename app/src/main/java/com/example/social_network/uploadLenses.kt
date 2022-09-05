package com.example.social_network

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class uploadLenses : AppCompatActivity() {
    //Firebase
    val db = Firebase.firestore
    //TextFields
    private var LensName: TextView? = null
    private var LensModel: TextView? = null
    private var LensTexture: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_upload_lenses)
        findViews()
    }
    fun upload(view: View) {
        val newLens = hashMapOf(
            "model" to "${LensModel?.text}",
            "name" to "${LensName?.text}",
            "texture" to "${LensTexture?.text}"
        )
        Toast.makeText(this, "Uploading... ${LensModel?.text}", Toast.LENGTH_SHORT).show()
        db.collection("lenses").add(newLens)
    }
    private fun findViews(){
        LensName = findViewById(R.id.LensName)
        LensModel = findViewById(R.id.LensModel)
        LensTexture = findViewById(R.id.LensTexture)
    }
    fun cancel(view: View) {
        finish()
    }
}