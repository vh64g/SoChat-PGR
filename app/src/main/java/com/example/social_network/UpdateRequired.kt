package com.example.social_network

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class UpdateRequired : AppCompatActivity() {

    val db = Firebase.firestore

    //TextViews
    var updateName: TextView? = null
    var updateDescription: TextView? = null
    var updateChangeLog: TextView? = null

    //Button
    var updateButton: Button? = null

    var updateLink: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_update_required)
        findViews()
        getUpdate()
        clickUpdate()
    }

    private fun findViews() {
        updateName = findViewById(R.id.UpdateRequiredActivityUpdateName)
        updateDescription = findViewById(R.id.UpdateRequiredActivityUpdateDescription)
        updateChangeLog = findViewById(R.id.UpdateRequiredActivityUpdateChangeLog)
        updateButton = findViewById(R.id.UpdateRequiredActivityUpdateButton)
    }

    private fun getUpdate() {
        db.collection("INFORMATION").document("BUILD").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    updateName?.text = document.getString("Name")
                    updateDescription?.text = document.getString("Description")
                    updateChangeLog?.text = document.getString("ChangeLog")
                    updateLink = document.getString("UpdateLink")
                }
        }
    }

    private fun clickUpdate() {
        updateButton?.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(updateLink))
            startActivity(browserIntent)
        }
    }
}