package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage ps;

    @Override
    public void start(Stage ps1) throws Exception{
        ps = ps1;
        Parent root = FXMLLoader.load(getClass().getResource("myfxml.fxml"));
        ps.setTitle("Rome Route Finder");
        ps.setScene(new Scene(root));
        ps.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
