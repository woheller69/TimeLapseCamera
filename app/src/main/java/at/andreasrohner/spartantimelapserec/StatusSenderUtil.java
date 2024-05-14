package at.andreasrohner.spartantimelapserec;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Send a status of the recoding
 */
public final class StatusSenderUtil {

	/**
	 * Utility class
	 */
	private StatusSenderUtil() {
	}

	/**
	 * Send Error
	 *
	 * @param handler Handler
	 * @param msg     Message
	 */
	public static void sendError(Handler handler, String msg) {
		if (handler == null) {
			return;
		}

		Message m = new Message();
		Bundle b = new Bundle();
		b.putString("status", "error");
		b.putString("msg", msg);
		m.setData(b);
		m.setTarget(handler);
		handler.sendMessage(m);
	}

	/**
	 * Send Success
	 *
	 * @param handler Handler
	 */
	public static void sendSuccess(Handler handler) {
		if (handler == null) {
			return;
		}

		Message m = new Message();
		Bundle b = new Bundle();
		b.putString("status", "success");
		m.setData(b);
		m.setTarget(handler);
		handler.sendMessage(m);
	}
}
