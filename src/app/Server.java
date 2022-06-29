package app;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import app.Server.Handle;

public class Server {
	public Server() {
		new Thread(() -> {
			try {
				ServerSocket serverSocket = new ServerSocket(9999);
				System.out.println("MultithreadServer started at : " + LocalDateTime.now());
				Socket socket = serverSocket.accept();
				System.out.println("Connected");
				new Thread(new Handle(socket)).start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	class Handle implements Runnable {
		private Socket socket;
		private DataOutputStream out;
		private ObjectInputStream in;

		public Handle(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				out = new DataOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());

				while (true) {
					String cmd = in.readUTF();
					if (cmd.equals("shutdown")) {
						System.out.println("> Shutdown");
						if (shutdown()) {
							out.writeUTF("Shutdowned...");
						} else {
							out.writeUTF("Failed to shutdown.");
						}

					} else if (cmd.equals("restart")) {
						System.out.println("> Restart");
						if (restart()) {
							out.writeUTF("Restarted...");
						} else {
							out.writeUTF("Failed to restart");
						}

					} else if (cmd.equals("open")) {
						String path = in.readUTF();
						System.out.println(String.format("> Open %s", path));

						if (open(path)) {
							out.writeUTF("Opened: " + path);
						} else {
							out.writeUTF("Failed to open the file/directory: " + path);
						}
					}
					out.flush();

				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static void main(String[] args) {
		Server server = new Server();
	}

	public boolean shutdown() {
		Runtime runtime = Runtime.getRuntime();
		try {
			System.out.println("Shutting down the PC after 5 seconds.");
			runtime.exec("shutdown -s -t 5");
			return true;
		} catch (IOException e) {
			System.out.println("Exception: " + e);
		}
		return false;
	}
	
	public boolean restart() {
		Runtime runtime = Runtime.getRuntime();
		try {
			System.out.println("Restarting the PC after 5 seconds.");
			runtime.exec("shutdown -r -t 5");
			return true;
		} catch (IOException e) {
			System.out.println("Exception: " + e);
		}
		return false;
		
	}

	public boolean open(String path) {
		Desktop desktop = Desktop.getDesktop();
        File dirToOpen = null;
        try {
            dirToOpen = new File(path);
            desktop.open(dirToOpen);
            return true;
        } catch (IllegalArgumentException iae) {
            System.out.println("File Not Found");
        } catch (IOException e) {
			e.printStackTrace();
		}
        return false;
	}
}
