package ServerApp.Model.server;

import ServerApp.Model.listener.ClientListener;
import ServerApp.Model.users.UsersInfo;
import ServerApp.ServerMain;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;

public class Server {
    static private int port;            //服务端端口设置
    private ServerSocket serverSocket;  //ServerSocket
    private Thread sendThread;          //发送消息线程
    private ExecutorService exec;       //创建线程池来管理客户端的连接线程


    public Server(){
        try {
            //创建新的用户及相应的与服务端连接的socket集合
            UsersInfo.storeInfo = new HashMap<String, Socket>();
            exec = Executors.newCachedThreadPool();
        }catch (Exception e){
            System.out.println("Server launch failed.");
            ServerMain.controller.receiveMsgBox.appendText("Server launch failed.");
        }
    }

    //设置端口
    public static void setPort(int port) {
        Server.port = port;
    }

    //关闭连接
    public void closeConnection() {
        try {
            if (serverSocket.isBound()) {
                serverSocket.close();
            }
        } catch (Exception ignored) {
        }
    }


    public void start() {
        try {
            this.serverSocket = new ServerSocket(port);
            //通过循环不断接受新的连接，储存在线程池中
            while(true) {
                System.out.println("waiting...");
                Socket newConnection = serverSocket.accept();

                //获取客户端name
                String userName;
                try {
                    userName = this.getUserName(newConnection);
                    System.out.println(userName + " is connecting.");
                }catch (Exception e){
                    continue;//若用户名设置异常则中断连接
                }

                //向用户集合中添加name:userSocket键值对
                UsersInfo.putUser(userName,newConnection);

                //更新用户列表
                ServerMain.controller.receiveMsgBox.appendText(userName + " 已连接"+"\n\n");
                Thread.sleep(100);
                Server.updateUsersList();
                /*
                 * 启动一个线程，由线程来处理客户端的请求，这样可以再次监听
                 * 下一个客户端的连接
                 */
                exec.execute(new ClientListener(newConnection,userName)); //通过线程池来分配线程
            }
        } catch (Exception ignored) {
        }
    }

    /*
    从新的socket连接中获得客户端用户名，并作简单处理
    若用户名不合法，则通知用户重新设置用户名，并抛出异常
     */
    public String getUserName(Socket connection)throws Exception{
        String name;
        try {
            DataInputStream getNameStream = new DataInputStream(connection.getInputStream());
            DataOutputStream response = new DataOutputStream(connection.getOutputStream());
            name = getNameStream.readUTF().trim();
            //若客户端用户名异常或重名或包含非法字符
            if(name==null||"".equals(name)||UsersInfo.storeInfo.containsKey(name)
                    ||name.contains(":")||name.contains("&")||name.contains("|")||name.contains("\\")){
                response.writeUTF("[系统通知]昵称设置错误，请重新设置");//向客户端发送错误通知
                throw new Exception();
            }else {
                response.writeUTF("[系统通知]欢迎来到聊天室");
                return name;
            }
        }catch (IOException ioExp){
            System.out.println("Get UserName failed.");
            return null;
        }
    }

    //更新用户列表，并发送给所有连接的客户端
    public static synchronized void updateUsersList() {
        Runnable task = () -> {
            System.out.println("Server::updateUsersList():: update UsersList.");
            String usersList = UsersInfo.getUsersList();
            ServerMain.controller.UsersList.setText(usersList.substring(usersList.indexOf(":") + 1));

            //通过循环遍历HashMap来获取User.socket,从而发送信息
            for (Socket connection : UsersInfo.storeInfo.values()) {
                try {
                    DataOutputStream outUsersList = new DataOutputStream(connection.getOutputStream());
                    outUsersList.writeUTF(UsersInfo.getUsersList());
                    outUsersList.flush();
                } catch (Exception e) {
                    System.out.println("update UserList failed.");
                    ServerMain.controller.receiveMsgBox.appendText("update UserList failed." + "\n\n");
                }
            }
        };
        Thread updateUserListThread = new Thread(task);
        updateUserListThread.start();
    }

    public void sendMsg(String msgString){
        Runnable task = () -> {
            for (Socket connection : UsersInfo.storeInfo.values()) {
                try {
                    DataOutputStream outUsersList = new DataOutputStream(connection.getOutputStream());
                    outUsersList.writeUTF(("[系统通知]" + msgString).trim());
                    outUsersList.flush();
                    ServerMain.controller.receiveMsgBox.appendText(("[系统通知]" + msgString).trim()+"\n\n");
                } catch (Exception e) {
                    System.out.println("系统通知发送失败.\n");
                    ServerMain.controller.receiveMsgBox.appendText("系统通知发送失败.\n");
                }
            }
        };
        this.sendThread = new Thread(task);
        this.sendThread.start();
    }

}//class Server
