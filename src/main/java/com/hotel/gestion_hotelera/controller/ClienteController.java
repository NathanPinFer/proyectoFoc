package com.hotel.gestion_hotelera.controller;

import com.hotel.gestion_hotelera.model.Cliente;
import com.hotel.gestion_hotelera.service.ClienteService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class ClienteController implements Initializable {

    @Autowired
    private ClienteService clienteService;

    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellidos;
    @FXML private TableColumn<Cliente, String> colDni;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colCiudad;
    @FXML private TableColumn<Cliente, Boolean> colVip;
    @FXML private TableColumn<Cliente, Void> colAcciones;
    @FXML private TextField txtBuscar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colVip.setCellValueFactory(new PropertyValueFactory<>("vip"));

        cargarClientes();
        configurarColumnaAcciones();
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteService.obtenerTodos();
        tablaClientes.setItems(FXCollections.observableArrayList(clientes));
    }

    @FXML
    private void buscarCliente() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            cargarClientes();
        } else {
            List<Cliente> resultado = clienteService.buscarPorNombre(texto);
            tablaClientes.setItems(FXCollections.observableArrayList(resultado));
        }
    }

    @FXML
    private void filtrarVip() {
        List<Cliente> vips = clienteService.obtenerVip();
        tablaClientes.setItems(FXCollections.observableArrayList(vips));
    }

    @FXML
    private void abrirFormularioNuevo() {
        mostrarFormulario(null);
    }

    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");

            {
                btnEditar.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

                btnEditar.setOnAction(e -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());
                    mostrarFormulario(cliente);
                });

                btnEliminar.setOnAction(e -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());
                    confirmarEliminar(cliente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5, btnEditar, btnEliminar);
                    setGraphic(box);
                }
            }
        });
    }

    private void confirmarEliminar(Cliente cliente) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar cliente");
        alert.setHeaderText("¿Eliminar a " + cliente.getNombre() + " " + cliente.getApellidos() + "?");
        alert.setContentText("Esta acción no se puede deshacer.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clienteService.eliminar(cliente.getIdCliente());
                cargarClientes();
            }
        });
    }

    private void mostrarFormulario(Cliente clienteExistente) {
        Dialog<Cliente> dialog = new Dialog<>();
        dialog.setTitle(clienteExistente == null ? "Nuevo Cliente" : "Editar Cliente");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField fNombre = new TextField();
        TextField fApellidos = new TextField();
        TextField fDni = new TextField();
        TextField fEmail = new TextField();
        TextField fTelefono = new TextField();
        TextField fDireccion = new TextField();
        TextField fCiudad = new TextField();
        TextField fCodigoPostal = new TextField();
        CheckBox fVip = new CheckBox("Cliente VIP");

        if (clienteExistente != null) {
            fNombre.setText(clienteExistente.getNombre());
            fApellidos.setText(clienteExistente.getApellidos());
            fDni.setText(clienteExistente.getDni());
            fEmail.setText(clienteExistente.getEmail());
            fTelefono.setText(clienteExistente.getTelefono());
            fDireccion.setText(clienteExistente.getDireccion());
            fCiudad.setText(clienteExistente.getCiudad());
            fCodigoPostal.setText(clienteExistente.getCodigoPostal());
            fVip.setSelected(clienteExistente.getVip());
        }

        grid.add(new Label("Nombre:"), 0, 0);       grid.add(fNombre, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);    grid.add(fApellidos, 1, 1);
        grid.add(new Label("DNI:"), 0, 2);          grid.add(fDni, 1, 2);
        grid.add(new Label("Email:"), 0, 3);        grid.add(fEmail, 1, 3);
        grid.add(new Label("Teléfono:"), 0, 4);     grid.add(fTelefono, 1, 4);
        grid.add(new Label("Dirección:"), 0, 5);    grid.add(fDireccion, 1, 5);
        grid.add(new Label("Ciudad:"), 0, 6);       grid.add(fCiudad, 1, 6);
        grid.add(new Label("Código Postal:"), 0, 7); grid.add(fCodigoPostal, 1, 7);
        grid.add(fVip, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt == btnGuardar) {
                Cliente c = clienteExistente != null ? clienteExistente : new Cliente();
                c.setNombre(fNombre.getText());
                c.setApellidos(fApellidos.getText());
                c.setDni(fDni.getText());
                c.setEmail(fEmail.getText());
                c.setTelefono(fTelefono.getText());
                c.setDireccion(fDireccion.getText());
                c.setCiudad(fCiudad.getText());
                c.setCodigoPostal(fCodigoPostal.getText());
                c.setVip(fVip.isSelected());
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            clienteService.guardar(c);
            cargarClientes();
        });
    }
}