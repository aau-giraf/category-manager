package dk.aau.cs.giraf.pictoadmin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import dk.aau.cs.giraf.categorylib.PARROTCategory;

/**
 * @author SW605f13 Parrot-group
 * Dialog prompting for a new title for a category
 */
@SuppressLint("ValidFragment")
public class TitleDialogFragment extends DialogFragment{
	private MainActivity startActiviy;
	private int pos;
	private boolean isCategory;
	
	public TitleDialogFragment(MainActivity activity, int position, boolean isCategory) {
		this.startActiviy =  activity;
		this.pos = position;
		this.isCategory = isCategory;
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
                	   PARROTCategory tempCategory = new PARROTCategory(titel.getText().toString(), 0, null);
                	   startActiviy.updateSettings(tempCategory, pos, isCategory, "title");
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
