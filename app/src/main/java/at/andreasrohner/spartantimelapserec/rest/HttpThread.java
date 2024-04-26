package at.andreasrohner.spartantimelapserec.rest;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Environment;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.BaseForegroundService;
import at.andreasrohner.spartantimelapserec.BuildConfig;
import at.andreasrohner.spartantimelapserec.Camera1ForegroundService;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.ServiceHelper;
import at.andreasrohner.spartantimelapserec.recorder.ImageRecorder;

import static android.os.Environment.DIRECTORY_PICTURES;
import static at.andreasrohner.spartantimelapserec.R.raw;

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
	private final RestService restService;

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
			if (processGetRequest(url, header)) {
				return;
			}
		}

		sendReplyHeader(ReplyCode.NOT_FOUND, "text/plain");

		sendLine("File not found");
		sendLine("");
		sendFooter();
	}

	/**
	 * Query battery level
	 *
	 * @return Battery level in percent
	 */
	private float getBatteryLevel() {
		Intent batteryStatus = restService.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int batteryLevel = -1;
		int batteryScale = 1;
		if (batteryStatus != null) {
			batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, batteryLevel);
			batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, batteryScale);
		}
		return batteryLevel / (float) batteryScale * 100;
	}

	/**
	 * Process a GET Request
	 *
	 * @param url    URL
	 * @param header Header fields
	 * @return true if processed
	 */
	private boolean processGetRequest(String url, Map<String, String> header) throws IOException {
		if ("/".equals(url)) {
			replyFile(raw.index, "text/html");
			return true;
		}

		if ("/rest".equals(url)) {
			replyFile(R.raw.help, "text/plain");
			return true;
		}

		if ("/favicon.ico".equals(url)) {
			replyFile(raw.favicon, "image/x-icon");
			return true;
		}

		if (url.startsWith("/1/device/battery")) {
			sendReplyHeader(ReplyCode.FOUND, "text/plain");
			sendLine(String.valueOf(getBatteryLevel()));
			return true;
		}

		if (url.startsWith("/1/current/")) {
			if (processCurrentRequest(url.substring(11))) {
				return true;
			}
		}

		if (url.startsWith("/1/ctrl/")) {
			if (processControlRequest(url.substring(8))) {
				return true;
			}
		}

		if (url.startsWith("/1/img/")) {
			if (processImageRequest(url.substring(6))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Process Current Request
	 *
	 * @param command Command
	 * @return true on success, false if not a valid command
	 */
	private boolean processCurrentRequest(String command) throws IOException {
		if ("img".equals(command)) {
			File lastImg = ImageRecorder.getCurrentRecordedImage();
			if (lastImg != null && lastImg.isFile()) {
				sendFileFromFilesystem(lastImg);
				return true;
			}
		}

		String result = null;
		if ("imgcount".equals(command)) {
			result = String.valueOf(ImageRecorder.getRecordedImagesCount());
		} else if ("lastimg".equals(command)) {
			File lastImg = ImageRecorder.getCurrentRecordedImage();
			if (lastImg == null) {
				result = "null";
			} else {
				File rootDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath());
				String relative = rootDir.toURI().relativize(lastImg.toURI()).getPath();
				result = lastImg.getName() + "\r\n" + lastImg.lastModified() + "\r\n" + relative;
			}
		}

		if (result != null) {
			sendReplyHeader(ReplyCode.FOUND, "text/plain");
			sendLine(result);
			return true;
		}

		return false;
	}

	/**
	 * Reply a file from RAW resources
	 *
	 * @param fileId      File ID
	 * @param contentType Content Type
	 * @throws IOException
	 */
	private void replyFile(int fileId, String contentType) throws IOException {
		sendReplyHeader(ReplyCode.FOUND, contentType);

		try (InputStream in = restService.getApplicationContext().getResources().openRawResource(fileId)) {
			copy(in, this.out);
		}
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

		// Download a file
		File requestedFile = new File(rootDir, req);
		if (requestedFile.isFile()) {
			sendFileFromFilesystem(requestedFile);
			return true;
		}

		return false;
	}

	/**
	 * Send a file from Filesystem
	 *
	 * @param file File
	 */
	private void sendFileFromFilesystem(File file) throws IOException {
		Map<String, String> additionalFields = new HashMap<>();
		additionalFields.put("Content-length", String.valueOf(file.length()));
		String mime = "application/octet-stream";

		if (file.getName().endsWith(".jpg")) {
			mime = "image/jpeg";
		} else if (file.getName().endsWith(".mp4")) {
			mime = "video/mp4";
		}

		sendReplyHeader(ReplyCode.FOUND, mime, additionalFields);
		try (InputStream in = new FileInputStream(file)) {
			copy(in, this.out);
		}
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
			result = BaseForegroundService.isRunning() ? "running" : "stopped";
		} else if ("start".equals(command)) {
			ServiceHelper helper = new ServiceHelper(restService.getApplicationContext());
			helper.start(false);
			result = "ok";
		} else if ("stop".equals(command)) {
			ServiceHelper helper = new ServiceHelper(restService.getApplicationContext());
			helper.stop();
			result = "ok";
		} else if ("param".equals(command)) {
			StringBuilder b = new StringBuilder();

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
	 * Send HTTP Text Footer
	 */
	private void sendFooter() throws IOException {
		sendLine("-------------------------------");
		sendLine("TimeLapseCam");
	}

	/**
	 * Send HTTP Header
	 *
	 * @param code             Code
	 * @param contentType      Content Type
	 * @param additionalFields Additional header fields
	 * @throws IOException
	 */
	public void sendReplyHeader(ReplyCode code, String contentType, Map<String, String> additionalFields) throws IOException {
		sendLine("HTTP/1.1 " + code.code + " " + code.text);
		sendLine("Date: " + HTTP_DATE_FORMAT.format(System.currentTimeMillis()));
		sendLine("Server: TimeLapseCam/" + BuildConfig.VERSION_NAME + " (Android)");
		sendLine("Content-Type: " + contentType);
		for (Map.Entry<String, String> e : additionalFields.entrySet()) {
			sendLine(e.getKey() + ": " + e.getValue());
		}

		// Empty line / end of header
		sendLine("");
	}

	@Override
	public void sendReplyHeader(ReplyCode code, String contentType) throws IOException {
		sendReplyHeader(code, contentType, Collections.emptyMap());
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