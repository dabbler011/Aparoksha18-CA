package org.aparoksha.app18.ca.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.aparoksha.app18.ca.fragments.NewCardFragment
import org.aparoksha.app18.ca.R
import org.aparoksha.app18.ca.fragments.ScratchCardFragment

/**
 * Created by sashank on 9/11/17.
 */

class ScratchCardsActivity : AppCompatActivity() {

    private var pointsRecieved = -1;

    fun getPointsRecieved(): Int {
        return pointsRecieved
    }

    fun setPointsRecieved(x: Int) {
        pointsRecieved = x;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch)
        title = "Cards"

        val ft =supportFragmentManager.beginTransaction()
        ft.add(R.id.frame,ScratchCardFragment())
        ft.commit()
    }

    override fun onBackPressed() {
        val fragmentList = supportFragmentManager.fragments

        if(fragmentList.size != 0) {
            val activeFragment = fragmentList[fragmentList.size - 1]
            if (activeFragment is ScratchCardFragment) {
                super.onBackPressed()
            } else if (activeFragment is NewCardFragment) {
                supportFragmentManager.beginTransaction().
                        replace(R.id.frame, ScratchCardFragment()).commit()
            }
            val data = Intent()
            data.data = Uri.parse(Integer.toString(pointsRecieved))
            if(pointsRecieved == -1)
                setResult(Activity.RESULT_CANCELED, data)
            else
                setResult(Activity.RESULT_OK, data)
            finish()
        }
        else
            super.onBackPressed()
    }
}