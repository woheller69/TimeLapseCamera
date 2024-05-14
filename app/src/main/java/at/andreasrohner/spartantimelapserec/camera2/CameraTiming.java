package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Camera timing values
 */
public class CameraTiming {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

	/**
	 * Predefined time values, copied from a Canon EOS Camera (a little extended)
	 */
	private static final String TIMES = "1/16000 1/8000 1/4000 1/3200 1/2500 1/2000 1/1600 1/1250 1/1000 1/800 1/640 1/500 1/400 1/320 1/250 1/200 1/160 1/125 1/100 1/80 1/60 1/50 1/40 1/30 1/25 1/20 1/15 1/13 1/10 1/8 1/6 1/5 1/4 0\"3 0\"4 0\"5 0\"6 0\"8 1\" 1\"3 1\"6 2\" 2\"5 3\"2 4\" 5\" 6\" 8\" 10\" 13\" 15\" 20\" 25\" 30\"";

	/**
	 * Display values
	 */
	private List<String> displayValues = new ArrayList<>();

	/**
	 * Time values in ns
	 */
	private List<Long> timeValues = new ArrayList<>();

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	public CameraTiming(Context context) {
		displayValues.add(context.getString(R.string.camera_value_auto));
		timeValues.add(-1L);
	}

	/**
	 * @return Display values
	 */
	public List<String> getDisplayValues() {
		return displayValues;
	}

	/**
	 * @return Time values in ns
	 */
	public List<Long> getTimeValues() {
		return timeValues;
	}

	/**
	 * Build exposure time selection
	 * <p>
	 * Example, Samsung Galaxy S8:
	 * min 60000/1000000000s => 6/100000s
	 * max 100000000/1000000000s => 1/10s
	 *
	 * @param lower Lower range
	 * @param upper Upper range
	 */
	public void buildRangeSelection(long lower, long upper) {
		for (String t : TIMES.split(" ")) {
			long ns = parseTime(t);
			if (ns < lower) {
				continue;
			}
			if (ns > upper) {
				break;
			}

			displayValues.add(t);
			timeValues.add(ns);
		}
	}

	/**
	 * Parse Time String
	 *
	 * @param t Time String
	 * @return Time in ns
	 */
	private long parseTime(String t) {
		if (t.startsWith("1/")) {
			long divisor = Long.parseLong(t.substring(2));
			return 1000000000L / divisor;
		}

		int pos = t.indexOf('"');
		if (pos != -1) {
			String s2 = t.substring(pos + 1);
			long t1 = Long.parseLong(t.substring(0, pos));
			if (s2.isEmpty()) {
				return 1000000000L * t1;
			}

			long t2 = Long.parseLong(s2);
			return 1000000000L * t1 + 1000000000L / t2;
		}

		logger.error("Invalid predefined time value «{}»", t);
		return -1;
	}

	/**
	 * Find best matching value
	 *
	 * @param exposure Exposure in ns
	 * @return Exposure String
	 */
	public String findBestMatchingValue(long exposure) {
		int bestMatching = -1;
		for (int i = 0; i < timeValues.size(); i++) {
			long v = timeValues.get(i);
			if (v < exposure) {
				bestMatching = i;
				continue;
			}
			if (v == exposure) {
				return displayValues.get(i);
			}

			if (v > exposure) {
				break;
			}
		}

		if (bestMatching != -1) {
			return displayValues.get(bestMatching);
		}

		// Shuld not happen, there is just a selection, no manual input
		return exposure + "ns";
	}
}
