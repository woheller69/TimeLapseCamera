package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.preference.PrefUtil;
import at.andreasrohner.spartantimelapserec.preference.preftype.ShowCameraInfoPreference;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Overlay for AF Fields
 */
public class PreviewOverlay extends androidx.appcompat.widget.AppCompatImageView {

	/**
	 * Rect size in pixel
	 */
	private static final int RECT_SIZE = 100;

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

	/**
	 * Draw debug border
	 */
	private static final boolean DRAW_DEBUG_BORDER = false;

	/**
	 * Calculate the scaling for the preview
	 */
	private PreviewScaling scaling;

	/**
	 * Focus state
	 */
	private FocusChangeListener.FocusState focusState;

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

	/**
	 * @param focusState Focus state
	 */
	public void setFocusState(FocusChangeListener.FocusState focusState) {
		this.focusState = focusState;
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
		PrefUtil.AfMode afMode = PrefUtil.getAfMode(prefs);
		if (afMode == PrefUtil.AfMode.AUTO) {
			return;
		}

		AfPos pos = AfPos.fromPref(prefs);
		if (pos == null) {
			return;
		}

		// X/Y Swapped, preview is always portrait, camera is always landscape
		float px = pos.getFocusRelY();
		float py = pos.getFocusRelX();
		int x = (int) (iw * px) + left;
		int y = (int) (ih * py) + top;

		x -= RECT_SIZE / 2;
		y -= RECT_SIZE / 2;
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}

		Rect rect = new Rect(x, y, x + RECT_SIZE, y + RECT_SIZE);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(7);
		canvas.drawRect(rect, paint);

		if (focusState == FocusChangeListener.FocusState.FOCUS_SUCCESS) {
			paint.setColor(Color.GREEN);
		} else if (focusState == FocusChangeListener.FocusState.FOCUS_SUCCESS) {
			paint.setColor(Color.RED);
		} else {
			paint.setColor(Color.GRAY);
		}
		paint.setStrokeWidth(3);
		canvas.drawRect(rect, paint);

		Paint pt = new Paint();
		pt.setColor(Color.WHITE);
		pt.setTextSize(70);
		if ("field".equals(afMode)) {
			canvas.drawText("A", x, y, pt);
		} else if ("manual".equals(afMode)) {
			String[] text = ShowCameraInfoPreference.formatMfDistance(getContext(), prefs);
			canvas.drawText("M: " + text[1], x, y, pt);
		}
	}
}
