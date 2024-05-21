package at.andreasrohner.spartantimelapserec.camera2.filename;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import androidx.preference.PreferenceManager;

/**
 * Controller for output filenames
 */
public abstract class AbstractFileNameController {

	/**
	 * Project name
	 */
	protected final String projectName;

	/**
	 * Context
	 */
	protected final Context context;

	/**
	 * Preferences
	 */
	protected final SharedPreferences prefs;

	/**
	 * File Numbering
	 */
	protected int fileIndex = 0;

	/**
	 * Factory method to create correct instance
	 *
	 * @param context Context
	 * @return Implementation
	 */
	public static AbstractFileNameController createInstance(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean enabled = prefs.getBoolean("external_storage_enabled", false);
		String externalStoragePath = prefs.getString("external_storage_path", null);
		if (enabled && externalStoragePath != null) {
			return new FileNameControllerExternal(context);
		} else {
			return new FileNameControllerInternal(context);
		}
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	protected AbstractFileNameController(Context context) {
		this.context = context;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.projectName = prefs.getString("pref_project_title", "NO_NAME");
	}

	/**
	 * Get Next output filename
	 *
	 * @param ext Extension
	 * @return File
	 * @throws IOException
	 */
	public abstract ImageOutput getOutputFile(String ext) throws IOException;

	/**
	 * Image Output
	 */
	public static class ImageOutput implements Closeable {

		/**
		 * Image name
		 */
		private String name;

		/**
		 * OutputStream
		 */
		private OutputStream out;

		/**
		 * Constructor
		 *
		 * @param name Image name
		 * @param out  OutputStream
		 */
		public ImageOutput(String name, OutputStream out) {
			this.name = name;
			this.out = out;
		}

		/**
		 * @return Image name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return OutputStream
		 */
		public OutputStream getOut() {
			return out;
		}

		@Override
		public void close() throws IOException {
			out.close();
		}
	}
}
