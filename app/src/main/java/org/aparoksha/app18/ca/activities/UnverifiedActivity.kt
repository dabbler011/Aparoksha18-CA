package org.aparoksha.app18.ca.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_unverified.*
import org.aparoksha.app18.ca.R

class UnverifiedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unverified)

        title = "Unverified Account"

        textView3.text = "Contact Team Aparoksha"

        textView4.text = "Your account is not verified yet.Contact team Aparoksha to get it verified ASAP."
    }
}
