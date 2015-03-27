package dk.aau.cs.giraf.cat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.cat.fragments.CategoryDetailFragment;
import dk.aau.cs.giraf.cat.fragments.InitialFragment;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Profile;


public class CategoryActivity extends GirafActivity implements AdapterView.OnItemClickListener, InitialFragment.OnFragmentInteractionListener {

    // Identifiers used to start activities for results
    public final int CREATE_CATEGORY_REQUEST = 1;

    // Helper that will be used to fetch profiles
    private final Helper helper = new Helper(this);

    // Profiles of which the categories will be loaded from
    private Profile childProfile;
    private Profile guardianProfile;

    // View to contain categories
    private ListView categoryContainer;

    private View selectedCategory = null;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO: Remove this
        Toast.makeText(CategoryActivity.this, "Den er klikket på!", Toast.LENGTH_SHORT).show();

        // Check if there is a previously selected view
        if (selectedCategory != null) {
            // Deselect
            selectedCategory.setSelected(false);

            // Remove background-color
            selectedCategory.setBackgroundColor(0x00000000);

            // Check if the view pressed is the currently selected view
            if (selectedCategory.getId() == view.getId()) {
                // Set the selected category to null (So that no category is "previously selected")
                selectedCategory = null;

                // Set the content of the frame layout to the default fragment
                getSupportFragmentManager().popBackStack();

                // The pressed category was deselected. Nothing more to do now
                return;
            }
        }

        // "Open" the fragment in the frame layout
        pushContent(CategoryDetailFragment.newInstance(id), R.id.categorytool_framelayout);

        // Set the selected flag for the clicked item
        view.setSelected(true);
        selectedCategory = view;

        // Set the background-color for the selected item
        view.setBackgroundColor(getResources().getColor(R.color.giraf_page_indicator_active));
    }

    @Override
    public void onBackPressed() {
        if(selectedCategory != null) {
            // Deselect button
            selectedCategory.setSelected(false);

            // Remove background-color
            selectedCategory.setBackgroundColor(0x00000000);

            // Set the selected category to null (So that no category is "previously selected")
            selectedCategory = null;
        }

        super.onBackPressed();
    }

    private class LoadCategoriesTask extends AsyncTask<Void, Void, List<Category>> {

        @Override
        protected List<Category> doInBackground(Void... params) {
            return helper.categoryHelper.getCategoriesByProfileId(getCurrentUser().getId());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Set view when list is empty
            categoryContainer.setEmptyView(new ProgressBar(CategoryActivity.this));
        }

        protected void onPostExecute(final List<Category> result) {
            final CategoryAdapter categoryAdapter = new CategoryAdapter(result, CategoryActivity.this);
            categoryContainer.setAdapter(categoryAdapter);

            // Set view when list is empty
            categoryContainer.setEmptyView(findViewById(R.id.empty_list_item));
        }

    }

    ;

    /**
     * Will return the current profile. If the application is launched from a child profile,
     * this method will return that profile. If the application is launched from a guardian profile,
     * this method will return that profile instead.
     * <p/>
     * Notice that when the application is launched from a child profile, the guardian profile is
     * also assigned.
     *
     * @return the current profile. {@code null} if no current profile.
     */
    private Profile getCurrentUser() {
        if (childProfile != null) {
            return childProfile;
        } else if (guardianProfile != null) {
            return guardianProfile;
        }

        return null;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Set the content of the frame layout to the default fragment
        setContent(InitialFragment.newInstance(), R.id.categorytool_framelayout);

        // Get the extra information from when the activity was started (contains profile ids etc.)
        final Bundle extras = getIntent().getExtras();

        // Test if the activity was started correctly
        if (extras == null) {
            Toast.makeText(CategoryActivity.this, getResources().getString(R.string.app_name) + " skal startes fra GIRAF", Toast.LENGTH_SHORT).show();

            // The activity was not started correctly, now finish it!
            finish();
            return;
        } else {
            int childId = extras.getInt("currentChildID");
            int guardianId = extras.getInt("currentGuardianID");

            if (childId != -1) {
                childProfile = helper.profilesHelper.getProfileById(childId);
            }

            if (guardianId != -1) {
                guardianProfile = helper.profilesHelper.getProfileById(guardianId);
            }
        }

        // Find the ListView that will contain the categories
        categoryContainer = (ListView) this.findViewById(R.id.category_container);
        categoryContainer.setOnItemClickListener(this);

        // Load the categories using the LoadCategoriesTask
        LoadCategoriesTask categoryLoader = (LoadCategoriesTask) new LoadCategoriesTask().execute();
    }

    /**
     * Will set the content (fragment) of the provided {@code FrameLayout}
     *
     * @param fragment            fragment to place in specific {@code FrameLayout}, see {@code frameLayoutResource}
     * @param frameLayoutResource resource to place fragment in
     */
    public void setContent(final Fragment fragment, final int frameLayoutResource) {
        final FragmentManager fm = getSupportFragmentManager();

        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.popBackStackImmediate();

        final Fragment currentContent = getSupportFragmentManager().findFragmentById(frameLayoutResource);

        if (currentContent != null && !fragment.getClass().equals(InitialFragment.class)) {
            fm.beginTransaction().replace(frameLayoutResource, InitialFragment.newInstance()).commit();
            fm.executePendingTransactions();
            fm.beginTransaction().replace(frameLayoutResource, fragment).addToBackStack(null).commit();
        } else {
            fm.beginTransaction().replace(frameLayoutResource, fragment).commit();
        }
    }

    /**
     * Push the provided (fragment) on top of the current fragment stack, see {@code setContent}
     *
     * @param fragment            fragment to place in specific {@code FrameLayout}, see {@code frameLayoutResource}
     * @param frameLayoutResource resource to place fragment in
     */
    public void pushContent(final Fragment fragment, final int frameLayoutResource) {
        final FragmentManager fm = getSupportFragmentManager();

        final Fragment currentContent = getSupportFragmentManager().findFragmentById(frameLayoutResource);

        if (currentContent == fragment) {
            fm.beginTransaction().remove(currentContent).commit();
            fm.executePendingTransactions();
        }

        fm.beginTransaction().replace(frameLayoutResource, fragment).addToBackStack(null).commit();
    }

    /**
     * Will be called when the "create category" button is pressed
     */
    @Override
    public void onCreateCategoryButtonClicked(final View view) {
        // Check if the current profile is a guardian profile
        if (guardianProfile == null) {
            // TODO: Handle it. Crash app or open select user dialog
        } else {
            final Intent intent = new Intent(this, CreateCategoryActivity.class);
            intent.putExtra("currentGuardianID", guardianProfile.getId());
            startActivityForResult(intent, CREATE_CATEGORY_REQUEST);
        }
    }

    /**
     * Will be called when an opened activity returns
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // Check which request we're responding to
        if (requestCode == CREATE_CATEGORY_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                final Bundle extras = data.getExtras();

                final int id = extras.getInt(CreateCategoryActivity.CATEGORY_CREATED_ID_TAG);

                // Reload all categories for the current profile
                final LoadCategoriesTask categoryLoader = (LoadCategoriesTask) new LoadCategoriesTask() {
                    @Override
                    protected void onPostExecute(final List<Category> result) {
                        super.onPostExecute(result);

                        // Assumes the new category is added to the end of the list
                        categoryContainer.setSelection(categoryContainer.getAdapter().getCount() - 1);
                    }
                }.execute();
            }
        }
    }
}
