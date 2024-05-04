package at.andreasrohner.spartantimelapserec.camera2.pupcfg;

import android.util.Size;

import androidx.annotation.NonNull;

/**
 * Camera Resolution Model
 */
public class ResolutionModel implements IdData, Comparable<ResolutionModel> {

	/**
	 * Display name
	 */
	private final String name;

	/**
	 * Size
	 */
	private final Size size;

	/**
	 * Constructor
	 *
	 * @param size Size
	 */
	public ResolutionModel(Size size) {
		this.size = size;
		this.name = size.toString();
	}

	@Override
	public int compareTo(ResolutionModel o) {
		return Long.signum((o.size.getWidth() * (long) o.size.getHeight()) - (size.getWidth() * (long) size.getHeight()));
	}

	@Override
	public String getId() {
		return name;
	}

	@NonNull
	@Override
	public String toString() {
		return name;
	}
}