package org.aparoksha.app18.ca.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintSet
import android.transition.TransitionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_splash.*
import org.aparoksha.app18.ca.R

class SplashActivity : AppCompatActivity() {

    private lateinit var mFirebaseAuth: FirebaseAuth

    val SPLASH_DISPLAY_LENGTH: Long = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(object : Runnable{
            override fun run() {
                val constraintSet = ConstraintSet()

                constraintSet.clone(this@SplashActivity,R.layout.secondary_splash)

                TransitionManager.beginDelayedTransition(splash)
                constraintSet.applyTo(splash)
            }
        }, 200)

        Handler().postDelayed(object : Runnable{
            override fun run() {
                mFirebaseAuth = FirebaseAuth.getInstance()

                if(mFirebaseAuth.currentUser != null) {
                    val i = Intent(this@SplashActivity,MainActivity::class.java)
                    startActivity(i)
                    finish()
                } else {
                    val i = Intent(this@SplashActivity,WelcomeActivity::class.java)
                    startActivity(i)
                    finish()
                }
            }
        }, SPLASH_DISPLAY_LENGTH)
    }
}
