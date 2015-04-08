package dk.aau.cs.giraf.cat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import dk.aau.cs.giraf.oasis.lib.models.Category;

/**
 * @author SW605f13 Parrot-group
 * Dialog prompting for a new title for a category
 */
@SuppressLint("ValidFragment")
public class TitleDialogFragment extends DialogFragment{
	private MainActivity startActiviy;
	private int pos;
	private boolean isCategory;
    private Category editingCat;
	
	public TitleDialogFragment(MainActivity activity, int position, boolean isCategory, Category editingCat) {
		this.startActiviy =  activity;
		this.pos = position;
		this.isCategory = isCategory;
        this.editingCat = editingCat;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout 			= inflater.inflate(R.layout.dialog_title, null);
        final EditText titel 	= (EditText) layout.findViewById(R.id.titelEdit);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout)
        	   .setTitle(R.string.title_change)
               .setPositiveButton(R.string.finished, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   /* tempCategory is used to hold the new title. The reason for using a category instead of sending
                	    * a string is to keep the updateSettings method simple
                	    */
//                	   Category tempCategory = new Category(titel.getText().toString(), 0, null, 0);
//                	   startActiviy.updateSettings(tempCategory, pos, isCategory, MainActivity.Setting.TITLE);

                        Category newCat = new Category(titel.getText().toString(), editingCat.getColour(), editingCat.getImage(), editingCat.getSuperCategoryId());
                        startActiviy.editCategory(editingCat, newCat, isCategory);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   // Do nothing
                   }
               });

        return builder.create();
    }
}
