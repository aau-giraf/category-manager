package dk.aau.cs.giraf.pictoadmin;

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
    private final LayoutInflater inflater;

    /**
     * Gives the illusion of always pairing a category with a view.
     * But in reality the view part is changed once the CategoryAdapter creates a new view for the
     * selected category returned by getSelectedMutableCategoryViewPair from an object that
     * implements CategoryAdapter.SelectedCategoryAware
     */
    public static class CategoryViewPair
    {
        private final Category category;
        private View view;

        public CategoryViewPair(final Category category, final View view)
        {
            this.category = category;
            this.view = view;
        }

        public Category getCategory()
        {
            return this.category;
        }

        public View getView()
        {
            return this.view;
        }

        /**
         * This method is only visible inside MutableCategoryViewPair and CategoryAdapter
         * The CategoryAdapter sets the view of this pair once it creates a new view for the associated category
         * @param view
         */
        private void setView(final View view)
        {
            this.view = view;
        }
    }

    public interface SelectedCategoryAware
    {
        CategoryViewPair getSelectedMutableCategoryViewPair();
    }

    private SelectedCategoryAware selectedCategoryAware;

    public CategoryAdapter(final Context context, final SelectedCategoryAware selectedCategoryAware, final List<Category> categoryList) {
        super();

        this.context = context;
        this.selectedCategoryAware = selectedCategoryAware;
        this.categoryList = categoryList;

        // Save the layout inflater. Will be used in {@link getView()}
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Object getItem(final int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return categoryList.get(position).getId();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        // Find the category in question
        final Category category = categoryList.get(position);

        // Find the view that the category should be inserted into
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.category_list_item, null);

        // Set the title of the category in the inflated view
        ((TextView) view.findViewById(R.id.category_title)).setText(category.getName());

        // Set the icon of the category in the inflated view
        ((ImageView) view.findViewById(R.id.category_icon)).setImageBitmap(category.getImage());

        if(selectedCategoryAware != null) {

            final CategoryViewPair selectedCategoryViewPair = selectedCategoryAware.getSelectedMutableCategoryViewPair();

            // Check if the view is selected
            if (selectedCategoryViewPair != null && category.getId() == selectedCategoryViewPair.category.getId()) {
                // Set the background-color for the selected item
                view.setBackgroundColor(context.getResources().getColor(R.color.giraf_page_indicator_active));
                selectedCategoryViewPair.setView(view);
            }
        }
        return view;
    }

    public Category getCategoryFromId(final long id) {
        for (Category category : categoryList) {
            if (category.getId() == id) {
                return category;
            }
        }

        return null;
    }
}
