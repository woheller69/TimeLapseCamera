package at.andreasrohner.spartantimelapserec;

import at.andreasrohner.spartantimelapserec.camera2.filename.ImageFile;

/**
 * State of the current image / video recording (count, Filename...)
 */
public final class ImageRecorderState {

	/**
	 * Current / last recorded image
	 */
	private static ImageFile currentRecordedImage;

	/**
	 * Count of recorded images within the whole app session
	 */
	private static int recordedImagesCount = 0;

	/**
	 * Utility class
	 */
	private ImageRecorderState() {
	}

	/**
	 * Set curent image filename
	 *
	 * @param file File
	 */
	public static void setCurrentImage(ImageFile file) {
		currentRecordedImage = file;
		recordedImagesCount++;
	}

	/**
	 * Reset Image Count
	 */
	public static void resetImageCount() {
		recordedImagesCount = 0;
	}

	/**
	 * @return Current / last recorded image
	 */
	public static ImageFile getCurrentRecordedImage() {
		return currentRecordedImage;
	}

	/**
	 * @return Count of recorded images within the whole app session
	 */
	public static int getRecordedImagesCount() {
		return recordedImagesCount;
	}
}
