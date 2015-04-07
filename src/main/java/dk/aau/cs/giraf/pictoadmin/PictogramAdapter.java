package dk.aau.cs.giraf.pictoadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * Created on 24/03/15.
 */
public class PictogramAdapter extends BaseAdapter {
    private List<Pictogram> pictogramList;
    private final LayoutInflater inflater;

    public PictogramAdapter(List<Pictogram> pictogramList, Context context) {
        super();

        if(pictogramList == null) {
            this.pictogramList = new ArrayList<Pictogram>();
        }
        else {
            this.pictogramList = pictogramList;
        }

        // Save the layout inflater. Will be used in {@link getView()}
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return pictogramList.size();
    }

    @Override
    public Object getItem(int position) {
        return pictogramList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return pictogramList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Find the pictogram in question
        final Pictogram pictogram = pictogramList.get(position);

        // Find the view that the pictogram should be inserted into
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.pictogram_grid_item, null);

        // Set the title of the pictogram in the inflated view
        ((TextView) view.findViewById(R.id.category_title)).setText(pictogram.getName());

        // Set the icon of the pictogram in the inflated view
        ((ImageView) view.findViewById(R.id.category_icon)).setImageBitmap(pictogram.getImage());

        return view;
    }

    /**
     * Will update the local list of categories
     *
     * @param pictogramList new list of categories
     */
    public void swap(final List<Pictogram> pictogramList) {
        this.pictogramList = pictogramList;

        // Flag the current data as invalid. After this the view will be re-rendered
        this.notifyDataSetInvalidated();
    }
}
