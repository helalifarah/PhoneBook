module com.example.supprimer_personne {
    requires javafx.controls;
    requires javafx.fxml;
    requires Annuaire.Backend;
    requires java.sql;


    opens com.example.supprimer_personne to javafx.fxml;
    exports com.example.supprimer_personne;
}