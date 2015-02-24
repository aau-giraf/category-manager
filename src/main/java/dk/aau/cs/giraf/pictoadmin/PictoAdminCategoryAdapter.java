package dk.aau.cs.giraf.pictoadmin;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * @author SW605f13 Parrot-group
 * This class takes a list of categories and loads them into a GridView.
 */
public class PictoAdminCategoryAdapter extends BaseAdapter{
	private List<Category> catList;
	private Context context;

	//Constructor taking List of PARROTCategories, and a Context.
	public PictoAdminCategoryAdapter(List<Category> catList, Context c){
		this.catList = catList;
		context = c;
	}

	@Override
	public int getCount() {
		return catList.size(); //return the number of categories
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	//create an image view for each icon of the categories in the list.
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		/*ImageView imageView;
		Pictogram pct = catList.get(position).getIcon();
		
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} 
		else {
			imageView = (ImageView) convertView;
		}
		
		// we then set the imageview to the icon of the category
		imageView.setImageBitmap(BitmapFactory.decodeFile(pct.getImagePath()));
		
		return imageView;*/


		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(85, 85);

		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = layoutInflater.inflate(R.layout.pictogramview, null);

		ImageView imageView = (ImageView) convertView.findViewById(R.id.pictogrambitmap); 
		imageView.setLayoutParams(layoutParams);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        int categoryColor = catList.get(position).getColour();
        int red = Color.red(categoryColor);
        int green = Color.green(categoryColor);
        int blue = Color.blue(categoryColor);
        float average = (red + green + blue) / 3;
        int textColor = average > 255 / 2 ? Color.BLACK : Color.WHITE;

		TextView textView = (TextView) convertView.findViewById(R.id.pictogramtext);
		textView.setText(catList.get(position).getName());
        textView.setTextColor(textColor);
		
		BitmapWorker worker = new BitmapWorker(imageView);

        if( catList.get(position) != null && catList.get(position).getImage() != null)
        {
            Bitmap pct = catList.get(position).getImage();
            worker.execute(pct);
        }

        LayerDrawable background = (LayerDrawable)convertView.getResources().getDrawable(R.drawable.category);
        final GradientDrawable shape = (GradientDrawable)background.findDrawableByLayerId(R.id.category_background);
        shape.setColor(catList.get(position).getColour());
        convertView.setBackgroundDrawable(background);

		return convertView;
	}

}
