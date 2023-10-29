package lk.ijse.dep11.pos.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep11.pos.db.CustomerDataAccess;
import lk.ijse.dep11.pos.db.ItemDataAccess;
import lk.ijse.dep11.pos.db.OrderDataAccess;
import lk.ijse.dep11.pos.tm.Customer;
import lk.ijse.dep11.pos.tm.Item;
import lk.ijse.dep11.pos.tm.OrderItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PlaceOrderFormController {
    public AnchorPane root;
    public JFXTextField txtCustomerName;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public TableView <OrderItem>tblOrderDetails;
    public JFXTextField txtUnitPrice;
    public JFXComboBox <Customer> cmbCustomerId;
    public JFXComboBox <Item>cmbItemCode;
    public JFXTextField txtQty;
    public Label lblId;
    public Label lblDate;
    public Label lblTotal;
    public JFXButton btnPlaceOrder;

    public void initialize() throws SQLException {

        String[] cols = {"code", "description", "qty", "unit_price", "total", "btnDelete"};
        for (int i = 0; i < cols.length; i++) {
            tblOrderDetails.getColumns().get(i).setCellValueFactory(new PropertyValueFactory<>(cols[i]));
        }

        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        newOrder();

        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener((o,prev,curr)->{
            enablePlaceOrderButton();
            if(curr!=null){
                txtCustomerName.setDisable(false);
                txtCustomerName.setEditable(false);
                txtCustomerName.setText(curr.getName());
            } else {
                txtCustomerName.clear();
                txtCustomerName.setDisable(true);
            }
        });
        cmbItemCode.getSelectionModel().selectedItemProperty().addListener((o,prev,curr)->{
            if(curr!=null){
                txtDescription.setText(curr.getDescription());
                txtQtyOnHand.setText(curr.getQty()+"");
                txtUnitPrice.setText(curr.getUnit_Price()+"");
                for (TextField tst : new TextField[]{txtUnitPrice, txtDescription, txtQtyOnHand}) {
                    tst.setDisable(false);
                    tst.setEditable(false);
                }
                if(curr.getQty()>0){
                    txtQty.setDisable(false);
                }
            } else {
                for (TextField tst : new TextField[]{txtUnitPrice, txtDescription, txtQtyOnHand}) {
                    tst.setDisable(true);
                    tst.clear();
                }
            }
        });

        txtQty.textProperty().addListener((o,prev,curr)->{
            btnSave.setDisable(true);
            if(curr.matches("\\d+")){
                if(Integer.parseInt(curr)<=Integer.parseInt(txtQtyOnHand.getText())&&Integer.parseInt(curr)>0){
                    btnSave.setDisable(false);
                }
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
    private void newOrder(){
        for (TextField textField : new TextField[]{txtDescription, txtCustomerName, txtQty, txtQtyOnHand, txtUnitPrice}) {
            textField.clear();
            textField.setDisable(true);
        }
        tblOrderDetails.getItems().clear();
        lblTotal.setText("Total: Rs. 0.00");
        btnSave.setDisable(true);
        btnPlaceOrder.setDisable(true);
        cmbCustomerId.getSelectionModel().clearSelection();
        cmbItemCode.getSelectionModel().clearSelection();
        Platform.runLater(cmbCustomerId::requestFocus);

        try{
            cmbCustomerId.getItems().clear();
            cmbCustomerId.getItems().addAll(CustomerDataAccess.getAllCustomers());
            cmbItemCode.getItems().clear();
            cmbItemCode.getItems().addAll(ItemDataAccess.getAllItems());

            String lastOrderId = OrderDataAccess.getLastOrderId();
            if(lastOrderId==null){
                lblId.setText("OD001");
            } else {
                int newOrderID = Integer.parseInt(lastOrderId.substring(2))+1;
                lblId.setText(String.format("OD%03d",newOrderID));
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void btnAdd_OnAction(ActionEvent actionEvent) {
        Item selectedItem = cmbItemCode.getSelectionModel().getSelectedItem();

        Optional<OrderItem> optorder = tblOrderDetails.getItems().stream().filter(
                item -> selectedItem.getCode().equals(item.getCode())).findFirst();

        if(optorder.isEmpty()){
            JFXButton deleteButton = new JFXButton("Delete");
            OrderItem newOrderItem = new OrderItem(selectedItem.getCode(), selectedItem.getDescription(), Integer.parseInt(txtQty.getText()),
                    selectedItem.getUnit_Price(),deleteButton );
            tblOrderDetails.getItems().add(newOrderItem);
            deleteButton.setOnAction(e->{
                tblOrderDetails.getItems().remove(newOrderItem);
                selectedItem.setQty(selectedItem.getQty()+ newOrderItem.getQty());
                calculateTotal();
                enablePlaceOrderButton();
            });
            selectedItem.setQty(selectedItem.getQty()- newOrderItem.getQty());



        }else {
            OrderItem orderItem = optorder.get();
            orderItem.setQty(orderItem.getQty()+Integer.parseInt(txtQty.getText()));
            tblOrderDetails.refresh();
            selectedItem.setQty(selectedItem.getQty()-Integer.parseInt(txtQty.getText()));
        }
        txtQty.clear();
        cmbItemCode.getSelectionModel().clearSelection();
        cmbItemCode.requestFocus();
        calculateTotal();
        enablePlaceOrderButton();
    }
    private void calculateTotal(){
        Optional<BigDecimal> orderTotal = tblOrderDetails.getItems().stream()
                .map(orderItem -> orderItem.getTotal())
                .reduce((prev, curr) -> prev.add(curr));
        lblTotal.setText("Total : Rs. "+orderTotal.orElseGet(()->BigDecimal.ZERO).setScale(2));
    }

    public void txtQty_OnAction(ActionEvent actionEvent) {

    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) {
        try {
            OrderDataAccess.saveOrder(lblId.getText().replace("OrderID: ","").strip(),
                    Date.valueOf(lblDate.getText()),cmbCustomerId.getValue().getId(), tblOrderDetails.getItems());
            newOrder();
            //printBill();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Failed to save the Order, try again").show();
        }
    }
    private void enablePlaceOrderButton(){
        Customer selectedItem = cmbCustomerId.getSelectionModel().getSelectedItem();
        btnPlaceOrder.setDisable(!(selectedItem!=null&&!tblOrderDetails.getItems().isEmpty()));
    }
}
