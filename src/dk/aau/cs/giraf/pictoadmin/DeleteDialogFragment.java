package dk.aau.cs.giraf.pictoadmin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import dk.aau.cs.giraf.categorylib.PARROTCategory;
import dk.aau.cs.giraf.pictogram.Pictogram;

/**
 * @author SW605f13 Parrot-group
 * Dialog making sure, that the user really intends to delete an item
 */
@SuppressLint("ValidFragment")
public class DeleteDialogFragment extends DialogFragment {
	private AdminCategory startActivity;
	private PARROTCategory category;
	private Pictogram pictogram = null;
	private int pos;
	private boolean isCategory;
	
	public DeleteDialogFragment(AdminCategory activity, PARROTCategory cat, int position, boolean yesOrNo) {
		this.startActivity = activity;
		this.category = cat;
		this.pos = position;
		this.isCategory = yesOrNo;
	}
	
	public DeleteDialogFragment(AdminCategory activity, Pictogram pic, int position, boolean yesOrNo) {
		this.startActivity = activity;
		this.pictogram = pic;
		this.pos = position;
		this.isCategory = yesOrNo;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.confirm_delete)
               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   if(pictogram == null){
                		   startActivity.updateSettings(category, pos, isCategory, "delete");
                	   }
                	   else{
                		   startActivity.updateSettings(category, pos, isCategory, "deletepictogram");
                	   }
                   }
               })
               .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
