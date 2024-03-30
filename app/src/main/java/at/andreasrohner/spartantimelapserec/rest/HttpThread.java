package at.andreasrohner.spartantimelapserec.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import at.andreasrohner.spartantimelapserec.BuildConfig;
import at.andreasrohner.spartantimelapserec.ForegroundService;
import at.andreasrohner.spartantimelapserec.ServiceHelper;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Handle one HTTP Connection
 * <p>
 * This is a very simple HTTP Server Implementation. As the Server does not support HTTPS, it's
 * also not secure.
 * It's an interface which should only be used in an internal network.
 * Also the Performance could be improved, with Threadpool, HTTP 1.1 Connection reuse and so on.
 * <p>
 * Andreas Butti, 2024
 */
public class HttpThread extends Thread implements HttpOutput, Closeable {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Pattern for Header Date / Time
	 */
	private final SimpleDateFormat HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

	/**
	 * HTTP Reply codes
	 */
	public enum ReplyCode {

		/**
		 * Found
		 */
		FOUND(200, "Found"),

		/**
		 * Forbidden
		 */
		FORBIDDEN(403, "Forbidden"),

		/**
		 * File was not found
		 */
		NOT_FOUND(404, "Not found");

		/**
		 * HTTP Code
		 */
		private final int code;

		/**
		 * Result Text
		 */
		private final String text;

		/**
		 * Constructor
		 *
		 * @param code HTTP Code
		 * @param text Result Text
		 */
		private ReplyCode(int code, String text) {
			this.code = code;
			this.text = text;
		}
	}

	/**
	 * TCP Socket
	 */
	private final Socket socket;

	/**
	 * Output Stream to reply
	 */
	private final OutputStream out;

	/**
	 * HTTP REST API Service
	 */
	private RestService restService;

	/**
	 * Constructor
	 *
	 * @param socket      TCP Socket
	 * @param restService HTTP REST API Service
	 */
	public HttpThread(Socket socket, RestService restService) throws IOException {
		this.socket = socket;
		this.out = new BufferedOutputStream(socket.getOutputStream());
		this.restService = restService;
	}

	@Override
	public void run() {
		try {
			processRequest();
			this.out.flush();
		} catch (IOException e) {
			Log.e(TAG, "Error parsing HTTP Request", e);
		} finally {
			close();
		}
	}

	/**
	 * Parse / Process the incoming request
	 */
	private void processRequest() throws IOException {
		InputStreamReader isr = new InputStreamReader(socket.getInputStream());
		BufferedReader reader = new BufferedReader(isr);

		String request = reader.readLine();
		int pos = request.indexOf(' ');
		if (pos == -1) {
			Log.e(TAG, "Invalid Request: «" + request + "»");
		}
		String method = request.substring(0, pos);
		int pos2 = request.indexOf(' ', pos + 1);
		if (pos2 == -1) {
			Log.e(TAG, "Invalid Request: «" + request + "»");
		}
		String url = request.substring(pos + 1, pos2);
		String protocol = request.substring(pos2);

		Log.d(TAG, "Request: «" + method + "» «" + url + "» «" + protocol + "»");

		String line = reader.readLine();
		Map<String, String> header = new HashMap<>();
		while (!line.isEmpty()) {
			pos = line.indexOf(':');
			if (pos == -1) {
				continue;
			}

			String key = line.substring(0, pos);
			String value = line.substring(pos + 1);
			header.put(key, value);

			line = reader.readLine();
		}

		processRequest(method, url, protocol, header);
	}

	/**
	 * Process a HTTP Request
	 *
	 * @param method   Method
	 * @param url      URL
	 * @param protocol Protocol
	 * @param header   Header Fields
	 */
	private void processRequest(String method, String url, String protocol, Map<String, String> header) throws IOException {
		if ("GET".equals(method)) {
			if ("/".equals(url)) {
				// Reply with Help
				replyHelp();
				return;
			}

			if (url.startsWith("/1/ctrl/")) {
				if (processControlRequest(url.substring(8))) {
					return;
				}
			}
			if (url.startsWith("/1/img/")) {
				if (processImageRequest(url.substring(6))) {
					return;
				}
			}
		}

		sendReplyHeader(ReplyCode.NOT_FOUND, "text/plain");

		sendLine("File not found");
		sendLine("");
		sendFooter();
	}

	/**
	 * Process Image Request
	 *
	 * @param req Requested URL
	 * @return true on success, false if not a valid command
	 */
	private boolean processImageRequest(String req) throws IOException {
		Log.d(TAG, "Request image: " + req);

		// No file access to parent directory
		if (req.contains("..")) {
			sendReplyHeader(ReplyCode.FORBIDDEN, "text/plain");

			sendLine("Forbidden");
			sendLine("");
			sendFooter();
			return true;
		}

		Context ctx = restService.getApplicationContext();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		File rootDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath());

		if (!rootDir.isDirectory()) {
			return false;
		}

		// List folder
		if (req.endsWith("/list")) {
			ListFolderPlain list = new ListFolderPlain(this, rootDir);
			return list.output(req.substring(0, req.length() - 5));
		}

		// HTML Interface
		if (req.endsWith("/listhtml")) {
			ListFolderHtml list = new ListFolderHtml(this, rootDir);
			return list.output(req.substring(0, req.length() - 9));
		}

		File requestedFile = new File(rootDir, req);
		if (requestedFile.isFile()) {
			sendReplyHeader(ReplyCode.FOUND, "image/jpeg");
			try (InputStream in = new FileInputStream(requestedFile)) {
				copy(in, this.out);
			}
			return true;
		}

		return false;
	}

	/**
	 * Copy inputstream to outputstream
	 *
	 * @param source Source
	 * @param target Target
	 * @throws IOException
	 */
	private void copy(InputStream source, OutputStream target) throws IOException {
		byte[] buf = new byte[8192];
		int length;
		while ((length = source.read(buf)) != -1) {
			target.write(buf, 0, length);
		}
	}

	/**
	 * Process Control Request
	 *
	 * @param command Command
	 * @return true on success, false if not a valid command
	 */
	private boolean processControlRequest(String command) throws IOException {
		String result = null;
		if ("status".equals(command)) {
			result = ForegroundService.mIsRunning ? "running" : "stopped";
		} else if ("start".equals(command)) {
			ServiceHelper helper = new ServiceHelper(restService.getApplicationContext());
			helper.start();
			result = "ok";
		} else if ("stop".equals(command)) {
			ServiceHelper helper = new ServiceHelper(restService.getApplicationContext());
			helper.stop();
			result = "ok";
		} else if ("param".equals(command)) {
			StringBuffer b = new StringBuffer();

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(restService.getApplicationContext());
			for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
				b.append(e.getKey());
				b.append('=');
				b.append(e.getValue());
				b.append("\r\n");
			}

			result = b.toString();
		}

		if (result != null) {
			sendReplyHeader(ReplyCode.FOUND, "text/plain");
			sendLine(result);
			return true;
		}

		return false;
	}

	/**
	 * Reply help
	 */
	private void replyHelp() throws IOException {
		sendReplyHeader(ReplyCode.FOUND, "text/plain");

		sendLine("HELP");
		sendLine("");
		sendLine("GET /: Show this Help");
		sendLine("");
		sendLine("REST API v1:");
		sendLine("GET /1/ctrl/status: Get current state: [stopped/running]");
		sendLine("GET /1/ctrl/start: Start recording");
		sendLine("GET /1/ctrl/stop: Stop recording");
		sendLine("GET /1/ctrl/param: Get parameter");
		sendLine("GET /1/img/list: List image folders");
		sendLine("GET /1/img/listhtml: user clickable HTML page");
		sendLine("GET /1/img/<folder>/list: List folder / images");
		sendLine("GET /1/img/<folder>/<folder>/list: List folder / images");
		sendLine("GET /1/img/<folder>/.../<image>: Download image");

		sendFooter();
	}

	/**
	 * Send HTTP Text Footer
	 */
	private void sendFooter() throws IOException {
		sendLine("-------------------------------");
		sendLine("TimeLapseCam");
	}

	@Override
	public void sendReplyHeader(ReplyCode code, String contentType) throws IOException {
		sendLine("HTTP/1.1 " + code.code + " " + code.text);
		sendLine("Date: " + HTTP_DATE_FORMAT.format(System.currentTimeMillis()));
		sendLine("Server: TimeLapseCam/" + BuildConfig.VERSION_NAME + " (Android)");
		sendLine("Content-Type: " + contentType);
		// Empty line / end of header
		sendLine("");
	}

	@Override
	public void sendLine(String line) throws IOException {
		out.write(line.getBytes(StandardCharsets.ISO_8859_1));
		out.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
	}

	@Override
	public synchronized void close() {
		if (socket == null) {
			return;
		}

		try {
			socket.close();
		} catch (Exception e) {
			Log.w(TAG, "Error closing socket", e);
		}
	}
}