package dk.aau.cs.giraf.cat.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.cat.CategoryActivity;
import dk.aau.cs.giraf.cat.PictogramAdapter;
import dk.aau.cs.giraf.cat.R;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafConfirmDialog;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * Created on 25/03/15.
 */
public class CategoryDetailFragment extends Fragment implements GirafConfirmDialog.Confirmation {

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

    private LoadPictogramTask loadPictogramTask;

    private Pictogram selectedPictogram = null;
    private Category selectedCategory = null;

    private boolean isChildCategory;

    /**
     * Class used to load pictograms into the pictogram grid
     */
    private class LoadPictogramTask extends AsyncTask<Void, Void, List<Pictogram>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Reset selected pictogram
            selectedPictogram = null;

            // Set view when list is empty
            pictogramGrid.setEmptyView(new ProgressBar(CategoryDetailFragment.this.getActivity()));
        }

        @Override
        protected List<Pictogram> doInBackground(Void... params) {
            final Bundle arguments = getArguments();

            if (arguments != null) {
                final long selectedCategoryId = arguments.getLong(CATEGORY_ID_TAG);

                selectedCategory = helper.categoryHelper.getCategoryById((int) selectedCategoryId);

                final List<Pictogram> pictogramList = helper.pictogramHelper.getPictogramsByCategory(selectedCategory);

                return pictogramList;
            }

            return new ArrayList<Pictogram>();
        }

        protected void onPostExecute(final List<Pictogram> result) {
            final PictogramAdapter categoryAdapter = new PictogramAdapter(result, CategoryDetailFragment.this.getActivity());
            pictogramGrid.setAdapter(categoryAdapter);

            // Set view when list is empty
            pictogramGrid.setEmptyView(categoryDetailLayout.findViewById(R.id.empty_gridview_text));
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
     * @param categoryId the id of the category to show details for
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        categoryDetailLayout = (ViewGroup) inflater.inflate(R.layout.fragment_category_detail, container, false);

        pictogramGrid = (GridView) categoryDetailLayout.findViewById(R.id.pictogram_gridview);
        pictogramGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Save a reference to the currently selected pictogram
                selectedPictogram = (Pictogram) pictogramGrid.getAdapter().getItem(position);
            }
        });


        final GirafButton deletePictogramButton = (GirafButton) categoryDetailLayout.findViewById(R.id.deletePictogramButton);
        deletePictogramButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPictogram == null) {
                    // TODO: Use something different than a toast
                    Toast.makeText(CategoryDetailFragment.this.getActivity(), getActivity().getResources().getString(R.string.pick_pictogram_before_delete), Toast.LENGTH_SHORT).show();
                } else // delete the selected pictogram and reload
                {
                    // Create and show confirmation dialog
                    final GirafConfirmDialog confirmDialog = GirafConfirmDialog.newInstance(
                        getActivity().getResources().getString(R.string.remove_pictogram_dialog_title), // Title of the dialog
                        String.format(getActivity().getResources().getString(R.string.remove_pictogram_dialog_body), selectedPictogram.getName(), selectedCategory.getName()), // Body of the dialog
                        CategoryActivity.CONFIRM_PICTOGRAM_DELETION_METHOD_ID
                    );
                    confirmDialog.show(getActivity().getSupportFragmentManager(), CONFIRM_PICTOGRAM_DELETION_DIALOG_FRAGMENT_TAG);
                }
            }
        });

        // Find the category-management buttons and hide them if the current category is a child-category
        if(isChildCategory) {
            // Hide the copy categories to user-button
            final GirafButton copyToUserButton = (GirafButton) categoryDetailLayout.findViewById(R.id.copyToUserButton);
            copyToUserButton.setEnabled(false);
            copyToUserButton.setOnDisabledClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Toast.makeText(getActivity(), "Kopiering er ikke tilgængelig", Toast.LENGTH_SHORT).show();
                }
            });

            // Hide the settings button
            final GirafButton categorySettingsButton = (GirafButton) categoryDetailLayout.findViewById(R.id.categorySettingsButton);
            categorySettingsButton.setEnabled(false);
            categorySettingsButton.setOnDisabledClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Toast.makeText(getActivity(), "Katagori settings er ikke tilgængelig", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return categoryDetailLayout;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start loading the pictograms of this category when the fragment is started
        loadPictogramTask = new LoadPictogramTask();
        loadPictogramTask.execute();
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
                // Remove the specific pictogram
                helper.pictogramCategoryHelper.removePictogramCategory(selectedCategory.getId(), selectedPictogram.getId());

                // Reload the list of pictograms
                loadPictogramTask = new LoadPictogramTask();
                loadPictogramTask.execute();
            }
        }
    }
}
