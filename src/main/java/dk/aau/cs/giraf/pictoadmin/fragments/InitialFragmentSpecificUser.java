package dk.aau.cs.giraf.pictoadmin.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dk.aau.cs.giraf.cat.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link dk.aau.cs.giraf.pictoadmin.fragments.InitialFragmentSpecificUser.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link dk.aau.cs.giraf.pictoadmin.fragments.InitialFragmentSpecificUser#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitialFragmentSpecificUser extends Fragment {

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitialFragment.
     */
    public static InitialFragmentSpecificUser newInstance() {
        InitialFragmentSpecificUser fragment = new InitialFragmentSpecificUser();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public InitialFragmentSpecificUser() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();

        if (arguments != null) {
            // Add arguments here if needed. Do it this way:
            // mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_initial_specific_user, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onCreateCategoryButtonClicked(View view);
    }

}