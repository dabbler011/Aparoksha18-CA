package org.aparoksha.app18.ca.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_leaderboard.*
import org.aparoksha.app18.ca.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.Query
import kotlinx.android.synthetic.main.layout_leaderboard_member.view.*
import org.aparoksha.app18.ca.models.User


/**
 * Created by sashank on 10/12/17.
 */

class LeaderboardFragment: Fragment() {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseDB: FirebaseDatabase
    private lateinit var DBQuery: Query


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDB = FirebaseDatabase.getInstance()

        if (mFirebaseAuth.currentUser != null) {
            DBQuery = mFirebaseDB.getReference("leaderboard").
                    orderByChild("score").limitToLast(50)

            recyclerview.layoutManager = LinearLayoutManager(context)
            recyclerview.adapter = LeaderboardAdapter(DBQuery)
        }
        else
            AuthUI.getInstance().signOut(activity)
    }
}

class LeaderboardViewHolder(private var mView: View) : RecyclerView.ViewHolder(mView) {

    fun bindView(user: User, position: Int) {
        mView.username.text = user.name;
        mView.score.text = user.score.toString()
        mView.rank.text = (position+1).toString()
    }

}

class LeaderboardAdapter(mRef : Query) : FirebaseRecyclerAdapter<User, LeaderboardViewHolder>(
        User::class.java,
        R.layout.layout_leaderboard_member,
        LeaderboardViewHolder::class.java,
        mRef) {

    override fun populateViewHolder(viewHolder: LeaderboardViewHolder?, model: User?, position: Int) {
        if (model != null) {
            viewHolder?.bindView(model,position)
        }
    }

}