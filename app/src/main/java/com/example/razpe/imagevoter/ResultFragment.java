package com.example.razpe.imagevoter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class ResultFragment extends Fragment {

    private Bundle info;
    private OnFragmentInteractionListener mListener;
    private Button button;

    public ResultFragment() {
        // Required empty public constructor
    }

    public static ResultFragment newInstance() {
        ResultFragment fragment = new ResultFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        this.info = bundle;
        if(bundle != null) {
            System.out.println("DATA:");
            System.out.println(bundle.getInt("fakeThinkersPercent", 0));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result, container, false);
        button = v.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println("CLOSE PLS");
                getActivity().getFragmentManager().popBackStack();

            }
        });

        ImageView imgView = v.findViewById(R.id.imageView3);
        imgView.setImageBitmap(Voter.currentImage);

        TextView youThink = v.findViewById(R.id.textViewYouThink);
        if(this.info.getBoolean("thinksIsReal", false) == true){
            youThink.setText("You think this is real!");
        } else { youThink.setText("You think this is fake!"); }

        TextView yourConfidence = v.findViewById(R.id.textViewYourConfidence);
        yourConfidence.setText("with confidence: " + this.info.getInt("thinksWithConfidence", 0));

        TextView fakePeople = v.findViewById(R.id.textViewFakeVotes);
        fakePeople.setText(this.info.getInt("fakeThinkersPercent", 0) + "% of people think this is fake!");

        TextView fakeConfidence = v.findViewById(R.id.textViewFakeConfidence);
        fakeConfidence.setText("with confidence: " + this.info.getInt("fakeThinkersConfidence", 0));

        TextView realPeople = v.findViewById(R.id.textViewRealVotes);
        realPeople.setText(this.info.getInt("realThinkersPercent", 0) + "% of people think this is real!");

        TextView realConfidence = v.findViewById(R.id.textViewRealConfidence);
        realConfidence.setText("with confidence: " + this.info.getInt("realThinkersConfidence", 0));

        TextView isActuallyReal = v.findViewById(R.id.textViewIsActuallyReal);
        if(this.info.getInt("isActuallyReal", 0) == 1){
            isActuallyReal.setText("THIS PICTURE IS REAL!");
        } else {isActuallyReal.setText("THIS PICTURE IS FAKE!"); }

        if(this.info.getBoolean("isLastImage", false) == true){
            this.button.setText("FINISH");
        } else { this.button.setText("NEXT"); }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        mListener = (OnFragmentInteractionListener) activity;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onFragmentClosed();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentClosed();
    }

}
