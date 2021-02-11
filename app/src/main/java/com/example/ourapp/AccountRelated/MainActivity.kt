package com.example.ourapp.AccountRelated

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.example.ourapp.HomePageUser.UserHome
import com.example.ourapp.R
import com.example.ourapp.SplashScreen


import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(),View.OnClickListener {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var login_button: Button;
    private lateinit var username: EditText;
    private lateinit var password: EditText;
    private lateinit var create_account: TextView;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        setContentView(R.layout.activity_main)
        login_button = findViewById(R.id.login)
        username = findViewById(R.id.user_name)
        password = findViewById(R.id.password)
        create_account = findViewById(R.id.create_account)
        login_button.setOnClickListener(this)
        create_account.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.create_account -> {
                    var intent = Intent(this,CreateAccountActivity::class.java)
                    startActivity(intent)
                }
                R.id.login -> {
                    var user_name = username.text.toString()
                    var password = password.text.toString()
                    mAuth.signInWithEmailAndPassword(user_name, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                val user = mAuth.currentUser
                                if (user != null) {
                                    var intent = Intent(this,UserHome::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(
                                    baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // ...
                            }

                            // ...
                        }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        var currentuser = mAuth.currentUser
        if (currentuser != null) {
            var intent = Intent(this, UserHome::class.java)
            startActivity(intent)
            finish()
        }
    }
}