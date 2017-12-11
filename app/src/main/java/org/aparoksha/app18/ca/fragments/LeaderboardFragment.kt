package org.aparoksha.app18.ca.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_leaderboard.*
import org.aparoksha.app18.ca.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.Query
import org.aparoksha.app18.ca.models.Data


/**
 * Created by sashank on 10/12/17.
 */
class LeaderboardFragment: Fragment() {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseDB: FirebaseDatabase
    private lateinit var mDBReference: DatabaseReference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDB = FirebaseDatabase.getInstance()

        if (mFirebaseAuth.currentUser != null) {
            mDBReference = mFirebaseDB.getReference("users")

            recyclerview.layoutManager = LinearLayoutManager(context)
            recyclerview.adapter = LeaderboardAdapter(mDBReference)
            Log.d("Ada",mDBReference.toString())
        }
    }
}

class LeaderboardViewHolder(private var mView: View) : RecyclerView.ViewHolder(mView) {

    fun bindView(data: Data) {
        val username = mView.findViewById<View>(R.id.username) as TextView
        val points = mView.findViewById<View>(R.id.points) as TextView

        username.text = data.identifier;
        points.text = data.totalPoints.toString()
    }

}

class LeaderboardAdapter(private val mRef : DatabaseReference) : FirebaseRecyclerAdapter<Data, LeaderboardViewHolder>(
        Data::class.java,
        R.layout.layout_leaderboard_member,
        LeaderboardViewHolder::class.java,
        mRef) {

    override fun populateViewHolder(viewHolder: LeaderboardViewHolder?, model: Data?, position: Int) {
        if (model != null) {
            viewHolder?.bindView(model)
        }
    }

}