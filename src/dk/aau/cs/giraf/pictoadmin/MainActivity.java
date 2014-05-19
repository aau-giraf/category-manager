package dk.aau.cs.giraf.pictoadmin;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.categorylib.CatLibHelper;
import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GButtonProfileSelect;
import dk.aau.cs.giraf.gui.GColorPicker;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GGridView;
import dk.aau.cs.giraf.gui.GList;
import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.PictogramCategory;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.models.ProfileCategory;

import com.google.analytics.tracking.android.EasyTracker;

import android.view.animation.AnimationUtils;

/**
 * @author SW605f13 Parrot-group
 * This is the main class in the CAT application.
 */
@SuppressLint("DefaultLocale")
public class MainActivity extends Activity implements CreateCategoryListener{
    /**
     * Used for debugging.
     * @return True if debug mode enabled, false otherwise.
     */
    private boolean IS_DEBUG() {
        return false;
    }

    private static Context context;
    public static Context getContext () {
        return context;
    }

    // Active child and guardian.
	private Profile child = null;
	private Profile guardian = null;

    // Selected category, subcategry, pictogram.
	private Category selectedCategory = null;
	private Category selectedSubCategory = null;
	private Pictogram selectedPictogram = null;

	private List<Category> categoryList = new ArrayList<Category>();
	private List<Category> subcategoryList = new ArrayList<Category>();
	private List<Pictogram> pictogramList = new ArrayList<Pictogram>();

	private GList categoryGList;
	private GList subCategoryGList;
	private GGridView pictogramGGridView;

    private PictoAdminCategoryAdapter categoryAdapter;
    private PictoAdminCategoryAdapter subCategoryAdapter;
    private PictoAdapter pictogramAdapter;

	private int selectedLocation; // Stores the location of the last pressed item in any gridview
	private int newCategoryColor; // Hold the value set when creating a new category or sub-category
    private Pictogram newCategoryIcon; // Hold the value set when creating a new category or sub-category

    // Oasis/db access
    private Helper helper = new Helper(this);

    public enum Setting{TITLE, COLOR, ICON, DELETE, DELETEPICTOGRAM}

    private static final String TAG = "CAT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_admin_category);

		Bundle extras = getIntent().getExtras();

        if(IS_DEBUG() && extras == null) {
            extras = setupDebug();
        }

        // Ugyldig login (ikke startet gennem GIRAF)
		if(extras == null){
            showInvalidLoginThenExit();
		} else {
			getProfiles(extras);
            selectAndLoadChild();
		}

        // Start logging this activity
        EasyTracker.getInstance(this).activityStart(this);
	}

    /**
     * Show a profile selector and select a child.
     * Used at launch.
     */
    private void selectAndLoadChild() {
        if (child == null || child.getId() == -1) {
            final GProfileSelector selector = new GProfileSelector(this, guardian, null, false);
             selector.setOnListItemClick(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     child = helper.profilesHelper.getProfileById((int) l);
                     loadChildAndSetupGUI();
                     selector.dismiss();
                 }
             });
            try {
                selector.backgroundCancelsDialog(false);
            } catch (Exception e) {
                finish();
            }
            selector.show();
        } else {
            loadChildAndSetupGUI();
        }
    }

    /**
     * Load child information and setup the GUI.
     */
    private void loadChildAndSetupGUI() {
        loadCategoriesFromChild();
        setupChangeProfileButton();
        setupGUI();
        setupChildText();
    }

    /**
     * If not started by GIRAF, we do not log in, but say "bad login", and then exit.
     */
    private void showInvalidLoginThenExit() {
        GDialogAlert diag = new GDialogAlert(this,
             getString(R.string.dialog_title),
             new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
             });
        diag.show();
    }

    /**
     * We want to enter debug mode, so we use the first guardian, and his first child.
     * @return A bundle containing a default child and guardian.
     */
    private Bundle setupDebug() {
        Bundle extras = new Bundle();

        Profile guard = helper.profilesHelper.getGuardians().get(0);
        Profile kid = helper.profilesHelper.getChildrenByGuardian(guard).get(0);

        extras.putInt("currentChildID", kid.getId());
        extras.putInt("currentGuardianID", guard.getId());

        // Show that we are debugging
        TextView debugText = (TextView) this.findViewById(R.id.DebugText);
        debugText.setVisibility(View.VISIBLE);

        return extras;
    }


    /**
     * Load the categories associated with the active child.
     */
    private void loadCategoriesFromChild() {
        categoryList = helper.categoryHelper.getCategoriesByProfileId(child.getId());
        if(categoryList == null)
        {
            categoryList = new ArrayList<Category>();
        }
    }

    /**
     * Setup the category, subcategory, and pictogramList in the GUI
     */
    private void setupGUI() {
        categoryAdapter = new PictoAdminCategoryAdapter(categoryList, this);
        subCategoryAdapter = new PictoAdminCategoryAdapter(subcategoryList, this);
        pictogramAdapter = new PictoAdapter(pictogramList, this);

        // Setup category list
        categoryGList = (GList) findViewById(R.id.category_listview);
        categoryGList.setAdapter(categoryAdapter);
        setCategoryGListListeners();

        // Setup sub-category list
        subCategoryGList = (GList) findViewById(R.id.subcategory_listview);
        subCategoryGList.setAdapter(subCategoryAdapter);
        setSubCategoryGListListeners();

        // Setup pictogram grid
        pictogramGGridView = (GGridView) findViewById(R.id.pictogram_gridview);
        pictogramGGridView.setAdapter(pictogramAdapter);
        setPictogramGridViewListeners();
    }

    /**
     * Show which child is active.
     */
    private void setupChildText() {
        TextView currentChild = (TextView) findViewById(R.id.currentChildName);
        currentChild.setText(child.getName());
    }

    /**
     * Show a dialog saying that a citizen must be selected from the profile selector (because the guardian is sometimes also loaded as selectable).
     */
    private void showChooseChitizenDialog() {
        GDialogAlert diag = new GDialogAlert(this,
                "Vælg venligst en borger.",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Do nothing
                    }
                });
        diag.show();
    }

    /**
     * Run setup on the profile selector button.
     */
    private void setupChangeProfileButton() {
        GButtonProfileSelect profSelBut = (GButtonProfileSelect)findViewById(R.id.change_profile);
        profSelBut.setup(guardian, null, new GButtonProfileSelect.onCloseListener() {
            @Override
            public void onClose(Profile guardianProfile, Profile currentProfile) {
                // A child was not selected
                if (currentProfile == null) {
                    showChooseChitizenDialog();
                    return;
                }

                // The active child was selected
                if (currentProfile.getId() == child.getId()) {
                    return;
                }

                // Load the selected child information
                child = currentProfile;
                loadCategoriesFromChild();
                setupChangeProfileButton();

                subcategoryList = new ArrayList<Category>();
                pictogramList = new ArrayList<Pictogram>();

                categoryAdapter = new PictoAdminCategoryAdapter(categoryList, getApplicationContext());
                subCategoryAdapter = new PictoAdminCategoryAdapter(subcategoryList, getApplicationContext());
                pictogramAdapter = new PictoAdapter(pictogramList, getApplicationContext());

                categoryGList.setAdapter(categoryAdapter);
                subCategoryGList.setAdapter(subCategoryAdapter);
                pictogramGGridView.setAdapter(pictogramAdapter);

                setupChildText();
            }
        });
    }

    /**
     * Set listeners on the categoryGList.
     */
    private void setCategoryGListListeners() {
        categoryGList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                setupOnClickActions(v, position, 1);

            }
        });

        categoryGList.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
                SettingDialogFragment settingDialog = new SettingDialogFragment(MainActivity.this,
                        categoryList.get(position),
                        position, true, v, guardian, child);
                settingDialog.show(getFragmentManager(), "chooseSettings");
                setupOnClickActions(v, position, 1);
                return false;
            }
        });
    }

    /**
     * Set listeners on the subcategoryGList.
     */
    private void setSubCategoryGListListeners() {
        subCategoryGList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                setupOnClickActions(v, position, 0);
            }
        });

        subCategoryGList.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
                SettingDialogFragment settingDialog = new SettingDialogFragment(MainActivity.this,
                        subcategoryList.get(position),
                        position, false, v, guardian, child);
                settingDialog.show(getFragmentManager(), "chooseSettings");
                setupOnClickActions(v, position, 0);
                return false;
            }
        });
    }

    /**
     * Set listeners on the pictogramGGridView.
     */
    private void setPictogramGridViewListeners() {
        pictogramGGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                setupOnClickActions(v, position, 2);
            }
        });
    }

    private void setupOnClickActions(View v, int position, int id){
        v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.pop));
        updateSelected(v, position, id);
        updateButtonVisibility(v);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Stop logging this activity
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_admin_category, menu);
		return true;
	}

	/*
	 * The following methods handle the creation of new categories and sub-categories
	 * Made by sw*f13
	 */
	@Override
	public void onCatCreateDialogPositiveClick(DialogFragment dialog, String title, boolean isCategory) {
        // Ensure that a title has been set
        if (title.isEmpty()) {
            alertDialog(this, getString(R.string.error), getString(R.string.title_missing));
            return;
        }

        // Ensure that a color has been selected
        if (newCategoryColor == 0) {
            alertDialog(this, getString(R.string.error), getString(R.string.category_color_missing));
            return;
        }

        if(newCategoryIcon == null)
        {
            newCategoryIcon = new Pictogram();
        }

        // Check if a related category ("main category" or subcategory) with same name exists
        List<Category> relatedCategories = isCategory ? categoryList : subcategoryList;
        boolean categoryWithNameExists = false;
        for(Category sc : relatedCategories) {
            if (sc.getName().equalsIgnoreCase(title)) {
                categoryWithNameExists = true;
                break;
            }
        }

        if (categoryWithNameExists) {
            alertDialog(this, getString(R.string.error), getString(R.string.title_used,this));
            return;
        }

        if (isCategory) { // Create category
            if (categoryList.isEmpty()) {
                categoryList = new ArrayList<Category>();
            }

            Category cat = new Category(title, newCategoryColor, newCategoryIcon.getImage(), 0);
            categoryList.add(cat);
            if (!helper.categoryHelper.getCategories().contains(cat)) {
                helper.categoryHelper.insertCategory(cat);
            }
            helper.profileCategoryController.insertProfileCategory(new ProfileCategory(child.getId(), cat.getId()));

            categoryAdapter = new PictoAdminCategoryAdapter(categoryList, this);
            categoryGList.setAdapter(categoryAdapter);

//            categoryAdapter.notifyDataSetChanged();
        } else { // Create subcategory
            Category cat = new Category(title, newCategoryColor, newCategoryIcon.getImage(), selectedCategory.getId());
            subcategoryList.add(cat);
            if (!helper.categoryHelper.getCategoriesByProfileId(child.getId()).contains(cat)) {
                helper.categoryHelper.insertCategory(cat);
            }

            subCategoryAdapter.notifyDataSetChanged();
        }

        dialog.dismiss();
        findViewById(R.id.back_dim_layout).setVisibility(View.GONE);

		newCategoryColor = 0;
        newCategoryIcon = null;
	}

	@Override
	public void onCatCreateDialogNegativeClick(DialogFragment dialog) {
		// Do nothing
        dialog.dismiss();
        findViewById(R.id.back_dim_layout).setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

    /**
     * Called when selecting a color for a new category
     * @param view
     */
	public void setNewCategoryColor(View view) {
        GColorPicker diag = new GColorPicker(view.getContext(), new GColorPicker.OnOkListener() {
            @Override
            public void OnOkClick(GColorPicker diag, int color) {
                newCategoryColor = color;
            }
        });
        diag.SetCurrColor(0xFF000000);
        diag.show();
	}

    public void setNewCategoryIcon(View view) {
        openPictoSearch(2); //Opens PictoSearch
    }

    /**
     * Leftover from sw/2013. We recommend you get rid of it and handle the cases separately (somehow).
     * @param category
     * @param pos
     * @param isCategory
     * @param setting
     */
	public void updateSettings(Category category, int pos, boolean isCategory, Setting setting) {

        switch (setting){
            case TITLE:
                if(isCategory) {
                    updateTitle(category, pos, categoryList);
                }
                else {
                    updateTitle(category, pos, subcategoryList);
                }
                break;
            case COLOR:
                updateColor(category, pos, isCategory);
                break;
            case ICON:
                updateIcon(category, pos, isCategory);
                break;
            case DELETE:
                if(isCategory){
                    subcategoryList.removeAll(subcategoryList);
                    helper.categoryHelper.removeCategory(selectedCategory);
                    categoryList.remove(pos);
                    selectedCategory = null;
                }
                else {
                    subcategoryList.remove(pos);
                }
                pictogramList.removeAll(pictogramList);
                selectedSubCategory = null;
                break;
            case DELETEPICTOGRAM:
                if(selectedSubCategory == null){
                    helper.pictogramCategoryHelper.removePictogramCategory(selectedCategory.getId(), selectedPictogram.getId());
                }
                else{
                    helper.pictogramCategoryHelper.removePictogramCategory(selectedSubCategory.getId(), selectedPictogram.getId());
                }
                selectedPictogram = null;
                break;
        }
		if(isCategory){
            categoryAdapter.notifyDataSetChanged();
		}

//		subCategoryGList.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, this));
//		pictogramGGridView.setAdapter(new PictoAdapter(pictogramList, this));
        subCategoryAdapter.notifyDataSetChanged();
        pictogramAdapter.notifyDataSetChanged();
		updateButtonVisibility(null);
	}

	private void updateTitle(Category changedCategory, int pos, List<Category> list) {
        String catName = changedCategory.getName();
        for(Category c : list) {
            if(c.getName().equals(catName)) {
                alertDialog(this, getString(R.string.error), getString(R.string.name_used));
                return;
            }
        }
        list.get(pos).setName(catName);
	}

    /**
     * Edit a category
     * @param catToEdit Which category to edit.
     * @param copyPropsFromThis The new properties to copy.
     * @param isCategory True if it is a category, false if it is a subcategory.
     */
    public void editCategory(Category catToEdit, Category copyPropsFromThis, boolean isCategory) {
        List<Category> list = (isCategory ?
            categoryList :
            subcategoryList);

        // If name change, verify that no other category has the same name
        if (!catToEdit.getName().equals(copyPropsFromThis.getName())) {
            for (Category cat : list) {
                if (cat.getName().equals(copyPropsFromThis.getName())) { // Remember string equality in java is reference equality. We spent way too much time confused before remembering.
                    alertDialog(this, getString(R.string.error), getString(R.string.name_used));
                    return;
                }
            }
        }

        // Copy the properties
        catToEdit.setName(copyPropsFromThis.getName());
        catToEdit.setColour(copyPropsFromThis.getColour());
        catToEdit.setImage(copyPropsFromThis.getImage());
        CategoryController catControl = new CategoryController(this);

        // Modify the category.
        catControl.modifyCategory(catToEdit);

        // Update the GUI.
        if (isCategory) {
            categoryAdapter.notifyDataSetChanged();
        } else {
            subCategoryAdapter.notifyDataSetChanged();
        }
    }

	private void updateColor(Category category, int pos, boolean isCategory) {
		if(isCategory){
			categoryList.set(pos, category);
		}
	}

    /**
     * Change the icon on a category or subcategory
     * @param category The category to change icon on.
     * @param pos Position in the categorylist/subcategorylist.
     * @param isCategory True if it is a category, false if it is a subcategory.
     */
	private void updateIcon(Category category, int pos, boolean isCategory) {
		if(isCategory){
			categoryList.set(pos, category);
		}
		else {
			subcategoryList.set(pos, category);
		}
	}

    /**
     * Load profiles from the Bundle received at launch.
     * @param extras
     */
	private void getProfiles(Bundle extras) {
        int childId = extras.getInt("currentChildID");
        int guardianId = extras.getInt("currentGuardianID");

        if (childId != -1) {
            helper.profilesHelper.getProfileById(childId);
        }

        guardian = helper.profilesHelper.getProfileById(guardianId);
	}

    /**
     * Handle which buttons are visible. Created by sw/2013
     * @param v
     */
	public void updateButtonVisibility(View v) {
        GButton delcat = (GButton) findViewById(R.id.delete_selected_category_button);
        GButton delsub = (GButton) findViewById(R.id.delete_selected_subcategory_button);
        GButton delpic = (GButton) findViewById(R.id.delete_selected_picture_button);
        GButton addsub = (GButton) findViewById(R.id.add_new_subcategory_button);
        GButton addpic = (GButton) findViewById(R.id.add_new_picture_button);

		if(selectedCategory != null) {
			delcat.setVisibility(View.VISIBLE);
			addsub.setVisibility(View.VISIBLE);
			addpic.setVisibility(View.VISIBLE);
		}
		else if(categoryList.size() == 1) {
			delcat.setVisibility(View.GONE);
		}
        else{
            delcat.setVisibility(View.GONE);
            addsub.setVisibility(View.INVISIBLE);
            addpic.setVisibility(View.INVISIBLE);
        }
		if(selectedSubCategory != null) {
			delsub.setVisibility(View.VISIBLE);
		}
		else {
			delsub.setVisibility(View.GONE);
		}
		if(selectedPictogram != null) {
			delpic.setVisibility(View.VISIBLE);
		}
		else {
			delpic.setVisibility(View.GONE);
		}
	}

	/*
	 * DONE: This method update what is currently selected (category, sub-category or pictogram)
	 * Made by sw*f13
	 */
	private void updateSelected(View view, int position, int id) {
		selectedLocation = position;

        //id 2 is the Grid with pictogramList
		if(id == 2) {
			selectedPictogram = pictogramList.get(position);
		}
        //id 1 is the list of categories
		if(id == 1) {
			selectedCategory    = categoryList.get(position);
			selectedSubCategory = null;
			selectedPictogram   = null;

            subcategoryList = helper.categoryHelper.getSubcategoriesByCategory(selectedCategory);
            pictogramList = helper.pictogramHelper.getPictogramsByCategory(selectedCategory);

            subCategoryAdapter = new PictoAdminCategoryAdapter(subcategoryList, this);
            pictogramAdapter = new PictoAdapter(pictogramList, this);
            subCategoryGList.setAdapter(subCategoryAdapter);
            pictogramGGridView.setAdapter(pictogramAdapter);
		}
        //id 0 is the list of subcategories
		else if(id == 0) {
			selectedSubCategory = subcategoryList.get(position);
			selectedPictogram   = null;

            pictogramList = helper.pictogramHelper.getPictogramsByCategory(subcategoryList.get(position));
            pictogramAdapter = new PictoAdapter(pictogramList, this);
            pictogramGGridView.setAdapter(pictogramAdapter);
        }
	}

    /**
     * Create a category.
     * @param view
     */
	public void createCategory(View view) {
        CreateCategoryDialog createDialog = new CreateCategoryDialog(R.string.category);
        createDialog.show(getFragmentManager(),"dialog");
        findViewById(R.id.back_dim_layout).setVisibility(View.VISIBLE);
	}

    /**
     * Is the user sure s/he wants to delete the selected category?
     * @param view
     */
	public void askDeleteCategory(View view) {
        GDialogMessage deleteDialog = new GDialogMessage(this,
                getString(R.string.confirm_delete),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Delete the category.
                        helper.categoryHelper.removeCategory(selectedCategory);
                        categoryList.remove(selectedLocation);

                        selectedCategory = null;
                        selectedSubCategory = null;
                        subcategoryList.clear();
                        pictogramList.clear();

                        // Upadte the GUI
                        categoryAdapter.notifyDataSetChanged();
                        subCategoryAdapter.notifyDataSetChanged();
                        pictogramAdapter.notifyDataSetChanged();
                        updateButtonVisibility(null);
                    }
                });
        deleteDialog.show();
	}

    /**
     * Create a subcategory.
     * @param view
     */
	public void createSubCategory(View view) {
        CreateCategoryDialog createDialog = new CreateCategoryDialog(R.string.subcategory, true);
        createDialog.show(getFragmentManager(),"dialog");
        findViewById(R.id.back_dim_layout).setVisibility(View.VISIBLE);
	}

    /**
     * Is the user sure s/he want to delete the selected subcategory?
     * @param view
     */
	public void askDeleteSubCategory(View view) {
        GDialogMessage deleteDialog = new GDialogMessage(this,
                getString(R.string.confirm_delete),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        // Delete the subcategory
                        helper.categoryHelper.removeCategory(selectedSubCategory);
                        subcategoryList.remove(selectedLocation);

                        selectedSubCategory = null;
                        pictogramList.clear();

                        // Update the GUI
                        subCategoryAdapter.notifyDataSetChanged();
                        pictogramAdapter.notifyDataSetChanged();
                        updateButtonVisibility(null);
                    }
                });
        deleteDialog.show();
	}

    public void createPictogram(View view) {
        openPictoSearch(1);
    }

	// Goes to pictogram search function
	public void openPictoSearch(int requestcode) {
		Intent request = new Intent();

		try{
			request.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
			request.putExtra("purpose", "CAT");

			startActivityForResult(request, requestcode);
		}
		catch (Exception e) {

            GDialogAlert diag = new GDialogAlert(this,
                    getString(R.string.pictosearch_unavaiable),
                    getString(R.string.ask_installed),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
            diag.show();
		}
	}

    /**
     * Is the user sure s/he wants to delete the selected pictogram
     * @param view
     */
	public void askDeletePictogram(View view) {
        GDialog deleteDialog = new GDialogMessage(view.getContext(), R.drawable.content_discard, getString(R.string.confirm_delete), "", new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (selectedSubCategory == null) {
                    // Remove the pictogram from the selected category
                    helper.pictogramCategoryHelper.removePictogramCategory(selectedCategory.getId(), selectedPictogram.getId());
                } else {
                    // Remove the pictogram from the selected subcategory.
                    helper.pictogramCategoryHelper.removePictogramCategory(selectedSubCategory.getId(), selectedPictogram.getId());
                }

                // Update the GUI
                pictogramList.remove(selectedLocation);
                selectedPictogram = null;
                pictogramAdapter.notifyDataSetChanged();
            }
        });
        deleteDialog.show();
	}

    // Made by sw*f13
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Pictogram pictoHolder = new Pictogram();
        if(data == null)
        {
            return;
        }
        Bundle extras = data.getExtras();

        if(data.hasExtra("checkoutIds"))
        {
            int[] checkoutIds = extras.getIntArray("checkoutIds");

            switch (requestCode){
                case 1: //Add pictogram to category or subcategory
                    if(selectedSubCategory == null){
                        checkAndAddPictograms(checkoutIds,selectedCategory);
                    }
                    else{
                        checkAndAddPictograms(checkoutIds,selectedSubCategory);
                    }
                    pictogramAdapter = new PictoAdapter(pictogramList, this);
                    pictogramGGridView.setAdapter(pictogramAdapter);
                    break;

                case 2: //Add icon to category or subcategory, that has just been created
                    if(checkoutIds.length>=1)
                    {
                        newCategoryIcon = helper.pictogramHelper.getPictogramById(checkoutIds[0]);
                        if(checkoutIds.length>1)
                        {
                            alertDialog(this, "Kun et pictogram kan vælges som ikon til kategorien.", "Det øverste i listen er valgt.");
                        }
                    }
                    break;

                case 3: //Change icon on existing category
                    if(checkoutIds.length>=1)
                    {
                        pictoHolder = helper.pictogramHelper.getPictogramById(checkoutIds[0]);
                        if(checkoutIds.length>1)
                        {
                            alertDialog(this, "Kun et pictogram kan vælges som ikon til kategorien.", "Det øverste i listen er valgt.");
                        }
                    }

                    selectedCategory.setImage(pictoHolder.getImage());
                    updateIcon(selectedCategory, selectedLocation, true);
                    categoryAdapter.notifyDataSetChanged();
                    helper.categoryHelper.modifyCategory(selectedCategory);
                    break;

                case 4: //Change icon on existing subcategory
                    if(checkoutIds.length>=1)
                    {
                        pictoHolder = helper.pictogramHelper.getPictogramById(checkoutIds[0]);
                        if(checkoutIds.length>1)
                        {
                            alertDialog(this, "Kun et pictogram kan vælges som ikon til kategorien.", "Det øverste i listen er valgt.");
                        }
                    }

                    selectedSubCategory.setImage(pictoHolder.getImage());
                    updateIcon(selectedSubCategory, selectedLocation, false);
                    subCategoryAdapter.notifyDataSetChanged();
                    helper.categoryHelper.modifyCategory(selectedSubCategory);
                    break;
            }

        }

    }

    private void alertDialog(Context context, String headline, String message){
        GDialogAlert diag = new GDialogAlert(context,
                R.drawable.ic_launcher,
                headline,
                message,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
        diag.show();
    }

    private void checkAndAddPictograms(int[] checkoutIds, Category category) {
        boolean legal;
        for(int id : checkoutIds){
            legal = true;

            for(Pictogram p : pictogramList){
                if(p.getId() == id){
                    legal = false;
                    break;
                }
            }
            if(legal){
                PictogramCategory piccat = new PictogramCategory(helper.pictogramHelper.getPictogramById(id).getId(), category.getId());
                helper.pictogramCategoryHelper.insertPictogramCategory(piccat);
                pictogramList = helper.pictogramHelper.getPictogramsByCategory(category);
            }
        }
    }

    /**
     * Called when pressing the close button (top right corner).
     * @param view
     */
    public void onCloseButtonPress(View view) {
        finish();
    }
}
