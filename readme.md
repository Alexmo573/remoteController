# 说明
* 本项目是一个远程控制木马，client是被控制端，controller控制端,控制端返回的所有信息都记录在**C盘下的pic目录**中，按时间划分
* 本项目的功能包括：
    * 1、程序开机自动启动，并自动发送邮件
    * 2、屏幕控制，可以观看被控制端屏幕，控制鼠标，控制键盘
    * 3、执行dos命令，并将信息返回、这里可以执行关机等命令
    * 4、锁定鼠标，这里通过一个线程实现
    * 5、获取被控制端的桌面截图，将桌面画面截图并发送给控制端
    * 6、在被控制端弹出对话框,多种对话框模式
    * 7、让被控制端闪屏
    * 8、复制被控制端文件到控制端
    * 9、格式化控制端的电脑
* 项目结构简介
    * 项目分为两个模块：被控制端：client；控制端：controller
    * client端
        - Main类是主类，包含命令控制的所有方法和类，启动类main
        - ScreenControlClient类是实现屏幕被控制的类
        - ControlCarrier类是屏幕控制类传输命令的类
    * controller端
        - Main类是主类，包含命令控制的所有方法和类，启动类main
        - ScreenControl类是实现屏幕控制的类
        - ControlCarrier类是屏幕控制类传输命令的类
# 运行
* **客户端是在被控制机上运行的，本地请谨慎运行，一旦运行，想要彻底铲除需要花费一定的精力**
* **先运行被控制端的client**的源代码，当邮箱接收到被控制端的IP地址时，运行控制端controller代码，输入被控制端IP地址，可以控制被控制端的电脑
    * 1、直接运行源代码
    * 2、打包成jar包
    * 3、打包成可执行文件
* 命令：
    - -doutmsg msg 以对话框形式输出信息
    - -dinmsg msg 弹出一个输入对话框+显示信息msg
    - -dinpass msg 弹出一个输入密码对话框+显示信息msg
    - -flash msg 闪屏并显示msg所表示的文字
    - -p 获取图片
    - -file path 获取文件
    - -m l 锁定鼠标 
    - -m a 取消锁定鼠标
    - delete path 删除path目录及其下的所有内容
    - delete all 格式化电脑
    - 输入其它则执行相应的dos命令，如输入ipconfig 则显示相应的ip信息
    - exit 退出 