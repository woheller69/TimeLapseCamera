package at.andreasrohner.spartantimelapserec.camera2;

import android.graphics.Matrix;
import android.util.Log;
import android.view.Surface;

/**
 * Calculate the scaling for the preview
 */
public class PreviewScaling {

	/**
	 * Log Tag
	 */
	private static final String TAG = PreviewScaling.class.getSimpleName();

	/**
	 * Texture width
	 */
	private float tw = 1;

	/**
	 * Texture height
	 */
	private float th = 1;

	/**
	 * Image width
	 */
	private float iw;

	/**
	 * Image height
	 */
	private float ih;

	/**
	 * Rotation Enum Value
	 */
	private int rotationEnum;

	/**
	 * Center X for rotation
	 */
	private float centerX;

	/**
	 * Center Y for rotation
	 */
	private float centerY;

	/**
	 * Scale X
	 */
	private float scaleX = 1;

	/**
	 * Scale Y
	 */
	private float scaleY = 1;

	/**
	 * Rotate
	 */
	private int rotate = 0;

	/**
	 * Constructor
	 */
	public PreviewScaling() {
	}

	/**
	 * Set Texture Size
	 *
	 * @param width  Width
	 * @param height Height
	 */
	public void setTextureSize(int width, int height) {
		this.tw = width;
		this.th = height;
	}

	/**
	 * Set Image Size
	 *
	 * @param width  Width
	 * @param height Height
	 */
	public void setImageSize(int width, int height) {
		this.iw = width;
		this.ih = height;
	}

	/**
	 * @param rotationEnum Rotation Enum Value
	 */
	public void setRotationEnum(int rotationEnum) {
		this.rotationEnum = rotationEnum;
	}

	/**
	 * @return Scale X
	 */
	public float getScaleX() {
		return scaleX;
	}

	/**
	 * @return Scale Y
	 */
	public float getScaleY() {
		return scaleY;
	}

	/**
	 * Calculating scaling
	 * <p>
	 * This is not fully implemented, the Activity is locked to 0 deg, so the rotation don't need to
	 * be implemented. This should be fixed sometimes, but for now this is working.
	 */
	public void calculate() {
		this.centerX = tw / 2f;
		this.centerY = th / 2f;
		scaleX = 1;
		scaleY = 1;
		rotate = 0;

		Log.d(TAG, "transformTexture: info tw=" + tw + ", th=" + th + ", r=" + (tw / th) + " | iw=" + iw + ", ih=" + ih + ", r=" + (iw / ih));

		if (rotationEnum == Surface.ROTATION_0) {
			// *** Portrait ***

			float factorX = ih / th;
			float factorY = iw / tw;
			Log.d(TAG, "transformTexture: 0° fx=" + factorX + ", fy=" + factorY);

			if (factorX < factorY) {
				scaleY = factorX / factorY;
			} else {
				scaleX = factorY / factorX;
			}

			Log.d(TAG, "transformTexture: 0° sx=" + scaleX + ", sy=" + scaleY);
		} else if (rotationEnum == Surface.ROTATION_90) {
			// *** Landscape, left rotated ***
			rotate = 270;
		} else if (rotationEnum == Surface.ROTATION_270) {
			// *** Landscape, right rotated ***
			rotate = 90;
		}

		// Surface.ROTATION_180 not supported, it seems at least
		// Samsung Phones don't allow 180° rotation
	}

	/**
	 * Create the matrix to scale the preview image
	 *
	 * @return Matrix
	 */
	public Matrix createMatrix() {
		Matrix adjustment = new Matrix();
		if (rotate != 0 && rotate != 180) {
			// scaleX/scaleY are swapped! Image is rotated!
			adjustment.postScale(scaleY, scaleX, centerX, centerY);
		} else {
			adjustment.postScale(scaleX, scaleY, centerX, centerY);
		}
		if (rotate != 0) {
			adjustment.postRotate(rotate, centerX, centerY);
		}
		return adjustment;
	}
}
