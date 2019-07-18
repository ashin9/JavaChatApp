package ServerApp.Controller;

import ServerApp.Model.server.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller extends ServerUIObjectsVar implements Initializable {
    private Server server;
    private boolean connectionStatus = false;
    private Thread backgroundThread;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.portTextField.setText(null);
        this.receiveMsgBox.setEditable(false);
        this.receiveMsgBox.setWrapText(true);
        this.msgBox.setWrapText(true);
        this.UsersList.setEditable(false);
        this.statusLabel.setText("Server is off");
        this.chatStatusLabel.setText("Start server and connect with server to chat");
    }

    @FXML
    private void startServerBtnAction(ActionEvent event) {
        if (!this.connectionStatus) {
            try {
                // Create a Runnable
                Runnable task = () -> {
                    this.server = new Server();
                    this.server.closeConnection();
                    int port = Integer.parseInt(this.portTextField.getText().trim());
                    this.server.setPort(port);
                    this.server.start();
                };
                // Run the task in a background thread
                this.backgroundThread = new Thread(task);
                // Start the thread
                this.backgroundThread.start();
                Thread.sleep(1000);

            } catch (Exception ignored) {
                this.statusLabel.setText("Wrong port number");
            }
            this.connectionStatus = true;
            System.out.println("Server is running");
            this.statusLabel.setText("Server started");
            this.startServerBtn.setText("Stop Server");
            this.chatStatusLabel.setText("Waiting for server");
        } else {
            server.closeConnection();
            try {
                this.backgroundThread.interrupt();
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
            this.connectionStatus = false;
            System.out.println("Server closed");
            this.statusLabel.setText("Server closed");
            this.startServerBtn.setText("Start Server");
            this.chatStatusLabel.setText("Start server and connect with server to chat");
        }
        event.consume();
    }

    @FXML
    private void sendMsgBtnAction (ActionEvent event) {
        //传入窗口文本框中的字符
        if (this.connectionStatus) {
            String msg = this.msgBox.getText().trim();
            if(msg!=null)
            this.server.sendMsg(this.msgBox.getText().trim());
        }
        else
            this.chatStatusLabel.setText("Wrong try");
        try {
            Thread.sleep(100);
            this.msgBox.setText(null);
        } catch (Exception ignored) {
        }
        event.consume();
    }

    @FXML
    private void clearMsgBtnAction (ActionEvent event) {
        //清空文本框中的消息
        this.receiveMsgBox.setText(null);
        this.msgBox.setText(null);
    }
}
