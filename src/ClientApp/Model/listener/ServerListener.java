package ClientApp.Model.listener;

import ClientApp.ClientMain;

import java.io.*;
import java.net.*;

/*
* ServerListener类
* 包含本客户端与服务端socket连接
* 用于监听服务端发送内容的线程类，并作出相应的窗口显示
*/
public class ServerListener implements Runnable {

    private Socket ClientSocket;//本客户端与服务端的socket

    public ServerListener(Socket socket){
        this.ClientSocket = socket;
    }

    @Override
    public void run() {
        //从socket中获得服务端的输入流
        try {
            while(true){
                //通过本客户端与服务端的socke获得输入流
                DataInputStream in = new DataInputStream(this.ClientSocket.getInputStream());
                //从输入流中获取内容
                String msgString = in.readUTF();
                handelMessage(msgString);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //处理服务端发送的字符消息
    private void handelMessage(String msgString){
        String msg = msgString.trim();
        System.out.println("handelMessage::[" + msg+"]");
        //检测服务端发送的是不是用户列表(形式 @UsersList@:用户列表)
        if(msg.startsWith("@UsersList@")) {
            int index = msg.indexOf(":");
            if(index >= 0) {
                //获取用户列表
                String usersList = msg.substring(index+1,msg.length());
                //获取发送信息
                ClientMain.controller.UsersList.setText(usersList);
                System.out.println("update UsersList.");
            }
        }else{
            System.out.println("recive message.");
            ClientMain.controller.receiveMsgBox.appendText(msg+"\n\n");
        }
    }

}
