package com.example.social_network

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class signup : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private companion object {
        private const val TAG = "SignUpActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_signup)
        auth = Firebase.auth
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
        }else{
            val eMailExtra = intent.getStringExtra("email")
            val passwordExtra = intent.getStringExtra("password")
            if(eMailExtra != null && passwordExtra != null){
                val email = findViewById<EditText>(R.id.edtTextEmail)
                val password = findViewById<EditText>(R.id.edtTextPassword)
                email.setText(eMailExtra)
                password.setText(passwordExtra)
            }
        }
    }

    fun swapToSignIn(view: View) {
        val intent: Intent = Intent(applicationContext, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun SignUp(view: View) {
        val username = findViewById<View>(R.id.edtTextUsername) as EditText
        val email = findViewById<View>(R.id.edtTextEmail) as EditText
        val password = findViewById<View>(R.id.edtTextPassword) as EditText
        val confirmPassword = findViewById<View>(R.id.confirmPassword) as EditText

        val usernameText = username.text.toString()
        val emailText = email.text.toString()
        val passwordText = password.text.toString()
        val confirmPasswordText = confirmPassword.text.toString()

        if (usernameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            username.error = "Please enter a username"
            email.error = "Please enter a email"
            password.error = "Please enter a password"
            confirmPassword.error = "Please enter a confirm password"
        } else if (passwordText != confirmPasswordText) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            password.error = "Passwords do not match"
            confirmPassword.error = "Passwords do not match"
        } else if (passwordText.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            password.error = "Password must be at least 6 characters"
            confirmPassword.error = "Password must be at least 6 characters"
        } else {
            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    val newUser = hashMapOf(
                        "username" to usernameText,
                        "email" to emailText,
                        "uid" to user?.uid,
                        "posts" to listOf<Any>()
                    )
                    db.collection("users").add(newUser)
                    val profileUpdates = userProfileChangeRequest { displayName = usernameText }
                    user?.updateProfile(profileUpdates)
                    user?.sendEmailVerification()?.addOnCompleteListener { task -> if (task.isSuccessful) { Toast.makeText(baseContext, "Verification email sent to ${user.email}", Toast.LENGTH_SHORT).show() } }
                    val intent: Intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    when ((e as FirebaseAuthException).errorCode) {
                        "ERROR_INVALID_EMAIL" -> Toast.makeText(this, "Invalid email", Toast.LENGTH_LONG).show()
                        "ERROR_WEAK_PASSWORD" -> Toast.makeText(this, "Password is too weak", Toast.LENGTH_LONG).show()
                        "ERROR_EMAIL_ALREADY_IN_USE" -> {
                            Toast.makeText(this, "Email already in use", Toast.LENGTH_LONG).show()
                            intent = Intent(applicationContext, login::class.java)
                            intent.putExtra("email", emailText)
                            intent.putExtra("password", passwordText)
                            startActivity(intent)
                        }
                        "ERROR_OPERATION_NOT_ALLOWED" -> Toast.makeText(this, "Operation not allowed, user disabled?", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
    fun signUpGoogle(view: View) {
    }
}