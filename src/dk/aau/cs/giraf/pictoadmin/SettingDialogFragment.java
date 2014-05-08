package dk.aau.cs.giraf.pictoadmin;

import dk.aau.cs.giraf.gui.GColorPicker;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
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
    private Pictogram newCategoryIcon; // Hold the value set when creating a new category or sub-category
    private PictogramController pictoHelp;

    public SettingDialogFragment(MainActivity activity, Category cat, int position, boolean isCategory, View view) {
        this.startActivity = activity;
        this.category = cat;
        this.pos = position;
        this.isCategory = isCategory;
        this.view = view;
    }

    public interface SettingDialogListener {
        public void onDialogSettingPositiveClick(DialogFragment dialog, int position);
        public void onDialogSettingNegativeClick(DialogFragment dialog);
    }

    SettingDialogListener listenerSetting;

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
                                    getActivity().startActivityForResult(request, 2);
                                } else {
                                    getActivity().startActivityForResult(request, 3);
                                }
                            } catch (Exception e) {

                           }
                        }
                        if (which == 3) {
                            // It's a category - copy to another child
                            if (isCategory) {
                                GDialogAlert diag = new GDialogAlert(startActivity,
                                        "Det er endnu ikke implementeret at kunne kopiere kategorier.",
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view1) {

                                            }
                                        });
                                diag.show();
                            }

                            // Sub category: copy to another category
                            else {
                                GDialogAlert diag = new GDialogAlert(startActivity,
                                        "Det er endnu ikke implementeret at kunne kopiere underkategorier",
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view1) {

                                            }
                                        });
                                diag.show();
                            }
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
}
