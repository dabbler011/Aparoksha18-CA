package org.aparoksha.app18.ca.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clock.scratch.ScratchView;

import org.aparoksha.app18.ca.R;

/**
 * Created by sashank on 12/11/17.
 */

public class NewCardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_card,container,false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ScratchView mScratchView = view.findViewById(R.id.scratch_view);
        mScratchView.setMaxPercent(40);
        mScratchView.setEraserSize(100.0F);
        mScratchView.setMaskColor(-0xff8c1a);
        mScratchView.setWatermark(-1);

        mScratchView.setEraseStatusListener(new ScratchView.EraseStatusListener() {
            @Override
            public void onProgress(final int percent) {

                view.findViewById(R.id.outer_frame).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(percent > 30 && percent != 100)
                            mScratchView.clear();
                    }
                });
            }

            @Override
            public void onCompleted(View view) {}
        });

    }
}