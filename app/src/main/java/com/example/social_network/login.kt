package com.example.social_network

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    val db = Firebase.firestore

    companion object {
        const val ADMOB_AD_UNIT_ID = "ca-app-pub-1839648225801792/8563792553"
        const val BUILDNUMBER = 7
        const val VersionNumber = 1
        const val TAG = "LogInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        googleAdMob()
    }

    private fun checkForUpdates(){
        db.collection("INFORMATION").document("BUILD").get()
            .addOnSuccessListener { document ->
                val newestBuild = document.data?.get("nr").toString().toFloat()
                val required = document.data?.get("required").toString().toBoolean()
                if (newestBuild > BUILDNUMBER){
                    if (required){
                        Toast.makeText(this, "You need to update the app to continue", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, UpdateRequired::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        if (newestBuild == BUILDNUMBER + 1f){
                            Toast.makeText(this, "There is a new optional major update available", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "There are new major updates available, you will have to get the newest version of the app, to be able to use it", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, UpdateRequired::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        db.collection("INFORMATION").document("VERSION").get()
            .addOnSuccessListener { document ->
                val newestVersion = document.data?.get("nr").toString().toFloat()
                if (newestVersion > VersionNumber){
                    if (newestVersion == VersionNumber + 1f){
                        Toast.makeText(this, "There is a new optional minor update available", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "There are new optional minor updates available", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun googleAdMob(){
        MobileAds.initialize(this) {}
        val adLoader = AdLoader.Builder(this, ADMOB_AD_UNIT_ID)
            .forNativeAd { nativeAd ->
                val background = ColorDrawable(-0x999a)
                val styles: NativeTemplateStyle =
                    com.google.android.ads.nativetemplates.NativeTemplateStyle.Builder()
                        .withMainBackgroundColor(background).build()
                val template: TemplateView = findViewById<TemplateView>(R.id.my_template)
                template.setStyles(styles)
                template.setNativeAd(nativeAd)
            }
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    public override fun onStart() {
        super.onStart()
        checkForUpdates()
        // Check if user is signed in
        val currentUser = auth.currentUser
        var valid: Boolean = true
        currentUser?.reload()?.exception?.let {
            Log.d(TAG, "Error reloading user: ${it.message}")
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            valid = false
        }
        if(currentUser != null && valid) {
            if (currentUser.isEmailVerified) {
                val intent: Intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                auth.signOut()
                Toast.makeText(this, "Please verify your email", Toast.LENGTH_LONG).show()
            }
        } else {
            val eMailExtra = intent.getStringExtra("email")
            val passwordExtra = intent.getStringExtra("password")
            if(eMailExtra != null && passwordExtra != null){
                val email: EditText = findViewById(R.id.edtTextEmail)
                val password: EditText = findViewById(R.id.edtTextPassword)
                email.setText(eMailExtra)
                password.setText(passwordExtra)
            }
        }
    }

    fun swapToSignUp(view: View) {
        val intent: Intent = Intent(applicationContext, signup::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun login(view: View) {
        val email = findViewById<View>(R.id.edtTextEmail) as EditText
        val password = findViewById<View>(R.id.edtTextPassword) as EditText

        val emailText = email.text.toString()
        val passwordText = password.text.toString()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            email.error = "Please enter an email"
            password.error = "Please enter a password"
            return
        }

        auth.signInWithEmailAndPassword(emailText, passwordText)
            .addOnSuccessListener {
                // Sign in success
                val user = auth.currentUser
                if (user!!.isEmailVerified){
                    Log.d(TAG, "signInWithEmail:success")
                    val intent: Intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    user.sendEmailVerification()
                        .addOnSuccessListener { auth.signOut() }
                        .addOnFailureListener { auth.signOut() }
                    Toast.makeText(this, "Please verify your email", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                when ((e as FirebaseAuthException).errorCode) {
                    "ERROR_INVALID_EMAIL" -> Toast.makeText(this, "Invalid email", Toast.LENGTH_LONG).show()
                    "ERROR_WRONG_PASSWORD" -> Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show()
                    "ERROR_USER_NOT_FOUND" -> {
                        val intent = Intent(applicationContext, signup::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra("email", emailText)
                        intent.putExtra("password", passwordText)
                        startActivity(intent)
                    }
                    "ERROR_USER_DISABLED" -> Toast.makeText(this, "User disabled", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signInGoogle(view: View) {

    }
}
