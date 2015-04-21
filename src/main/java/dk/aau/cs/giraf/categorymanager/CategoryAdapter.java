package dk.aau.cs.giraf.categorymanager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import dk.aau.cs.giraf.gui.GirafPictogramItemView;
import dk.aau.cs.giraf.oasis.lib.models.Category;

/**
 * Created on 24/03/15.
 */
public class CategoryAdapter extends BaseAdapter {
    private final Context context;
    private final List<Category> categoryList;
    private final SelectedCategoryAware selectedCategoryAware;

    private static final int CATEGORY_PADDING = 20;

    /**
     * Used to save a selected category paired with a specific view (View for when the category is selected)
     */
    public interface SelectedCategoryAware {
        CategoryViewPair getSelectedMutableCategoryViewPair();
    }

    /**
     * Gives the illusion of always pairing a category with a view.
     * But in reality the view part is changed once the CategoryAdapter creates a new view for the
     * selected category returned by getSelectedMutableCategoryViewPair from an object that
     * implements CategoryAdapter.SelectedCategoryAware
     */
    public static class CategoryViewPair {
        private final Category category;
        private View view;

        public CategoryViewPair(final Category category, final View view) {
            this.category = category;
            this.view = view;
        }

        public Category getCategory() {
            return this.category;
        }

        public View getView() {
            return this.view;
        }

        /**
         * This method is only visible inside MutableCategoryViewPair and CategoryAdapter
         * The CategoryAdapter sets the view of this pair once it creates a new view for the associated category
         *
         * @param view
         */
        private void setView(final View view) {
            this.view = view;
        }
    }

    /**
     * Constructor for the CategoryAdapter
     *
     * @param context
     * @param selectedCategoryAware Something that is aware of its selected category
     * @param categoryList          The list of categories to load
     */
    public CategoryAdapter(final Context context, final SelectedCategoryAware selectedCategoryAware, final List<Category> categoryList) {
        super();

        this.context = context;
        this.selectedCategoryAware = selectedCategoryAware;
        this.categoryList = categoryList;
    }

    /**
     * Gets a specific category from the list based on a specific identifier
     *
     * @param id the id of the category to find
     * @return the category corresponding to the id provided
     */
    public Category getCategoryFromId(final long id) {
        for (Category category : categoryList) {
            if (category.getId() == id) {
                return category;
            }
        }

        return null;
    }

    /*
     * Methods required for adapters below
     */

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

        // Create a new UI pictogram
        GirafPictogramItemView view = new GirafPictogramItemView(context, category, category.getName());

        // Add a small bit of padding. This will allow us to indicate that the category is selected
        int paddingInDP = (int) (CATEGORY_PADDING * context.getResources().getDisplayMetrics().density);
        view.setPadding(paddingInDP, paddingInDP, paddingInDP, paddingInDP);

        // Check if the user provided a SelectedCategoryAware
        if (selectedCategoryAware != null) {
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
}
