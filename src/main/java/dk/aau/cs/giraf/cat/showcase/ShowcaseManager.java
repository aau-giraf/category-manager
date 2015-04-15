package dk.aau.cs.giraf.cat.showcase;

import android.app.Activity;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dk.aau.cs.giraf.cat.R;

/**
 * Used to handle multiple showcases
 */
public class ShowcaseManager {

    /**
     * Interface to be used with addShowCase ( as anonymous classes ) to set the showcase view to a new target with a new description
     * For instance:
     *
     *  showcaseManager = new ShowcaseManager();
     *
     *  showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
     *      @Override
     *      public void configShowCaseView(final ShowcaseView showcaseView) {
     *
     *          showcaseView.setShowcase(target1, true);
     *          showcaseView.setContentTitle("Se kage!");
     *          showcaseView.setContentText("Det her er noget lækkert kage");
     *          showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
     *          showcaseView.setButtonPosition(lps);
     *      }
     *  });
     */
    public interface Showcase {
        public void configShowCaseView(ShowcaseView showcaseView);
    }

    /**
     * Interface which indicates that a given object is able to show showcases
     */
    public static interface ShowcaseCapable {

        public void showShowcase();

        public void toggleShowcase();

        public void hideShowcase();
    }

    public interface OnDoneListener {
        public void onDone(ShowcaseView showcaseView);
    }

    private ShowcaseView showcaseView;
    private OnDoneListener onDoneCallback;

    private final Queue<Showcase> showcases = new ConcurrentLinkedQueue<Showcase>();

    public void addShowCase(final Showcase sv) {
        showcases.add(sv);
    }

    /**
     * Start showing all added showcases and place text at @param textX, @param textY
     * @param activity
     */
    public void start(final Activity activity) {

        if (showcases.isEmpty()) {
            return;
        }

        if (showcaseView == null) {
            showcaseView = new ShowcaseView.Builder(activity, true)
                    .setTarget(Target.NONE)
                    .setContentTitle("")
                    .setContentText("")
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (!showcases.isEmpty()) {
                                start(activity);
                            } else {
                                if (onDoneCallback != null) {
                                    onDoneCallback.onDone(showcaseView);
                                }
                                showcaseView.hide();
                            }
                        }
                    })
                    .hasManualPosition(true)
                    .setStyle(R.style.GirafCustomShowcaseTheme)
                    .build();
        }

        showcases.poll().configShowCaseView(showcaseView);
    }

    /**
     * Stops the current showcases
     */
    public void stop() {
        if (showcaseView != null) {
            showcaseView.hide();
            showcaseView = null;
        }
    }

    /**
     * Set a callback which is invoked once all showcases have been shown
     * @param onDoneCallback
     */
    public void setOnDoneListener(final OnDoneListener onDoneCallback) {
        this.onDoneCallback = onDoneCallback;
    }
}