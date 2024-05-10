package at.andreasrohner.spartantimelapserec.rest;

import java.net.ServerSocket;
import java.net.Socket;

import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * HTTP REST API Service, TCP Listener Service
 * <p>
 * Inspirated by SwiFTP https://github.com/ppareit/swiftp/blob/master/app/src/main/java/be/ppareit/swiftp/server/TcpListener.java
 * <p>
 * (c) Andreas Butti, 2024
 */
public class TcpListener extends Thread {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

	/**
	 * Socket for incoming connections
	 */
	private ServerSocket listenSocket;

	/**
	 * HTTP REST API Service
	 */
	private RestService restService;

	/**
	 * Constructor
	 *
	 * @param listenSocket Socket for incoming connections
	 * @param restService  HTTP REST API Service
	 */
	public TcpListener(ServerSocket listenSocket, RestService restService) {
		this.listenSocket = listenSocket;
		this.restService = restService;
	}

	/**
	 * Close listening Socket
	 */
	public void quit() {
		try {
			listenSocket.close(); // if the TcpListener thread is blocked on accept,
			// closing the socket will raise an exception
		} catch (Exception e) {
			logger.warn("Exception closing TcpListener listenSocket", e);
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket clientSocket = listenSocket.accept();
				logger.info("New connection from {}", clientSocket.getRemoteSocketAddress());
				HttpThread newSession = new HttpThread(clientSocket, restService);
				newSession.start();
				restService.registerSessionThread(newSession);
			}
		} catch (Exception e) {
			logger.debug("Exception in TcpListener", e);
		}
	}
}