package dk.aau.cs.giraf.pictoadmin;

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

/**
 * Created by Martin on 06/03/14.
 */
public class CreateCategoryDialog extends DialogFragment {

    public CreateCategoryDialog(int category) {

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dial = new Dialog(getActivity(),R.style.CategoryDialogCrate);
        LayoutInflater inflater = getActivity().getLayoutInflater();


        View layout = inflater.inflate(R.layout.dialog_create, null);
        dial.setContentView(layout);
        return dial;

    }
}
