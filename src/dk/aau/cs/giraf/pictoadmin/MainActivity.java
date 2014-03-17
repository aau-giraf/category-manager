package dk.aau.cs.giraf.pictoadmin;

import java.util.ArrayList;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GGridView;
import dk.aau.cs.giraf.gui.GList;
import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import dk.aau.cs.giraf.categorylib.CategoryHelper;
import dk.aau.cs.giraf.categorylib.PARROTCategory;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfilesHelper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;

/**
 * @author SW605f13 Parrot-group
 * This is the main class in the CAT application.
 */
@SuppressLint("DefaultLocale")
public class MainActivity extends Activity implements CreateCategoryListener{
	private Profile child;
	private Profile guardian;

	private PARROTCategory selectedCategory    = null;
	private PARROTCategory selectedSubCategory = null;
	private Pictogram 	   selectedPictogram   = null;

	private ArrayList<PARROTCategory> categoryList    = new ArrayList<PARROTCategory>();
	private ArrayList<PARROTCategory> subcategoryList = new ArrayList<PARROTCategory>();
	private ArrayList<Pictogram> 	  pictograms	  = new ArrayList<Pictogram>();

	private GList categoryGrid;
	private GList subcategoryGrid;
	private GGridView pictogramGrid;

	private boolean somethingChanged = false; // If something is deleted, is has to be noted
	private int     selectedLocation; // Stores the location of the last pressed item in any gridview
	private int     newCategoryColor; // Hold the value set when creating a new category or sub-category

	private CategoryHelper catHelp;
	private ProfilesHelper proHelp;

	private MessageDialogFragment message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		setContentView(R.layout.activity_admin_category);
		catHelp =  new CategoryHelper(this);
		proHelp =  new ProfilesHelper(this);

		Bundle extras = getIntent().getExtras();

        //if a debugger is attatched at startup don't require login info
        if(extras == null && Debug.isDebuggerConnected())
        {
            extras = new Bundle();
            extras.putLong("currentChildID", 1);
            extras.putLong("currentGuardianID", 1);
        }


		if(extras == null){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_title);
            builder.setMessage(R.string.errorLogin);
            builder.setNegativeButton(R.string.returnItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK, so save the mSelectedItems results somewhere
                    // or return them to the component that opened the dialog
                    finish();
                }

            });

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
		}
		else{
			getProfiles(extras);
			Log.v("PROFILE","child " +child);
			Log.v("PROFILE","guardian" + guardian);


			Log.v("admincategory","child is " + child.getId());
			categoryList = catHelp.getChildsCategories(child.getId());
			if(categoryList == null)
			{
				categoryList = new ArrayList<PARROTCategory>();
			}

			// Setup category gridview
			categoryGrid = (GList) findViewById(R.id.category_listview);
			if(categoryList != null){
				categoryGrid.setAdapter(new PictoAdminCategoryAdapter(categoryList, this));
			}
			categoryGrid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
					//categoryGrid.setBackgroundColor(categoryList.get(position).getCategoryColor());
					//subcategoryGrid.setBackgroundColor(categoryList.get(position).getCategoryColor());
					updateSelected(v, position, 1);
					updateButtonVisibility(v);

				}
			});
			categoryGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
					SettingDialogFragment settingDialog = new SettingDialogFragment(MainActivity.this,
																				categoryList.get(position),
																				position, true);
					settingDialog.show(getFragmentManager(), "chooseSettings");
					return false;
				}
			});

			// Setup sub-category gridview
			subcategoryGrid = (GList) findViewById(R.id.subcategory_listview);
			subcategoryGrid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
					//subcategoryGrid.setBackgroundColor(subcategoryList.get(position).getCategoryColor());
					updateSelected(v, position, 0);
					updateButtonVisibility(v);
				}
			});
			subcategoryGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    SettingDialogFragment settingDialog = new SettingDialogFragment(MainActivity.this,
                            subcategoryList.get(position),
                            position, false);
                    settingDialog.show(getFragmentManager(), "chooseSettings");
                    return false;
                }
            });

			// Setup pictogram gridview
			pictogramGrid = (GGridView) findViewById(R.id.pictogram_gridview);
			pictogramGrid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
					updateSelected(v, position, 2);
					updateButtonVisibility(v);
				}
			});

			TextView currentChild = (TextView) findViewById(R.id.currentChildName);
			currentChild.setText(child.getFirstname()+ " " + child.getSurname());
		}
	}


    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_admin_category, menu);
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(subcategoryList != null){
			for(PARROTCategory sc : subcategoryList){
				if(sc.isChanged()){
					sc.getSuperCategory().setChanged(true);
				}
			}
		}
		if(categoryList != null){
			for(PARROTCategory c : categoryList){
				if(c.isChanged()){
					Log.v("klim", "ready to save");
					somethingChanged = true;
					catHelp.saveCategory(c, child.getId());
				}
			}
		}

		if(somethingChanged){
			catHelp.saveChangesToXML();
		}
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

        // Check if a related category ("main category" or subcategory) with same name exists
        ArrayList<PARROTCategory> relatedCategories = isCategory ? categoryList : subcategoryList;
        boolean categoryWithNameExists = false;
        for(PARROTCategory sc : relatedCategories) {
            if (sc.getCategoryName().equalsIgnoreCase(title)) {
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
            if (!categoryList.isEmpty()) {
                categoryList.add(new PARROTCategory(title, newCategoryColor, categoryList.get(0).getIcon()));
            } else {
                categoryList = new ArrayList<PARROTCategory>();
                categoryList.add(new PARROTCategory(title, newCategoryColor, PictoFactory.getPictogram(this, 1)));
            }

            categoryList.get(categoryList.size()-1).setChanged(true);
            somethingChanged = true;
            categoryGrid.setAdapter(new PictoAdminCategoryAdapter(categoryList, this));
        } else { // Create subcategory
            PARROTCategory cat = new PARROTCategory(title, newCategoryColor, categoryList.get(0).getIcon());
            cat.setSuperCategory(selectedCategory);
            subcategoryList.add(cat);
            selectedCategory.setChanged(true);
            subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, this));
        }

        dialog.dismiss();
        findViewById(R.id.back_dim_layout).setVisibility(View.GONE);

		newCategoryColor = 0;
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

	// DONE: Called when pressing the @id/colorButton and updates the newCategoryColor
	public void setNewCategoryColor(View view)
	{
		AmbilWarnaDialog colorDialog = new AmbilWarnaDialog(this, 0, new OnAmbilWarnaListener() {
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				newCategoryColor = color;
			}

			@Override
			public void onCancel(AmbilWarnaDialog dialog) {
				// Do nothing
			}
		});
		colorDialog.show();
	}

	/*
	 * The following methods handle updating of categories and sub-categories. This occurs when long-clicking either
	 * a category or sub-category. Depending on the setting parameter, individual methods for updating is called
	 */
	public void updateSettings(PARROTCategory category, int pos, boolean isCategory, String setting) {
		String settingLower = setting.toLowerCase();
		if(settingLower.equals("title")){
            if(isCategory) {
                updateTitle(category, pos, categoryList);
            }
            else {
                updateTitle(category, pos, subcategoryList);
            }
		}
		else if(settingLower.equals("color")){
			updateColor(category, pos, isCategory);
		}
		else if(settingLower.equals("icon")){
			updateIcon(category, pos, isCategory);
		}
		else if(settingLower.equals("delete")){
			if(isCategory){
				subcategoryList.removeAll(subcategoryList);
				catHelp.deleteCategory(selectedCategory);
				categoryList.remove(pos);
				selectedCategory = null;
			}
			else {
				subcategoryList.remove(pos);
				selectedCategory.setChanged(true);
			}
            pictograms.removeAll(pictograms);
            selectedSubCategory = null;
			somethingChanged = true;
		}
		else if(settingLower.equals("deletepictogram")){
			if(selectedSubCategory == null){
				selectedCategory.removePictogram(selectedLocation);
			}
			else{
				selectedSubCategory.removePictogram(selectedLocation);
			}
			selectedCategory.setChanged(true);
			selectedPictogram = null;
			somethingChanged = true;
		}

		if(isCategory){
			categoryGrid.setAdapter(new PictoAdminCategoryAdapter(categoryList, this));
		}

		subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, this));
		pictogramGrid.setAdapter(new PictoAdapter(pictograms, this));
		updateButtonVisibility(null);
	}


	private void updateTitle(PARROTCategory changedCategory, int pos, ArrayList<PARROTCategory> list) {
        String catName = changedCategory.getCategoryName();
        for(PARROTCategory c : list) {
            if(c.getCategoryName().equals(catName)) {
                message = new MessageDialogFragment(R.string.name_used,this);
                message.show(getFragmentManager(), "invalidName");
                return;
            }
        }
        list.get(pos).setCategoryName(catName);
        list.get(pos).setChanged(true);
	}


    // DONE
	private void updateColor(PARROTCategory category, int pos, boolean isCategory) {
		category.setChanged(true);

		if(isCategory){
			categoryList.set(pos, category);
		}
	}

	// DONE
	private void updateIcon(PARROTCategory category, int pos, boolean isCategory) {
		category.setChanged(true);

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

		if(extras.containsKey("currentChildID")){
			child = proHelp.getProfileById(extras.getLong("currentChildID"));
		}
		if(extras.containsKey("currentGuardianID")){

			guardian = proHelp.getProfileById(extras.getLong("currentGuardianID"));
		}
	}

	/*
	 * DONE: The following methods handle menu pressing
	 */
	public void saveChanges(MenuItem item) {
		for(PARROTCategory sc : subcategoryList){
			if(sc.isChanged()){
				sc.getSuperCategory().setChanged(true);
				sc.setChanged(false);
			}
		}

		for(PARROTCategory c : categoryList){
			if(c.isChanged()){
				somethingChanged = true;
				catHelp.saveCategory(c, child.getId());
				c.setChanged(false);
			}
		}

		if(somethingChanged){
			catHelp.saveChangesToXML();
		}

		somethingChanged = false;
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

			subcategoryList  = selectedCategory.getSubCategories();
			pictograms 		 = selectedCategory.getPictograms();

			subcategoryGrid.setAdapter(new PictoAdminCategoryAdapter(subcategoryList, view.getContext()));
			pictogramGrid.setAdapter(new PictoAdapter(pictograms, view.getContext()));
		}
        //id 0 is the list of subcategories
		else if(id == 0) {
			selectedSubCategory = subcategoryList.get(position);
			selectedPictogram   = null;

			pictograms = subcategoryList.get(position).getPictograms();

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
	public void deleteCategory(View view) {
        GDialog deleteDialog = new GDialog(view.getContext(), R.drawable.content_discard, getString(R.string.confirm_delete), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings(selectedCategory, selectedLocation, true, "delete");
            }
        });
        deleteDialog.show();
	}

	// DONE
	public void createSubCategory(View view) {
        CreateCategoryDialog createDialog = new CreateCategoryDialog(R.string.subcategory, true);
        createDialog.show(getFragmentManager(),"dialog");
        findViewById(R.id.back_dim_layout).setVisibility(View.VISIBLE);
	}

	// DONE
	public void deleteSubCategory(View view) {
		GDialog deleteDialog = new GDialog(view.getContext(), R.drawable.content_discard, getString(R.string.confirm_delete), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings(selectedSubCategory, selectedLocation, false, "delete");
            }
        });
        deleteDialog.show();
	}

	// Goes to pictogram search function
	public void createPictogram(View view) {
		Intent request = new Intent();

		try{
			request.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
			request.putExtra("purpose", "CAT");
			request.putExtra("currentChildID", child.getId());
			request.putExtra("currentGuardianID", guardian.getId());

			startActivityForResult(request, RESULT_FIRST_USER);
		}
		catch (Exception e) {
			message = new MessageDialogFragment(R.string.search_missing,this);
			message.show(getFragmentManager(), "notInstalled");
		}
	}

	// DONE
	public void deletePictogram(View view) {
        GDialog deleteDialog = new GDialog(view.getContext(), R.drawable.content_discard, getString(R.string.confirm_delete), "", new View.OnClickListener(){
            @Override
            public void onClick(View v){
                updateSettings(selectedSubCategory, selectedLocation, false, "deletepictogram");
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
			croc.setComponent(new ComponentName("dk.aau.cs.giraf.pictocreator", "dk.aau.cs.giraf.pictocreator.CrocActivity"));
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

        if(data.hasExtra("checkoutIds")){
            long[] checkoutIds = extras.getLongArray("checkoutIds");

            // Add pictograms to selectedCategory if no sub-category is selected
            if(selectedSubCategory == null){
                checkAndAddPictograms(checkoutIds,selectedCategory);
            }
            else{
                checkAndAddPictograms(checkoutIds,selectedSubCategory);
            }
            pictogramGrid.setAdapter(new PictoAdapter(pictograms, this));
        }

	}

    private void checkAndAddPictograms(long[] checkoutIds, PARROTCategory category) {
        boolean legal;
        for(long id : checkoutIds){
            legal = true;

            for(Pictogram p : pictograms){
                if(p.getPictogramID() == id){
                    legal = false;
                    break;
                }
            }
            if(legal){
                category.addPictogram(PictoFactory.getPictogram(this, id));
                category.setChanged(true);
                pictograms = category.getPictograms();
            }
        }
    }
}
