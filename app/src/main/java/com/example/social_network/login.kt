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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private companion object {
        const val ADMOB_AD_UNIT_ID = "ca-app-pub-1839648225801792/8563792553"
        const val TAG = "LogInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_login)
        // Firebase
        auth = Firebase.auth
        // Admob
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
        // Buttons:

    }

    public override fun onStart() {
        super.onStart()
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
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
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
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    val intent: Intent = Intent(applicationContext, signup::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
    }

    fun signInGoogle(view: View) {

    }
}
