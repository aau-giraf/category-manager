package dk.aau.cs.giraf.categorymanager.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import dk.aau.cs.giraf.categorymanager.R;
import dk.aau.cs.giraf.categorymanager.showcase.ShowcaseManager;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.utilities.GirafScalingUtilities;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link dk.aau.cs.giraf.categorymanager.fragments.InitialFragmentSpecificUser.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link dk.aau.cs.giraf.categorymanager.fragments.InitialFragmentSpecificUser#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitialFragmentSpecificUser extends Fragment implements ShowcaseManager.ShowcaseCapable {

    private OnFragmentInteractionListener mListener;
    private Profile profile;

    /**
     * Used to showcase views
     */
    private ShowcaseManager showcaseManager;

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
        if (profile != null) {
            // Find the image associated with the profile
            Bitmap profileImage = profile.getImage();

            // Find the place to insert the image
            ImageView profilePicture = (ImageView) initialFragmentSpecificUser.findViewById(R.id.profile_picture);

            // Check if the profile have a profile picture
            if (profileImage != null) {
                // Update the profile picture
                profilePicture.setImageBitmap(profileImage);
            } else {
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

    @Override
    public void showShowcase() {

        final ListView categoryListView = (ListView) getActivity().findViewById(R.id.giraf_sidebar_container);

        // Targets for the Showcase
        final ViewTarget sideBarEmptyViewTarget = new ViewTarget(categoryListView.getEmptyView(), 1.0f);
        final ViewTarget sideBarFirstCategoryViewTarget = new ViewTarget(categoryListView.getChildAt(categoryListView.getFirstVisiblePosition()), 1.0f);

        // Create a relative location for the next button
        final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        final int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        // Calculate position for the help text
        final int textX = getActivity().findViewById(R.id.category_sidebar).getLayoutParams().width + margin * 2;
        final int textY = getResources().getDisplayMetrics().heightPixels / 2 + margin;

        showcaseManager = new ShowcaseManager();

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                if (categoryListView.getCount() == 0) {
                    showcaseView.setShowcase(sideBarEmptyViewTarget, true);
                    showcaseView.setContentTitle("Kategorier");
                    showcaseView.setContentText("Når du har oprettet kategorier kan de ses her");
                    showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                    showcaseView.setButtonPosition(lps);
                    showcaseView.setTextPostion(textX + (int) GirafScalingUtilities.convertDpToPixel(getActivity(), 14), textY);
                } else {
                    showcaseView.setShowcase(sideBarFirstCategoryViewTarget, true);
                    showcaseView.setContentTitle("Kategorier");
                    showcaseView.setContentText("Tryk på en kategori for at se dennes indhold");
                    showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                    showcaseView.setButtonPosition(lps);
                    showcaseView.setTextPostion(textX, textY);
                }
            }
        });

        showcaseManager.setOnDoneListener(new ShowcaseManager.OnDoneListener() {
            @Override
            public void onDone(ShowcaseView showcaseView) {
                showcaseManager = null;
            }
        });

        showcaseManager.start(getActivity());
    }

    @Override
    public synchronized void hideShowcase() {

        if (showcaseManager != null) {
            showcaseManager.stop();
            showcaseManager = null;
        }
    }

    @Override
    public synchronized void toggleShowcase() {

        if (showcaseManager != null) {
            hideShowcase();
        } else {
            showShowcase();
        }
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
