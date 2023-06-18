package org.my;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现屏幕控制
 *
 *
 */
public class ScreenControl extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	private static final long serialVersionUID = -806686211556049511L;
	private String controlIp;
	private int controlPort;
	private BufferedImage controlDesktop;

	public ScreenControl(String controlIp, int controlPort) {
		this.controlIp = controlIp;
		this.controlPort = controlPort;

		setFocusable(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addKeyListener(this);

	}

	public void start() {
		ControlCarrier command = new ControlCarrier();
		sendControlCommand(command);
	}

	@Override
	public void paintComponent(Graphics g) {
		if (controlDesktop != null) {
			g.drawImage(controlDesktop, 0, 0, this);
		}
	}

	private void sendControlCommand(ControlCarrier command) {
		ObjectOutputStream objectOut = null;
		ObjectInputStream objectInput = null;
		try {
			Socket commandSocket = new Socket(InetAddress.getByName(controlIp), controlPort);

			//发送命令
			objectOut = new ObjectOutputStream(new BufferedOutputStream(commandSocket.getOutputStream()));
			objectOut.writeObject(command);
			objectOut.flush();

			objectInput = new ObjectInputStream(new BufferedInputStream(commandSocket.getInputStream()));

			//接收被控制端图像
			ControlCarrier desktopState = (ControlCarrier) objectInput.readObject();
			displayDesktopState(desktopState);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (objectOut != null) {
				try {
					objectOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (objectInput != null) {
				try {
					objectInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void displayDesktopState(ControlCarrier desktopState) {
		ByteArrayInputStream bin = new ByteArrayInputStream(desktopState.getDesktopImg());
		try {
			controlDesktop = ImageIO.read(bin);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "被控制端已断开连接，或网络异常!");
		}
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		ControlCarrier command = new ControlCarrier();
		command.setType("mouseClicked");
		if (e.getClickCount() == 1) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				command.setMousePressBtn(InputEvent.BUTTON1_MASK);
				command.setMouseReleaseBtn(InputEvent.BUTTON1_MASK);
			} else if (e.getButton() == MouseEvent.BUTTON2) {
				command.setMousePressBtn(InputEvent.BUTTON2_MASK);
				command.setMouseReleaseBtn(InputEvent.BUTTON2_MASK);
			} else if (e.getButton() == MouseEvent.BUTTON3) {
				command.setMousePressBtn(InputEvent.BUTTON3_MASK);
				command.setMouseReleaseBtn(InputEvent.BUTTON3_MASK);
			}
		} else if (e.getClickCount() == 2) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				command.setMousePressBtn(InputEvent.BUTTON1_MASK);
				command.setMouseReleaseBtn(InputEvent.BUTTON1_MASK);
				command.setMousePressBtn(InputEvent.BUTTON1_MASK);
				command.setMouseReleaseBtn(InputEvent.BUTTON1_MASK);
			} else if (e.getButton() == MouseEvent.BUTTON2) {
				command.setMousePressBtn(InputEvent.BUTTON2_MASK);
				command.setMouseReleaseBtn(InputEvent.BUTTON2_MASK);
				command.setMousePressBtn(InputEvent.BUTTON2_MASK);
				command.setMouseReleaseBtn(InputEvent.BUTTON2_MASK);
			} else if (e.getButton() == MouseEvent.BUTTON3) {
				command.setMousePressBtn(InputEvent.BUTTON3_MASK);
				command.setMouseReleaseBtn(InputEvent.BUTTON3_MASK);
				command.setMousePressBtn(InputEvent.BUTTON3_MASK);
				command.setMouseReleaseBtn(InputEvent.BUTTON3_MASK);
			}
		}
		sendControlCommand(command);
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		ControlCarrier command = new ControlCarrier();
		command.setType("mousePressed");
		if (e.getButton() == MouseEvent.BUTTON1) {
			command.setMousePressBtn(InputEvent.BUTTON1_MASK);
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			command.setMousePressBtn(InputEvent.BUTTON2_MASK);
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			command.setMousePressBtn(InputEvent.BUTTON3_MASK);
		}
		sendControlCommand(command);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		ControlCarrier command = new ControlCarrier();
		command.setType("mouseReleased");
		if (e.getButton() == MouseEvent.BUTTON1) {
			command.setMouseReleaseBtn(InputEvent.BUTTON1_MASK);
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			command.setMouseReleaseBtn(InputEvent.BUTTON2_MASK);
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			command.setMouseReleaseBtn(InputEvent.BUTTON3_MASK);
		}
		sendControlCommand(command);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		ControlCarrier command = new ControlCarrier();
		command.setType("mouseDragged");
		command.setMousePressBtn(e.getButton());
		command.setMouseX(e.getX());
		command.setMouseY(e.getY());
		sendControlCommand(command);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		ControlCarrier command = new ControlCarrier();
		command.setType("mouseMoved");
		command.setMouseX(e.getX());
		command.setMouseY(e.getY());
		sendControlCommand(command);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		ControlCarrier command = new ControlCarrier();
		command.setType("mouseWheelMoved");
		command.setWheelAmt(e.getWheelRotation());
		sendControlCommand(command);
	}

	@Override
	public void keyPressed(KeyEvent e) {

		ControlCarrier command = new ControlCarrier();
		command.setType("keyPressed");
		List<Integer> keyPress = new ArrayList<Integer>();
		if (e.isControlDown()) {
			keyPress.add(KeyEvent.VK_CONTROL);
		}
		if (e.isAltDown()) {
			keyPress.add(KeyEvent.VK_ALT);
		}
		if (e.isShiftDown()) {
			keyPress.add(KeyEvent.VK_SHIFT);
		}
		keyPress.add(e.getKeyCode());
		command.setKeyPressCode(keyPress);
		sendControlCommand(command);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		ControlCarrier command = new ControlCarrier();
		command.setType("keyReleased");
		List<Integer> keyRelease = new ArrayList<Integer>();
		keyRelease.add(e.getKeyCode());
		command.setKeyReleaseCode(keyRelease);
		sendControlCommand(command);
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), getHeight());
	}

}
