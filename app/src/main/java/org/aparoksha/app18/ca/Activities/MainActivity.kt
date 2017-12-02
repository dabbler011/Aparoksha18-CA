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
import android.util.Log
import android.graphics.Bitmap
import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Images
import com.google.firebase.database.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.aparoksha.app18.ca.Models.Data
import org.aparoksha.app18.ca.R
import org.jetbrains.anko.UI
import org.jetbrains.anko.doAsync
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
    private val RC_PHOTO_PICKER = 2
    private val CAMERA_REQUEST = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                if(mFirebaseAuth.currentUser!!.email != null) {
                    mDBReference = mFirebaeDB.getReference(mFirebaseAuth.currentUser!!.email!!.replace(".", "")
                            .replace("[", "").replace("#", "").replace("]", ""))
                } else if(mFirebaseAuth.currentUser!!.phoneNumber != null){
                    mDBReference = mFirebaeDB.getReference(mFirebaseAuth.currentUser!!.phoneNumber)
                } else {
                    AuthUI.getInstance().signOut(this)
                }
                mDBReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {
                         //To change body of created functions use File | Settings | File Templates.
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
                var path:String

                if(mFirebaseAuth.currentUser!!.email !=null) {
                    path = mFirebaseAuth.currentUser!!.email.toString().replace(".", "")
                            .replace("[", "").replace("#", "").replace("]", "")
                } else if (mFirebaseAuth.currentUser!!.phoneNumber != null) {
                    path = mFirebaseAuth.currentUser!!.phoneNumber.toString()
                } else {
                    toast("Authentication Error")
                    return
                }

                val userStorage = mStorageReference.child(path)
                val photoRef = userStorage.child(System.currentTimeMillis().toString())
                photoRef.putFile(SelectedImageUri).addOnSuccessListener(this) { taskSnapshot ->
                    pd.dismiss()
                    getScore(path)
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
                var path:String

                if(mFirebaseAuth.currentUser!!.email !=null) {
                    path = mFirebaseAuth.currentUser!!.email.toString().replace(".", "")
                            .replace("[", "").replace("#", "").replace("]", "")
                } else if (mFirebaseAuth.currentUser!!.phoneNumber != null) {
                    path = mFirebaseAuth.currentUser!!.phoneNumber.toString()
                } else {
                    toast("Authentication Error")
                    return
                }

                val userStorage = mStorageReference.child(path)
                val photoRef = userStorage.child(System.currentTimeMillis().toString())
                photoRef.putFile(SelectedImageUri).addOnSuccessListener(this) { taskSnapshot ->
                    pd.dismiss()
                    toast("Successfully Uploaded")
                }.addOnFailureListener(this) {
                    pd.dismiss()
                    toast("Failed")
                }
            }
        }
    }

    fun updateScore(value: Long,path: String) {
        mDBReference = mFirebaeDB.getReference(path)
        mDBReference.child("count").setValue(value+1)
        if((value+1) % 5 == 0L){

        }
    }

    fun getScore(path: String){
        doAsync {
            val client = OkHttpClient()

            val request = Request.Builder()
                    .url("https://aporoksha18-ca.firebaseio.com/" + path + ".json")
                    .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                //val updatesList: ArrayList<Notification> = ArrayList()
                try {
                    val body = JSONObject(response.body()?.string())
                    val keys = body.keys()
                    if (keys.hasNext()) {
                        val key = keys.next().toString()
                        var data = Data()
                        data.value = body.getLong(key)
                        if (data.value != 0L) {
                            uiThread { updateScore(data.value,path) }
                        }
                    }
                } catch (e: Exception){
                    uiThread { updateScore(0L,path) }
                }
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
