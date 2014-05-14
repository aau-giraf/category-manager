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
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;

import com.google.analytics.tracking.android.EasyTracker;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * @author SW605f13 Parrot-group
 * This is the main class in the CAT application.
 */
@SuppressLint("DefaultLocale")
public class MainActivity extends Activity implements CreateCategoryListener{
    private boolean IS_DEBUG() {
        return true;
    }

	private Profile child;
	private Profile guardian;

	private Category selectedCategory = null;
	private Category selectedSubCategory = null;
	private Pictogram selectedPictogram = null;

	private List<Category> categoryList = new ArrayList<Category>();
	private List<Category> subcategoryList = new ArrayList<Category>();
	private List<Pictogram> pictograms = new ArrayList<Pictogram>();

	private GList categoryGrid;
	private GList subcategoryGrid;
	private GGridView pictogramGrid;

    private boolean isIcon = false;
	private int selectedLocation; // Stores the location of the last pressed item in any gridview
	private int newCategoryColor; // Hold the value set when creating a new category or sub-category
    private Pictogram newCategoryIcon; // Hold the value set when creating a new category or sub-category

	private CategoryController categoryController = new CategoryController(this);
	private ProfileController profileController = new ProfileController(this);
    private PictogramController pictogramController = new PictogramController(this);
    private CatLibHelper catlibhelp = new CatLibHelper(this);

	private MessageDialogFragment message;

    public enum Setting{TITLE, COLOR, ICON, DELETE, DELETEPICTOGRAM}

    private static final String TAG = "CAT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category);

		Bundle extras = getIntent().getExtras();
        if(IS_DEBUG() && extras == null) {
            extras = setupDebug(extras);
        }

        // "Ugyldige login informationer"
		if(extras == null){
            invalidLoginExit();
		}
		else{
			getProfiles(extras);
            VerifyChildSelectedOnLaunch();
            loadChildProfile();
            setupGUI();
		}

        // Start logging this activity
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

    private void VerifyChildSelectedOnLaunch() {
        // Will be used when the new profile selector is implemented launcher
        if (child.getId() == -1) {
            GDialogAlert diag = new GDialogAlert(this,
                    getString(R.string.select_citizen),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
            diag.show();
        }
    }

    private void invalidLoginExit() {
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

    private Bundle setupDebug(Bundle extras) {
        extras = new Bundle();

        extras.putInt("currentChildID", profileController.getChildren().get(0).getId());
        extras.putInt("currentGuardianID", profileController.getGuardians().get(0).getId());

        TextView debugText = (TextView) this.findViewById(R.id.DebugText);
        debugText.setVisibility(View.VISIBLE);
        return extras;
    }

    private void loadChildProfile() {
        categoryList = catlibhelp.getCategoriesFromProfile(child);
        if(categoryList == null)
        {
            categoryList = new ArrayList<Category>();
        }
    }

    private void setupGUI() {
        // Setup category gridview
        categoryGrid = (GList) findViewById(R.id.category_listview);
        setCategoryGridAdapter();
        setCategoryGridListeners();

        // Setup sub-category gridview
        subcategoryGrid = (GList) findViewById(R.id.subcategory_listview);
        setSubCategoryGridListeners();

        // Setup pictogram gridview
        pictogramGrid = (GGridView) findViewById(R.id.pictogram_gridview);
        setPictogramGridListeners();

        setupChangeProfileButton();

        setupChildText();
    }

    private void setupChildText() {
        TextView currentChild = (TextView) findViewById(R.id.currentChildName);
        currentChild.setText(child.getName());
    }

    private void showChooseChitizenDialog() {
        GDialogAlert diag = new GDialogAlert(this,
                "Vælg venligst en borger.",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
        diag.show();
    }

    private void setupChangeProfileButton() {
        GButtonProfileSelect profSelBut = (GButtonProfileSelect)findViewById(R.id.change_profile);
        profSelBut.setup(guardian, null, new GButtonProfileSelect.onCloseListener() {
            @Override
            public void onClose(Profile guardianProfile, Profile currentProfile) {
                if (currentProfile == null) {
                    showChooseChitizenDialog();
                    return;
                }

                if (currentProfile.getId() == child.getId()) {
                    return;
                }

                child = currentProfile;
                loadChildProfile();
                setupChangeProfileButton();

                subcategoryList = new ArrayList<Category>();
                pictograms = new ArrayList<Pictogram>();
                subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, findViewById(R.id.category_listview).getContext()));
                pictogramGrid.setAdapter(new PictoAdapter(pictograms, findViewById(R.id.category_listview).getContext()));

                setupChildText();
                setCategoryGridAdapter();
            }
        });
    }

    private void setCategoryGridAdapter() {
        if(categoryList != null){
            categoryGrid.setAdapter(new PictoAdminCategoryAdapter(categoryList, this));
        }
    }

    private void setCategoryGridListeners() {
        categoryGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.pop));
                updateSelected(v, position, 1);
                updateButtonVisibility(v);

            }
        });

        categoryGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
                SettingDialogFragment settingDialog = new SettingDialogFragment(MainActivity.this,
                        categoryList.get(position),
                        position, true, v, guardian, child);
                settingDialog.show(getFragmentManager(), "chooseSettings");
                return false;
            }
        });
    }

    private void setSubCategoryGridListeners() {
        subcategoryGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.pop));
                updateSelected(v, position, 0);
                updateButtonVisibility(v);
            }
        });
        subcategoryGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
                SettingDialogFragment settingDialog = new SettingDialogFragment(MainActivity.this,
                        subcategoryList.get(position),
                        position, false, v, guardian, child);
                settingDialog.show(getFragmentManager(), "chooseSettings");
                return false;
            }
        });
    }

    private void setPictogramGridListeners() {
        pictogramGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.pop));
                updateSelected(v, position, 2);
                updateButtonVisibility(v);
            }
        });
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

	@Override
	protected void onPause() {
		super.onPause();
	}

	/*
	 * The following methods handle the creation of new categories and sub-categories
	 */
	@Override
	public void onCatCreateDialogPositiveClick(DialogFragment dialog, String title, boolean isCategory) {
        // Ensure that a title has been set
        if (title.isEmpty()) {
            message = new MessageDialogFragment(R.string.title_missing,this);
            message.show(getFragmentManager(), "missingTitle");
            return;
        }

        // Ensure that a color has been selected
        if (newCategoryColor == 0) {
            message = new MessageDialogFragment(R.string.category_color_missing, this);
            message.show(getFragmentManager(), "categoryColorMissing");
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
            message = new MessageDialogFragment(R.string.title_used,this);
            message.show(getFragmentManager(), "usedTitle");
            return;
        }

        if (isCategory) { // Create category
            if (categoryList.isEmpty()) {
                categoryList = new ArrayList<Category>();
            }

            Category cat = new Category(title, newCategoryColor, newCategoryIcon.getImage(), 0);

            categoryList.add(cat); // IMPORTANT: hvor null PictoFactory.getPictogram(this, 1))

            catlibhelp.addCategoryToProfile(child, cat);
            PictoAdminCategoryAdapter pc = new PictoAdminCategoryAdapter(categoryList, this);

            categoryGrid.setAdapter(pc);
        } else { // Create subcategory
            Category cat = new Category(title, newCategoryColor, newCategoryIcon.getImage(), selectedCategory.getId());
            subcategoryList.add(cat);
            catlibhelp.giveCategorySuperCategory(selectedCategory, cat);
            subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, this));
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
	public void setNewCategoryColor(View view)
	{
        GColorPicker diag = new GColorPicker(view.getContext(), new GColorPicker.OnOkListener() {
            @Override
            public void OnOkClick(GColorPicker diag, int color) {
                newCategoryColor = color;
            }
        });
        diag.SetCurrColor(0xFF000000);
        diag.show();
	}

    public void setNewCategoryIcon(View view)
    {
        isIcon = true;
        createPictogram(view); //Opens PictoSearch
    }

	/*
	 * The following methods handle updating of categories and sub-categories. This occurs when long-clicking either
	 * a category or sub-category. Depending on the setting parameter, individual methods for updating is called
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
                    catlibhelp.deleteCategory(selectedCategory);
                    categoryList.remove(pos);
                    selectedCategory = null;
                }
                else {
                    subcategoryList.remove(pos);
                }
                pictograms.removeAll(pictograms);
                selectedSubCategory = null;
                break;
            case DELETEPICTOGRAM:
                if(selectedSubCategory == null){
                    catlibhelp.deletePictogramFromCategory(selectedPictogram, selectedCategory);
                }
                else{
                    catlibhelp.deletePictogramFromCategory(selectedPictogram, selectedSubCategory);
                }
                selectedPictogram = null;
                break;
        }
		if(isCategory){
			categoryGrid.setAdapter(new PictoAdminCategoryAdapter(categoryList, this));
		}

		subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, this));
		pictogramGrid.setAdapter(new PictoAdapter(pictograms, this));
		updateButtonVisibility(null);
	}

	private void updateTitle(Category changedCategory, int pos, List<Category> list) {
        String catName = changedCategory.getName();
        for(Category c : list) {
            if(c.getName().equals(catName)) {
                message = new MessageDialogFragment(R.string.name_used,this);
                message.show(getFragmentManager(), "invalidName");
                return;
            }
        }
        list.get(pos).setName(catName);
//        list.get(pos).setChanged(true);
	}

    public void editCategory(Category catToEdit, Category copyPropsFromThis, boolean isCategory) {
        List<Category> list = (isCategory ?
            categoryList :
            subcategoryList);

        if (!catToEdit.getName().equals(copyPropsFromThis.getName())) {
            for (Category cat : list) {
                if (cat.getName().equals(copyPropsFromThis.getName())) {
                    message = new MessageDialogFragment(R.string.name_used,this);
                    message.show(getFragmentManager(), "invalidName");
                    return;
                }
            }
        }

        catToEdit.setName(copyPropsFromThis.getName());
        catToEdit.setColour(copyPropsFromThis.getColour());
        catToEdit.setImage(copyPropsFromThis.getImage());
        CategoryController catControl = new CategoryController(this);
        catControl.modifyCategory(catToEdit);

        if (isCategory) {
            categoryGrid.setAdapter(new PictoAdminCategoryAdapter(categoryList, this));
        } else {
            subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, this));
        }
    }


    // DONE
	private void updateColor(Category category, int pos, boolean isCategory) {
//		category.setChanged(true);

		if(isCategory){
			categoryList.set(pos, category);
		}
	}

	// DONE
	private void updateIcon(Category category, int pos, boolean isCategory) {
//		category.setChanged(true);

		if(isCategory){
			categoryList.set(pos, category);
		}
		else {
			subcategoryList.set(pos, category);
		}
	}

	/*
	 * This method gets all extras in the extras bundle from the intent that started this activity
	 */
	private void getProfiles(Bundle extras) {
		if(extras.containsKey("currentChildID")) {
			child = profileController.getProfileById(extras.getInt("currentChildID"));
		}
		if(extras.containsKey("currentGuardianID")){
			guardian = profileController.getProfileById(extras.getInt("currentGuardianID"));
		}
	}

	/*
	 * DONE: The following method update the visibility of buttons. This depends on what is selected. This limits
	 * the buttons the user has access to, thereby limiting what the user can do (such as deletion and addition)
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
	 */
	private void updateSelected(View view, int position, int id) {
		selectedLocation = position;

        //id 2 is the Grid with pictograms
		if(id == 2) {
			selectedPictogram = pictograms.get(position);
		}
        //id 1 is the list of categories
		if(id == 1) {
			selectedCategory    = categoryList.get(position);
			selectedSubCategory = null;
			selectedPictogram   = null;

            subcategoryList = catlibhelp.getSubCategoriesFromCategory(selectedCategory);
            pictograms = catlibhelp.getPictogramsFromCategory(selectedCategory);

			subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, view.getContext()));
			pictogramGrid.setAdapter(new PictoAdapter(pictograms, view.getContext()));
		}
        //id 0 is the list of subcategories
		else if(id == 0) {
			selectedSubCategory = subcategoryList.get(position);
			selectedPictogram   = null;

			pictograms = catlibhelp.getPictogramsFromCategory(subcategoryList.get(position));
            pictograms = catlibhelp.getPictogramsFromCategory(subcategoryList.get(position));

			pictogramGrid.setAdapter(new PictoAdapter(pictograms, view.getContext()));
		}
	}

	/*
	 * DONE: The following methods handle the creation and deletion of categories and sub-categories
	 */
	public void createCategory(View view) {
        CreateCategoryDialog createDialog = new CreateCategoryDialog(R.string.category);
        createDialog.show(getFragmentManager(),"dialog");
        findViewById(R.id.back_dim_layout).setVisibility(View.VISIBLE);
	}

	// DONE
	public void askDeleteCategory(View view) {
        GDialogMessage deleteDialog = new GDialogMessage(this,
                getString(R.string.confirm_delete),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        catlibhelp.deleteCategory(selectedCategory);
                        categoryList.remove(selectedLocation);
                        selectedCategory = null;
                        categoryGrid.setAdapter(new PictoAdminCategoryAdapter(categoryList, view.getContext()));
                        selectedSubCategory = null;
                        subcategoryList.clear();
                        pictograms.clear();
                        subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, view.getContext()));
                        pictogramGrid.setAdapter(new PictoAdapter(pictograms, view.getContext()));
                        updateButtonVisibility(null);
                    }
                }
                );

        deleteDialog.show();

	}

	// DONE
	public void createSubCategory(View view) {
        CreateCategoryDialog createDialog = new CreateCategoryDialog(R.string.subcategory, true);
        createDialog.show(getFragmentManager(),"dialog");
        findViewById(R.id.back_dim_layout).setVisibility(View.VISIBLE);
	}

	// DONE
	public void askDeleteSubCategory(View view) {
//		GDialog deleteDialog = new GDialog(view.getContext(), R.drawable.content_discard, getString(R.string.confirm_delete), "", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateSettings(selectedSubCategory, selectedLocation, false, Setting.DELETE);
//            }
//        });
        GDialogMessage deleteDialog = new GDialogMessage(this,
                getString(R.string.confirm_delete),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        catlibhelp.deleteCategory(selectedSubCategory);
                        subcategoryList.remove(selectedLocation);
                        selectedSubCategory = null;
                        pictograms.clear();
                        subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, view.getContext()));
                        pictogramGrid.setAdapter(new PictoAdapter(pictograms, view.getContext()));
                        updateButtonVisibility(null);
                    }
                }
                );
        deleteDialog.show();
	}

	// Goes to pictogram search function
	public void createPictogram(View view) {
		Intent request = new Intent();

		try{
			request.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
			request.putExtra("purpose", "CAT");
            //request.putExtra("currentChildID", child.getId());
			//request.putExtra("currentGuardianID", guardian.getId());

			startActivityForResult(request, RESULT_FIRST_USER);
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

	// DONE
	public void askDeletePictogram(View view) {
        GDialog deleteDialog = new GDialogMessage(view.getContext(), R.drawable.content_discard, getString(R.string.confirm_delete), "", new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(selectedSubCategory == null){
//                    catlibhelp.deletePictogramFromCategory(pictogramController.getPictogramById(selectedLocation), selectedCategory); // IMPORTANT: selectedCategory.removePictogram(selectedLocation);

                    catlibhelp.deletePictogramFromCategory(selectedPictogram, selectedCategory);
                }
                else{
//                    catlibhelp.deletePictogramFromCategory(pictogramController.getPictogramById(selectedLocation), selectedSubCategory);// IMPORTANT: selectedSubCategory.removePictogram(selectedLocation);
                    catlibhelp.deletePictogramFromCategory(selectedPictogram, selectedSubCategory);
                }
                pictograms.remove(selectedLocation);
                selectedPictogram = null;
                //pictograms.removeAll(pictograms);??
                pictogramGrid.setAdapter(new PictoAdapter(pictograms, v.getContext()));
            }
        });

        deleteDialog.show();
	}

	/*
	 * The following method handle how we access pictoCreator
	 */
	public void gotoPictoCreator(View view) {
		Intent croc = new Intent();

		try{
			croc.setComponent(new ComponentName("dk.aau.cs.giraf.pictocreator", "dk.aau.cs.giraf.pictocreator.MainActivity"));
			startActivity(croc);
		}
		catch (Exception e) {
			message = new MessageDialogFragment(R.string.croc_missing,this);
			message.show(getFragmentManager(), "notInstalled");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

        if(data==null)
        {
            return;
        }
        Bundle extras = data.getExtras();

        if(data.hasExtra("checkoutIds"))
        {
            int[] checkoutIds = extras.getIntArray("checkoutIds");
            if(requestCode==2 || requestCode==3)//Comes from SettingDialogFragment. 2 = Category, 3 = Subcategory.
            {
                Pictogram pictoHolder = new Pictogram();
                if(checkoutIds.length>=1)
                {
                    pictoHolder = pictogramController.getPictogramById(checkoutIds[0]);
                    if(checkoutIds.length>1)
                    {
                        iconAlertDialog(this);
                    }
                }
                if(requestCode == 2)//Category
                {
                    selectedCategory.setImage(pictoHolder.getImage());
                    updateIcon(selectedCategory, selectedLocation, true);
                    categoryGrid.setAdapter(new PictoAdminCategoryAdapter(categoryList, this));
                    categoryController.modifyCategory(selectedCategory);
                }
                else//Subcategory
                {
                    selectedSubCategory.setImage(pictoHolder.getImage());
                    updateIcon(selectedSubCategory, selectedLocation, false);
                    subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, this));
                    categoryController.modifyCategory(selectedSubCategory);
                }
            }
            else
            {
                if(isIcon!=true)
                {
                    // Add pictograms to selectedCategory if no sub-category is selected
                    if(selectedSubCategory == null){
                        checkAndAddPictograms(checkoutIds,selectedCategory);
                    }
                    else{
                        checkAndAddPictograms(checkoutIds,selectedSubCategory);
                    }
                    pictogramGrid.setAdapter(new PictoAdapter(pictograms, this));
                }
                else
                {
                    isIcon = false;
                    if(checkoutIds.length>1)
                    {
                        iconAlertDialog(this);
                    }
                    newCategoryIcon = pictogramController.getPictogramById(checkoutIds[0]);
                }
            }
        }
	}

    private void iconAlertDialog(Context context)
    {
        GDialogAlert diag = new GDialogAlert(context,
                R.drawable.ic_launcher,
                "Kun et pictogram kan vælges som ikon til kategorien.",
                "Det øverste i listen er valgt.",
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

            for(Pictogram p : pictograms){
                if(p.getId() == id){
                    legal = false;
                    break;
                }
            }
            if(legal){
                catlibhelp.addPictogramToCategory(pictogramController.getPictogramById(id), category);
                pictograms = pictogramController.getPictogramsByCategory(category);
            }
        }
    }

    public void onCloseButtonPress(View view) {
        finish();
    }


}
