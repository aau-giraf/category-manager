package dk.aau.cs.giraf.categorymanager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;

/**
 * Created on 24/03/15.
 */
public class PictogramAdapter extends BaseAdapter {
    private List<Pictogram> pictogramList;
    private final Context context;

    public PictogramAdapter(List<Pictogram> pictogramList, final Context context) {
        super();

        if(pictogramList == null) {
            this.pictogramList = new ArrayList<Pictogram>();
        }
        else {
            this.pictogramList = pictogramList;
        }

        this.context = context;
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

        // Check if the view is in memory (reuse)
        if(convertView == null) {
            // Create the pictogram view and return it
            GirafPictogramItemView girafPictogramItemView = new GirafPictogramItemView(context, pictogram, pictogram.getName());
            girafPictogramItemView.setLayoutParams(new AbsListView.LayoutParams(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT));

            return girafPictogramItemView;
        }

        // Reuse the view that was previously discarded.
        // Note that this is a "random" view recovered from memory.
        else {
            // Update the old view accordingly to the provided pictogram
            GirafPictogramItemView girafPictogramItemView = (GirafPictogramItemView) convertView;
            girafPictogramItemView.resetPictogramView();
            girafPictogramItemView.setImageModel(pictogram);
            girafPictogramItemView.setTitle(pictogram.getName());
            return girafPictogramItemView;
        }
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
