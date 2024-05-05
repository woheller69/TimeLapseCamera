package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Overlay for AF Fields
 */
public class PreviewOverlay extends androidx.appcompat.widget.AppCompatImageView {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Draw debug border
	 */
	private static final boolean DRAW_DEBUG_BORDER = false;

	/**
	 * Calculate the scaling for the preview
	 */
	private PreviewScaling scaling;

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	public PreviewOverlay(Context context) {
		super(context);
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   Attributes
	 */
	public PreviewOverlay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Constructor
	 *
	 * @param context  Context
	 * @param attrs    Attributes
	 * @param defStyle Style
	 */
	public PreviewOverlay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * @param scaling Calculate the scaling for the preview
	 */
	public void setScaling(PreviewScaling scaling) {
		this.scaling = scaling;
	}

	@Override
	public void onDraw(Canvas canvas) {
		float sx = scaling.getScaleX();
		float sy = scaling.getScaleY();
		int vw = getWidth();
		int vh = getHeight();
		int iw = (int) (vw * sx);
		int ih = (int) (vh * sy);
		int left = (vw - iw) / 2;
		int top = (vh - ih) / 2;

		if (DRAW_DEBUG_BORDER) {
			Rect boundingBox = new Rect(left, top, iw + left, ih + top);
			Paint p = new Paint();
			p.setColor(Color.GREEN);
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(5);
			canvas.drawRect(boundingBox, p);
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		String afField = prefs.getString("pref_camera_af_field", null);
		if (afField == null) {
			return;
		}
		String afMode = prefs.getString("pref_camera_af_mode", null);
		if ("auto".equals(afMode)) {
			return;
		}

		float px;
		float py;
		try {
			String[] parts = afField.split("/");
			px = Float.parseFloat(parts[0]);
			py = Float.parseFloat(parts[1]);
		} catch (Exception e) {
			Log.e(TAG, "Invalid AF Value: «" + afField + "»");
			return;
		}
		int x = (int) (iw * px) + left;
		int y = (int) (ih * py) + top;

		Rect rect = new Rect(x, y, x + 100, y + 100);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(7);
		canvas.drawRect(rect, paint);

		paint.setColor(Color.RED);
		paint.setStrokeWidth(3);
		canvas.drawRect(rect, paint);

		Paint pt = new Paint();
		pt.setColor(Color.WHITE);
		pt.setTextSize(70);
		if ("field".equals(afMode)) {
			canvas.drawText("A", x, y, pt);
		} else if ("manual".equals(afMode)) {
			canvas.drawText("M", x, y, pt);
		}
	}
}
