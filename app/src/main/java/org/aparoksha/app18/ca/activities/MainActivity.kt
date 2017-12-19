package org.aparoksha.app18.ca.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import org.aparoksha.app18.ca.R
import org.aparoksha.app18.ca.isUserSignedIn
import org.aparoksha.app18.ca.models.User
import org.aparoksha.app18.ca.models.Image
import org.aparoksha.app18.ca.models.LeaderboardData
import org.aparoksha.app18.ca.uploadFile
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity(), AnkoLogger {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseDB: FirebaseDatabase
    private lateinit var mDBReference: DatabaseReference
    private lateinit var dbData: User
    private lateinit var mLeaderboardRef: DatabaseReference
    private lateinit var dialog: ProgressDialog

    private val RC_PHOTO_PICKER = 2
    private val CAMERA_REQUEST = 3

    private fun initDB() {
        dbData = User()
        mFirebaseAuth = FirebaseAuth.getInstance()

        mFirebaseDB = FirebaseDatabase.getInstance()
    }

    private fun setListeners() {

        upload.setOnClickListener({
            if (fab_menu.isOpened)
                fab_menu.close(true)
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Select Photo to be uploaded"), RC_PHOTO_PICKER)
        })

        camera.setOnClickListener({
            if (fab_menu.isOpened)
                fab_menu.close(true)

            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        })

        openScratchCardsButton.setOnClickListener({
            startActivity<ScratchCardsActivity>()
        })
    }

    private fun fetchInitialsTotalProgress() {
        mLeaderboardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                val list: MutableList<LeaderboardData> = mutableListOf()
                if (snapshot != null) {
                    snapshot.children.mapNotNullTo(list) {
                        it.getValue<LeaderboardData>(LeaderboardData::class.java)
                    }

                    var max: Long = 0
                    for (e in list) {
                        max = maxOf(e.score, max)
                    }

                    val myRef = mFirebaseDB.getReference("leaderboard").child(mFirebaseAuth.currentUser!!.uid)

                    myRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            if (dataSnapshot != null) {
                                val myPoints: LeaderboardData? = dataSnapshot.getValue(LeaderboardData::class.java)
                                if (myPoints != null) {
                                    if (myPoints.score == 0L) {
                                        totalProgress.progress = 0
                                    } else {
                                        totalProgress.progress = (myPoints.score * 100 / max).toInt()
                                    }
                                    pointsText.text = myPoints.score.toString() + " / " + max.toString()
                                    main.visibility = View.VISIBLE
                                    dialog.dismiss()
                                }
                            } else {
                                totalProgress.progress = 0
                                pointsText.text = "0 / " + max
                                main.visibility = View.VISIBLE
                                dialog.dismiss()
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                    })
                }
            }

            override fun onCancelled(p0: DatabaseError?) {
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main.visibility = View.INVISIBLE
        dialog = ProgressDialog.show(this, "Fetching User", "Loading...")
        initDB()
        setListeners()

        title = "My Dashboard"

        mDBReference = mFirebaseDB.getReference("users").child(mFirebaseAuth.currentUser!!.uid)
        mLeaderboardRef = mFirebaseDB.getReference("leaderboard")

        mDBReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    dbData = snapshot.getValue(User::class.java)!!
                    user.text = dbData.userName
                    scratchcardxp.max = 8
                    scratchcardxp.progress = (((dbData.totalPoints % 200) / 25) % 8L).toInt()
                    if (!dbData.accountVerified) {
                        val intent = Intent(this@MainActivity, UnverifiedActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError?) {
                // Shorthand Anko Logging ;)
                error(error)
            }
        })

        try {
            fetchInitialsTotalProgress()
        } catch (e: Exception) {
            totalProgress.progress = 0
            pointsText.text = "0 / 0"
            main.visibility = View.VISIBLE
            dialog.dismiss()
        }

        user.text = dbData.userName
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            AuthUI.getInstance().signOut(this)
            user.text = ""
            finish()
            return true
        } else if (item.itemId == R.id.uploadedImages) {
            startActivity<UploadsActivity>()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)



        if (requestCode == RC_PHOTO_PICKER && resultCode == Activity.RESULT_OK) {
            if (mFirebaseAuth.currentUser != null) {
                val pd = ProgressDialog.show(this, "Uploading File", "Processing...")
                val selectedImageUri = data!!.data

                uploadFile(selectedImageUri,mFirebaseAuth,mDBReference,pd,this)
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            if (mFirebaseAuth.currentUser != null) {
                val pd = ProgressDialog.show(this, "Uploading File", "Processing...")
                val extras = data!!.extras
                val imageBitmap = extras.get("data") as Bitmap
                val selectedImageUri = getImageUri(applicationContext, imageBitmap)

                uploadFile(selectedImageUri,mFirebaseAuth,mDBReference,pd,this)
            }
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    override fun onResume() {
        super.onResume()
        if (isUserSignedIn()) {
            user.text = dbData.userName
        }
    }

    override fun onBackPressed() {
        if (fab_menu.isOpened)
            fab_menu.close(true)
        else
            super.onBackPressed()
    }
}