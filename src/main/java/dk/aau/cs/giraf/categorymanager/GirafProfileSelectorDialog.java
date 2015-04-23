package dk.aau.cs.giraf.categorymanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;

import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafDialog;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;
import dk.aau.cs.giraf.gui.GirafProfileSelectorAdapter;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Created on 26/03/15.
 * GirafProfileSelectorDialog is not yet working!
 */
public class GirafProfileSelectorDialog extends GirafDialog {

    private static final String PROFILE_IDS_TAG = "PROFILE_IDS_TAG";
    private static final String IS_MULTI_SELECT_TAG = "IS_MULTI_SELECT_TAG";
    private static final String DIALOG_IDENTIFIER_TAG = "DIALOG_IDENTIFIER_TAG";
    private static final String DESCRIPTION_TAG = "DESCRIPTION_TAG";

    private Helper helper;

    private List<Profile> selectedProfiles = new ArrayList<Profile>(); // The selected profiles in the dialog

    /**
     * An interface to perform the confirm action for a GirafProfileSelectorDialog
     */
    public interface OnReturnProfilesListener {
        /**
         * Gets the selected profiles
         * @param dialogIdentifier the identifier of the dialog
         */
        public void onProfilesSelected(int dialogIdentifier, List<Profile> selectedProfiles);
    }

    // An instance of the confirmation interface user
    OnReturnProfilesListener profileListener;

    /**
     * Gets a new instance of the GirafProfileSelector
     * @param context the context of where the dialog appears
     * @param guardianID the identifier of the guardian of which citizens should be selectable
     * @param includeGuardian should the guardian with the given identifier be included in the list of selectable profiles
     * @param isMultiselect should it be possible to select multiple profiles
     * @param description the description on the dialog
     * @param dialogIdentifier a unique identifier of the dialog
     * @return a GirafProfileSelector
     */
    public static GirafProfileSelectorDialog newInstance(Context context, int guardianID, boolean includeGuardian, boolean isMultiselect, String description, int dialogIdentifier) {
        Helper helper = new Helper(context);
        Profile guardian = helper.profilesHelper.getById(guardianID);
        List<Profile> profiles = helper.profilesHelper.getChildrenByGuardian(guardian);

        if(includeGuardian) {
            profiles.add(0, guardian);
        }

        return newInstance(profiles, isMultiselect, description, dialogIdentifier);
    }

    /**
     * Gets a new instance of the GirafProfileSelector
     * @param profiles a list of profiles to be selectable
     * @param isMultiselect should it be possible to select multiple profiles
     * @param description the description on the dialog
     * @param dialogIdentifier a unique identifier of the dialog
     * @return a GirafProfileSelector
     */
    private static GirafProfileSelectorDialog newInstance(List<Profile> profiles, boolean isMultiselect, String description, int dialogIdentifier) {
        GirafProfileSelectorDialog girafProfileSelectorDialog = new GirafProfileSelectorDialog();

        // Store the identifier of the profiles to make it parcleable
        ArrayList<Integer> profileIds = new ArrayList<Integer>();

        for(Profile profile : profiles) {
            profileIds.add(profile.getId());
        }

        // Create the argument bundle
        Bundle args = new Bundle();

        // Store the arguments into the bundle
        args.putIntegerArrayList(PROFILE_IDS_TAG,profileIds);
        args.putBoolean(IS_MULTI_SELECT_TAG, isMultiselect);
        args.putString(DESCRIPTION_TAG, description);
        args.putInt(DIALOG_IDENTIFIER_TAG, dialogIdentifier);

        // Store the bundle
        girafProfileSelectorDialog.setArguments(args);

        return girafProfileSelectorDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Check if the activity using the fragment implements the needed interface
        try {
            profileListener = (OnReturnProfilesListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnReturnProfilesListener interface");
        }

        // Sets the helper of the activity
        helper = new Helper(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Fetch the arguments
        final Bundle args = this.getArguments();
        ArrayList<Integer> profileIds = args.getIntegerArrayList(PROFILE_IDS_TAG);
        boolean isMultiselect = args.getBoolean(IS_MULTI_SELECT_TAG);
        String description = args.getString(DESCRIPTION_TAG);
        final int dialogIdentifier = args.getInt(DIALOG_IDENTIFIER_TAG);

        // Write title according to if it is multiselect
        if(isMultiselect) {
            setTitle("Vælg profiler");
        } else {
            setTitle("Vælg profil");
        }

        setDescription(description); // Set the description

        // Inflate
        final GridView profileGrid = (GridView) getActivity().getLayoutInflater().inflate(R.layout.giraf_profile_selector_dialog_gridview_2, null);

        setCustomView(profileGrid); // Set the customview on the dialog

        // Initialize a list of profiles to be filled
        ArrayList<Profile> profiles = new ArrayList<Profile>();

        // Convert profileIds to profiles
        for(int id : profileIds) {
            Helper helper1 = helper;
            Profile profile = helper.profilesHelper.getById(id);
            profiles.add(profile);
            profiles.add(profile);
            profiles.add(profile);
        }



        // Create the adapter with correct content
        GirafProfileSelectorAdapter adapter = new GirafProfileSelectorAdapter(getActivity(),profiles);
        profileGrid.setAdapter(adapter); // Set the adpater

        // Set listeners dependent on selectmode
        if(isMultiselect) {

            // Enable markin of profiles
            profileGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Profile selectedProfile = (Profile) profileGrid.getAdapter().getItem(position);

                    // If the pictogram is in the selected set remove it
                    if (selectedProfiles.contains(selectedProfile)) {
                        // Remove the pictogram to the selected pictogram(s)
                        selectedProfiles.remove(selectedProfile);
                    }
                    // If the pictogram is not in the selected set add it
                    else {
                        // Add the pictogram to the selected pictogram(s)
                        selectedProfiles.add(selectedProfile);
                    }

                    // Update the UI accordingly to above changes
                    ((GirafPictogramItemView) view).toggle();
                }
            });

            // Add a button that ends the selection
            GirafButton selectButton = new GirafButton(getActivity(), getActivity().getResources().getDrawable(R.drawable.icon_accept), "Vælg");
            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profileListener.onProfilesSelected(dialogIdentifier,selectedProfiles);
                    dismiss();
                }
            });
            addButton(selectButton);
        } else {
            // Return the first selected item
            profileGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Profile selectedProfile = (Profile) profileGrid.getAdapter().getItem(position);

                    selectedProfiles.add(selectedProfile);
                    profileListener.onProfilesSelected(dialogIdentifier,selectedProfiles);
                    dismiss();

                }
            });
        }


        return dialog;
    }
}
