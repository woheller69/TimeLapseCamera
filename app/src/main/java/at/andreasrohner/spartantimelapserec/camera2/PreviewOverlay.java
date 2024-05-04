package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * Overlay for AF Fields
 */
public class PreviewOverlay extends androidx.appcompat.widget.AppCompatImageView {

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

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawColor(Color.TRANSPARENT);
		super.onDraw(canvas);

		Rect r = new Rect(100, 100, 200, 200);
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(7);
		canvas.drawRect(r, p);

		p.setColor(Color.RED);
		p.setStrokeWidth(3);
		canvas.drawRect(r, p);
		this.postInvalidate();
	}
}
