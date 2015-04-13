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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.List;
import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.cat.fragments.CategoryDetailFragment;
import dk.aau.cs.giraf.cat.fragments.InitialFragment;
import dk.aau.cs.giraf.gui.GirafConfirmDialog;
import dk.aau.cs.giraf.gui.GirafInflateableDialog;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Profile;


public class CategoryActivity extends GirafActivity implements AdapterView.OnItemClickListener, InitialFragment.OnFragmentInteractionListener, CategoryAdapter.SelectedCategoryAware, GirafConfirmDialog.Confirmation {

    // Identifiers used to start activities for results
    public final int CREATE_CATEGORY_REQUEST = 1;

    // Identifiers used to create fragments
    private static final String CATEGORY_SETTINGS_TAG = "CATEGORY_SETTINGS_TAG";

    // Helper that will be used to fetch profiles
    private final Helper helper = new Helper(this);

    // Profiles of which the categories will be loaded from
    private Profile childProfile;
    private Profile guardianProfile;

    // View to contain categories
    private ListView categoryContainer;

    private CategoryAdapter categoryAdapter;
    private CategoryAdapter.CategoryViewPair selectedCategoryAndViewItem = null;


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Check if there is a previously selected view
        if (selectedCategoryAndViewItem != null) {
            // Set the content of the frame layout to the default fragment
            getSupportFragmentManager().popBackStack();

            selectedCategoryAndViewItem.getView().setBackgroundColor(this.getResources().getColor(R.color.giraf_page_indicator_inactive));

            // Check if the same category was selected twice (deselected)
            if (selectedCategoryAndViewItem.getCategory().getId() == id) {
                selectedCategoryAndViewItem = null;
                return;
            }
        }

        // Set the selected category
        selectedCategoryAndViewItem = new CategoryAdapter.CategoryViewPair(categoryAdapter.getCategoryFromId(id), view);
        view.setBackgroundColor(this.getResources().getColor(R.color.giraf_page_indicator_active));

        // "Open" the fragment in the frame layout
        pushContent(CategoryDetailFragment.newInstance(id), R.id.categorytool_framelayout);
    }

    @Override
    public void onBackPressed() {

        // Check if there is a previously selected view and if there is no popup
        if (selectedCategoryAndViewItem != null && getSupportFragmentManager().findFragmentByTag(CATEGORY_SETTINGS_TAG) == null)
        {
            // Set the selected category to "null" and set background to in-active
            selectedCategoryAndViewItem.getView().setBackgroundColor(this.getResources().getColor(R.color.giraf_page_indicator_inactive));
            selectedCategoryAndViewItem = null;
        }

        super.onBackPressed();
    }

    @Override
    public CategoryAdapter.CategoryViewPair getSelectedMutableCategoryViewPair() {
        return selectedCategoryAndViewItem;
    }

    @Override
    public void confirmDialog(final int methodID) {
        final CategoryDetailFragment categoryDetailFragment = (CategoryDetailFragment) getSupportFragmentManager().findFragmentById(R.id.categorytool_framelayout);
        categoryDetailFragment.confirmDialog(methodID);
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
            categoryAdapter = new CategoryAdapter(CategoryActivity.this, CategoryActivity.this, result);
            categoryContainer.setAdapter(categoryAdapter);

            // Set view when list is empty
            categoryContainer.setEmptyView(findViewById(R.id.empty_list_item));
        }

    }

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
     * Called when a category is selected and the delete button is pressed
     */
    public void onDeleteCategoryClicked(final View view) {
        Toast.makeText(CategoryActivity.this, "Kategorien blev slettet", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when a category is selected and the save button is pressed
     */
    public void onSaveCategoryClicked(final View view) {
        Toast.makeText(CategoryActivity.this, "Kategorien blev gemt", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when a category is selected and when the settings buttons is pressed
     */
    public void onSettingsButtonClicked(View view) {
        // Create the dialog
        GirafInflateableDialog dialog = GirafInflateableDialog.newInstance("Indstillinger for Trafik", "Her kan du ændre piktogrammet og titlen for kategorien", R.layout.category_settings_dialog);

        dialog.show(getSupportFragmentManager(), CATEGORY_SETTINGS_TAG);
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
