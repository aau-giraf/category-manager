package dk.aau.cs.giraf.cat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * Created on 14/04/2015.
 */
public class GirafPictogram extends LinearLayout {

    private final Pictogram pictogram;

    private ImageView iconContainer;
    private TextView titleContainer;

    public GirafPictogram(Context context, Pictogram pictogram) {
        super(context);

        this.pictogram = pictogram;

        initialize();
    }

    public GirafPictogram(Context context, AttributeSet attrs, Pictogram pictogram) {
        super(context, attrs);

        this.pictogram = pictogram;

        initialize();
    }

    public GirafPictogram(Context context, AttributeSet attrs, int defStyle, Pictogram pictogram) {
        super(context, attrs, defStyle);

        this.pictogram = pictogram;

        initialize();
    }

    /**
     * Initialized the different components
     */
    private void initialize() {
        // Find the XML for the pictogram and load it into the view
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.giraf_pictogram, this);

        // Set the pictogram icon depending on the provided pictogram
        iconContainer = (ImageView) view.findViewById(R.id.pictogram_icon);
        iconContainer.setImageBitmap(pictogram.getImage());

        // Set the name of the pictogram provided
        titleContainer = (TextView) view.findViewById(R.id.pictogram_title);
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
