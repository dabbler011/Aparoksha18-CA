package org.aparoksha.app18.ca.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_uploads.*
import org.aparoksha.app18.ca.R
import org.aparoksha.app18.ca.adapters.UploadsAdapter

class UploadsActivity : AppCompatActivity() {

    private lateinit var mFirebaseStorage: FirebaseStorage
    private lateinit var mFirebaseDB: FirebaseDatabase
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var DBQuery: Query

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uploads)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDB = FirebaseDatabase.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()

        title = "Uploads"

        DBQuery = mFirebaseDB.getReference("users").
                    child(mFirebaseAuth.currentUser!!.uid).child("images")

        uploadsList.layoutManager = LinearLayoutManager(this)
        uploadsList.adapter = UploadsAdapter(DBQuery,mFirebaseStorage.reference,this)
    }
}
