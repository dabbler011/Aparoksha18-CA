package org.aparoksha.app18.ca

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import org.aparoksha.app18.ca.models.Image
import org.jetbrains.anko.toast

/**
 * Created by betterclever on 18/12/17.
 */

fun isUserSignedIn() = FirebaseAuth.getInstance().currentUser != null

fun fetchDBCurrentUser(): DatabaseReference? {
    val mFirebaseDB = FirebaseDatabase.getInstance()
    val mFirebaseAuth = FirebaseAuth.getInstance()

    return if (isUserSignedIn()) mFirebaseDB.getReference("users").child(mFirebaseAuth.currentUser!!.uid) else null
}

fun setIntentDetails(intent: Intent, fullName: String, collegeName: String, userName: String, gender: String,uri: Uri?): Intent {
    intent.putExtra("fullName", fullName)
    intent.putExtra("collegeName", collegeName)
    intent.putExtra("userName", userName)
    intent.putExtra("gender",gender)
    intent.putExtra("uri",uri)
    return intent
}

fun uploadFile(selectedImageUri: Uri,mFirebaseAuth: FirebaseAuth,mDBReference: DatabaseReference,pd: ProgressDialog,mActivity: Activity){
    val mFirebaseStorage = FirebaseStorage.getInstance()
    val mStorageReference = mFirebaseStorage.reference
    val userStorage = mStorageReference.child("user")
    val idReference = userStorage.child(mFirebaseAuth.currentUser!!.uid)
    val photoRef = idReference.child(System.currentTimeMillis().toString())
    photoRef.putFile(selectedImageUri).addOnSuccessListener(mActivity) {
        pd.dismiss()
        mDBReference.child("images").push().setValue(Image(photoRef.path, false))
        mActivity.toast("Successfully Uploaded")
    }.addOnFailureListener(mActivity) {
        pd.dismiss()
        mActivity.toast("Failed")
    }
}