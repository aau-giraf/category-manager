package dk.aau.cs.giraf.cat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.PictogramCategory;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.ProfileCategory;


public class CreateCategoryActivity extends GirafActivity {

    public static final String CATEGORY_CREATED_ID_TAG = "CATEGORY_CREATED_ID_TAG";

    // Helper that will be used to fetch profiles
    private final Helper helper = new Helper(this);

    private Profile guardianProfile;

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
            final int guardianId = extras.getInt("currentGuardianID");

            if (guardianId != -1) {
                guardianProfile = helper.profilesHelper.getProfileById(guardianId);
            }
        }

        // Set the default result (if something goes wrong or the user canceled the process)
        setResult(RESULT_CANCELED, returnIntent);

        // Override the behaviour of the create button
        createButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final EditText titleBox = (EditText) findViewById(R.id.create_category_title);

                final Category createdCategory = new Category(titleBox.getText().toString(), R.color.gBrowncolor);

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

}
