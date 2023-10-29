package lk.ijse.dep11.pos.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep11.pos.db.ItemDataAccess;
import lk.ijse.dep11.pos.db.OrderDataAccess;
import lk.ijse.dep11.pos.tm.Item;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;

public class ManageItemFormController {
    public AnchorPane root;
    public JFXTextField txtCode;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public JFXButton btnDelete;
    public TableView <Item> tblItems;
    public JFXTextField txtUnitPrice;
    public JFXButton btnNew;

    public void initialize() {
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unit_Price"));

        btnSave.setDefaultButton(true);
        btnDelete.setDisable(true);

        Platform.runLater(()->{
            txtCode.requestFocus();
        });

        try {
            tblItems.getItems().addAll(ItemDataAccess.getAllItems());
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Error").show();
        }
        tblItems.getSelectionModel().selectedItemProperty().addListener((o,prev,curr)->{
            if(curr!=null){
                txtCode.setEditable(false);
                btnSave.setText("Update");
                btnDelete.setDisable(false);
                txtCode.setText(curr.getCode());
                txtDescription.setText(curr.getDescription());
                txtQtyOnHand.setText(curr.getQty()+"");
                txtUnitPrice.setText(curr.getUnit_Price().toString());
            } else {
                btnSave.setText("Save");
                btnDelete.setDisable(true);
            }
        });
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

    public void btnAddNew_OnAction(ActionEvent actionEvent) {
        txtCode.setEditable(true);
        for(TextField textField : new TextField[]{txtCode,txtDescription,txtQtyOnHand,txtUnitPrice}) textField.clear();
        txtCode.requestFocus();
        tblItems.getSelectionModel().clearSelection();
    }
    private boolean isDataValid(){
        String code = txtCode.getText().strip();
        String description = txtDescription.getText().strip();
        String unitPrice = txtUnitPrice.getText().strip();
        String qty = txtQtyOnHand.getText().strip();

        if(!code.matches("\\d{4,}")){
            txtCode.requestFocus();
            txtCode.selectAll();
            return false;
        } else if (!description.matches("[A-Za-z0-9 ]{4,}")) {
            txtDescription.requestFocus();
            txtDescription.selectAll();
            return false;
        }  else if (!qty.matches("\\d+")||Integer.parseInt(qty)<=0) {
            txtQtyOnHand.requestFocus();
            txtQtyOnHand.selectAll();
            return false;
        }else if(!isPrice(unitPrice)){
            txtUnitPrice.requestFocus();
            txtUnitPrice.selectAll();
            return false;
        }
        return true;
    }
    private boolean isPrice(String input){
        try{
            double price = Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
        if(!isDataValid()) return;
        String code = txtCode.getText().strip();
        String desc = txtDescription.getText().strip();
        String qty = txtQtyOnHand.getText().strip();
        String unit = txtUnitPrice.getText().strip();
        Item item = new Item(code, desc, Integer.parseInt(qty), new BigDecimal(unit).setScale(2));
        try {

            if(btnSave.getText().equals("Save")){
                if(ItemDataAccess.existsItem(code)){
                    new Alert(Alert.AlertType.ERROR,"Item Already Exists").show();
                    txtCode.requestFocus();
                    txtCode.selectAll();
                    return;
                }

                ItemDataAccess.saveItem(item);
                tblItems.getItems().add(item);
            } else {
                ItemDataAccess.updateItem(item);
                ObservableList<Item> itemList = tblItems.getItems();
                Item selectedItem = tblItems.getSelectionModel().getSelectedItem();
                itemList.set(itemList.indexOf(selectedItem),item);
                tblItems.refresh();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Failed to save Item").show();
        }
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {
        Item selectedItem = tblItems.getSelectionModel().getSelectedItem();
        try {
            if(OrderDataAccess.existsOrderByItemId(selectedItem.getCode())){
                new Alert(Alert.AlertType.ERROR,"Item Already associated with order").show();
            } else {
                ItemDataAccess.deleteItem(selectedItem.getCode());
                tblItems.getItems().remove(selectedItem);
                if(tblItems.getItems().isEmpty()) btnNew.fire();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Failed To delete").show();
        }
    }
}
