package dk.aau.cs.giraf.pictoadmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * @author SW605f13 Parrot-group
 * Used to display a message to the user
 */
@SuppressLint("ValidFragment")
public class MessageDialogFragment extends DialogFragment {

	public String message;
	
	public MessageDialogFragment(int msg) {
        this.message = getString(msg);
	}

    public MessageDialogFragment(int title_missing, Activity act) {
        this.message = act.getString(title_missing);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
	        .setPositiveButton(R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Do nothing
				}
			});
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
