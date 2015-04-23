package dk.aau.cs.giraf.categorymanager.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import dk.aau.cs.giraf.categorymanager.R;
import dk.aau.cs.giraf.dblib.models.Profile;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link dk.aau.cs.giraf.categorymanager.fragments.InitialFragmentSpecificUser.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link dk.aau.cs.giraf.categorymanager.fragments.InitialFragmentSpecificUser#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitialFragmentSpecificUser extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Profile profile;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitialFragment.
     */
    public static InitialFragmentSpecificUser newInstance(Profile profile) {
        InitialFragmentSpecificUser fragment = new InitialFragmentSpecificUser();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.profile = profile;
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
        View initialFragmentSpecificUser = inflater.inflate(R.layout.fragment_initial_specific_user, container, false);

        // Check if user is signed in (aka the fragment was created correctly)
        if(profile != null) {
            // Find the image associated with the profile
            Bitmap profileImage = profile.getImage();

            // Find the place to insert the image
            ImageView profilePicture = (ImageView) initialFragmentSpecificUser.findViewById(R.id.profile_picture);

            // Check if the profile have a profile picture
            if(profileImage != null) {
                // Update the profile picture
                profilePicture.setImageBitmap(profileImage);
            }
            else {
                // Set default image
                profilePicture.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.icon_default_citizen));
            }
        }

        return initialFragmentSpecificUser;
    }

    @Override
    public void onAttach(final Activity activity) {
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
