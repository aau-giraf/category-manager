package dk.aau.cs.giraf.pictoadmin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import dk.aau.cs.giraf.categorylib.PARROTCategory;
import dk.aau.cs.giraf.pictogram.Pictogram;

/**
 * @author SW605f13 Parrot-group
 * Used to display all pictogram in a category, when changing icon of the given category
 */
@SuppressLint("ValidFragment")
public class IconDialogFragment extends DialogFragment{
	private AdminCategory startActiviy;
	private Pictogram icon = null;
	private PARROTCategory changedCategory;
	private int pos;
	private boolean isCategory;
	
	public IconDialogFragment(AdminCategory activity, PARROTCategory cat, int position, boolean isCategory) {
		this.startActiviy =  activity;
		this.changedCategory = cat;
		this.pos = position;
		this.isCategory = isCategory;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_icon, null);
        
        GridView grid = (GridView) layout.findViewById(R.id.iconGrid);
        grid.setAdapter(new PictoAdapter(changedCategory.getPictograms(), false, getActivity()));
        grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				icon = changedCategory.getPictograms().get(position);
			}
		});
        
        builder.setView(layout)
        	   .setTitle("Ændre icon")
               .setPositiveButton("Færdig", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   if(icon != null){
                		   changedCategory.setIcon(icon);
                		   startActiviy.updateSettings(changedCategory, pos, isCategory, "icon");
                	   }
                   }
               })
               .setNegativeButton("Annuller", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   }
               });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
