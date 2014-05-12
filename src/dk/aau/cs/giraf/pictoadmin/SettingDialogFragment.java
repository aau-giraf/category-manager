package dk.aau.cs.giraf.pictoadmin;

import dk.aau.cs.giraf.categorylib.CatLibHelper;
import dk.aau.cs.giraf.gui.GColorPicker;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.gui.GToast;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramCategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.PictogramCategory;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

/**
 * @author SW605f13 Parrot-group
 * Dialog displaying possible options: Change title, color or icon
 */
@SuppressLint("ValidFragment")
public class SettingDialogFragment extends DialogFragment{
    private MainActivity startActivity;
    private Category category;
    private int pos;
    private boolean isCategory;
    private View view;
    private MessageDialogFragment message;
    private Profile guardian;
    private Profile child;

    public SettingDialogFragment(MainActivity activity, Category cat, int position, boolean isCategory, View view, Profile guardian, Profile child) {
        this.startActivity = activity;
        this.category = cat;
        this.pos = position;
        this.isCategory = isCategory;
        this.view = view;
        this.guardian = guardian;
        this.child = child;
    }

    public interface SettingDialogListener {
        public void onDialogSettingPositiveClick(DialogFragment dialog, int position);
        public void onDialogSettingNegativeClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.edit_category)
                .setItems(R.array.dialog_options, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Change title
                        if (which == 0) {
                            TitleDialogFragment titelDialog = new TitleDialogFragment(startActivity, pos, isCategory, category);
                            titelDialog.show(getFragmentManager(), "changeTitle");
                        }
                        //Change color
                        if (which == 1) {
                            GColorPicker diag = new GColorPicker(view.getContext(), new GColorPicker.OnOkListener() {
                                @Override
                                public void OnOkClick(GColorPicker diag, int color) {
                                    Category newCat = new Category(category.getName(), color, category.getImage(), category.getSuperCategoryId());
                                    startActivity.editCategory(category, newCat, isCategory);
                                }
                            });
                            diag.SetCurrColor(0xFF000000);
                            diag.show();
                        }
                        //Change icon
                        if (which == 2) {
                            Intent request = new Intent();

                            try {
                                request.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
                                request.putExtra("purpose", "CAT");
                                if (isCategory == true) {
                                    getActivity().startActivityForResult(request, 2); //Sends the info to OnActivityResult in MainActivity. Category
                                } else {
                                    getActivity().startActivityForResult(request, 3); //Sends the info to OnActivityResult in MainActivity. Subcategory
                                }
                            } catch (Exception e) {
                                message = new MessageDialogFragment(R.string.search_missing, getActivity());
                                message.show(getFragmentManager(), "notInstalled");
                            }
                        }
                        if (which == 3) {
                            CopyCategory();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void CopyCategory() {
        // It's a category - copy to another child
        if (isCategory) {
            final GProfileSelector profileSelector = new GProfileSelector(view.getContext(), guardian, null);

            profileSelector.setOnListItemClick(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ProfileController profileController = new ProfileController(view.getContext());
                    CategoryController categoryController = new CategoryController(view.getContext());
                    PictogramController pictogramController = new PictogramController(view.getContext());
                    PictogramCategoryController pictogramCategoryController = new PictogramCategoryController(view.getContext());
                    CatLibHelper catLibHelper = new CatLibHelper(view.getContext());

                    Profile copyToChild = profileController.getProfileById((int) id);

                    if (copyToChild.getId() == child.getId()) {
                        GToast toast = new GToast(view.getContext(), getString(R.string.cannot_copy_same_citizen), 5);
                        toast.show();
                        profileSelector.dismiss();
                        return;
                    }

                    for (Category c : categoryController.getCategoriesByProfileId(copyToChild.getId())) {
                        if (c.getName().equals(category.getName())) {
                            GToast toast = new GToast(view.getContext(), copyToChild.getName() + getString(R.string.already_has_category), 5);
                            toast.show();
                            profileSelector.dismiss();
                            return;
                        }
                    }

                    int subCatsCopied = 0;
                    int pictosCopied = 0;

                    // Add a new category to the child
                    Category newCat = new Category(category.getName(), category.getColour(), category.getImage());
                    categoryController.insertCategory(newCat);
                    catLibHelper.addCategoryToProfile(copyToChild, newCat);

                    // Copy all subcategories from the original category to the new category on the other child
                    for (Category subCat : categoryController.getSubcategoriesByCategory(category)) {
                        Category newSub = new Category(subCat.getName(), subCat.getColour(), subCat.getImage(), newCat.getId());
                        categoryController.insertCategory(newSub);
                        subCatsCopied++;


                        // Copy picograms on subcategories
                        for (Pictogram picto : pictogramController.getPictogramsByCategory(subCat)) {
                            PictogramCategory piccat = new PictogramCategory(picto.getId(), newSub.getId());
                            pictogramCategoryController.insertPictogramCategory(piccat);
                            pictosCopied++;
                        }
                    }

                    // Copy all pictograms from the original category to the new category on the other child
                    for (Pictogram picto : pictogramController.getPictogramsByCategory(category)) {
                        PictogramCategory piccat = new PictogramCategory(picto.getId(), newCat.getId());
                        pictogramCategoryController.insertPictogramCategory(piccat);
                        pictosCopied++;
                    }

                    GToast toast = new GToast(view.getContext(), getString(R.string.subcat_copied) + subCatsCopied + getString(R.string.pictograms_copied) + pictosCopied, 3);
                    toast.show();

                    profileSelector.dismiss();
                }
            });
            profileSelector.show();
        }

        // Sub category: copy to another category
        else {
            GDialogAlert diag = new GDialogAlert(startActivity,
                    getString(R.string.not_implemented),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view1) {

                        }
                    });
            diag.show();
        }
    }
}
