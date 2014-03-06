package dk.aau.cs.giraf.pictoadmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dk.aau.cs.giraf.gui.GButton;

/**
 * Created by Martin on 06/03/14.
 */
public class CreateCategoryDialog extends DialogFragment {

    private final int category;

    public CreateCategoryDialog(int category) {
        this.category = category;
    }

    public interface CreateDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String titel);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    CreateDialogListener listener;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dial = new Dialog(getActivity(),R.style.CategoryDialogCrate);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View layout = inflater.inflate(R.layout.dialog_create, null);

        final TextView headerText = (TextView) layout.findViewById(R.id.dialog_headline);
        headerText.setText(String.format("%s %s", getString(R.string.create_new), getString(category)));

        GButton okButton = (GButton)layout.findViewById(R.id.dialog_ok);

        final EditText titleText = (EditText) layout.findViewById(R.id.username);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        GButton cancelButton = (GButton)layout.findViewById(R.id.dialog_cancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dial.dismiss();
            }
        });

        dial.setContentView(layout);
        return dial;

    }
}
