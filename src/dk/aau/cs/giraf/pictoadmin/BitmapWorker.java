package dk.aau.cs.giraf.pictoadmin;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * This class is used to loading the bitmaps into memory and displaying them in the pictogramGrid when 
 * they are to be posted. This is happending off the UI Thread via AsyncTask.
 * @author Anders Vinther, SW605f13 Parrot-group
 */
public class BitmapWorker extends AsyncTask<Object, Void, Bitmap> {
	// En weak reference gør den "flagged" som "garbage collectable" :)
	// A weak reference flags the imageview as garbage collectable
	private final WeakReference<ImageView> imageview;

	private Pictogram pictogram;
	private Context context;
	
	public BitmapWorker(ImageView img) {
		imageview = new WeakReference<ImageView>(img);
	}

	@Override
	protected Bitmap doInBackground(Object... params) {
//		pictogram = (Pictogram) params[0];
//		Bitmap bmp = null;
//
//		if(pictogram.getId() == -1) {
//			bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.usynlig);
//		}
//		else {
//			bmp = pictogram.getImageData();
//			//bmp = BitmapFactory.decodeFile(pictogram.getImageData());
//		}
//
//		return bmp;

        return (Bitmap)params[0];
	}
	
	protected void onPostExecute(Bitmap result) {
		if(result != null && imageview != null) {
			final ImageView imgview = imageview.get();
			
			if(imgview != null) {
				imgview.setImageBitmap(result);
			}
		}
	}
}
