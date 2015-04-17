package dk.aau.cs.giraf.cat.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import dk.aau.cs.giraf.cat.R;
import dk.aau.cs.giraf.cat.showcase.ShowcaseManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InitialFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InitialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitialFragment extends Fragment implements OnShowcaseEventListener, ShowcaseManager.ShowcaseCapable {

    private static final String IS_FIRST_RUN_KEY = "IS_FIRST_RUN_KEY_INITIAL_FRAGMENT";

    private ShowcaseManager showcaseManager;
    private OnFragmentInteractionListener mListener;
    private boolean isFirstRun;

    /**
     * Used in onResume and onPause for handling showcaseview for first run
     */
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitialFragment.
     */
    public static InitialFragment newInstance() {
        InitialFragment fragment = new InitialFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public InitialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
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
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_initial, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if this is the first run of the app
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        this.isFirstRun = prefs.getBoolean(IS_FIRST_RUN_KEY, true);

        // If it is the first run display ShowcaseView
        if (isFirstRun) {
            getView().getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    showShowcase();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(IS_FIRST_RUN_KEY, false);
                    editor.commit();

                    synchronized (InitialFragment.this) {
                        globalLayoutListener = null;
                        getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        synchronized (InitialFragment.this) {
            if (globalLayoutListener != null)
                getView().getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
            globalLayoutListener = null;
        }

        if (showcaseManager != null) {
            showcaseManager.stop();
        }
    }


    /*
    * Shows a quick walkthrough of the functionality
    * */

    @Override
    public synchronized void showShowcase() {

        // Targets for the Showcase
        final ViewTarget createCategoryTarget = new ViewTarget(R.id.category_create_button, getActivity(), 1.5f);
        final ViewTarget adminCitizenTarget = new ViewTarget(R.id.administrate_citizen_button, getActivity(), 1.5f);

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

                showcaseView.setShowcase(createCategoryTarget, true);
                showcaseView.setContentTitle(getString(R.string.create_category_button_showcase_help_titel_text));
                showcaseView.setContentText(getString(R.string.create_category_button_showcase_help_content_text));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {
                showcaseView.setShowcase(adminCitizenTarget, true);
                showcaseView.setContentTitle(getString(R.string.administrate_citizen_button_showcase_help_titel_text));
                showcaseView.setContentText(getString(R.string.administrate_citizen_button_showcase_help_content_text));

                if (!isFirstRun) {
                    showcaseView.setStyle(R.style.GirafLastCustomShowcaseTheme);
                } else {
                    showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                }
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        if (isFirstRun) {
            final ViewTarget helpButtonTarget = new ViewTarget(getActivity().getActionBar().getCustomView().findViewById(R.id.help_button), 1.5f);

            showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
                @Override
                public void configShowCaseView(final ShowcaseView showcaseView) {
                    showcaseView.setShowcase(helpButtonTarget, true);
                    showcaseView.setContentTitle("Hjælpe knap");
                    showcaseView.setContentText("Hvis du bliver i tvivl kan du altid få hjælp her");
                    showcaseView.setStyle(R.style.GirafLastCustomShowcaseTheme);
                    showcaseView.setButtonPosition(lps);
                    showcaseView.setTextPostion(textX, textY);
                }
            });
        }

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
    public void onShowcaseViewHide(final ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewDidHide(final ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewShow(final ShowcaseView showcaseView) {

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
