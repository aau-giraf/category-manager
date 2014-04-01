package dk.aau.cs.giraf.pictoadmin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.categorylib.CatLibHelper;

import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * @author SW605f13 Parrot-group
 * Used to display all pictogram in a category, when changing icon of the given category
 */
@SuppressLint("ValidFragment")
public class IconDialogFragment extends DialogFragment{
	private MainActivity startActiviy;
	private Pictogram icon = null;
	private Category changedCategory;
	private int pos;
	private boolean isCategory;

    private CatLibHelper catlibhelp;
    private PictogramController pictoControl;
	
	public IconDialogFragment(MainActivity activity, Category cat, int position, boolean isCategory) {
		this.startActiviy =  activity;
		this.changedCategory = cat;
		this.pos = position;
		this.isCategory = isCategory;
        catlibhelp = new CatLibHelper(null);
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_icon, null);
        
        GridView grid = (GridView) layout.findViewById(R.id.iconGrid);
        grid.setAdapter(new PictoAdapter(catlibhelp.getPictogramsFromCategory(changedCategory), false, getActivity()));
        grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				icon = pictoControl.getPictogramById(position); // IMPORTANT: is position = id?
			}
		});
        
        builder.setView(layout)
        	   .setTitle(R.string.change_icon)
               .setPositiveButton(R.string.finished, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   if(icon != null){
                		   changedCategory.setImage(new byte[] {}); // IMPORTANT: icon was used before
                		   startActiviy.updateSettings(changedCategory, pos, isCategory, MainActivity.Setting.ICON);
                	   }
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   }
               });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
