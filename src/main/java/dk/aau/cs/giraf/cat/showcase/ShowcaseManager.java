package dk.aau.cs.giraf.cat.showcase;

import android.app.Activity;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dk.aau.cs.giraf.cat.R;

public class ShowcaseManager {

    public interface Showcase {
        public void configShowCaseView(ShowcaseView showcaseView);
    }

    private ShowcaseView showcaseView;

    private final Queue<Showcase> showcases = new ConcurrentLinkedQueue<Showcase>();

    public void addShowCase(Showcase sv) {
        showcases.add(sv);
    }

    public void show(final Activity activity, final int textX, final int textY) {

        if (showcaseView == null) {
            showcaseView = new ShowcaseView.Builder(activity, true)
                    .setTarget(Target.NONE)
                    .setContentTitle("")
                    .setContentText("")
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!showcases.isEmpty()) {
                                show(activity, textX, textY);
                            } else {
                                showcaseView.hide();
                            }
                        }
                    })
                    .hasManualPosition(true)
                    .xPostion(textX)
                    .yPostion(textY)
                    .setStyle(R.style.GirafCustomShowcaseTheme)
                    .build();
        }

        showcaseView.setShouldCentreText(true);

        showcases.poll().configShowCaseView(showcaseView);
    }


    public void hide() {
        if (showcaseView != null) {
            showcaseView.hide();
            showcaseView = null;
        }
    }

}