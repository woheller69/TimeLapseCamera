package at.andreasrohner.spartantimelapserec;

import java.io.File;

/**
 * State of the current image / video recording (count, Filename...)
 */
public final class ImageRecorderState {

	/**
	 * Current / last recorded image
	 */
	private static File currentRecordedImage;

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
	public static void setCurrentImage(File file) {
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
	public static File getCurrentRecordedImage() {
		return currentRecordedImage;
	}

	/**
	 * @return Count of recorded images within the whole app session
	 */
	public static int getRecordedImagesCount() {
		return recordedImagesCount;
	}
}
