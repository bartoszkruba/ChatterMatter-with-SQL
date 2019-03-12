package client;

import client.clientApp.Client;
import client.clientApp.util.FileManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.UserData;

public class ClientMain extends Application {
   public static Stage primaryStage;

   @Override
   public void start(Stage primaryStage) throws Exception {
      UserData data = (UserData) FileManager.loadFile("user-data.ser");
      if (data == null) {
         data = new UserData();
      }

      Client.getInstance().setUserData(data);

      ClientMain.primaryStage = primaryStage;
      FXMLLoader loader = new FXMLLoader(getClass().getResource("clientApp/views/Startup_Window.fxml"));
      Parent root = loader.load();

      primaryStage.getIcons().add(new Image("/client/clientApp/images/logo.png"));
      primaryStage.setResizable(false);
      primaryStage.setTitle("Chatter Matter");
      primaryStage.setScene(new Scene(root, 380, 270));


      if (data.isDarkMode()) {
         String darkmodeCss = this.getClass().getResource("/client/clientApp/css/darkmode.css").toExternalForm();
         primaryStage.getScene().getStylesheets().add(darkmodeCss);
      } else {
         String normalCss = this.getClass().getResource("/client/clientApp/css/normal.css").toExternalForm();
         primaryStage.getScene().getStylesheets().add(normalCss);
      }

      primaryStage.show();
   }


   public static void main(String[] args) {
      launch(args);
   }
}
