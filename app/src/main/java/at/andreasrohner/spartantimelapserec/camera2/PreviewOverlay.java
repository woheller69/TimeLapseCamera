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
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Overlay for AF Fields
 */
public class PreviewOverlay extends androidx.appcompat.widget.AppCompatImageView {

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
		float scaleX = scaling.getScaleX();
		float scaleY = scaling.getScaleY();
		int displayWidth = getWidth();
		int displayHeight = getHeight();
		int scaledWidth = (int) (displayWidth * scaleX);
		int scaledHeight = (int) (displayHeight * scaleY);
		int left = (displayWidth - scaledWidth) / 2;
		int top = (displayHeight - scaledHeight) / 2;

		if (DRAW_DEBUG_BORDER) {
			Rect boundingBox = new Rect(left, top, scaledWidth + left, scaledHeight + top);
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
		int x = (int) (scaledWidth * px) + left;
		int y = (int) (scaledHeight * py) + top;

		float scaleRect = scaleX;
		if (scaleY < scaleX) {
			scaleRect = scaleY;
		}

		int scaledRectWidth = (int) (pos.getFocusWidth() * scaleRect);
		int scaledRectHeight = (int) (pos.getFocusHeight() * scaleRect);

		Rect rect = new Rect(x, y, x + scaledRectWidth, y + scaledRectHeight);
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
	}
}
