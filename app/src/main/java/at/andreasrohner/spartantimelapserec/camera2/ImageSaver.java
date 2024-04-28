package at.andreasrohner.spartantimelapserec.camera2;

import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Save the Image when it's available
 */
public class ImageSaver implements ImageReader.OnImageAvailableListener {

	public ImageSaver() {
	}

	public void onImageAvailable(ImageReader reader) {
		try (Image image = reader.acquireLatestImage()) {
			ByteBuffer buffer = image.getPlanes()[0].getBuffer();
			byte[] bytes = new byte[buffer.capacity()];
			buffer.get(bytes);

			// TODO Filename
			final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");

			try (OutputStream output = new FileOutputStream(file)) {
				output.write(bytes);
			}
		} catch (IOException e) {
			ProcessErrorHandler.error("Error saving image", e);
		}
	}
}
