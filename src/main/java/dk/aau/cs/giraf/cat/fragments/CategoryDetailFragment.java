package dk.aau.cs.giraf.cat.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.cat.PictogramAdapter;
import dk.aau.cs.giraf.cat.R;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * Created on 25/03/15.
 */
public class CategoryDetailFragment extends Fragment {

    private static final String CATEGORY_ID_TAG = "CATEGORY_ID_TAG";

    // Helper that will be used to fetch profiles
    private Helper helper;

    private ViewGroup categoryDetailLayout;
    private GridView pictogramGrid;

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

        return categoryDetailLayout;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start loading the pictograms of this category when the fragment is started
        final LoadPictogramTask task = new LoadPictogramTask();
        task.execute();

    }

    private class LoadPictogramTask extends AsyncTask<Void, Void, List<Pictogram>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Set view when list is empty
            pictogramGrid.setEmptyView(new ProgressBar(CategoryDetailFragment.this.getActivity()));
        }

        @Override
        protected List<Pictogram> doInBackground(Void... params)
        {
            final Bundle arguments = getArguments();

            if (arguments != null)
            {
                final long selectedCategoryId = arguments.getLong(CATEGORY_ID_TAG);

                final Category selectedCategory = helper.categoryHelper.getCategoryById((int)selectedCategoryId);

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

}
