package org.aparoksha.app18.ca.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.clock.scratch.ScratchView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.aparoksha.app18.ca.R;

/**
 * Created by sashank on 12/11/17.
 */

public class NewCardFragment extends Fragment {
    @Nullable

    ScratchView mScratchView;
    LottieAnimationView animationView;
    FrameLayout cardFrame;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_card,container,false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mScratchView = view.findViewById(R.id.scratch_view);
        animationView = view.findViewById(R.id.animationView);
        cardFrame = view.findViewById(R.id.card_frame);

        String key="";
        int points=0;

        if(this.getArguments().get("reference") != null)
            key = this.getArguments().get("reference").toString();

        if(this.getArguments().get("points") != null)
            points = Integer.parseInt(this.getArguments().get("points").toString());

        final DatabaseReference myRef = FirebaseDatabase.getInstance().
                getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("cards")
                .child(key);

        mScratchView.setEraserSize(100.0F);
        mScratchView.setWatermark(R.drawable.ic_logo);

        final int finalPoints = points;
        mScratchView.setEraseStatusListener(new ScratchView.EraseStatusListener() {
            @Override
            public void onProgress(final int percent) {
                if(percent > 40 && percent != 100) {
                    mScratchView.clear();
                    showResultScratch(finalPoints);
                    myRef.child("revealed").setValue(true);
                    if(finalPoints != 0)
                        ((TextView) view.findViewById(R.id.textView)).
                                setText("You won " + Integer.toString(finalPoints) + " Points!!");
                    else
                        ((TextView) view.findViewById(R.id.textView)).
                                setText("Bad luck. Come back soon !!");
                }
            }

            @Override
            public void onCompleted(View view) {}
        });

    }

    private void showResultScratch(int generatedPoint){
        cardFrame.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);

        if(generatedPoint != 0)
            animationView.setAnimation("box_gift.json");
        else
            animationView.setAnimation("box_empty.json");

        animationView.playAnimation();
        animationView.loop(true);
    }

}