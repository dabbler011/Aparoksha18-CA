package org.aparoksha.app18.ca

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_details.view.*

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        title = "Personal Details"

        submit.setOnClickListener({
            if (!collegeName.text.equals("") && !userName.text.equals("") && !fullName.text.equals("") && gender_group.checkedRadioButtonId != -1) {
                if(gender_group.male.isChecked) {
                    val intent = intent
                    intent.putExtra("fullName", fullName.text)
                    intent.putExtra("collegeName", collegeName.text)
                    intent.putExtra("userName", userName.text)
                    intent.putExtra("gender","male")
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    val intent = intent
                    intent.putExtra("fullName", fullName.text)
                    intent.putExtra("collegeName", collegeName.text)
                    intent.putExtra("userName", userName.text)
                    intent.putExtra("gender","female")
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })
    }
}