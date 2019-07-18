package ClientApp.Model.client;

import ClientApp.ClientMain;
import ClientApp.Model.listener.ClientFileListener;
import ClientApp.Model.listener.ServerListener;
import javafx.application.Platform;

import java.io.*;
import java.util.concurrent.*;
import java.net.*;

public class Client {
    public  String userName;        //客户端用户名
    public  int port;               //连接端口
    public  int filePort;            //文件传送服务端口
    public  String server;          //服务端IP地址
    private Socket connection;      //与服务端的连接socket
    private Socket fileSocket;      //传送文件的socket
    private Thread fileSendThread;  //发送文件线程
    private Thread snedMsgThread;   //发送消息线程
    //线程池
    private ExecutorService exec = Executors.newCachedThreadPool();

    //设置客户端用户名方法
    public void setUserName(String userName){
        this.userName = userName;
    }

    //设置端口方法
    public void setPort(int port) {
        this.port = port;
        this.filePort = 55555;
    }

    //设置服务端host方法
    public void setServer(String server) {
        this.server = server;
    }

    //关闭连接方法
    public void closeConnection() {
        try {
            if (connection.isBound()) {//如果socket被绑定，则关闭socket
                connection.close();
            }
        } catch (Exception ignored) {
        }
    }

    //连接Server方法，并开始接受Server发送的消息
    public void start() throws Exception{
        this.connection = new Socket(this.server, this.port);
        //若连接成功，客户端首先发送自己的userName,服务端将会做出响应
        this.sendUserName();
        Platform.runLater(() -> ClientMain.controller.statusLabel.setText("Connecting"));
        //如果socket被绑定UI对应改变显示
        if (this.connection.isBound()) {
            System.out.println("Server connected");
            Platform.runLater(() -> {
                ClientMain.controller.chatStatusLabel.setText("Server connected");
                ClientMain.controller.statusLabel.setText("Connected");
                ClientMain.controller.connectServerBtn.setText("Disconnect");
            });

            //消息获取，通过线程池中的ServerListener做出相应反应
            exec.execute(new ServerListener(connection));
            //创建一个新的文件接受线程
            new ClientFileListener().start();

        } else {//否则，UI对应改变显示
            Platform.runLater(() -> {
                ClientMain.controller.statusLabel.setText("Connect");
                ClientMain.controller.chatStatusLabel.setText("Can not connect to the server");
            });
        }
    }

    /*
      从socket连接中输出客户端用户名
      服务端会返回用户名合法性信息
    */
    public void sendUserName() throws Exception{
        try {
            DataOutputStream out = new DataOutputStream(this.connection.getOutputStream());
            DataInputStream in = new DataInputStream(this.connection.getInputStream());
            out.writeUTF(this.userName);//上传自己的名字
            System.out.println("name is sent.");
            String response = in.readUTF();//接受来自服务端的响应
            System.out.println(response);
            if("[系统通知]昵称设置错误，请重新设置".equals(response)){
                ClientMain.controller.receiveMsgBox.appendText(response+"\n\n");
                throw new Exception();
            }//若系统通知用户名不合法，则抛出异常
        }catch (IOException ioExp){
            System.out.println("Send UserName Error.");
        }
        System.out.println(this.userName);
    }

    //从窗口发送消息文本框接受targetName,String字符消息，并发送消息方法
    public void sendMsg(String targetName,String msg) {
        Runnable task = () -> {
            try {
                //通过与服务端的连接socket获得输出流
                DataOutputStream out = new DataOutputStream(this.connection.getOutputStream());

                //如果发送目标文本框为空则为群聊
                if(targetName == null||"".equals(targetName)) {
                    String tmp = msg.trim();
                    //ClientMain.controller.receiveMsgBox.appendText("\n[public]" + "You: " + tmp + "\n");
                    out.writeUTF(tmp);//发送
                    out.flush();
                    System.out.println("[public]" + "You:[" + tmp + "]\n");
                }else{//私聊
                    //私聊，格式为  @targetName:information
                    String tmp = "@"+targetName.trim()+":" + msg.trim();
                    out.writeUTF(tmp);
                    out.flush();
                    ClientMain.controller.receiveMsgBox.appendText("[private]" + "You"+ tmp + "\n\n");
                    System.out.println("[private]" + "You>>["+ tmp + "]\n");
                }
                Platform.runLater(() -> ClientMain.controller.msgBox.setText(null));
                Platform.runLater(() -> ClientMain.controller.chatStatusLabel.setText("Message sent"));
            } catch (IOException ignored) {
                return;
            }
        };
        //添加进线程池
        this.snedMsgThread = new Thread(task);
        this.snedMsgThread.start();
    }

    public void sendFile(String targetIP,String filePath){
        Runnable sendFiletask = () -> {
            try {
                //获取文件名
                String fileName = filePath.substring(filePath.lastIndexOf("\\")+1);
                if(fileName==null||"filename".equals(fileName))
                    throw new Exception();

                //请求与客户端的连接
                try {
                    fileSocket = new Socket(targetIP, this.filePort);
                }catch (Exception e){
                    ClientMain.controller.receiveMsgBox.appendText("连接 "+targetIP+" 失败\n\n");
                    return ;
                }

                //发送文件名
                DataOutputStream namesend = new DataOutputStream(fileSocket.getOutputStream());
                namesend.writeUTF(fileName);
                namesend.flush();
                Thread.sleep(100);

                //创建文件输入流
                BufferedInputStream fin = new BufferedInputStream(new FileInputStream(filePath));
                //创建输出给客户端的文件输出流
                BufferedOutputStream fout = new BufferedOutputStream(fileSocket.getOutputStream());

                ClientMain.controller.receiveMsgBox.appendText("发送 "+fileName+" 给 "+targetIP+"\n\n");
                byte[] bytes = new byte[4096];
                int len;
                while((len = fin.read(bytes))!=-1){
                    //每读取len个字节输出给socket
                    fout.write(bytes,0,len);
                    fout.flush();
                }
                ClientMain.controller.receiveMsgBox.appendText("发送 "+filePath+" 成功\n\n");
                fileSocket.close();
            } catch (Exception ignored) {
                ClientMain.controller.receiveMsgBox.appendText("发送 "+filePath+" 失败\n\n");
            }
        };

        this.fileSendThread = new Thread(sendFiletask);
        this.fileSendThread.start();
    }

}
