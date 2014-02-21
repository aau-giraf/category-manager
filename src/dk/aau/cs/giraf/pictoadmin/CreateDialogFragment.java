package dk.aau.cs.giraf.pictoadmin;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author SW605f13 Parrot-group
 * Dialog displayed when creating new category with input options; title and color, where only title is recurred.
 * Color is set to defauld 0, if no color if chosen.
 */
@SuppressLint("ValidFragment")
public class CreateDialogFragment extends DialogFragment{
	
	String message;
	boolean isCategory;
	
	public CreateDialogFragment(boolean yesOrNO, String msg) {
		this.isCategory = yesOrNO;
		this.message = msg;
	}
	
	public interface CreateDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog, String titel, boolean isCategory);
		public void onDialogNegativeClick(DialogFragment dialog);
	}
	
	CreateDialogListener listener; 
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (CreateDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
		}
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_create, null);
        
        final EditText titelText = (EditText) layout.findViewById(R.id.username);
        Button colorButton = (Button) layout.findViewById(R.id.colorButton);
        /*  Only choose color if creating new category. Sub-categories automatically has the
         *  color of the super-category
         */
        if(!isCategory) {
        	colorButton.setVisibility(View.GONE);
        }

        builder.setView(layout)
        	   .setTitle(String.format("%s %s", getString(R.string.create_new), message))
               .setPositiveButton(getString(R.string.finished), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       listener.onDialogPositiveClick(CreateDialogFragment.this, titelText.getText().toString(), isCategory);
                   }
               })
               .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       listener.onDialogNegativeClick(CreateDialogFragment.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	

}
