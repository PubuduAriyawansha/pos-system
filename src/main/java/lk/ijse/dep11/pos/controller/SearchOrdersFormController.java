package lk.ijse.dep11.pos.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep11.pos.db.SearchAccessDataSource;
import lk.ijse.dep11.pos.tm.Search;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class SearchOrdersFormController {
    public TextField txtSearch;
    public TableView <Search> tblOrders;
    public AnchorPane root;

    public void initialize() throws SQLException {
        tblOrders.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblOrders.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
        tblOrders.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("customer_id"));
        tblOrders.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("customer_name"));
        tblOrders.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));

        tblOrders.getItems().addAll(SearchAccessDataSource.loadAll());

        txtSearch.textProperty().addListener((o,prev,curr)->{
            tblOrders.getItems().clear();
            if(curr!=null && !curr.isEmpty()){
                try {
                    List<Search> searchList = SearchAccessDataSource.searchItem(curr);
                    tblOrders.getItems().addAll(searchList);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void tblOrders_OnMouseClicked(MouseEvent mouseEvent) {
    }

    public void navigateToHome(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/view/MainForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        Platform.runLater(primaryStage::sizeToScene);
    }
}
