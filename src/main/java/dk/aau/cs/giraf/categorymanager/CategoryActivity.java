package dk.aau.cs.giraf.categorymanager;

import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.categorymanager.fragments.CategoryDetailFragment;
import dk.aau.cs.giraf.categorymanager.fragments.InitialFragment;
import dk.aau.cs.giraf.categorymanager.fragments.InitialFragmentSpecificUser;
import dk.aau.cs.giraf.categorymanager.showcase.ShowcaseManager;
import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafConfirmDialog;
import dk.aau.cs.giraf.gui.GirafInflatableDialog;
import dk.aau.cs.giraf.gui.GirafNotifyDialog;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Department;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.PictogramCategory;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public class CategoryActivity extends GirafActivity implements AdapterView.OnItemClickListener, InitialFragment.OnFragmentInteractionListener, InitialFragmentSpecificUser.OnFragmentInteractionListener, CategoryAdapter.SelectedCategoryAware, GirafConfirmDialog.Confirmation, GirafInflatableDialog.OnCustomViewCreatedListener, GirafNotifyDialog.Notification {

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

    // TODO - Fix access modifier for constants

    // Identifiers used to create fragments
    private static final String CATEGORY_SETTINGS_TAG = "CATEGORY_SETTINGS_TAG";

    // Helper that will be used to fetch profiles
    private final Helper helper = new Helper(this);

    // Profiles of which the categories will be loaded from
    private Profile childProfile;
    private Profile guardianProfile;

    // Edit category dialog elements
    GirafInflatableDialog editDialog;
    private Pictogram changedPictogram; // Returned from pictosearch
    private EditText categoryTitle; // The textView
    private String changedText; // The text before openeing pictosearch

    // View to contain categories
    private ListView categoryContainer;

    // List of categories
    private List<Category> categoryList;

    // Save the current category and its adapters
    private CategoryAdapter categoryAdapter;
    private CategoryAdapter.CategoryViewPair selectedCategoryAndViewItem = null;

    /**
     * Will be called every time the back-button is pressed
     * Used to handle the fragment-stack properly
     */
    @Override
    public void onBackPressed() {

        // Check if there is a previously selected view and if there is no popup
        if (selectedCategoryAndViewItem != null && getSupportFragmentManager().findFragmentByTag(CATEGORY_SETTINGS_TAG) == null) {
            // Set the selected category to "null" and set background to in-active
            selectedCategoryAndViewItem.getView().setBackgroundColor(this.getResources().getColor(R.color.giraf_page_indicator_inactive));
            selectedCategoryAndViewItem = null;
        }

        super.onBackPressed();
    }

    @Override
    public void editCustomView(ViewGroup viewGroup, int i) {
        switch (i) {
            case EDIT_CATEGORY_DIALOG:
                // Finds the views
                GirafPictogramItemView girafPictogram = (GirafPictogramItemView) viewGroup.findViewById(R.id.category_pictogram);
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

                // If the text was changed insert that into the edittex otherwise use selected category name
                if (changedText == null) {
                    categoryTitle.setText(getSelectedCategory().getName());
                } else {
                    categoryTitle.setText(changedText);
                }

                break;
        }
    }

    /**
     * Used to load categories into the category container (left side)
     */
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
            categoryList = result;
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

        // Test if the activity was started correctly
        if (extras == null) {
            Toast.makeText(CategoryActivity.this, String.format(getString(R.string.error_must_be_started_from_giraf), getString(R.string.categorymanager)), Toast.LENGTH_SHORT).show();

            // The activity was not started correctly, now finish it!
            finish();
            return;
        } else {
            int childId = extras.getInt(getString(R.string.current_child_id));
            int guardianId = extras.getInt(getString(R.string.current_guardian_id));

            if (childId != -1) {
                childProfile = helper.profilesHelper.getProfileById(childId);
            }

            if (guardianId != -1) {
                guardianProfile = helper.profilesHelper.getProfileById(guardianId);
            }
        }

        Profile currentUserProfile = getCurrentUser();

        // Change the title of the action-bar and content of right side depending on what type of categories are being modified
        if (currentUserProfile != null && getCurrentUser().getRole() == Profile.Roles.CHILD) {
            // Change the title bar text
            setActionBarTitle(String.format(getString(R.string.categories_for), currentUserProfile.getName()));

            // Set the content of the frame layout to the default fragment
            setContent(InitialFragmentSpecificUser.newInstance(getCurrentUser()), R.id.categorytool_framelayout);
        } else {
            // Find the department for the guardian
            Department department = helper.departmentsHelper.getDepartmentById(currentUserProfile.getDepartmentId());

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
        if (guardianProfile != null) {

            // Check if the user currently signed in is a guardian
            if (getCurrentUser().getRole() != Profile.Roles.CHILD) {
                // Add the change-user button to the top-bar
                GirafButton changeUserGirafButton = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_change_user));

                // Method to use whenever the change user-button is pressed
                changeUserGirafButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Create the new profile editor. Note that it is important that guardians are not shown in this list.
                        final GProfileSelector profileSelectorDialog = new GProfileSelector(CategoryActivity.this, guardianProfile, null, false);

                        // Method to use whenever the user has selected a button
                        profileSelectorDialog.setOnListItemClick(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Profile selectedProfile = helper.profilesHelper.getProfileById((int) id);

                                // Make sure that the selected profile is a child
                                if (selectedProfile.getRole() == Profile.Roles.CHILD) {
                                    // Dismiss the dialog
                                    profileSelectorDialog.dismiss();

                                    // Start a new activity with the selected child
                                    Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
                                    intent.putExtra(getString(R.string.current_child_id), selectedProfile.getId());
                                    intent.putExtra(getString(R.string.current_guardian_id), guardianProfile.getId());
                                    startActivity(intent);
                                }
                            }
                        });

                        // Show the dialog
                        profileSelectorDialog.show();
                    }
                });

                addGirafButtonToActionBar(changeUserGirafButton, GirafActivity.LEFT);
            }
            // The user is signed in as a child
            else {

            }

        }

    }

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
    }

    /*
     * Methods for button clicks below
     */

    /**
     * Called when a category is selected and the delete button is pressed
     */
    public void onDeleteCategoryClicked(final View view) {

        helper.categoryHelper.removeCategory(getSelectedCategory()); // Remove from DB
        categoryList.remove(getSelectedCategory()); // Remove from list in adapter
        categoryAdapter.notifyDataSetChanged(); // Tell the adapter to update

        // TODO - Check if any children are affected by this, and ask if they still want to delete it
        Toast.makeText(CategoryActivity.this, "Kategorien blev slettet", Toast.LENGTH_SHORT).show();
        editDialog.dismiss();
    }

    /**
     * Called when a category is selected and the save button is pressed
     */
    public void onSaveCategoryClicked(final View view) {

        // Update the category
        if (changedPictogram != null) {
            getSelectedCategory().setImage(changedPictogram.getImage());
        }

        getSelectedCategory().setName(categoryTitle.getText().toString());
        helper.categoryHelper.modifyCategory(getSelectedCategory());

        // TODO - Update the children affected by this update

        // Tell the adapter that i updated its content
        categoryAdapter.notifyDataSetChanged();

        changedText = null; // Reset the changed text so that next edit wont have this text

        // Dismiss the dialog
        editDialog.dismiss();

        Toast.makeText(CategoryActivity.this, "Kategorien blev gemt", Toast.LENGTH_SHORT).show();

    }

    /**
     * Called when a category is selected and when the settings buttons is pressed
     *
     * @param view needed for onClickListner
     */
    public void onSettingsButtonClicked(View view) {
        // Create the dialog
        editDialog = GirafInflatableDialog.newInstance(String.format(getString(R.string.settings_for), getSelectedCategory().getName()),
                getString(R.string.settings_dialog_description),
                R.layout.category_settings_dialog, EDIT_CATEGORY_DIALOG);

        // Sho the dialog
        editDialog.show(getSupportFragmentManager(), CATEGORY_SETTINGS_TAG);

    }

    /**
     * Called when the pictogram is clicked
     *
     * @param view needed for onClickListner
     */
    public void onEditCategoryPictogramClicked(View view) {

        // Reset the returned value
        changedPictogram = null;
        changedText = categoryTitle.getText().toString();

        Intent request = new Intent(); // A intent request

        // Try to send the intent
        try {
            // Sets properties on the intent
            request.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
            request.putExtra(PICTO_SEARCH_PURPOSE_TAG, PICTO_SEARCH_SINGLE_TAG);

            // Sends the intent
            startActivityForResult(request, GET_SINGLE_PICTOGRAM);
        } catch (Exception e) {

            Toast.makeText(this, "Could not open PictoSearch", Toast.LENGTH_SHORT).show();
            // TODO - Open notify dialog instead of toast
        }
    }

    /**
     * When a person clicks the add button to add a pictogram
     *
     * @param view
     */
    public void onAddButtonClick(View view) {
        Intent request = new Intent(); // A intent request

        // Try to send the intent
        try {
            // Sets properties on the intent
            request.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
            request.putExtra(PICTO_SEARCH_PURPOSE_TAG, PICTO_SEARCH_MULTI_TAG);

            // Sends the intent
            startActivityForResult(request, GET_MULTIPLE_PICTOGRAMS);
        } catch (Exception e) {

            Toast.makeText(this, "Could not open PictoSearch", Toast.LENGTH_SHORT).show();
            // TODO - Open notify dialog instead of toast
        }
    }

    /*
     * Methods required from interfaces below
     *//**/

    /**
     * Called whenever an item in the category list is clicked/selected
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            intent.putExtra("currentGuardianID", guardianProfile.getId());
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
                        // TODO pictosearch should use longs instead of integers
                        int[] pictogramIds = extras.getIntArray(PICTO_SEARCH_IDS_TAG);
                        // TODO Update when pictosearch changes how they return a single pictogram

                        // If there were returned more than one pictogram tell the user that the first is used
                        if (pictogramIds.length > 1) {
                            Toast.makeText(this, getString(R.string.multiple_pictogram_selected_first_used), Toast.LENGTH_LONG).show();
                        } else if (pictogramIds.length < 1) {
                            Toast.makeText(this, getString(R.string.no_pictogram_selected), Toast.LENGTH_LONG).show();
                        } else {
                            // Set the wanted pictogram to be what was returned form pictosearh
                            changedPictogram = helper.pictogramHelper.getPictogramById(pictogramIds[0]);
                        }
                    }
                }
                break;
            }

            // When returning from PictoSearch with multiple pictograms (external intent)
            case GET_MULTIPLE_PICTOGRAMS: {

                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras(); // Get the data from the intent

                    // Check if there was returned any pictogram ids
                    if (data.hasExtra(PICTO_SEARCH_IDS_TAG)) {
                        // TODO pictosearch should use longs instead of integers
                        int[] pictogramIds = extras.getIntArray(PICTO_SEARCH_IDS_TAG);

                        // Foreach pictogramid insert them to the currently selected category
                        for (int id : pictogramIds) {
                            helper.pictogramCategoryHelper.insertPictogramCategory(
                                    new PictogramCategory(id, getSelectedCategory().getId())
                            );
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
            final CategoryDetailFragment categoryDetailFragment = (CategoryDetailFragment) getSupportFragmentManager().findFragmentById(R.id.categorytool_framelayout);
            categoryDetailFragment.confirmDialog(methodID);
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

    private Category getSelectedCategory() {
        return selectedCategoryAndViewItem.getCategory();
    }
}
