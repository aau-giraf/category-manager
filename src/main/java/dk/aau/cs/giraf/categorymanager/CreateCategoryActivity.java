package dk.aau.cs.giraf.categorymanager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import dk.aau.cs.giraf.dblib.controllers.ImageEntity;
import dk.aau.cs.giraf.showcaseview.ShowcaseView;
import dk.aau.cs.giraf.showcaseview.targets.ViewTarget;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.showcaseview.ShowcaseManager;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.dblib.models.ProfileCategory;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;


public class CreateCategoryActivity extends GirafActivity implements ShowcaseManager.ShowcaseCapable {

    // Constants
    public static final String CATEGORY_CREATED_ID_TAG = "CATEGORY_CREATED_ID_TAG";
    public static final int GET_SINGLE_PICTOGRAM = 103;
    public static final String PICTO_SEARCH_IDS_TAG = "checkoutIds";
    public static final String PICTO_SEARCH_PURPOSE_TAG = "purpose";
    public static final String PICTO_SEARCH_SINGLE_TAG = "single";
    private static final String IS_FIRST_RUN_KEY = "IS_FIRST_RUN_KEY_CREATE_CATEGORY_ACTIVITY";
    private Pictogram iconPictogram;

    // Helper that will be used to fetch profiles
    private final Helper helper = new Helper(this);

    private Profile guardianProfile;
    private GirafPictogramItemView iconView;

    private Intent returnIntent = new Intent();

    /**
     * Used to showcase views
     */
    private ShowcaseManager showcaseManager;
    private boolean isFirstRun;

    /**
     * Used in onResume and onPause for handling showcaseview for first run
     */
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_category);

        final GirafButton createButton = (GirafButton) findViewById(R.id.category_create_button);

        // Get the extra information from when the activity was started (contains profile ids etc.)
        final Bundle extras = getIntent().getExtras();

        // Test if the activity was started correctly
        if (extras == null) {
            Toast.makeText(this, "The activity was not started correctly", Toast.LENGTH_LONG).show();
        } else {
            final long guardianId = extras.getLong(getString(R.string.current_guardian_id));

            if (guardianId != -1) {
                guardianProfile = helper.profilesHelper.getProfileById(guardianId);
            }
        }

        iconView = (GirafPictogramItemView) findViewById(R.id.editable_pictogram_view);

        // Create a "template" pictogram
        final Pictogram pictogramPlaceholder = new Pictogram();
        iconView.setImageModel(pictogramPlaceholder, this.getResources().getDrawable(R.drawable.icon_change_picto_nodpi));

        // Set the default result (if something goes wrong or the user canceled the process)
        setResult(RESULT_CANCELED, returnIntent);

        // Override the behaviour of the create button
        createButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final EditText titleBox = (EditText) findViewById(R.id.create_category_title);

                if (iconPictogram == null) {
                    Toast.makeText(CreateCategoryActivity.this, getString(R.string.create_category_no_pictogram_selected), Toast.LENGTH_LONG).show();
                    return;
                }

                final Category createdCategory = new Category(titleBox.getText().toString(), R.color.gBrowncolor, iconPictogram.getImage());

                // Test if the category is already added
                if (!helper.categoryHelper.getListOfObjects().contains(createdCategory)) {
                    // Add the category into database
                    helper.categoryHelper.insert(createdCategory);

                    // Add relation between the created category and the guardian who created the category
                    helper.profileCategoryController.insert(new ProfileCategory(guardianProfile.getId(), createdCategory.getId()));
                }

                // Add the ID to the result. This can later be used to identify what category was created
                returnIntent.putExtra(CATEGORY_CREATED_ID_TAG, createdCategory.getId());

                // Set the result to indicate that the activity finished as expected
                setResult(RESULT_OK, returnIntent);

                // Finish the activity
                finish();
            }

        });

        final GirafButton helpGirafButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_help));
        helpGirafButton.setId(R.id.help_button);
        helpGirafButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateCategoryActivity.this.toggleShowcase();
            }
        });

        addGirafButtonToActionBar(helpGirafButton, GirafActivity.RIGHT);
    }

    public void onIconClick(View view) {
        Intent request = new Intent(this, dk.aau.cs.giraf.pictosearch.PictoAdminMain.class); // A intent request

        // Try to send the intent
        try {
            // Sets properties on the intent
            request.putExtra(PICTO_SEARCH_PURPOSE_TAG, PICTO_SEARCH_SINGLE_TAG);

            request.putExtra(getString(R.string.current_child_id), (long) getResources().getInteger(R.integer.no_child_selected_id));
            request.putExtra(getString(R.string.current_guardian_id), guardianProfile.getId());

            // Sends the intent
            startActivityForResult(request, GET_SINGLE_PICTOGRAM);
        } catch (Exception e) {

            Toast.makeText(this, "Could not open PictoSearch", Toast.LENGTH_SHORT).show();
            // TODO - Open notify dialog instead of toast
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if this is the first run of the app
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.isFirstRun = prefs.getBoolean(IS_FIRST_RUN_KEY, true);

        // If it is the first run display ShowcaseView
        /*
        if (isFirstRun) {

            final ViewGroup rootView = (ViewGroup) this.findViewById(android.R.id.content);
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    showShowcase();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(IS_FIRST_RUN_KEY, false);
                    editor.commit();

                    synchronized (this) {
                        globalLayoutListener = null;
                        rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
        */
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

                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras(); // Get the data from the intent

                    // Check if there was returned any pictogram ids
                    if (data.hasExtra(PICTO_SEARCH_IDS_TAG)) {
                        // TODO pictosearch should use longs instead of integers
                        long[] pictogramIds = extras.getLongArray(PICTO_SEARCH_IDS_TAG);
                        // TODO Update when pictosearch changes how they return a single pictogram

                        // If there were returned more than one pictogram tell the user that the first is used
                        if (pictogramIds.length < 1) {
                            Toast.makeText(this, getString(R.string.no_pictogram_selected), Toast.LENGTH_LONG).show();
                        } else {
                            if (pictogramIds.length > 1) {
                                Toast.makeText(this, getString(R.string.multiple_pictogram_selected_first_used), Toast.LENGTH_LONG).show();
                            }

                            // Set the wanted pictogram to be what was returned form pictosearh
                            iconPictogram = helper.pictogramHelper.getById(pictogramIds[0]);

                            // Update the gui with the found pictogram
                            iconView.setImageModel(iconPictogram);
                        }
                    }
                }
                break;
        }
    }

    /*
    * Shows a quick walkthrough of the functionality
    * */

    @Override
    public synchronized void showShowcase() {

        // Targets for the Showcase
        final ViewTarget choosePictogramAsIconTarget = new ViewTarget(R.id.editable_pictogram_view, this, 1.5f);
        final ViewTarget chooseCategoryTitle = new ViewTarget(R.id.create_category_title, this, 1.5f);
        final ViewTarget createCategoryButtonTarget = new ViewTarget(R.id.category_create_button, this, 1.5f);

        // Create a relative location for the next button
        final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        final int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        // Calculate position for the help text
        final int textX = this.findViewById(R.id.editable_pictogram_view).getLayoutParams().width + margin * 2;
        final int textY = getResources().getDisplayMetrics().heightPixels / 2 + margin;

        showcaseManager = new ShowcaseManager();

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                showcaseView.setShowcase(choosePictogramAsIconTarget, true);
                showcaseView.setContentTitle(getString(R.string.create_category_pick_icon_showcase_help_title_text));
                showcaseView.setContentText(getString(R.string.create_category_pick_icon_showcase_help_content_text));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {
                showcaseView.setShowcase(chooseCategoryTitle, true);
                showcaseView.setContentTitle(getString(R.string.create_category_naming_showcase_help_title_text));
                showcaseView.setContentText(getString(R.string.create_category_naming_showcase_help_content_text));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {
                showcaseView.setShowcase(createCategoryButtonTarget, true);
                showcaseView.setContentTitle(getString(R.string.create_category_button_showcase_help_titel_text));
                showcaseView.setContentText(getString(R.string.create_category_button_showcase_help_content_text));

                if (!isFirstRun) {
                    showcaseView.setStyle(R.style.GirafLastCustomShowcaseTheme);
                } else {
                    showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                }
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        if (isFirstRun) {
            final ViewTarget helpButtonTarget = new ViewTarget(this.getActionBar().getCustomView().findViewById(R.id.help_button), 1.5f);

            showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
                @Override
                public void configShowCaseView(final ShowcaseView showcaseView) {
                    showcaseView.setShowcase(helpButtonTarget, true);
                    showcaseView.setContentTitle("Hjælpe knap");
                    showcaseView.setContentText("Hvis du bliver i tvivl kan du altid få hjælp her");
                    showcaseView.setStyle(R.style.GirafLastCustomShowcaseTheme);
                    showcaseView.setButtonPosition(lps);
                    showcaseView.setTextPostion(textX, textY);
                }
            });
        }

        ShowcaseManager.OnDoneListener onDoneCallback = new ShowcaseManager.OnDoneListener() {
            @Override
            public void onDone(ShowcaseView showcaseView) {
                showcaseManager = null;
                isFirstRun = false;
            }
        };
        showcaseManager.setOnDoneListener(onDoneCallback);

        showcaseManager.start(this);
    }

    @Override
    public synchronized void hideShowcase() {

        if (showcaseManager != null) {
            showcaseManager.stop();
            showcaseManager = null;
        }
    }

    @Override
    public synchronized void toggleShowcase() {

        if (showcaseManager != null) {
            hideShowcase();
        } else {
            showShowcase();
        }
    }
}
