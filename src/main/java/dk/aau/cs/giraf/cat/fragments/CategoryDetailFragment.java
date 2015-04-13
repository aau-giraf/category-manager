package dk.aau.cs.giraf.cat.fragments;

import android.content.ComponentName;
import android.content.Intent;
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
import dk.aau.cs.giraf.gui.GDialogAlert;
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

    // Dialog fragment Tags
    private static final String CONFIRM_PICTOGRAM_DELETION_DIALOG_FRAGMENT_TAG = "CONFIRM_PICTOGRAM_DELETION_DIALOG_FRAGMENT_TAG";

    // Helper that will be used to fetch profiles
    private Helper helper;

    private ViewGroup categoryDetailLayout;
    private GridView pictogramGrid;

    private LoadPictogramTask loadPictogramTask;

    private Pictogram selectedPictogram = null;
    private Category selectedCategory = null;

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
     * @return A new instance of fragment CategoryDetailFragment.
     */
    public static CategoryDetailFragment newInstance(final long categoryId) {
        CategoryDetailFragment fragment = new CategoryDetailFragment();
        Bundle args = new Bundle();
        args.putLong(CATEGORY_ID_TAG, categoryId);
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
    public void confirmDialog(int methodID) {
        if (methodID == CategoryActivity.CONFIRM_PICTOGRAM_DELETION_METHOD_ID) {
            // Remove the specific pictogram
            helper.pictogramCategoryHelper.removePictogramCategory(selectedCategory.getId(), selectedPictogram.getId());

            // Reload the list of pictograms
            loadPictogramTask = new LoadPictogramTask();
            loadPictogramTask.execute();
        }
    }
}
