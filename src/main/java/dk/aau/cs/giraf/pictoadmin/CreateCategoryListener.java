package dk.aau.cs.giraf.pictoadmin;

import android.app.DialogFragment;

/**
 * Created by Martin on 06/03/14.
 */
public interface CreateCategoryListener {
    public void onCatCreateDialogPositiveClick(DialogFragment dialog, String titel, boolean isCategory);
    public void onCatCreateDialogNegativeClick(DialogFragment dialog);
}