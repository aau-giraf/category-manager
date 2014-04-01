package dk.aau.cs.giraf.pictoadmin;

import dk.aau.cs.giraf.oasis.lib.models.Category;
import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

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

    public SettingDialogFragment(MainActivity activity, Category cat, int position, boolean isCategory) {
        this.startActivity = activity;
        this.category = cat;
        this.pos = position;
        this.isCategory = isCategory;
    }

    public interface SettingDialogListener {
        public void onDialogSettingPositiveClick(DialogFragment dialog, int position);
        public void onDialogSettingNegativeClick(DialogFragment dialog);
    }

    SettingDialogListener listenerSetting;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(R.string.edit_category)
//                .setItems(R.array.dialog_options, new OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //Change title
//                        if(which == 0) {
//                            TitleDialogFragment titelDialog = new TitleDialogFragment(startActivity, pos, isCategory);
//                            titelDialog.show(getFragmentManager(), "changeTitle");
//                        }
//                        //Change color
//                        if(which == 1) {
//                            AmbilWarnaDialog colorDialog = new AmbilWarnaDialog(startActivity, category.getColour(), new OnAmbilWarnaListener() {
//                                @Override
//                                public void onOk(AmbilWarnaDialog dialog, String color) {
//                                    category.setColour(color);
//
//                                    startActivity.updateSettings(category, pos, isCategory, MainActivity.Setting.COLOR);
//                                }
//
//                                @Override
//                                public void onCancel(AmbilWarnaDialog dialog) {
//                                    // Do nothing
//                                }
//                            });
//                            colorDialog.show();
//                        }
//                        //Change icon
//                        if(which == 2) {
//                            IconDialogFragment iconDialog = new IconDialogFragment(startActivity, category, pos, isCategory);
//                            iconDialog.show(getFragmentManager(), "changeIcon");
//                        }
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });
//        // Create the AlertDialog object and return it
//        return builder.create();

        return null;
    }
}
