package com.example.ourapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.ourapp.AccountRelated.MainActivity

class SplashScreen : AppCompatActivity() {
    public val SPLASH_SCREEN_TIME = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed(Runnable {
            run {
                var intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, SPLASH_SCREEN_TIME.toLong())
    }
}