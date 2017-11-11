package org.aparoksha.app18.ca

import android.app.Activity
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
import android.util.Log
import android.graphics.Bitmap
import android.R.attr.data
import android.content.Context
import android.net.Uri
import android.support.v4.app.NotificationCompat.getExtras
import android.provider.MediaStore.Images
import org.jetbrains.anko.UI
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private lateinit var mFirebaseAuth : FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private var userName = ""
    private lateinit var mStorageReference :StorageReference
    private lateinit var mFirebaseStorage: FirebaseStorage
    private val RC_SIGN_IN = 1
    private val RC_PHOTO_PICKER = 2
    private val CAMERA_REQUEST = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()

        mStorageReference = mFirebaseStorage.getReference().child("cam")

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
            if(mUser!=null) {
                userName = mUser.toString()
            } else {
                userName = ""
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.logout) {
            AuthUI.getInstance().signOut(this)
            userName = ""
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("aaya","aaya")
        if (requestCode == RC_SIGN_IN) {
            if(resultCode == Activity.RESULT_OK) {
                toast("Signed In")
            } else {
                toast("Sign In Cancelled")
                finish()
            }
        } else if(requestCode == RC_PHOTO_PICKER && resultCode == Activity.RESULT_OK) {
            if(mFirebaseAuth.currentUser != null) {
                val SelectedImageUri = data.getData()
                toast(SelectedImageUri.toString())
                val userStorage = mStorageReference.child(mFirebaseAuth.currentUser!!.email.toString())
                val Reference  = userStorage.child(mFirebaseAuth.currentUser!!.uid)
                val photoRef = Reference.child(System.currentTimeMillis().toString())
                photoRef.putFile(SelectedImageUri).addOnSuccessListener(this) { taskSnapshot ->
                       toast("Successfully Uploaded")
                }

            } else {
                toast("Authentication Error")
                userName = ""
                AuthUI.getInstance().signOut(this)
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val extras = data.extras
            val imageBitmap = extras.get("data") as Bitmap
            val SelectedImageUri = getImageUri(applicationContext, imageBitmap)
            val userStorage = mStorageReference.child(mFirebaseAuth.currentUser!!.email.toString())
            val Reference  = userStorage.child(mFirebaseAuth.currentUser!!.uid)
            val photoRef = Reference.child(System.currentTimeMillis().toString())
            photoRef.putFile(SelectedImageUri).addOnSuccessListener(this) { taskSnapshot ->
                toast("Successfully Uploaded")
            }
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
