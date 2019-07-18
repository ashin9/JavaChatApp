package ClientApp.Controller;

import ClientApp.Model.client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;



/*
Controller类
用于窗口变量接受响应事件，并调用Client类中方法，完成其功能实现，显示在客户端
*/

public class Controller extends ClientUIObjectsVars implements Initializable {
    private Client client;
    private boolean connectionStatus = false;
    private Thread backgroundThread;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.serverTextField.setText(null);
        this.portTextField.setText(null);
        this.UsersList.setEditable(false);      //用户列表框不可改
        this.receiveMsgBox.setEditable(false);  //接收消息框不可改
        this.receiveMsgBox.setWrapText(true);
        this.msgBox.setWrapText(true);
        this.chatStatusLabel.setText("Start server, then connect to start chatting");
    }

    //连接Server按钮上的事件
    @FXML
    private void connectServerBtnAction(ActionEvent event) {
        if (!this.connectionStatus) {//如果没有连接则尝试连接Server
            try {
                //创建一个Runnable
                Runnable task = () -> {
                    this.client = new Client();
                    System.out.println("creat Client succuess.");
                    this.client.closeConnection();
                    this.client.setUserName(this.userName.getText().trim());
                    this.client.setPort(Integer.parseInt(this.portTextField.getText().trim()));
                    this.client.setServer(this.serverTextField.getText().trim());
                    System.out.println(this.client.userName+" "+this.client.server+" "+this.client.port);
                    try {
                        this.client.start();
                    }catch (Exception e) {
                        System.out.println("Reconnect.");
                        this.userName.setText(null);
                    }

                };
                //在后台运行一个线程任务
                this.backgroundThread = new Thread(task);
                //开始线程任务
                this.backgroundThread.start();
                //线程暂停5ms
                Thread.sleep(500);

            } catch (Exception exp) {
                this.statusLabel.setText("Connect Error!");
            }
            this.connectionStatus = true;//连接后设置连接状态为true
        } else {
            client.closeConnection();
            try {
                this.backgroundThread.interrupt();
                Thread.sleep(500);
            } catch (Exception ignored) {
            }
            this.connectionStatus = false;
            System.out.println("Server closed");
            this.statusLabel.setText("Server closed");
            this.connectServerBtn.setText("Connect");
            this.chatStatusLabel.setText("Start server and connect with to chat");
        }
        //销毁事件
        event.consume();
    }

    //发送消息按钮上的事件
    @FXML
    private void sendMsgBtnAction (ActionEvent event) {
        //如果处于连接状态则发送消息，否则在聊天状态标签显示please connect first
        if (this.connectionStatus)
            this.client.sendMsg(this.targetName.getText(),this.msgBox.getText().trim());
        else
            this.chatStatusLabel.setText("Please connect first!");

        //暂停1ms
        try {
            Thread.sleep(100);
        } catch (Exception ignored) {
        }
        //销毁事件
        event.consume();
    }

    //发送文件按钮上的事件
    @FXML
    private void sendFileBtnAction (ActionEvent event) {
        //如果处于连接状态则发送消息，否则在聊天状态标签显示please connect first
        if (this.connectionStatus)
            this.client.sendFile(this.sendFileTargetName.getText(),this.fileBox.getText().trim());
        else
            this.chatStatusLabel.setText("Please connect first!");
        //暂停1ms
        try {
            Thread.sleep(100);
        } catch (Exception ignored) {
        }
        //销毁事件
        event.consume();
    }

    //清除按钮上的事件：清除目标name、发送、接受消息区域的内容
    @FXML
    private void clearMsgBtnAction (ActionEvent event) {
        this.targetName.setText(null);
        this.receiveMsgBox.setText(null);
        this.fileBox.setText(null);
        this.sendFileTargetName.setText(null);
        this.msgBox.setText(null);
    }
}

