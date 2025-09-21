package main.controller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import processmining.splitminer.ui.dfgp.DFGPUIResult;
import java.util.Map;

public class SplitMinerSettingsController {
	@FXML
	private Slider frequencyThresholdSlider;
	@FXML
	private Label frequencyThresholdValue;
	@FXML
	private Slider parallelismThresholdSlider;
	@FXML
	private Label parallelismThresholdValue;
	@FXML
	private ComboBox<DFGPUIResult.FilterType> filterTypeCombo;
	@FXML
	private CheckBox replaceORsCheck;
	@FXML
	private CheckBox useInclusiveSemanticsCheck;
	@FXML
	private CheckBox removeLoopActivitiesCheck;
	@FXML
	private Button resetButton;

	private Map<String, Object> parameters;

	public void initialize(Map<String, Object> parameters) {
		this.parameters = parameters;

		// Initialise controls
		frequencyThresholdSlider.setValue((double) parameters.getOrDefault("frequencyThreshold", 0.8));
		parallelismThresholdSlider.setValue((double) parameters.getOrDefault("parallelismThreshold", 0.4));
		filterTypeCombo.getItems().addAll(DFGPUIResult.FilterType.values());
		filterTypeCombo
				.setValue((DFGPUIResult.FilterType) parameters.getOrDefault("filterType", DFGPUIResult.FilterType.FWG));
		replaceORsCheck.setSelected((boolean) parameters.getOrDefault("replaceORs", false));
		useInclusiveSemanticsCheck.setSelected((boolean) parameters.getOrDefault("useInclusiveSemantics", false));
		removeLoopActivitiesCheck.setSelected((boolean) parameters.getOrDefault("removeLoopActivities", false));

		// Set up listeners
		frequencyThresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			frequencyThresholdValue.setText(String.format("%.2f", newVal));
		});

		parallelismThresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			parallelismThresholdValue.setText(String.format("%.2f", newVal));
		});

		// Set initial values
		frequencyThresholdValue.setText(String.format("%.2f", frequencyThresholdSlider.getValue()));
		parallelismThresholdValue.setText(String.format("%.2f", parallelismThresholdSlider.getValue()));
	}

	public void updateSettings() {
		parameters.put("frequencyThreshold", frequencyThresholdSlider.getValue());
		parameters.put("parallelismThreshold", parallelismThresholdSlider.getValue());
		parameters.put("filterType", filterTypeCombo.getValue());
		parameters.put("replaceORs", replaceORsCheck.isSelected());
		parameters.put("useInclusiveSemantics", useInclusiveSemanticsCheck.isSelected());
		parameters.put("removeLoopActivities", removeLoopActivitiesCheck.isSelected());

	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void resetToDefaults() {
		// Reset all fields to default values
		frequencyThresholdSlider.setValue(0.4);
		parallelismThresholdSlider.setValue(0.1);
		filterTypeCombo.setValue(DFGPUIResult.FilterType.FWG);
		replaceORsCheck.setSelected(false);
		useInclusiveSemanticsCheck.setSelected(false);
		removeLoopActivitiesCheck.setSelected(false);

		// Update the displayed values
		frequencyThresholdValue.setText("0.40");
		parallelismThresholdValue.setText("0.10");

		// Update the parameters object
		updateSettings();
	}
}