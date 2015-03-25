package dk.aau.cs.giraf.cat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dk.aau.cs.giraf.oasis.lib.models.Category;

/**
 * Created on 24/03/15.
 */
public class PictogramCategoryAdapter extends BaseAdapter {
    private List<Category> categoryList;
    private final LayoutInflater inflater;

    public PictogramCategoryAdapter(List<Category> categoryList, Context context) {
        super();

        this.categoryList = categoryList;

        // Save the layout inflater. Will be used in {@link getView()}
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return categoryList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Find the category in question
        final Category category = categoryList.get(position);

        // Find the view that the category should be inserted into
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.category_list_item, null);

        // Set the title of the category in the inflated view
        ((TextView) view.findViewById(R.id.category_title)).setText(category.getName());

        // Set the icon of the category in the inflated view
        ((ImageView) view.findViewById(R.id.category_icon)).setImageBitmap(category.getImage());

        return view;
    }

    /**
     * Will update the local list of categories
     *
     * @param categoryList new list of categories
     */
    public void swap(final List<Category> categoryList) {
        this.categoryList = categoryList;

        // Flag the current data as invalid. After this the view will be re-rendered
        this.notifyDataSetInvalidated();
    }
}
