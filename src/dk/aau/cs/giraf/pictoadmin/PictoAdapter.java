package dk.aau.cs.giraf.pictoadmin;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.aau.cs.giraf.pictogram.Pictogram;

/**
 * @author SW605f13 Parrot-group
 * It is used to import the pictograms into a gridview.
 */
public class PictoAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Pictogram> pictograms;
	private boolean displayText = true;
	
	public PictoAdapter(ArrayList<Pictogram> p, Context c) {
		super();
		this.pictograms = p;
		context = c;
	}
	
	public PictoAdapter(ArrayList<Pictogram> p, boolean display, Context c) {
		super();
		this.pictograms = p;
		this.displayText = display;
		context = c;
	}

	@Override
	public int getCount() {
		return pictograms.size(); //return the number of pictograms
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	
	//create an image view for each pictogram in the list.
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		Pictogram pctNew = pictograms.get(position);
		
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);

		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = layoutInflater.inflate(R.layout.pictogramview, null);

		ImageView imageView = (ImageView) convertView.findViewById(R.id.pictogrambitmap); 
		imageView.setLayoutParams(layoutParams);
		
		if(displayText) {
			TextView textView = (TextView) convertView.findViewById(R.id.pictogramtext);
			textView.setText(pctNew.getTextLabel());
		}
		
		BitmapWorker worker = new BitmapWorker(imageView);
		worker.execute(pctNew);
		
		convertView.setPadding(5, 5, 5, 5);

		return convertView;
	}
}
