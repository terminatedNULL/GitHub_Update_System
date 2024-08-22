package com.example.finance_tracker;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URISyntaxException;

public class FinanceTrackerController {
    private final String updatePath = "financeTracker";

    public FinanceTrackerController() throws IOException, URISyntaxException {

    }

    @FXML
    private Label version;

    @FXML
    protected void onHelloButtonClick() {
        version.setText("v0.1a");
    }
}