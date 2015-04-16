package dk.aau.cs.giraf.cat;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * Created on 14/04/2015.
 */
public class GirafPictogram extends LinearLayout {

    // The pictogram to base the view upon
    private Pictogram pictogram;

    // The inflated view (See constructors)
    private View inflatedView;

    private ImageView iconContainer;
    private TextView titleContainer;

    /**
     * Do not use this constructor. It is only available for creating the pictogram in xml!
     */
    public GirafPictogram(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(!isInEditMode()) {
            initialize(null);
            return;
        }

        Pictogram sample = new Pictogram();
        sample.setName("Sample pictogram");
        sample.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_copy));

        initialize(sample);
    }

    public GirafPictogram(Context context, Pictogram pictogram) {
        super(context);

        initialize(pictogram);
    }

    public GirafPictogram(Context context, AttributeSet attrs, Pictogram pictogram) {
        super(context, attrs);

        initialize(pictogram);
    }

    public GirafPictogram(Context context, AttributeSet attrs, int defStyle, Pictogram pictogram) {
        super(context, attrs, defStyle);

        initialize(pictogram);
    }

    /**
     * Initialized the different components
     */
    private void initialize(Pictogram pictogram) {
        // Find the XML for the pictogram and load it into the view
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflatedView = inflater.inflate(R.layout.giraf_pictogram, this);

        setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));

        setPictogram(pictogram);
    }

    /**
     * Will update the view with the provided pictogram
     * @param pictogram the pictogram to update based upon
     */
    public void setPictogram(final Pictogram pictogram) {
        // If provided with null, do not update!
        if(pictogram == null) {
            return;
        }

        this.pictogram = pictogram;

        // Set the pictogram icon depending on the provided pictogram
        iconContainer = (ImageView) inflatedView.findViewById(R.id.pictogram_icon);
        iconContainer.setImageBitmap(pictogram.getImage());

        // Set the name of the pictogram provided
        titleContainer = (TextView) inflatedView.findViewById(R.id.pictogram_title);
        titleContainer.setText(pictogram.getName());
    }

    /**
     * Will hide the title of the pictogram
     */
    public void hideTitle() {
        titleContainer.setVisibility(GONE);
    }

    /**
     * Will show the title of the pictogram
     */
    public void showTitle() {
        titleContainer.setVisibility(VISIBLE);
    }
}
