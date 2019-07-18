package ServerApp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
/*
 *  ServerUIObjectsVar类
 *  包含窗口的各个变量
 */

class ServerUIObjectsVar {
    @FXML//端口文本框
    public TextField portTextField;

    @FXML//接受消息区域
    public TextArea receiveMsgBox;

    @FXML//用户列表区域
    public TextArea UsersList;

    @FXML//消息区域
    public TextArea msgBox;

    @FXML//服务器启动按钮
    public Button startServerBtn;

    @FXML//发送消息按钮
    public Button sendMsgBtn;

    @FXML//清除按钮
    public Button clearMsgBtn;

    @FXML//状态标签
    public Label statusLabel;

    @FXML//聊天状态标签
    public Label chatStatusLabel;
}
