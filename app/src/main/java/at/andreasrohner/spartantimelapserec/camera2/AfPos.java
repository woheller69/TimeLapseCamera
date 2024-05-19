package at.andreasrohner.spartantimelapserec.camera2;

import android.content.SharedPreferences;

import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Autofocus position
 */
public class AfPos {

	/**
	 * Logger
	 */
	private static Logger logger = new Logger(AfPos.class);

	/**
	 * Image resolution
	 */
	private int width = 0;

	/**
	 * Image resolution
	 */
	private int heigth = 0;

	/**
	 * Focus position X
	 */
	private int focusX = 0;

	/**
	 * Focus position Y
	 */
	private int focusY = 0;

	/**
	 * Focus Width
	 */
	private int focusWidth = 0;

	/**
	 * Focus Height
	 */
	private int focusHeight = 0;

	/**
	 * Parse position from String, e.g. "Res:4032/4032 Pos:2043,1362,100,100"
	 *
	 * @param str String
	 * @return Position, or null
	 */
	public static AfPos fromString(String str) {
		if (str == null || "".equals(str)) {
			logger.debug("AF Pos is null / not set");
			return null;
		}

		if (!str.startsWith("Res:")) {
			logger.error("Invalid AF Pos #1: «{}»", str);
			return null;
		}

		String[] parts = str.split(" ");
		if (parts.length != 2) {
			logger.error("Invalid AF Pos #2: «{}», len={}", str, parts.length);
			return null;
		}

		String resString = parts[0].trim();
		String posString = parts[1].trim();

		if (!resString.startsWith("Res:") || !posString.startsWith("Pos:")) {
			logger.error("Invalid AF Pos #3: «{}»", str);
			return null;
		}

		resString = resString.substring(4).trim();
		posString = posString.substring(4).trim();

		String[] sizeParts = resString.split("/");
		String[] posParts = posString.split(",");

		if (sizeParts.length != 2 || posParts.length != 4) {
			logger.error("Invalid AF Pos #4: «{}»", str);
			return null;
		}

		AfPos afPos = new AfPos();
		try {
			afPos.width = Integer.parseInt(sizeParts[0]);
			afPos.heigth = Integer.parseInt(sizeParts[1]);

			afPos.focusX = Integer.parseInt(posParts[0]);
			afPos.focusY = Integer.parseInt(posParts[1]);
			afPos.focusWidth = Integer.parseInt(posParts[2]);
			afPos.focusHeight = Integer.parseInt(posParts[3]);
		} catch (Exception ex) {
			logger.error("Invalid AF Pos #5: «{}»", str, ex);
			return null;
		}

		return afPos;
	}

	/**
	 * Parse position from Preferences
	 *
	 * @param prefs Preferences
	 * @return Position, or null
	 */
	public static AfPos fromPref(SharedPreferences prefs) {
		return fromString(prefs.getString("pref_camera_af_field", null));
	}

	/**
	 * Use from... Method to create instance
	 */
	private AfPos() {
	}

	/**
	 * @return Image resolution
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return Image resolution
	 */
	public int getHeigth() {
		return heigth;
	}

	/**
	 * @return Focus position X
	 */
	public int getFocusX() {
		return focusX;
	}

	/**
	 * @return Focus position Y
	 */
	public int getFocusY() {
		return focusY;
	}

	/**
	 * @return Focus Width
	 */
	public int getFocusWidth() {
		return focusWidth;
	}

	/**
	 * @return Focus Height
	 */
	public int getFocusHeight() {
		return focusHeight;
	}

	/**
	 * @return Relative focus position, 0.00 ... 1.00
	 */
	public float getFocusRelX() {
		return (float) focusX / (float) width;
	}

	/**
	 * @return Relative focus position, 0.00 ... 1.00
	 */
	public float getFocusRelY() {
		return (float) focusY / (float) heigth;
	}
}
