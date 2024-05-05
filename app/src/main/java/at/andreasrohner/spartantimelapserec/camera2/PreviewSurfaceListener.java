package at.andreasrohner.spartantimelapserec.camera2;

import android.graphics.SurfaceTexture;
import android.view.TextureView;

/**
 * Listener for Surface state
 */
public class PreviewSurfaceListener implements TextureView.SurfaceTextureListener {

	/**
	 * Callback
	 */
	private final AvailableCallback callback;

	/**
	 * Constructor
	 *
	 * @param callback Callback
	 */
	public PreviewSurfaceListener(AvailableCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		this.callback.onAvailable();
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		// Transform you image captured size according to the surface width and height
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

	/**
	 * Callback if available
	 */
	public interface AvailableCallback {

		/**
		 * Called when the source is available
		 */
		void onAvailable();
	}
};
