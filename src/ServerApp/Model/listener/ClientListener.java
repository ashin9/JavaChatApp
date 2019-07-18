package ServerApp.Model.listener;

import ServerApp.Model.server.Server;
import ServerApp.Model.users.UsersInfo;
import ServerApp.ServerMain;

import java.io.*;
import java.net.Socket;

/*
* ClientListenr类
* 实现Runnable接口
* 包含客户端name与相应的socket
* 通过创建新的线程来处理客户端发送给服务端的信息，并根据标识来转发给其他客户端
* */

public class ClientListener implements Runnable{
    private Socket userSocket;  //服务端与客户端的连接
    private String userName;        //客户端的name

    public ClientListener(Socket userSocket, String userName) {
        this.userSocket = userSocket;
        this.userName = userName;
    }//构造

    // 将给定的消息转发给所有客户端
    private synchronized void sendToAll(String message) {
        //通过循环遍历HashMap来获取客户端的socket,从而发送信息
        for(Socket receiverSocket: UsersInfo.storeInfo.values()) {
            try {
                DataOutputStream out =  new DataOutputStream(receiverSocket.getOutputStream());
                if(out!=null) {
                    out.writeUTF(message);
                    out.flush();
                    System.out.println("Client Listener::sendToAll() ["+message+"]");
                }
            }catch (Exception e){
                ServerMain.controller.receiveMsgBox.appendText(this.userName+" send to AllUsers failed.");
            }
        }
    }

    // 将给定的message消息转发给私聊的客户端name
    private synchronized void sendTo(String receiver, String message) {
        //由name得到发送目标用户与服务端的socket
        Socket receiverSocket = UsersInfo.storeInfo.get(receiver);
        try{
            //从用户socket中获取输出流
            DataOutputStream out = new DataOutputStream(receiverSocket.getOutputStream());
            if (out != null) {
                System.out.println("send["+message+"]to["+receiver+receiverSocket.getInetAddress()+"]\n");
                out.writeUTF(message);
                out.flush();
            }
        }catch (IOException e){
            ServerMain.controller.receiveMsgBox.appendText("[private]"+this.userName +" send to " + receiver + " failed.");
        }
    }

    @Override
    public void run(){
        try{
            //服务端将name已上线的消息发送给所有人
            sendToAll("[系统通知] " + userName + " 已上线");

            //将更新后的用户列表发送给所有人
            Server.updateUsersList();

            //通过与服务端相连的客户端的socket来获取客户端的信息
            DataInputStream datain = new DataInputStream(userSocket.getInputStream());

            while(true) {
                //从客户端读取到消息
                String msgString;
                try {
                    msgString = datain.readUTF().trim();
                }catch (Exception e){
                    break;
                }
                // 检验是否为私聊（格式：@昵称：内容）
                System.out.println(msgString);
                if(msgString.startsWith("@")) {
                    int index = msgString.indexOf(":");
                    if(index >= 0) {
                        //获取接收者names
                        String reciverNamesString = msgString.substring(1, index);
                        String[] reciverNames = reciverNamesString.split("&");
                        //循环发送给接收者
                        for(int i = 0;i < reciverNames.length;i++) {
                            reciverNames[i] = reciverNames[i].trim();
                            if(reciverNames[i]!=null||!"".equals(reciverNames[i])) {
                                //如果查询不到接收者
                                if (!UsersInfo.storeInfo.containsKey(reciverNames[i])) {
                                    System.out.println("There is no " + reciverNames[i]);
                                    ServerMain.controller.receiveMsgBox.appendText("无用户: " + reciverNames[i] + "\n\n");
                                    new DataOutputStream(this.userSocket.getOutputStream()).writeUTF("[系统通知]查找用户: " + reciverNames[i] + " 失败");
                                    continue;
                                }
                                //获取发送信息
                                String info = msgString.substring(index + 1, msgString.length());
                                ServerMain.controller.receiveMsgBox.appendText("[private]" + this.userName + " send to " + reciverNames[i] + "：" + info + "\n");
                                info = this.userName + "：" + info;
                                System.out.println("run::if::[" + info + "]>>[" + reciverNames[i] + "]");
                                sendTo(reciverNames[i], info);
                            }
                        }
                    }
                }
                //否则将客户端发送的消息发送给所有人
               else {
                    ServerMain.controller.receiveMsgBox.appendText("[public] " + userName + " ：" + msgString+"\n");
                    System.out.println("Client Listener::run::else::"+"[public] "+ this.userName + " ：" + msgString);
                    sendToAll("[public] "+ this.userName + " ：" + msgString);
                }
            }//while
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //如果连接已断开
            System.out.println(this.userName + " 已下线");
            ServerMain.controller.receiveMsgBox.appendText(this.userName + " 已下线\n\n");
            //从用户HashMap集合中删除该用户
            UsersInfo.removeUser(userName);

            // 通知所有客户端，客户端name已经下线
            sendToAll("[系统通知] " + this.userName + " 已下线");

            //将更新后的用户列表发送给所有客户端
            Server.updateUsersList();

            if (userSocket != null) {
                try {
                    //关闭该客户端的socket
                    userSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }//run()
}