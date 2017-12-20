package org.aparoksha.app18.ca.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.paolorotolo.appintro.ISlidePolicy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_details_intro.*
import org.aparoksha.app18.ca.R
import org.aparoksha.app18.ca.activities.EnterDetailsActivity
import org.aparoksha.app18.ca.isUserSignedIn
import org.aparoksha.app18.ca.models.User
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

/**
 * Created by betterclever on 18/12/17.
 */
class DetailsIntroFragment : Fragment(), ISlidePolicy {

    private val RC_ENTER_DETAILS: Int = 23
    private var areDetailsAvailable = false
    private var isLoading = true
    private var isUploading = false
    private lateinit var detailsListener: ValueEventListener
    private lateinit var userRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(context).inflate(R.layout.fragment_details_intro, container, false)
    }


    override fun onResume() {
        super.onResume()

        if (isUserSignedIn()) {
            val fbUser = FirebaseAuth.getInstance().currentUser!!
            updateViewByStatus()

            enterDetailsButton.setOnClickListener {
                startActivityForResult(activity?.intentFor<EnterDetailsActivity>(), RC_ENTER_DETAILS)
            }

            userRef = FirebaseDatabase.getInstance().getReference("/users/${fbUser.uid}")

            detailsListener = object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError?) {
                    areDetailsAvailable = false
                    isLoading = false
                    updateViewByStatus()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    isLoading = false
                    if (user != null) {
                        areDetailsAvailable = (user.userName != "")
                    }
                    updateViewByStatus()
                }
            }

            userRef.addValueEventListener(detailsListener)
        }
    }

    override fun isPolicyRespected() = areDetailsAvailable


    override fun onUserIllegallyRequestedNextPage() {
        activity?.toast("Please Enter Details First!!")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_ENTER_DETAILS) {
            if (resultCode == Activity.RESULT_OK) {

                val fbUser = FirebaseAuth.getInstance().currentUser!!
                val userRef = FirebaseDatabase.getInstance().getReference("/users/${fbUser.uid}")
                val storageRef = FirebaseStorage.getInstance().getReference("/user/${fbUser.uid}/ID_CARD")

                if (data != null) {
                    val extras = data.extras
                    val user = User(
                            fullName = extras["fullName"].toString(),
                            userName = extras["userName"].toString(),
                            collegeName = extras["collegeName"].toString(),
                            gender = extras["gender"].toString(),
                            totalPoints = 0,
                            tokenFCM = FirebaseInstanceId.getInstance().token,
                            identifier = if (fbUser.email == null) fbUser.phoneNumber!! else fbUser.email!!
                    )

                    val uri: Uri = extras["uri"] as Uri

                    isLoading = true
                    isUploading = true
                    updateViewByStatus()

                    storageRef.putFile(uri).addOnCompleteListener {
                        userRef.setValue(user).addOnCompleteListener {
                            areDetailsAvailable = true
                            isLoading = false
                            isUploading = false
                            updateViewByStatus()
                        }
                    }
                }
            }
        }
    }

    fun updateViewByStatus() {
        if(isUploading) {
            isLoading = true
        }
        if (isLoading) {
            progressView.visibility = View.VISIBLE
            availableTextView.visibility = View.GONE
            enterDetailsButton.visibility = View.GONE
        } else {
            progressView.visibility = View.GONE
            if (areDetailsAvailable) {
                enterDetailsButton.visibility = View.GONE
                availableTextView.visibility = View.VISIBLE
            } else {
                enterDetailsButton.visibility = View.VISIBLE
                availableTextView.visibility = View.GONE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (::userRef.isInitialized) {
            userRef.removeEventListener(detailsListener)
        }
    }


}