package dk.aau.cs.giraf.cat;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.ProfileCategory;


public class CreateCategoryActivity extends GirafActivity {

    // Constants
    public static final String CATEGORY_CREATED_ID_TAG = "CATEGORY_CREATED_ID_TAG";
    public static final int GET_SINGLE_PICTOGRAM = 103;
    public static final String PICTO_SEARCH_IDS_TAG = "checkoutIds";
    public static final String PICTO_SEARCH_PURPOSE_TAG = "purpose";
    public static final String PICTO_SEARCH_SINGLE_TAG = "single";
    private Pictogram iconPictogram;

    // Helper that will be used to fetch profiles
    private final Helper helper = new Helper(this);

    private Profile guardianProfile;
    private ImageView iconView;

    private Intent returnIntent = new Intent();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_category);

        final GirafButton createButton = (GirafButton) findViewById(R.id.category_create_button);

        // Get the extra information from when the activity was started (contains profile ids etc.)
        final Bundle extras = getIntent().getExtras();

        // Test if the activity was started correctly
        if (extras == null) {
            Toast.makeText(this, "The activity was not started correctly", Toast.LENGTH_LONG);
        } else {
            final int guardianId = extras.getInt(CategoryActivity.INTENT_CURRENT_GUARDIAN_ID);

            if (guardianId != -1) {
                guardianProfile = helper.profilesHelper.getProfileById(guardianId);
            }
        }

        iconView = (ImageView) findViewById(R.id.create_category_pictogram);

        // Set the default result (if something goes wrong or the user canceled the process)
        setResult(RESULT_CANCELED, returnIntent);

        // Override the behaviour of the create button
        createButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final EditText titleBox = (EditText) findViewById(R.id.create_category_title);

                if(iconPictogram == null) {
                    Toast.makeText(CreateCategoryActivity.this, getString(R.string.create_category_no_pictogram_selected), Toast.LENGTH_LONG).show();
                    return;
                }

                final Category createdCategory = new Category(titleBox.getText().toString(), R.color.gBrowncolor, iconPictogram.getImage());

                // Test if the category is already added
                if (!helper.categoryHelper.getCategories().contains(createdCategory)) {
                    // Add the category into database
                    helper.categoryHelper.insertCategory(createdCategory);

                    // Add relation between the created category and the guardian who created the category
                    helper.profileCategoryController.insertProfileCategory(new ProfileCategory(guardianProfile.getId(), createdCategory.getId()));
                }

                // Add the ID to the result. This can later be used to identify what category was created
                returnIntent.putExtra(CATEGORY_CREATED_ID_TAG, createdCategory.getId());

                // Set the result to indicate that the activity finished as expected
                setResult(RESULT_OK, returnIntent);

                // Finish the activity
                finish();
            }

        });
    }

    public void onIconClick(View view){
        Intent request = new Intent(); // A intent request

        // Try to send the intent
        try{
            // Sets properties on the intent
            request.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
            request.putExtra(PICTO_SEARCH_PURPOSE_TAG, PICTO_SEARCH_SINGLE_TAG);

            // Sends the intent
            startActivityForResult(request, GET_SINGLE_PICTOGRAM);
        }
        catch (Exception e) {

            Toast.makeText(this,"Could not open PictoSearch", Toast.LENGTH_SHORT).show();
            // TODO - Open notify dialog instead of toast
        }
    }

    /**
     * Will be called when an opened activity returns
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        switch (requestCode) {
            case GET_SINGLE_PICTOGRAM:
                Bundle extras = data.getExtras(); // Get the data from the intent

                // Check if there was returned any pictogram ids
                if (data.hasExtra(PICTO_SEARCH_IDS_TAG)) {
                    // TODO pictosearch should use longs instead of integers
                    int[] pictogramIds = extras.getIntArray(PICTO_SEARCH_IDS_TAG);
                    // TODO Update when pictosearch changes how they return a single pictogram

                    // If there were returned more than one pictogram tell the user that the first is used
                    if(pictogramIds.length > 1)
                    {
                        Toast.makeText(this,"Mere end et piktogram valgt, det øverste i listen bruges",Toast.LENGTH_LONG).show();
                    }

                    // Set the wanted pictogram to be what was returned form pictosearh
                    iconPictogram = helper.pictogramHelper.getPictogramById(pictogramIds[0]);

                    // Update the gui with the found pictogram
                    iconView.setImageBitmap(iconPictogram.getImage());
                }
                break;
        }
    }
}
