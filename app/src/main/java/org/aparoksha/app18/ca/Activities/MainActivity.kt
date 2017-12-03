package org.aparoksha.app18.ca.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.util.*
import android.graphics.Bitmap
import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Images
import com.google.firebase.database.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.aparoksha.app18.ca.Models.Data
import org.aparoksha.app18.ca.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.email
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var mFirebaseAuth : FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mStorageReference :StorageReference
    private lateinit var mFirebaseStorage: FirebaseStorage
    private lateinit var mFirebaeDB: FirebaseDatabase
    private lateinit var mDBReference: DatabaseReference
    private val RC_SIGN_IN = 1
    private lateinit var dbData: Data
    private val RC_PHOTO_PICKER = 2
    private val CAMERA_REQUEST = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbData = Data()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()
        mStorageReference = mFirebaseStorage.getReference()
        mFirebaeDB = FirebaseDatabase.getInstance()

        upload.setOnClickListener(View.OnClickListener { view ->
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/jpeg"
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                startActivityForResult(Intent.createChooser(intent, "Select Photo to be uploaded"), RC_PHOTO_PICKER)
        })

        camera.setOnClickListener(View.OnClickListener { view ->
            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        })

        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val mUser = firebaseAuth.currentUser
            if(mUser!=null) {} else {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(
                                        Arrays.asList<AuthUI.IdpConfig>(
                                                AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                                AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                .build(),
                        RC_SIGN_IN)
            }
        }

        scratch.setOnClickListener({
            val i = Intent(this,ScratchCardsActivity::class.java)
            startActivity(i)
        })

        if(mFirebaseAuth.currentUser != null) {
            mDBReference = mFirebaeDB.getReference("users").child(mFirebaseAuth.currentUser!!.uid)
            mDBReference.keepSynced(true)
            mDBReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.value != null) {
                        dbData = p0.getValue(Data::class.java)!!
                    } else {
                        dbData = Data()
                    }
                }
                override fun onCancelled(p0: DatabaseError?) {
                        //To change body of created functions use File | Settings | File Templates
                }
            })
        } else {
            AuthUI.getInstance().signOut(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.logout) {
            AuthUI.getInstance().signOut(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if(resultCode == Activity.RESULT_OK) {
                toast("Signed In")
                if(mFirebaseAuth.currentUser != null) {
                    mDBReference = mFirebaeDB.getReference("users").child(mFirebaseAuth.currentUser!!.uid)
                    mDBReference.keepSynced(true)
                }
                mDBReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.value != null){
                            dbData = p0.getValue(Data::class.java)!!
                        } else {
                            dbData = Data()
                        }
                    }
                    override fun onCancelled(p0: DatabaseError?) {
                         //To change body of created functions use File | Settings | File Templates.
                    }
                })

            } else {
                toast("Sign In Cancelled")
                finish()
            }
        } else if(requestCode == RC_PHOTO_PICKER && resultCode == Activity.RESULT_OK) {
            if(mFirebaseAuth.currentUser != null) {
                val pd = ProgressDialog.show(this,"Uploading File","Processing...")
                val SelectedImageUri = data!!.getData()

                val userStorage = mStorageReference.child("user")
                val idReference = userStorage.child(mFirebaseAuth.currentUser!!.uid)
                val photoRef = idReference.child(System.currentTimeMillis().toString())
                photoRef.putFile(SelectedImageUri).addOnSuccessListener(this) { taskSnapshot ->
                    pd.dismiss()
                    updateScore()
                    toast("Successfully Uploaded")
                }.addOnFailureListener(this) {
                    pd.dismiss()
                    toast("Failed")
                }
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            if(mFirebaseAuth.currentUser != null) {
                val pd = ProgressDialog.show(this, "Uploading File", "Processing...")
                val extras = data!!.extras
                val imageBitmap = extras.get("data") as Bitmap
                val SelectedImageUri = getImageUri(applicationContext, imageBitmap)

                val userStorage = mStorageReference.child("user")
                val idReference = userStorage.child(mFirebaseAuth.currentUser!!.uid)
                val photoRef = idReference.child(System.currentTimeMillis().toString())
                photoRef.putFile(SelectedImageUri).addOnSuccessListener(this) { taskSnapshot ->
                    pd.dismiss()
                    updateScore()
                    toast("Successfully Uploaded")
                }.addOnFailureListener(this) {
                    pd.dismiss()
                    toast("Failed")
                }
            }
        }
    }

    fun updateScore() {
        dbData.count++
        mDBReference.child("count").setValue(dbData.count)
        if (dbData.count == 1L) {
            if(mFirebaseAuth.currentUser!!.email == null) {
                mDBReference.child("identifier").setValue(mFirebaseAuth.currentUser!!.phoneNumber)
            } else {
                mDBReference.child("identifier").setValue(mFirebaseAuth.currentUser!!.email)
            }
            mDBReference.child("revealedCount").setValue(0)
            mDBReference.child("totalPoints").setValue(0)
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener (mAuthStateListener)
    }
}