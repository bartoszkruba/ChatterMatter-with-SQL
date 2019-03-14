package client.clientApp.controllers;

import client.ClientMain;
import client.clientApp.Client;
import client.clientApp.MessageInboxHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import models.UserData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartupWindowController {

   @FXML
   TextField nicknameTextField;
   @FXML
   TextField serverAddressTextField;
   @FXML
   Label errorLabel;

   private UserData data;

   public void initialize() {
      data = Client.getInstance().getUserData();
      nicknameTextField.setText(data.getUsername());
      serverAddressTextField.setText(data.getIP());
   }

   @FXML
   private void connectBtnPressed() throws Exception {
      if (validateInput()) {
         if (Client.getInstance().connect(serverAddressTextField.getText())) {
            data.setUsername(nicknameTextField.getText());
            Client.getInstance().setUserData(data);
            switchWindow();
         } else {
            errorLabel.setText("Could not connect");
         }
      }
   }

   private boolean validateInput() {
      String nickname = nicknameTextField.getText().trim();
      nicknameTextField.setText(nickname);

      String serverAddress = serverAddressTextField.getText().trim();
      serverAddressTextField.setText(serverAddress);

      String nicknameRegex = "^[\\w]{3,10}$";
      Pattern pattern = Pattern.compile(nicknameRegex);
      Matcher nicknameMatcher = pattern.matcher(nickname);

      if (!nicknameMatcher.matches()) {
         errorLabel.setText("Name should be 3-10 characters long containing only letters and/or numbers");
         nicknameTextField.selectAll();
         return false;
      }

      if (serverAddress.equals("")) {
         serverAddressTextField.selectAll();
         return false;
      } else {
         return true;
      }
   }

   private void switchWindow() throws Exception {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/Chat_Window.fxml"));
      Parent root = loader.load();
      ChatWindowController chatWindowController = loader.getController();
      ClientMain.primaryStage.setUserData(chatWindowController);

      ClientMain.primaryStage.setResizable(false);
      ClientMain.primaryStage.setOnCloseRequest(e -> Client.getInstance().saveData());
      ClientMain.primaryStage.setTitle("Chatter Matter");
      ClientMain.primaryStage.setScene(new Scene(root, 1000, 600));

      MessageInboxHandler.getInstance().createNewChatWindowController();
   }

   @FXML
   private void KeyReleasedOnNameField(KeyEvent e) throws Exception {
      if (e.getCode().toString().equals("ENTER")) {
         connectBtnPressed();
      }
   }

   @FXML
   private void KeyReleasedOnIPField(KeyEvent e) throws Exception {
      if (e.getCode().toString().equals("ENTER")) {
         connectBtnPressed();
      }
   }
}
