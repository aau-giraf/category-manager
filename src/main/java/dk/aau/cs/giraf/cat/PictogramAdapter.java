package dk.aau.cs.giraf.cat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.gui.GirafPictogram;

/**
 * Created on 24/03/15.
 */
public class PictogramAdapter extends BaseAdapter {
    private List<Pictogram> pictogramList;
    private final LayoutInflater inflater;
    private final Context context;

    public PictogramAdapter(List<Pictogram> pictogramList, Context context) {
        super();

        if(pictogramList == null) {
            this.pictogramList = new ArrayList<Pictogram>();
        }
        else {
            this.pictogramList = pictogramList;
        }

        this.context = context;

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
        // Find the pictogram to show
        final Pictogram pictogram = pictogramList.get(position);

        // Create the pictogram view and return it
        return new GirafPictogram(context, pictogram);
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
