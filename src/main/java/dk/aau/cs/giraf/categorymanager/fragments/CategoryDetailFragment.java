package dk.aau.cs.giraf.categorymanager.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.aau.cs.giraf.categorymanager.CategoryActivity;
import dk.aau.cs.giraf.categorymanager.PictogramAdapter;
import dk.aau.cs.giraf.categorymanager.R;
import dk.aau.cs.giraf.categorymanager.showcase.ShowcaseManager;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafConfirmDialog;
import dk.aau.cs.giraf.gui.GirafNotifyDialog;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.utilities.GirafScalingUtilities;

/**
 * Created on 25/03/15.
 */
public class CategoryDetailFragment extends Fragment implements ShowcaseManager.ShowcaseCapable, GirafConfirmDialog.Confirmation {

    private static final String IS_FIRST_RUN_KEY = "IS_FIRST_RUN_KEY_CATEGORY_DETAIL_FRAGMENT";

    // Bundle (Argument) Tags
    private static final String CATEGORY_ID_TAG = "CATEGORY_ID_TAG";
    private static final String IS_CHILD_CATEGORY_TAG = "IS_CHILD_CATEGORY_TAG";

    // Dialog fragment Tags
    private static final String CONFIRM_PICTOGRAM_DELETION_DIALOG_FRAGMENT_TAG = "CONFIRM_PICTOGRAM_DELETION_DIALOG_FRAGMENT_TAG";

    // Helper that will be used to fetch profiles
    private Helper helper;

    // Identifiers to use when handling dialogs
    public static final int DISABLED_BUTTON_HELP_DIALOG_RESPONSE = 101;

    private ViewGroup categoryDetailLayout;
    private GridView pictogramGrid;
    private ProgressBar loadingPictogramsIndicator;

    private LoadPictogramTask loadPictogramTask;

    private Set<Pictogram> selectedPictograms = null;
    private Category selectedCategory = null;

    /**
     * Used to showcase views
     */
    private ShowcaseManager showcaseManager;
    private boolean isFirstRun;


    private boolean isChildCategory;

    /**
     * Used in onResume and onPause for handling showcaseview for first run
     */
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    /**
     * Class used to load pictograms into the pictogram grid
     */
    private class LoadPictogramTask extends AsyncTask<Void, Void, PictogramAdapter> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Reset selected pictogram
            selectedPictograms.clear();
            pictogramGrid.setAdapter(null);


            // Set view when list is empty
            pictogramGrid.setEmptyView(new ProgressBar(CategoryDetailFragment.this.getActivity()));
            loadingPictogramsIndicator = (ProgressBar) categoryDetailLayout.findViewById(R.id.loading_pictograms_indicator);

            // Show the loading indicator
            loadingPictogramsIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected PictogramAdapter doInBackground(Void... params) {

            final Bundle arguments = getArguments();

            System.gc();

            if (arguments != null) {
                final long selectedCategoryId = arguments.getLong(CATEGORY_ID_TAG);

                selectedCategory = helper.categoryHelper.getCategoryById((int) selectedCategoryId);

                final List<Pictogram> pictogramList = helper.pictogramHelper.getPictogramsByCategory(selectedCategory);

                return new PictogramAdapter(pictogramList, CategoryDetailFragment.this.getActivity()) {
                    // Set pictogram to be selected if it is in the set of selected pictogram(s)
                    @Override
                    public View getView(final int position, final View convertView, final ViewGroup parent) {

                        final GirafPictogramItemView girafPictogram = (GirafPictogramItemView) super.getView(position, convertView, parent);

                        // Check if the pictogram is in the selected pictogram(s) set
                        if (selectedPictograms.contains(this.getItem(position))) {
                            girafPictogram.setChecked(true);
                        }

                        return girafPictogram;
                    }
                };
            }

            return null;
        }

        protected void onPostExecute(final PictogramAdapter result) {

            // Set view when list is empty
            pictogramGrid.setEmptyView(categoryDetailLayout.findViewById(R.id.empty_gridview_text));
            pictogramGrid.setAdapter(result);

            // Hide the loading indicator
            loadingPictogramsIndicator.setVisibility(View.GONE);
        }

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param categoryId the id of the category to show details for
     * @return A new instance of fragment CategoryDetailFragment.
     */
    public static CategoryDetailFragment newInstance(final long categoryId) {
        return newInstance(categoryId, false);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param categoryId      the id of the category to show details for
     * @param isChildCategory True if you want to hide category-management buttons (Copy to citizens / settings). Defaults to false.
     * @return
     */
    public static CategoryDetailFragment newInstance(final long categoryId, boolean isChildCategory) {
        CategoryDetailFragment fragment = new CategoryDetailFragment();
        fragment.isChildCategory = isChildCategory;
        Bundle args = new Bundle();
        args.putLong(CATEGORY_ID_TAG, categoryId);
        args.putBoolean(IS_CHILD_CATEGORY_TAG, isChildCategory);
        fragment.setArguments(args);
        return fragment;
    }

    /*
     * Methods for fragment lifecycle below
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Helper that will be used to fetch profiles
        helper = new Helper(this.getActivity());

        selectedPictograms = new HashSet<Pictogram>();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        categoryDetailLayout = (ViewGroup) inflater.inflate(R.layout.fragment_category_detail, container, false);

        pictogramGrid = (GridView) categoryDetailLayout.findViewById(R.id.pictogram_gridview);

        pictogramGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find the selected pictogram
                Pictogram selectedPictogram = (Pictogram) pictogramGrid.getAdapter().getItem(position);

                // If the pictogram is in the selected set remove it
                if (selectedPictograms.contains(selectedPictogram)) {
                    // Remove the pictogram to the selected pictogram(s)
                    selectedPictograms.remove(selectedPictogram);
                }
                // If the pictogram is not in the selected set add it
                else {
                    // Add the pictogram to the selected pictogram(s)
                    selectedPictograms.add(selectedPictogram);
                }

                // Update the UI accordingly to above changes
                ((GirafPictogramItemView) view).toggle();
            }
        });

        // Find the category-management buttons and hide them if the current category is a child-category
        if (isChildCategory) {
            // Hide the copy categories to user-button
            final GirafButton copyToUserButton = (GirafButton) categoryDetailLayout.findViewById(R.id.userSettingsButton);
            copyToUserButton.setEnabled(false);

            // Help the user to realize that it is not possible to copy child-categories
            copyToUserButton.setOnDisabledClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    GirafNotifyDialog notifyDialog = GirafNotifyDialog.newInstance(getString(R.string.disabled_button_notification_dialog_title), getString(R.string.disabled_button_notification_dialog_body_copy), CategoryActivity.NOTIFICATION_DIALOG_DO_NOTHING);
                    notifyDialog.show(getActivity().getSupportFragmentManager(), "" + CategoryActivity.NOTIFICATION_DIALOG_DO_NOTHING);
                }
            });

            // Hide the settings button
            final GirafButton categorySettingsButton = (GirafButton) categoryDetailLayout.findViewById(R.id.categorySettingsButton);
            categorySettingsButton.setEnabled(false);

            // Help the user to realize that it is not possible to change settings for a child-category
            categorySettingsButton.setOnDisabledClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    GirafNotifyDialog notifyDialog = GirafNotifyDialog.newInstance(getString(R.string.disabled_button_notification_dialog_title), getString(R.string.disabled_button_notification_dialog_body_settings), CategoryActivity.NOTIFICATION_DIALOG_DO_NOTHING);
                    notifyDialog.show(getActivity().getSupportFragmentManager(), "" + CategoryActivity.NOTIFICATION_DIALOG_DO_NOTHING);
                }
            });
        }

        return categoryDetailLayout;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start loading the pictograms of this category when the fragment is started
        loadPictograms();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if this is the first run of the app
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        this.isFirstRun = prefs.getBoolean(IS_FIRST_RUN_KEY, true);

        // If it is the first run display ShowcaseView
        if (isFirstRun) {
            getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    showShowcase();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(IS_FIRST_RUN_KEY, false);
                    editor.commit();

                    synchronized (CategoryDetailFragment.this) {
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

        synchronized (CategoryDetailFragment.this) {
            if (globalLayoutListener != null)
                getView().getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
            globalLayoutListener = null;
        }

        if (showcaseManager != null) {
            showcaseManager.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // In case the loadPictogramTask didn't complete before this fragment is destroyed; close it.
        if (loadPictogramTask != null) {
            loadPictogramTask.cancel(true);
        }
    }

    /*
     * Methods required from interfaces below
     */

    /**
     * Will be called whenever a confirm dialog is handled
     */
    @Override
    public void confirmDialog(final int methodID) {

        switch (methodID) {
            case CategoryActivity.CONFIRM_PICTOGRAM_DELETION_METHOD_ID: {

                // Remove the specific pictograms
                for (Pictogram selectedPictogram : selectedPictograms) {
                    helper.pictogramCategoryHelper.removePictogramCategory(selectedCategory.getId(), selectedPictogram.getId());
                }

                // Reload the list of pictograms
                loadPictograms();
            }
        }
    }

    public synchronized void loadPictograms()
    {
        if(loadPictogramTask != null)
        {
            loadPictogramTask.cancel(true);
        }

        loadPictogramTask = new LoadPictogramTask();
        loadPictogramTask.execute();
    }

    @Override
    public void showShowcase() {

        // Create a relative location for the next button
        final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        final int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        // Calculate position for the help text
        final int textX = getActivity().findViewById(R.id.category_sidebar).getLayoutParams().width + margin * 2;
        final int textY = getResources().getDisplayMetrics().heightPixels / 2 + margin;

        // Create a relative location for the next button
        final RelativeLayout.LayoutParams rightButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rightButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightButtonParams.setMargins(margin, margin, margin, margin);

        // Create a relative location for the next button
        final RelativeLayout.LayoutParams centerRightButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        centerRightButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
        centerRightButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        centerRightButtonParams.setMargins(margin, margin, margin, margin);


        showcaseManager = new ShowcaseManager();

        // Add showcase for copyToUserButton
        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                final ViewTarget copyToUserButtonTarget = new ViewTarget(R.id.userSettingsButton, getActivity());

                showcaseView.setShowcase(copyToUserButtonTarget, true);
                showcaseView.setContentTitle(getString(R.string.copy_category_to_user_button_showcase_help_titel_text));
                showcaseView.setContentText(getString(R.string.copy_category_to_user_button_showcase_help_content_text));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(rightButtonParams);

                final int textXPosition = (int) GirafScalingUtilities.convertDpToPixel(getActivity(), 220);
                final int textYPosition = copyToUserButtonTarget.getPoint().y - (int) GirafScalingUtilities.convertDpToPixel(getActivity(), 200);
                showcaseView.setTextPostion(textXPosition, textYPosition);
            }
        });

        // Add showcase for categorySettingsButton
        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                final ViewTarget categorySettingsButtonTarget = new ViewTarget(R.id.categorySettingsButton, getActivity());

                showcaseView.setShowcase(categorySettingsButtonTarget, true);
                showcaseView.setContentTitle(getString(R.string.pictogram_settings_button_showcase_help_titel_text));
                showcaseView.setContentText(getString(R.string.pictogram_settings_button_showcase_help_content_text));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(rightButtonParams);

                final int textXPosition = categorySettingsButtonTarget.getPoint().x;
                final int textYPosition = categorySettingsButtonTarget.getPoint().y - (int) GirafScalingUtilities.convertDpToPixel(getActivity(), 200);
                showcaseView.setTextPostion(textXPosition, textYPosition);
            }
        });

        // Add showcase for deletePictogramButton
        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                final ViewTarget deletePictogramButtonTarget = new ViewTarget(R.id.deletePictogramButton, getActivity());

                showcaseView.setShowcase(deletePictogramButtonTarget, true);
                showcaseView.setContentTitle(getString(R.string.delete_pictogram_button_showcase_help_titel_text));
                showcaseView.setContentText(getString(R.string.delete_pictogram_button_showcase_help_content_text));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(centerRightButtonParams);

                final View deletePictogramButton = categoryDetailLayout.findViewById(R.id.deletePictogramButton);

                final int textXPosition = deletePictogramButtonTarget.getPoint().x - deletePictogramButton.getWidth() * 3;
                final int textYPosition = deletePictogramButtonTarget.getPoint().y - (int) GirafScalingUtilities.convertDpToPixel(getActivity(), 200);
                showcaseView.setTextPostion(textXPosition, textYPosition);
            }
        });

        // Add showcase for addPictogramButton
        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                final ViewTarget addPictogramButtonTarget = new ViewTarget(R.id.addPictogramButton, getActivity());

                showcaseView.setShowcase(addPictogramButtonTarget, true);
                showcaseView.setContentTitle(getString(R.string.add_pictogram_button_showcase_help_titel_text));
                showcaseView.setContentText(getString(R.string.add_pictogram_button_showcase_help_content_text));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(centerRightButtonParams);
                //showcaseView.setTextPostion();
            }
        });

        // Add showcase for either empty_gridview_text or the first pictogram in the grid (Depends if there is a pictogram in the current category)
        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                int[] categoryDetailLayoutPositionOnScreen = new int[2];
                categoryDetailLayout.getLocationOnScreen(categoryDetailLayoutPositionOnScreen);
                showcaseView.setContentTitle(getString(R.string.pictogram_grid_showcase_help_titel_text));

                if (pictogramGrid.getCount() == 0) {
                    final ViewTarget pictogramGridTarget = new ViewTarget(R.id.empty_gridview_text, getActivity(), 1.3f);
                    showcaseView.setShowcase(pictogramGridTarget, false);

                    showcaseView.setContentText(getString(R.string.pictogram_grid_empty_showcase_help_content_text));

                    // Calculate the position of the help text
                    final int textXPosition = categoryDetailLayoutPositionOnScreen[0] + margin * 2;
                    final int textYPosition = categoryDetailLayoutPositionOnScreen[1] + margin * 2;

                    showcaseView.setTextPostion(textXPosition, textYPosition);

                } else {
                    final ViewTarget pictogramTarget = new ViewTarget(pictogramGrid.getChildAt(0), 1.3f);
                    showcaseView.setShowcase(pictogramTarget, true);
                    showcaseView.setContentText(getString(R.string.pictogram_grid_pictogram_showcase_help_content_text));

                    // Calculate the position of the help text
                    final int textXPosition = (int) (categoryDetailLayoutPositionOnScreen[0] * 2.5);
                    final int textYPosition = (int) (categoryDetailLayoutPositionOnScreen[1] * 1.5 + margin * 2);

                    showcaseView.setTextPostion(textXPosition, textYPosition);
                }
                if (!isFirstRun) {
                    showcaseView.setStyle(R.style.GirafLastCustomShowcaseTheme);
                } else {
                    showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                }

                showcaseView.setHideOnTouchOutside(true);
                showcaseView.setButtonPosition(rightButtonParams);

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
                isFirstRun = false;
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

    public Set<Pictogram> getSelectedPicotgrams() {
        return selectedPictograms;
    }

}
