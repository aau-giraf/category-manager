package dk.aau.cs.giraf.categorymanager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created on 14/04/2015.
 */
public class GirafEditableLayout extends RelativeLayout {
    public GirafEditableLayout(Context context, View view) {
        super(context);

        initialize(view);
    }

    public GirafEditableLayout(Context context, AttributeSet attrs, View view) {
        super(context, attrs);

        initialize(view);
    }

    public GirafEditableLayout(Context context, AttributeSet attrs, int defStyle, View view) {
        super(context, attrs, defStyle);

        initialize(view);
    }

    private void initialize(View view) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.giraf_editable_layout, this);

        FrameLayout fl = (FrameLayout) layout.findViewById(R.id.editable_content);

        fl.addView(view);
    }
}
