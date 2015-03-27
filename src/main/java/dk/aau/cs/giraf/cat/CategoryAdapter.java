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
public class CategoryAdapter extends BaseAdapter {
    private final Context context;
    private List<Category> categoryList;
    private Category selectedCategory;
    private final LayoutInflater inflater;

    public CategoryAdapter(Context context, List<Category> categoryList, Category selectedCategory) {
        super();

        this.context = context;
        this.categoryList = categoryList;
        this.selectedCategory = selectedCategory;

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

        // Check if the view is selected
        if (selectedCategory != null && category.getId() == selectedCategory.getId()) {
            // Set the background-color for the selected item
            view.setBackgroundColor(context.getResources().getColor(R.color.giraf_page_indicator_active));
        }

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

    public Category getCategoryFromId(long id) {
        for (Category category : categoryList) {
            if (category.getId() == id) {
                return category;
            }
        }

        return null;
    }
}
