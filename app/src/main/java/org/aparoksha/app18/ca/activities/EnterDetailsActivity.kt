package org.aparoksha.app18.ca.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_details.view.*
import org.aparoksha.app18.ca.R
import org.aparoksha.app18.ca.setIntentDetails

class EnterDetailsActivity : AppCompatActivity() {

    private val RC_PHOTO_PICKER = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        title = "Personal Details"

        select_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Select Photo to be uploaded"), RC_PHOTO_PICKER)
        }

        submit.setOnClickListener({
            if (!collegeName.text.equals("") && !userName.text.equals("") && !fullName.text.equals("")
                    && gender_group.checkedRadioButtonId != -1 && imageUri != null) {
                if(male.isChecked) {
                    val intent: Intent= setIntentDetails(intent,fullName.text.toString(),
                            collegeName.text.toString(),userName.text.toString(),"male", imageUri)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    val intent: Intent= setIntentDetails(intent,fullName.text.toString(),
                            collegeName.text.toString(),userName.text.toString(),"female",imageUri)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_PHOTO_PICKER && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.data
                Glide.with(this)
                        .load(R.drawable.selected)
                        .into(imageView)
                imageView.visibility = View.VISIBLE
            }
        }
    }
}