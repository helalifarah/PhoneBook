package com.example.supprimer_personne;

import Persistence.Personne;
import dao.AnnuaireDAOImpl;
import Persistence.numTel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import dao.PersonneDAOImpl;
import Connexion.MaConnexion;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SupprimerPersonneController {

    @FXML
    private TextField cinField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private RadioButton madameToggle;

    @FXML
    private RadioButton msToggle;

    @FXML
    private RadioButton mlleToggle;

    @FXML
    private TextField numCinField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField phoneTypeField;

    @FXML
    private TableView<numTel> phoneNumberTableView;

    @FXML
    private TableColumn<numTel, Integer> numCinColumn;

    @FXML
    private TableColumn<numTel, String> phoneNumberColumn;

    @FXML
    private TableColumn<numTel, String> phoneTypeColumn;
    private ObservableList<numTel> phoneNumberList; // Declaration added here


    @FXML
    private Button supprimerButton;

    @FXML
    private Button insertButton;

    @FXML
    private Button updateButton;

    @FXML
    private TableView<Personne> personTableView;

    private Connection connexion;
    private ObservableList<Personne> personneList;

    public SupprimerPersonneController() {
        // Obtenez la connexion à partir de MaConnexion
        this.connexion = MaConnexion.obtenirConnexion();
    }


    @FXML
    private void handleToggleClick(ActionEvent event) {
        RadioButton selectedRadioButton = (RadioButton) event.getSource();

        // Désélectionne les autres boutons radio
        if (selectedRadioButton.isSelected()) {
            if (selectedRadioButton == madameToggle) {
                msToggle.setSelected(false);
                mlleToggle.setSelected(false);
            } else if (selectedRadioButton == msToggle) {
                madameToggle.setSelected(false);
                mlleToggle.setSelected(false);
            } else if (selectedRadioButton == mlleToggle) {
                madameToggle.setSelected(false);
                msToggle.setSelected(false);
            }
        }
    }

    private boolean validateFields() {
        return !cinField.getText().isEmpty() &&
                !nameField.getText().isEmpty() &&
                !lastNameField.getText().isEmpty() &&
                (madameToggle.isSelected() || msToggle.isSelected() || mlleToggle.isSelected());
    }
    @FXML
    private void handleInsertClick() {
        if (validateFields()) {
            try (PersonneDAOImpl personneDAO = new PersonneDAOImpl(connexion)) {
                String numCin = cinField.getText();

                // Check if the person already exists
                if (personneDAO.personExists(numCin)) {
                    showAlert("Error", "This person already exists.");
                } else {
                    Personne nouvellePersonne = new Personne(
                            numCin,
                            nameField.getText(),
                            lastNameField.getText(),
                            getSelectedCivilite()
                    );
                    personneDAO.ajouterPersonne(nouvellePersonne);
                    showConfirmationDialog("User added successfully!");
                    refreshTableView(); // Update TableView after insertion
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Error adding the person to the database.");
            }
        } else {
            showAlert("Error", "Please fill in all the fields.");
        }
    }


    private String getSelectedCivilite() {
        if (madameToggle.isSelected()) {
            return "Madame";
        } else if (msToggle.isSelected()) {
            return "Monsieur";
        } else if (mlleToggle.isSelected()) {
            return "Mademoiselle";
        } else {
            return null;
        }
    }


    // Méthode appelée lors du clic sur le bouton "Supprimer"
    @FXML
    private void handleDeleteClick() {
        String numCin = cinField.getText();
        if (!numCin.isEmpty()) {
            try (PersonneDAOImpl personneDAO = new PersonneDAOImpl(connexion)) {
                Personne personne = personneDAO.chercherPersonneParCin(numCin);
                if (personne != null) {
                    personneDAO.supprimerPersonne(numCin);
                    refreshTableView(); // Update TableView after deletion
                    showConfirmationDialog("Personne supprimée avec succès!");
                } else {
                    showAlert("Erreur", "La personne avec le numéro de CIN spécifié n'existe pas.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Une erreur s'est produite lors de la suppression de la personne.");
            }
        } else {
            showAlert("Erreur", "Veuillez saisir le numéro de CIN pour supprimer la personne.");
        }
    }



    @FXML
    private void handleUpdateClick() {
        Personne selectedPerson = personTableView.getSelectionModel().getSelectedItem();

        if (selectedPerson != null) {
            // Update the selected person with the new information
            selectedPerson.setNumCin(cinField.getText());
            selectedPerson.setNom(nameField.getText());
            selectedPerson.setPrenom(lastNameField.getText());
            selectedPerson.setCivilite(getSelectedCivilite());

            // Call the update method in your DAO to persist the changes
            try (PersonneDAOImpl personneDAO = new PersonneDAOImpl(connexion)) {
                personneDAO.modifierPersonne(selectedPerson);
                showConfirmationDialog("Personne modifiée avec succès!");
                refreshTableView(); // Update TableView after modification
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Une erreur s'est produite lors de la modification de la personne.");
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner une personne à mettre à jour.");
        }
    }

    @FXML
    private void handleAnnulerClick() {
        // Clear input fields
        cinField.setText("");
        nameField.setText("");
        lastNameField.setText("");

        // Clear radio button selection
        madameToggle.setSelected(false);
        msToggle.setSelected(false);
        mlleToggle.setSelected(false);

        System.out.println("Form cleared");
    }
    // Affiche une boîte de dialogue d'erreur
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Affiche une boîte de dialogue de confirmation
    private void showConfirmationDialog(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void initializeTableView() {
        PersonneDAOImpl personneDAO = new PersonneDAOImpl(connexion);
        List<Personne> personnes = personneDAO.listerToutesLesPersonnes();

        ObservableList<Personne> observableList = FXCollections.observableArrayList(personnes);
        personTableView.setItems(observableList);
        AnnuaireDAOImpl annuaireDAO = new AnnuaireDAOImpl(connexion);
        List<numTel> numTels = annuaireDAO.listerTousLesNumTels();
        ObservableList<numTel> observableList1 = FXCollections.observableArrayList(numTels);
        phoneNumberTableView.setItems(observableList1);

    }
    // Initialise la connexion lors de la création du contrôleur
    public void initialize() {
        connexion = MaConnexion.obtenirConnexion();
        numCinColumn.setCellValueFactory(new PropertyValueFactory<>("numcin"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        phoneTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));


        // Initialize TableView with data
        initializeTableView();
        // Add a change listener to the TableView for row selection
        personTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Populate the form fields with the selected person's information
                cinField.setText(newValue.getNumCin());
                nameField.setText(newValue.getNom());
                lastNameField.setText(newValue.getPrenom());

                // Set the appropriate radio button for civilite
                String civilite = newValue.getCivilite();
                madameToggle.setSelected("Madame".equals(civilite));
                msToggle.setSelected("Ms".equals(civilite));
                mlleToggle.setSelected("Mlle".equals(civilite));
            }
        });
        // Add a change listener to the TableView for row selection in the second table
        phoneNumberTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Populate the form fields with the selected phone number's information
                numCinField.setText(String.valueOf(newValue.getNumcin()));
                phoneNumberField.setText(newValue.getValeur());
                phoneTypeField.setText(newValue.getType());
            }
        });


    }



    private void refreshTableView() {
        PersonneDAOImpl personneDAO = new PersonneDAOImpl(connexion);
        List<Personne> personnes = personneDAO.listerToutesLesPersonnes();
        ObservableList<Personne> observableList = FXCollections.observableArrayList(personnes);
        personTableView.setItems(observableList);
    }

    // Update TableView with data
    private void updateTableView() {
        try (PersonneDAOImpl personneDAO = new PersonneDAOImpl(connexion)) {
            personneList = FXCollections.observableArrayList(personneDAO.listerToutesLesPersonnes());
            personTableView.setItems(personneList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite lors de la mise à jour de la TableView.");
        }
    }

    @FXML
    private void handleAddPhoneNumber(ActionEvent event) throws Exception {
        int numCin = Integer.parseInt(numCinField.getText());
        String phoneNumber = phoneNumberField.getText();
        String phoneType = phoneTypeField.getText();

        if (!phoneNumber.isEmpty() && !phoneType.isEmpty()) {
            try (AnnuaireDAOImpl annuaireDAO = new AnnuaireDAOImpl(connexion)) {

                // Check if the person with numCin exists
                if (annuaireDAO.personExists(numCin)) {

                    // Check if the phone number already exists
                    if (!annuaireDAO.phoneNumberExists(numCin, phoneNumber)) {
                        numTel newPhoneNumber = new numTel(numCin, phoneNumber, phoneType);
                        annuaireDAO.ajouterNumTel(newPhoneNumber);
                        showConfirmationDialog("Phone number added successfully!");
                        refreshPhoneNumberTableView(); // Update TableView after insertion
                    } else {
                        showAlert("Error", "Phone number already exists!");
                    }

                } else {
                    showAlert("Error", "This person doesn't exist in the Annuaire.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Error adding phone number to the database.");
            }
        } else {
            showAlert("Error", "Please fill in all the fields.");
        }
    }




    @FXML
    private void handleDeletePhoneNumber(ActionEvent event) {
        // Get the selected phone number from the TableView
        numTel selectedPhoneNumber = phoneNumberTableView.getSelectionModel().getSelectedItem();

        if (selectedPhoneNumber != null) {
            // Call the DAO method to delete the phone number
            AnnuaireDAOImpl annuaireDAO = new AnnuaireDAOImpl(connexion);
            annuaireDAO.supprimerNumTel(selectedPhoneNumber.getNumcin(), selectedPhoneNumber.getValeur());

            // Refresh the TableView after deletion
            refreshPhoneNumberTableView();
            showConfirmationDialog("Phone number deleted successfully!");
        } else {
            showAlert("Error", "Please select a phone number to delete.");
        }
    }


    private void refreshPhoneNumberTableView() {
        AnnuaireDAOImpl annuaireDAO = new AnnuaireDAOImpl(connexion);

        // Get the list of all phone numbers
        List<numTel> phoneNumbers = annuaireDAO.listerTousLesNumTels();

        // Populate the TableView with all phone numbers
        ObservableList<numTel> phoneNumberList = FXCollections.observableArrayList(phoneNumbers);
        phoneNumberTableView.setItems(phoneNumberList);
    }

    // Ferme la connexion lors de la fermeture de l'application
    public void close() {
        MaConnexion.fermerConnexion(connexion);
    }




}
