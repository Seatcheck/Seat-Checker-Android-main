package app.rubbickcube.seatcheck.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import app.rubbickcube.seatcheck.R;

public class AppIntroFragmentThree extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_app_intro_fragment_three, container, false);
        Glide.with(getContext()).load(R.drawable.intro_three).into((ImageView) view.findViewById(R.id.intro_img_src_three));
        return view;
    }

}
