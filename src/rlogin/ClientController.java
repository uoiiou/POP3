package rlogin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private boolean exit = false;

    @FXML
    private TextField command;
    @FXML
    private TextArea dialog;
    @FXML
    private TextArea rules;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            socket = new Socket("127.0.0.1", 110);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            dialog.appendText(dataInputStream.readUTF() + '\n');
        } catch (IOException ex) {
            System.out.println("Client - " + ex);
        }

        rules.setEditable(false);
        dialog.setEditable(false);
        initrules();
    }

    @FXML
    void send() {
        if (!exit)
        {
            try {
                dataOutputStream.writeUTF(command.getText());
                dialog.appendText(dataInputStream.readUTF() + '\n');
                String string =command.getText();
                command.clear();

                if (string.equals("QUIT"))
                {
                    dataOutputStream.close();
                    dataInputStream.close();
                    socket.close();
                    exit = true;
                }
            } catch (IOException ex) {
                System.out.println("Client - " + ex);
            }
        }
        else
            System.out.println("Server closed");
    }

    private void initrules(){
        rules.appendText("From client:\n");
        rules.appendText("\t1. USER username\n");
        rules.appendText("\t2. PASS password\n");
        rules.appendText("\t3. QUIT\n");
        rules.appendText("\t4. STAT\n");
        rules.appendText("\t5. RETR msgnumb\n");
        rules.appendText("\t6. DELE msgnumb\n");
        rules.appendText("\t7. NOOP\n\n");
        rules.appendText("From server:\n");
        rules.appendText("\t +OK message - if everything is ok\n");
        rules.appendText("\t +ERR message - if something went wrong");
    }
}