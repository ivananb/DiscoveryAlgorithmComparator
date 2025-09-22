package main.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import main.utils.ExportController;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import java.io.File;

/**
 * Controller class for the Log Explorer UI.
 * Manages the display and interaction with event logs.
 */
public class LogExplorerController {
	@FXML
	private ListView<String> traceListView;
	@FXML
	private TextArea traceDetailsTextArea;
	@FXML
	private Button exportTxtButton;

	private XLog log;
	private File currentPnmlFile;
	private String miningAlgorithm;

	public void setLog(XLog log) {
		this.log = log;
		populateTraceList();
	}

	public void setOriginalFile(File pnmlFile) {
		this.currentPnmlFile = pnmlFile;
	}

	public void setMiningAlgorithm(String algorithm) {
		this.miningAlgorithm = algorithm;
	}

	private void populateTraceList() {
		traceListView.getItems().clear();
		for (int i = 0; i < log.size(); i++) {
			traceListView.getItems().add("Trace " + (i + 1) + " (" + log.get(i).size() + " events)");
		}

		traceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				int selectedIndex = traceListView.getSelectionModel().getSelectedIndex();
				showTraceDetails(selectedIndex);
			}
		});
	}

	private void showTraceDetails(int traceIndex) {
		XTrace trace = log.get(traceIndex);
		StringBuilder sb = new StringBuilder();

		sb.append("Trace ").append(traceIndex + 1).append(":\n");
		sb.append("Number of events: ").append(trace.size()).append("\n\n");

		for (int i = 0; i < trace.size(); i++) {
			String eventName = trace.get(i).getAttributes().get("concept:name").toString();
			sb.append(i + 1).append(". ").append(eventName).append("\n");
		}

		traceDetailsTextArea.setText(sb.toString());
	}

	@FXML
	private void handleExportTxt() {
		if (log == null) {
			return;
		}
		ExportController.exportLogAsTxt(log, currentPnmlFile, miningAlgorithm, exportTxtButton.getScene().getWindow());
	}

}