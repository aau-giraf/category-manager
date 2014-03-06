package dk.aau.cs.giraf.pictoadmin;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import dk.aau.cs.giraf.gui.GButton;

/**
 * Created by Martin on 06/03/14.
 */
public class CreateCategoryDialog extends DialogFragment {

    private final int category;
    private boolean isSub = false;

    public CreateCategoryDialog(int category) {
        this.category = category;
    }
    public CreateCategoryDialog(int category, boolean sub) {
        this.category = category;
        isSub = sub;
    }


    CreateDialogFragment.CreateDialogListener listener;



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (CreateDialogFragment.CreateDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

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
                listener.onDialogPositiveClick(CreateCategoryDialog.this, titleText.getText().toString(), !isSub);
            }
        });

        GButton cancelButton = (GButton)layout.findViewById(R.id.dialog_cancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDialogNegativeClick(CreateCategoryDialog.this);
            }
        });
        dial.setContentView(layout);
        return dial;

    }
}
