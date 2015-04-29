package dk.aau.cs.giraf.categorymanager;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.categorymanager.fragments.CategoryDetailFragment;
import dk.aau.cs.giraf.categorymanager.fragments.InitialFragment;
import dk.aau.cs.giraf.categorymanager.fragments.InitialFragmentSpecificUser;
import dk.aau.cs.giraf.categorymanager.showcase.ShowcaseManager;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Department;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.dblib.models.PictogramCategory;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.dblib.models.ProfileCategory;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafConfirmDialog;
import dk.aau.cs.giraf.gui.GirafInflatableDialog;
import dk.aau.cs.giraf.gui.GirafNotifyDialog;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;
import dk.aau.cs.giraf.gui.GirafProfileSelectorDialog;
import dk.aau.cs.giraf.gui.GirafWaitingDialog;

public class CategoryActivity extends GirafActivity implements AdapterView.OnItemClickListener, InitialFragment.OnFragmentInteractionListener, InitialFragmentSpecificUser.OnFragmentInteractionListener, CategoryAdapter.SelectedCategoryAware, GirafConfirmDialog.Confirmation, GirafInflatableDialog.OnCustomViewCreatedListener, GirafNotifyDialog.Notification, GirafProfileSelectorDialog.OnMultipleProfilesSelectedListener, GirafProfileSelectorDialog.OnSingleProfileSelectedListener, CategoryDetailFragment.OnSelectedPictogramsUpdateListener {

    // Identifiers used to start activities etc. for results
    public static final int CREATE_CATEGORY_REQUEST = 101;
    public static final int CONFIRM_PICTOGRAM_DELETION_METHOD_ID = 102;

    public static final int GET_SINGLE_PICTOGRAM = 103;
    public static final int GET_MULTIPLE_PICTOGRAMS = 104;

    public static final int NOTIFICATION_DIALOG_DO_NOTHING = 105;
    public static final int EDIT_CATEGORY_DIALOG = 106;

    public static final int UPDATE_CATEGORY_REQUEST = 108;

    public static final String PICTO_SEARCH_IDS_TAG = "checkoutIds";
    public static final String PICTO_SEARCH_PURPOSE_TAG = "purpose";
    public static final String PICTO_SEARCH_MULTI_TAG = "multi";
    public static final String PICTO_SEARCH_SINGLE_TAG = "single";
    public static final String INTENT_STRING_CURRENT_GUARDIAN_ID = "currentGuardianID";

    // Identifiers used to create fragments
    private static final String CATEGORY_SETTINGS_TAG = "CATEGORY_SETTINGS_TAG";
    private static final int UPDATE_CITIZEN_CATEGORIES_DIALOG = 109;
    private static final int ADD_PICTOGRAMS_TO_CATEGORIES_DIALOG = 110;
    private static final int REMOVE_PICTOGRAMS_FROM_CATEGORIES_DIALOG = 111;
    private static final int DELTE_CATEGORY_CONFIRM_DIALOG = 112;
    private static final int CHANGE_USER_DIALOG = 113;

    // Helper that will be used to fetch profiles
    private final Helper helper = new Helper(this);

    // Profiles of which the categories will be loaded from
    private Profile childProfile;
    private Profile guardianProfile;

    // Edit category dialog elements
    GirafInflatableDialog editDialog;
    private Pictogram changedPictogram; // Returned from pictosearch
    private EditText categoryTitle; // The textView
    private String changedText; // The text before opening pictosearch

    // View to contain categories
    private ListView categoryContainer;

    // List of categories
    private List<Category> categoryList;
    private List<Pair<Category, Pair<Profile, Boolean>>> profileCategoryStatusList;

    // Save the current category and its adapters
    private CategoryAdapter categoryAdapter;
    private CategoryAdapter.CategoryViewPair selectedCategoryAndViewItem = null;

    // Lists to update pictograms
    private List<Long> lastAddedPictogramsIds = new ArrayList<Long>();
    private List<Long> selectedPictogramsIdsInFragment = new ArrayList<Long>();

    // Reference to category detail fragment
    //private CategoryDetailFragment categoryDetailFragment;


    /**
     * Used to load categories into the category container (left side)
     */
    public class LoadCategoriesTask extends AsyncTask<Void, Void, List<Category>> {

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
            categoryList = result;

            new UpdateCategoryProfileStatusList().execute();

            categoryAdapter = new CategoryAdapter(CategoryActivity.this, CategoryActivity.this, result);
            categoryContainer.setAdapter(categoryAdapter);

            // Set view when list is empty
            categoryContainer.setEmptyView(findViewById(R.id.empty_list_item));
        }

    }

    /**
     * Class to process the deleting and adding of new categories
     */
    public class UpdateCategoryProfileStatusList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            profileCategoryStatusList = new ArrayList<Pair<Category, Pair<Profile, Boolean>>>();
            for (Category guardianCategory : categoryList) {

                // Find the children that the guardian is responsible for. Notice that getCurrentUser will always return a guardian
                List<Profile> profiles = helper.profilesHelper.getChildrenByGuardian(getCurrentUser());

                // Run through all the profiles and check if the user "has" the category
                boolean citizenHasCategory = false;
                List<Category> userCategories;
                for (Profile citizen : profiles) {
                    userCategories = helper.categoryHelper.getCategoriesByProfileId(citizen.getId());

                    for (Category citizenCategory : userCategories) {
                        if (citizenCategory.getSuperCategoryId() == guardianCategory.getId()) {
                            citizenHasCategory = true;
                            break;
                        }
                    }

                    // Add the category-profile-boolean pair to the list
                    Pair<Category, Pair<Profile, Boolean>> categoryProfileStaus = new Pair<Category, Pair<Profile, Boolean>>(guardianCategory, new Pair<Profile, Boolean>(citizen, citizenHasCategory));
                    profileCategoryStatusList.add(categoryProfileStaus);

                    // Reset variable
                    citizenHasCategory = false;
                }
            }

            // Background thread is now complete...
            return null;
        }

    }

    /**
     * Class to process the deleting and adding of new categories
     */
    public class SetCitizenCategories extends AsyncTask<Void, Void, Void> {

        private static final String SETTING_UP_CATEGORIES_WAITING_DIALOG = "SETTING_UP_CATEGORIES_WAITING_DIALOG";
        private final List<Pair<Profile, Boolean>> checkedProfileList;
        private GirafWaitingDialog waitingDialog;

        public SetCitizenCategories(List<Pair<Profile, Boolean>> checkedProfileList) {
            this.checkedProfileList = checkedProfileList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            waitingDialog = GirafWaitingDialog.newInstance(getString(R.string.please_wait_waitdialog_title), getString(R.string.setting_up_categories_waitdialog_description));
            waitingDialog.show(getSupportFragmentManager(), SETTING_UP_CATEGORIES_WAITING_DIALOG);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Find the selected category to be copied
            Category selectedCategory = getSelectedCategory();

            // Run through the list and modify the children's categories dependently
            for (Pair<Profile, Boolean> profileBooleanPair : checkedProfileList) {
                final Profile citizen = profileBooleanPair.first;
                final boolean checkedStatus = profileBooleanPair.second;

                // Check if the citizen already have a copy of this category
                List<Category> categoriesByProfileId = helper.categoryHelper.getCategoriesByProfileId(citizen.getId());

                // Boolean and category reference used to indicate weather or not the citizen already has the category
                boolean categoryAlreadyCopied = false;
                Category citizenCategory = null;

                // Run through the citizen's categories, searching for the selected category
                for (Category ownedCategory : categoriesByProfileId) {
                    if (ownedCategory.getSuperCategoryId() == selectedCategory.getId()) {
                        categoryAlreadyCopied = true;
                        citizenCategory = ownedCategory;
                        break;
                    }
                }

                // Check if the citizen already has the category and is supposed to have it
                // OR if the citizen does not have the category and is not supposed to have it
                if ((categoryAlreadyCopied && checkedStatus) || (!categoryAlreadyCopied && !checkedStatus)) {
                    continue;
                }
                // Check if the citizen already has the category, but is not supposed to have it
                else if (categoryAlreadyCopied && !checkedStatus) {
                    // Find all pictograms that should be removed from the citizen's category
                    List<Pictogram> pictograms = helper.pictogramHelper.getPictogramsByCategory(citizenCategory);

                    // Remove all relations between the above pictograms and the citizen's category
                    for (Pictogram pictogram : pictograms) {
                        helper.pictogramCategoryHelper.remove(citizenCategory.getId(), pictogram.getId());
                    }

                    // Remove the category and the join-table entry
                    helper.categoryHelper.remove(citizenCategory);
                    helper.profileCategoryController.remove(citizen.getId(), citizenCategory.getId());
                } else if (!categoryAlreadyCopied && checkedStatus) {
                    // Find the pictograms of the selected category to be copied
                    List<Pictogram> pictograms = helper.pictogramHelper.getPictogramsByCategory(selectedCategory);

                    // Create a copy of the selected category (this will be "given" to the citizen)
                    Category newCategory = new Category();
                    newCategory.setName(selectedCategory.getName());
                    newCategory.setColour(selectedCategory.getColour());
                    newCategory.setImage(selectedCategory.getImage());
                    newCategory.setSuperCategoryId(selectedCategory.getId());

                    // Add the category and join-table entry
                    long insertedCategoryIdentifier = helper.categoryHelper.insert(newCategory);

                    // Insert the same pictograms to the new category
                    for (Pictogram pictogram : pictograms) {
                        helper.pictogramCategoryHelper.insert(new PictogramCategory(pictogram.getId(), insertedCategoryIdentifier));
                    }

                    // Insert the relation between the new category and the associated citizen
                    helper.profileCategoryController.insert(new ProfileCategory(citizen.getId(), insertedCategoryIdentifier));
                }
            }

            // Background thread is now complete...
            return null;
        }

        @Override
        protected void onPostExecute(Void m) {
            super.onPostExecute(m);
            new UpdateCategoryProfileStatusList().execute();
            waitingDialog.dismiss();
        }
    }

    ;

    /**
     * Class to process the deleting and adding of new categories
     */
    public class UpdateCategories extends AsyncTask<Void, Void, Void> {

        private static final String UPDATING_CATEGORIES_WAITING_DIALOG = "UPDATING_CATEGORIES_WAITING_DIALOG";
        private GirafWaitingDialog waitingDialog;
        private Category oldCategory;
        private Category newCategory;

        public UpdateCategories(Category oldCategory, Category newCategory) {
            this.oldCategory = oldCategory;
            this.newCategory = newCategory;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            waitingDialog = GirafWaitingDialog.newInstance(getString(R.string.please_wait_waitdialog_title), getString(R.string.updating_categories_waitdialog_description));
            waitingDialog.show(getSupportFragmentManager(), UPDATING_CATEGORIES_WAITING_DIALOG);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Category> citizenCategories = helper.categoryHelper.getSubcategoriesByCategory(oldCategory);

            // Update the super category
            helper.categoryHelper.modify(newCategory);

            // Update the categories of the citizens
            for (Category citizenCategory : citizenCategories) {
                citizenCategory.setName(newCategory.getName());
                citizenCategory.setImage(newCategory.getImage());
                helper.categoryHelper.modify(citizenCategory);
            }

            // Background thread is now complete...
            return null;
        }

        @Override
        protected void onPostExecute(Void m) {
            super.onPostExecute(m);

            // If the category was from the categoryAdapter update the adapter
            if (getSelectedCategory().getId() == oldCategory.getId()) {
                getSelectedCategory().setName(newCategory.getName());
                getSelectedCategory().setImage(newCategory.getImage());
                categoryAdapter.notifyDataSetChanged();
            }

            waitingDialog.dismiss();
        }
    }

    ;

    /**
     * Class to process the deleting and adding of new categories
     */
    public class DeleteCategories extends AsyncTask<Void, Void, Void> {

        private static final String DELETING_CATEGORIES_WAITING_DIALOG = "UPDATING_CATEGORIES_WAITING_DIALOG";
        private GirafWaitingDialog waitingDialog;
        private Category deletionCategory;

        public DeleteCategories(Category deletionCategory) {
            this.deletionCategory = deletionCategory;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            waitingDialog = GirafWaitingDialog.newInstance(getString(R.string.please_wait_waitdialog_title), getString(R.string.deleting_categories_waitdialog_description));
            waitingDialog.show(getSupportFragmentManager(), DELETING_CATEGORIES_WAITING_DIALOG);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Category> citizenCategories = helper.categoryHelper.getSubcategoriesByCategory(deletionCategory);

            // Remove the pictograms of the main category
            for (Pictogram pictogram : helper.pictogramHelper.getPictogramsByCategory(deletionCategory)) {
                helper.pictogramCategoryHelper.remove(new PictogramCategory(pictogram.getId(), deletionCategory.getId()));
            }

            // Delete the super category
            helper.categoryHelper.remove(deletionCategory);

            // Delete the categories of the citizens
            for (Category citizenCategory : citizenCategories) {

                // Remove the pictograms from the citizen category
                for (Pictogram pictogram : helper.pictogramHelper.getPictogramsByCategory(citizenCategory)) {
                    helper.pictogramCategoryHelper.remove(new PictogramCategory(pictogram.getId(), citizenCategory.getId()));
                }
                helper.categoryHelper.modify(citizenCategory);
            }

            // Background thread is now complete...
            return null;
        }

        @Override
        protected void onPostExecute(Void m) {
            super.onPostExecute(m);

            // If the category was from the categoryAdapter update the adapter
            if (getSelectedCategory().getId() == deletionCategory.getId()) {
                categoryList.remove(getSelectedCategory()); // Remove from list in adapter
                categoryAdapter.notifyDataSetChanged(); // Tell the adapter to update
            }

            selectedCategoryAndViewItem = null; // Set the selected category to nothing
            getSupportFragmentManager().popBackStack(); // Remove the pictogram grid and go back to start page
            waitingDialog.dismiss();
        }
    }

    ;

    /**
     * Class to process the deleting and adding of new categories
     */
    public class UpdatePictogramsInCategory extends AsyncTask<Void, Void, Void> {

        private static final String EDITING_PICTOGRAMS_IN_CATEGORY_WAITING_DIALOG = "EDITING_PICTOGRAMS_IN_CATEGORY_WAITING_DIALOG";
        private final List<Pair<Profile, Boolean>> checkedProfileList;
        private GirafWaitingDialog waitingDialog;
        private int pictogramAction;
        private List<Long> pictogramIds;
        public static final int ADD_PICTOGRAMS = 0;
        public static final int REMOVE_PICTOGRAMS = 1;

        public UpdatePictogramsInCategory(List<Pair<Profile, Boolean>> checkedProfileList, int pictogramAction, List<Long> pictogramIds) {
            this.checkedProfileList = checkedProfileList;

            // Throw exception if illegal arguments is given
            if (pictogramAction != ADD_PICTOGRAMS && pictogramAction != REMOVE_PICTOGRAMS) {
                throw new IllegalArgumentException("UpdatePictogramsInCategory needs a correct action for pictograms (ADD_PICTOGRAMS or REMOVE_PICTOGRAMS");
            }

            this.pictogramAction = pictogramAction;
            this.pictogramIds = pictogramIds;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            String waitingDescription;
            if (pictogramAction == ADD_PICTOGRAMS) {
                waitingDescription = getString(R.string.add_pictograms_wait_dialog_description);
            } else if (pictogramAction == REMOVE_PICTOGRAMS) {
                waitingDescription = getString(R.string.remove_pictograms_wait_dialog_description);
            } else {
                waitingDescription = "";
            }

            waitingDialog = GirafWaitingDialog.newInstance(getString(R.string.please_wait_waitdialog_title), waitingDescription);
            waitingDialog.show(getSupportFragmentManager(), EDITING_PICTOGRAMS_IN_CATEGORY_WAITING_DIALOG);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Find the selected category to be copied
            final Category selectedCategory = getSelectedCategory();

            // Update the seleted category accordingly
            if (pictogramAction == ADD_PICTOGRAMS) {
                // Add pictograms to the selected category
                for (long pictogramId : pictogramIds) {
                    helper.pictogramCategoryHelper.insert(new PictogramCategory(pictogramId, getSelectedCategory().getId()));
                }
            } else if (pictogramAction == REMOVE_PICTOGRAMS) {
                // Remove pictograms from the selected category
                for (long pictogramId : pictogramIds) {
                    helper.pictogramCategoryHelper.remove(new PictogramCategory(pictogramId, getSelectedCategory().getId()));
                }
            }

            // Run through the list and modify the children's categories dependently
            for (Pair<Profile, Boolean> profileBooleanPair : checkedProfileList) {
                final Profile citizen = profileBooleanPair.first;
                final boolean checkedStatus = profileBooleanPair.second;

                // If the citizen was not checked ignore it on this particular citizen
                if (checkedStatus == false) {
                    continue;
                }

                // Find this citizens subcategory of the selected category
                List<Category> categoriesByProfileId = helper.categoryHelper.getCategoriesByProfileId(citizen.getId());

                // The category of the citizen
                Category citizenCategory = null;

                // Run through the citizen's categories, searching for the selected category
                for (Category ownedCategory : categoriesByProfileId) {
                    if (ownedCategory.getSuperCategoryId() == selectedCategory.getId()) {

                        citizenCategory = ownedCategory;
                        break;
                    }
                }

                // Update the seleted category accordingly
                if (pictogramAction == ADD_PICTOGRAMS) {
                    // Add pictograms to the selected category
                    for (long pictogramId : pictogramIds) {
                        helper.pictogramCategoryHelper.insert(new PictogramCategory(pictogramId, citizenCategory.getId()));
                    }
                } else if (pictogramAction == REMOVE_PICTOGRAMS) {
                    // Remove pictograms from the selected category
                    for (long pictogramId : pictogramIds) {
                        helper.pictogramCategoryHelper.remove(new PictogramCategory(pictogramId, citizenCategory.getId()));
                    }
                }

            }

            // Background thread is now complete...
            return null;
        }

        @Override
        protected void onPostExecute(Void m) {
            super.onPostExecute(m);
            new UpdateCategoryProfileStatusList().execute();

            // Close the dialog
            waitingDialog.dismiss();

            // Update the pictograms in the gridview
            final CategoryDetailFragment fragment = (CategoryDetailFragment) getSupportFragmentManager().findFragmentById(R.id.categorytool_framelayout);

            if (fragment != null) {
                fragment.loadPictograms();
            }
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
    public Profile getCurrentUser() {
        if (childProfile != null) {
            return childProfile;
        } else if (guardianProfile != null) {
            return guardianProfile;
        }

        return null;
    }

    /**
     * Will be called every time the activity starts
     *
     * @param savedInstanceState the saved state of the activity
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category);
        TextView empty_list_item = (TextView) this.findViewById(R.id.empty_list_item);
        empty_list_item.setText(R.string.no_categories_text);

        // Get the extra information from when the activity was started (contains profile ids etc.)
        final Bundle extras = getIntent().getExtras();

        //Check if user is monkey
        if (ActivityManager.isUserAMonkey()) {
            Helper h = new Helper(this);

            Profile child = h.profilesHelper.getChildren().get(0);
            guardianProfile = h.profilesHelper.getGuardiansByChild(child).get(0);

        }
        // Test if the activity was started correctly
        else if (extras == null || (!extras.containsKey(getString(R.string.current_child_id)) && !extras.containsKey(getString(R.string.current_guardian_id)))) {
            Toast.makeText(CategoryActivity.this, String.format(getString(R.string.error_must_be_started_from_giraf), getString(R.string.categorymanager)), Toast.LENGTH_SHORT).show();

            // The activity was not started correctly, now finish it!
            finish();
            return;
        } else {
            final long childId = extras.getLong(getString(R.string.current_child_id));
            final long guardianId = extras.getLong(getString(R.string.current_guardian_id));

            if (childId != -1) {
                childProfile = helper.profilesHelper.getById(childId);
            }

            if (guardianId != -1) {
                guardianProfile = helper.profilesHelper.getById(guardianId);
            }
        }

        final Profile currentUserProfile = getCurrentUser();

        if (currentUserProfile == null) {
            Toast.makeText(CategoryActivity.this, String.format(getString(R.string.error_must_be_started_with_valid_profile), getString(R.string.categorymanager)), Toast.LENGTH_SHORT).show();

            // The activity was not started correctly, now finish it!
            finish();
            return;
        }

        // Change the title of the action-bar and content of right side depending on what type of categories are being modified
        if (currentUserProfile.getRole() == Profile.Roles.CHILD) {
            // Change the title bar text
            setActionBarTitle(String.format(getString(R.string.categories_for), currentUserProfile.getName()));

            // Set the content of the frame layout to the default fragment
            setContent(InitialFragmentSpecificUser.newInstance(currentUserProfile), R.id.categorytool_framelayout);
        } else {
            // Find the department for the guardian
            Department department = helper.departmentsHelper.getById((int) currentUserProfile.getDepartmentId());

            // Change the title bar text
            setActionBarTitle(String.format(getString(R.string.categories_for), department.getName()));

            // Set the content of the frame layout to the default fragment
            setContent(InitialFragment.newInstance(), R.id.categorytool_framelayout);
        }

        final GirafButton helpGirafButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_help));
        helpGirafButton.setId(R.id.help_button);
        helpGirafButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ShowcaseManager.ShowcaseCapable currentContent = (ShowcaseManager.ShowcaseCapable) getSupportFragmentManager().findFragmentById(R.id.categorytool_framelayout);
                currentContent.toggleShowcase();
            }
        });

        addGirafButtonToActionBar(helpGirafButton, GirafActivity.RIGHT);

        // Find the ListView that will contain the categories
        categoryContainer = (ListView) this.findViewById(R.id.giraf_sidebar_container);
        categoryContainer.setOnItemClickListener(this);

        // Load the categories using the LoadCategoriesTask
        LoadCategoriesTask categoryLoader = (LoadCategoriesTask) new LoadCategoriesTask().execute();

        // Check if we are aware of the current guardian profile
        if (getCurrentUser() != null) {


            // Add the change-user button to the top-bar
            GirafButton changeUserGirafButton = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_change_user));

            // Method to use whenever the change user-button is pressed
            changeUserGirafButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create the new profile editor. Note that it is important that guardians are not shown in this list.
                    final GirafProfileSelectorDialog profileSelectorDialog = GirafProfileSelectorDialog.newInstance(CategoryActivity.this, guardianProfile.getId(), false, false, getString(R.string.categorymanager_change_user_dialog_description), CHANGE_USER_DIALOG);

                    profileSelectorDialog.show(getSupportFragmentManager(), "" + CHANGE_USER_DIALOG);

                }
            });

            addGirafButtonToActionBar(changeUserGirafButton, GirafActivity.LEFT);

        }
    }
    /**/
    /*
     * Methods to handle right-side fragment (FrameLayout) below
     */

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

        /*
        if (fragment instanceof CategoryDetailFragment) {
            categoryDetailFragment = (CategoryDetailFragment) fragment;
        }*/
    }

    /*
     * Methods for button clicks below
     */

    /**
     * Will be called every time the back-button is pressed
     * Used to handle the fragment-stack properly
     */
    @Override
    public void onBackPressed() {

        // Check if there is a previously selected view and if there is no popup
        if (selectedCategoryAndViewItem != null && getSupportFragmentManager().findFragmentByTag(CATEGORY_SETTINGS_TAG) == null) {
            // Set the selected category to "null" and set background to in-active
            selectedCategoryAndViewItem.getView().setBackgroundColor(Color.TRANSPARENT);
            selectedCategoryAndViewItem = null;
        }

        super.onBackPressed();
    }

    /**
     * Called when the pictogram is clicked
     *
     * @param view needed for onClickListner
     */
    public void onEditCategoryPictogramClicked(final View view) {

        // Reset the returned value
        changedPictogram = null;
        changedText = categoryTitle.getText().toString();

        // Try to send the intent
        try {

            makePictosearchRequest(PICTO_SEARCH_SINGLE_TAG, GET_SINGLE_PICTOGRAM);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.could_not_open_pictosearch), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when a category is selected and the delete button is pressed
     */
    public void onDeleteCategoryClicked(final View view) {

        // TODO Insert strings to strings.xml

        int subCategoryCount = getProfileWithCategoryList(getSelectedCategory()).size();
        String deleteDescription = "Vil du slette kategorien " + getSelectedCategory().getName() + "?";

        if (subCategoryCount > 0) {
            deleteDescription = deleteDescription + " Der er " + subCategoryCount + " borgere som mister denne kategori.";
        }

        editDialog.dismiss(); // Close the editing dialog

        // Ask for permission
        GirafConfirmDialog deleteConfirm = GirafConfirmDialog.newInstance("Slet " + getSelectedCategory().getName() + "?", deleteDescription, DELTE_CATEGORY_CONFIRM_DIALOG);
        deleteConfirm.show(getSupportFragmentManager(), "" + DELTE_CATEGORY_CONFIRM_DIALOG);
    }

    /**
     * Called when a category is selected and the save button is pressed
     */
    public void onSaveCategoryClicked(final View view) {

        final Category oldCategory = getSelectedCategory();

        final Category newCategory = new Category();
        newCategory.setId(getSelectedCategory().getId());
        newCategory.setName(categoryTitle.getText().toString());

        // If an image was update use that otherwise use the old one
        if (changedPictogram != null) {
            newCategory.setImage(changedPictogram.getImage());
        } else {
            newCategory.setImage(getSelectedCategory().getImage());
        }

        new UpdateCategories(oldCategory, newCategory).execute();
        changedText = null; // Reset the changed text so that next edit wont have this text
        editDialog.dismiss();

    }

    /**
     * Called when a category is selected and the user settings buttons is pressed
     *
     * @param view needed for onClickListner
     */
    public void onUserSettingsButtonClicked(final View view) {

        // The list of people who has the selectedCategor associated
        List<Pair<Profile, Boolean>> pairList = getProfileStatusListFromCategory(getSelectedCategory());

        // Create the dialog and show it to the user
        GirafProfileSelectorDialog selectorDialog = GirafProfileSelectorDialog.newInstance(pairList, true, getString(R.string.select_users_to_get_category), UPDATE_CITIZEN_CATEGORIES_DIALOG);
        selectorDialog.show(getSupportFragmentManager(), "" + UPDATE_CITIZEN_CATEGORIES_DIALOG);
    }

    /**
     * Called when a category is selected and when the settings buttons is pressed
     *
     * @param view needed for onClickListner
     */
    public void onSettingsButtonClicked(final View view) {
        // Create the dialog

        changedPictogram = null;
        editDialog = GirafInflatableDialog.newInstance(String.format(getString(R.string.settings_for), getSelectedCategory().getName()),
                getString(R.string.settings_dialog_description),
                R.layout.category_settings_dialog, EDIT_CATEGORY_DIALOG);

        // Sho the dialog
        editDialog.show(getSupportFragmentManager(), CATEGORY_SETTINGS_TAG);

    }

    /**
     * When a person clicks the add button to add a pictogram
     *
     * @param view
     */
    public void onAddButtonClick(final View view) {
        // Try to send the intent
        try {
            makePictosearchRequest(PICTO_SEARCH_MULTI_TAG, GET_MULTIPLE_PICTOGRAMS);

        } catch (Exception e) {
            Toast.makeText(this, String.format(getString(R.string.could_not_open_pictosearch), getString(R.string.pictosearch)), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When a person clicks the add button to add a pictogram
     *
     * @param view
     */
    public void onRemoveButtonClick(final View view) {

        // If there are any pictograms selected
        if (selectedPictogramsIdsInFragment.size() > 0) {
            // If the category has any profiles added ask who wants the change
            if (getProfileWithCategoryList(getSelectedCategory()).size() > 0) {

                // Get a list of profiles who has the category and a boolean sat to true
                List<Pair<Profile, Boolean>> pairList = getProfileWithCategoryList(getSelectedCategory());
                GirafProfileSelectorDialog removePictoGramDialog = GirafProfileSelectorDialog.newInstance(pairList, true, getString(R.string.remove_pictograms_from_category_dialog_description), REMOVE_PICTOGRAMS_FROM_CATEGORIES_DIALOG);
                removePictoGramDialog.show(getSupportFragmentManager(), "" + REMOVE_PICTOGRAMS_FROM_CATEGORIES_DIALOG);

            } else {
                GirafConfirmDialog confirmDelete = GirafConfirmDialog.newInstance("Fjern?", "Vil du fjerne disse " + selectedPictogramsIdsInFragment.size() + " piktogram(mer)?", CONFIRM_PICTOGRAM_DELETION_METHOD_ID);
                confirmDelete.show(getSupportFragmentManager(), "" + CONFIRM_PICTOGRAM_DELETION_METHOD_ID);

            }
        } else {
            Toast.makeText(this, getString(R.string.no_pictograms_selected), Toast.LENGTH_SHORT).show();
        }
    }


    public Category getSelectedCategory() {
        return selectedCategoryAndViewItem.getCategory();
    }

    /**
     * Finds a list of profiles status if the boolean is true the profile has the category else false
     *
     * @param category the category of which a status is wanted
     * @return a list of profiles and a boolean set to the status of the profile having the category
     */
    public List<Pair<Profile, Boolean>> getProfileStatusListFromCategory(final Category category) {

        final List<Pair<Profile, Boolean>> profileBooleanList = new ArrayList<Pair<Profile, Boolean>>();

        // Run through all categories and find the wanted one
        for (Pair<Category, Pair<Profile, Boolean>> profileStatusPair : profileCategoryStatusList) {

            // If it is the correct category copy the profile boolean pair to the list
            if (profileStatusPair.first == category) {
                profileBooleanList.add(profileStatusPair.second);
            }
        }

        return profileBooleanList;
    }

    /**
     * Finds a list of profiles who have this category and the boolean is all true
     *
     * @param category the category of which a list of associated profiles is wanted
     * @return a list of profiles who has the category along with a boolean sat to true
     */
    public List<Pair<Profile, Boolean>> getProfileWithCategoryList(final Category category) {

        // Get the list of total status for a category
        final List<Pair<Profile, Boolean>> oldProfileBooleanList = getProfileStatusListFromCategory(category);

        // Create a list to be returned
        final List<Pair<Profile, Boolean>> newProfileBooleanList = new ArrayList<Pair<Profile, Boolean>>();

        for (Pair<Profile, Boolean> profileBooleanPair : oldProfileBooleanList) {
            // If it the profile status was true in the old list add it to this list
            if (profileBooleanPair.second == true) {
                newProfileBooleanList.add(profileBooleanPair);
            }
        }

        return newProfileBooleanList;
    }

    private void makePictosearchRequest(final String pictosearchPurposeTag, final int pictosearchRequestCode) {

        final Intent request = new Intent(); // A intent request

        // Sets properties on the intent
        request.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        request.putExtra(PICTO_SEARCH_PURPOSE_TAG, pictosearchPurposeTag);

        if (childProfile != null) {
            request.putExtra(getString(R.string.current_child_id), childProfile.getId());
        } else {
            request.putExtra(getString(R.string.current_child_id), getResources().getInteger(R.integer.no_child_selected_id));
        }

        request.putExtra(getString(R.string.current_guardian_id), guardianProfile.getId());

        // Sends the intent
        startActivityForResult(request, pictosearchRequestCode);
    }

    /*
     * Methods required from interfaces below
     * */

    /**
     * Will be called whenever a custom dialog box requests content (ie when settings button is pressed)
     */
    @Override
    public void editCustomView(final ViewGroup viewGroup, final int i) {

        if (i == EDIT_CATEGORY_DIALOG) {// Finds the views

            final GirafPictogramItemView girafPictogram = (GirafPictogramItemView) viewGroup.findViewById(R.id.editable_pictogram_view);
            categoryTitle = (EditText) viewGroup.findViewById(R.id.category_edit_title);

            // If PictoSearch returned a pictogram, update the view. Otherwise set it to the regular pictogram
            if (changedPictogram != null) {
                girafPictogram.setImageModel(changedPictogram);
            } else {
                Pictogram temp = new Pictogram();
                temp.setName(getSelectedCategory().getName());
                temp.setImage(getSelectedCategory().getImage());
                girafPictogram.setImageModel(temp);
            }

            // Hide the title of the pictogram.
            // Notice: This is the title of the pictogram selected for the category and not the actual title of the category
            girafPictogram.hideTitle();

            // If the text was changed insert that into the edit text otherwise use selected category name
            if (changedText == null) {
                categoryTitle.setText(getSelectedCategory().getName());
            } else {
                categoryTitle.setText(changedText);
            }
        }
    }

    /**
     * Called whenever an item in the category list is clicked/selected
     */
    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        // Check if there is a previously selected view
        if (selectedCategoryAndViewItem != null) {

            // Set the content of the frame layout to the default fragment
            getSupportFragmentManager().popBackStack();

            selectedCategoryAndViewItem.getView().setBackgroundDrawable(null); // Remove the background

            // Check if the same category was selected twice (deselected)
            if (selectedCategoryAndViewItem.getCategory().getId() == id) {
                selectedCategoryAndViewItem = null;
                return;
            }
        }

        // Set the selected category and view item
        selectedCategoryAndViewItem = new CategoryAdapter.CategoryViewPair(categoryAdapter.getCategoryFromId(id), view);

        view.setBackgroundColor(this.getResources().getColor(R.color.giraf_page_indicator_active));

        // "Open" the fragment in the frame layout
        pushContent(CategoryDetailFragment.newInstance(id, getCurrentUser().getRole() == Profile.Roles.CHILD), R.id.categorytool_framelayout);
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
            intent.putExtra(INTENT_STRING_CURRENT_GUARDIAN_ID, guardianProfile.getId());
            startActivityForResult(intent, CREATE_CATEGORY_REQUEST);
        }
    }

    /**
     * Will be called when an opened activity returns
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        switch (requestCode) {

            // When returning from CreatCategoryActivity (internal intent)
            case CREATE_CATEGORY_REQUEST: {
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
                break;
            }
            // When returning from PictoSearch with single pictogram (external intent)
            case GET_SINGLE_PICTOGRAM: {

                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras(); // Get the data from the intent

                    // Check if there was returned any pictogram ids
                    if (data.hasExtra(PICTO_SEARCH_IDS_TAG)) {
                        lastAddedPictogramsIds.clear();

                        final long[] pictosearchIds = extras.getLongArray(PICTO_SEARCH_IDS_TAG);

                        for (long i : pictosearchIds) {
                            lastAddedPictogramsIds.add(i);
                        }

                        // If there were returned more than one pictogram tell the user that the first is used
                        if (lastAddedPictogramsIds.isEmpty()) {
                            Toast.makeText(this, getString(R.string.no_pictogram_selected), Toast.LENGTH_LONG).show();
                        } else {
                            if (lastAddedPictogramsIds.size() > 1) {
                                Toast.makeText(this, getString(R.string.multiple_pictogram_selected_first_used), Toast.LENGTH_LONG).show();
                            }
                            // Set the wanted pictogram to be what was returned form pictosearh
                            changedPictogram = helper.pictogramHelper.getById(lastAddedPictogramsIds.get(0));
                        }
                    }
                }
                break;
            }

            // When returning from PictoSearch with multiple pictograms (external intent)
            case GET_MULTIPLE_PICTOGRAMS: {

                // Make sure the request was successful
                if (resultCode == RESULT_OK) {

                    final Bundle extras = data.getExtras(); // Get the data from the intent

                    // Check if there was returned any pictogram ids
                    if (data.hasExtra(PICTO_SEARCH_IDS_TAG)) {

                        lastAddedPictogramsIds.clear();
                        for (long i : extras.getLongArray(PICTO_SEARCH_IDS_TAG)) {
                            lastAddedPictogramsIds.add(i);
                        }

                        // If no pictograms was returned tell the user
                        if (lastAddedPictogramsIds.isEmpty()) {
                            Toast.makeText(this, this.getString(R.string.no_pictograms_selected), Toast.LENGTH_SHORT).show();
                        } else {
                            // If the category has any profiles added ask who wants the change
                            if (getProfileWithCategoryList(getSelectedCategory()).size() > 0) {

                                // Get a list of profiles who has the category and a boolean sat to true
                                List<Pair<Profile, Boolean>> pairList = getProfileWithCategoryList(getSelectedCategory());
                                GirafProfileSelectorDialog addPictoGramDialog = GirafProfileSelectorDialog.newInstance(pairList, true, "Hvilke brugere skal have de valgt piktogrammer tilføjet? Alle er valgt fra starten", ADD_PICTOGRAMS_TO_CATEGORIES_DIALOG);
                                addPictoGramDialog.show(getSupportFragmentManager(), "" + ADD_PICTOGRAMS_TO_CATEGORIES_DIALOG);
                            } else {
                                new UpdatePictogramsInCategory(getProfileWithCategoryList(getSelectedCategory()), UpdatePictogramsInCategory.ADD_PICTOGRAMS, lastAddedPictogramsIds).execute();
                            }
                        }

                    }
                }
                break;
            }
        }
    }

    /**
     * Will be called when a confirm dialog is handled
     */
    @Override
    public void confirmDialog(final int methodID) {
        // Check if the confirmation is from the delete pictogram dialog
        if (methodID == CONFIRM_PICTOGRAM_DELETION_METHOD_ID) {
            new UpdatePictogramsInCategory(getProfileWithCategoryList(getSelectedCategory()), UpdatePictogramsInCategory.REMOVE_PICTOGRAMS, selectedPictogramsIdsInFragment).execute();
        } else if (methodID == DELTE_CATEGORY_CONFIRM_DIALOG) {
            new DeleteCategories(getSelectedCategory()).execute();
        }
    }

    /**
     * Used to fetch the currently selected category and its associated view
     */
    @Override
    public CategoryAdapter.CategoryViewPair getSelectedMutableCategoryViewPair() {
        return selectedCategoryAndViewItem;
    }

    /**
     * Will be called whenever a notification dialog is handled
     *
     * @param i the method id (response code)
     */
    @Override
    public void noticeDialog(int i) {
        if (i == NOTIFICATION_DIALOG_DO_NOTHING) {
            // Do nothing
        }
    }

    /**
     * Will be called whenever the user selects some profiles
     *
     * @param dialogIdentifier   the identifier of the dialog the user just handled
     * @param checkedProfileList a list of pairs of users and booleans representing the selected status for that specific user
     */
    @Override
    public void onProfilesSelected(int dialogIdentifier, List<Pair<Profile, Boolean>> checkedProfileList) {
        // Check if the dialog was a "copy to user" dialog
        if (dialogIdentifier == UPDATE_CITIZEN_CATEGORIES_DIALOG) {
            new SetCitizenCategories(checkedProfileList).execute();
        } else if (dialogIdentifier == ADD_PICTOGRAMS_TO_CATEGORIES_DIALOG) {
            new UpdatePictogramsInCategory(checkedProfileList, UpdatePictogramsInCategory.ADD_PICTOGRAMS, lastAddedPictogramsIds).execute();
        } else if (dialogIdentifier == REMOVE_PICTOGRAMS_FROM_CATEGORIES_DIALOG) {
            new UpdatePictogramsInCategory(checkedProfileList, UpdatePictogramsInCategory.REMOVE_PICTOGRAMS, selectedPictogramsIdsInFragment).execute();
        }
    }

    @Override
    public void onProfileSelected(final int i, final Profile profile) {
        if (i == CHANGE_USER_DIALOG) {
            // If it is a citizen profile
            if (getCurrentUser().getRole() == Profile.Roles.CHILD) {
                finish();
            }

            // Start a new activity with the selected child
            final Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra(getString(R.string.current_child_id), profile.getId());
            intent.putExtra(getString(R.string.current_guardian_id), guardianProfile.getId());
            startActivity(intent);
        }
    }

    @Override
    public void pictogramsUpdated(final List<Pictogram> selectedPictograms) {
        selectedPictogramsIdsInFragment.clear();
        for (Pictogram pictogram : selectedPictograms) {
            selectedPictogramsIdsInFragment.add(pictogram.getId());
        }

    }
}
