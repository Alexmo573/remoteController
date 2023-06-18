package org.my;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 实现屏幕被控制
 *
 *
 */
public class ScreenControlClient implements Runnable {

	private static final long serialVersionUID = -927388268343256207L;
	private ServerSocket server;
	private Thread thread;
	private Robot controlMouseRobot;
	private int port;
	public ScreenControlClient(int port) throws IOException, AWTException {
		this.port=port;
		server = new ServerSocket(port);
		thread = new Thread(this);
		controlMouseRobot = new Robot();
	}

	public void start() {
		thread.start();
	}

	@SuppressWarnings("deprecation")
	public void stop() {
		thread.stop();
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		while (true) {
			ObjectInputStream request = null;
			ObjectOutputStream response = null;
			try {
				Socket client = server.accept();
				response = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream()));
				request = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
				ControlCarrier carrier = (ControlCarrier) request.readObject();

				System.out.println("收到命令:" + carrier);

				if (carrier.getMouseX() != -1 && carrier.getMouseY() != -1) {
					controlMouseRobot.mouseMove(carrier.getMouseX(), carrier.getMouseY());
				}

				if (carrier.getMousePressBtn() != -1) {
					controlMouseRobot.mousePress(carrier.getMousePressBtn());
				}

				if (carrier.getMouseReleaseBtn() != -1) {
					controlMouseRobot.mouseRelease(carrier.getMouseReleaseBtn());
				}

				if (carrier.getWheelAmt() != -1) {
					controlMouseRobot.mouseWheel(carrier.getWheelAmt());
				}

				for (Integer pressKey : carrier.getKeyPressCode()) {
					controlMouseRobot.keyPress(pressKey);
				}

				for (Integer releaseKey : carrier.getKeyReleaseCode()) {
					controlMouseRobot.keyRelease(releaseKey);
				}
				System.out.println(carrier.getType());

				//发送桌面图像回客户端
				Dimension desktopSize = Toolkit.getDefaultToolkit().getScreenSize();
				BufferedImage curDesktop = controlMouseRobot.createScreenCapture(new Rectangle((int)desktopSize.getWidth()+50,(int)desktopSize.getHeight()));
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(curDesktop, "jpg", out);
				ControlCarrier desktopState = new ControlCarrier();
				desktopState.setDesktopImg(out.toByteArray());

				response.writeObject(desktopState);
				response.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {

				if (request != null) {
					try {
						request.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

}
