package org.aparoksha.app18.ca

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Created by betterclever on 18/12/17.
 */

class AparokshaCA : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseMessaging.getInstance().subscribeToTopic("all")
    }
}