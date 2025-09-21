package main.controller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class InductiveMinerSettingsController {

	@FXML
	private Slider noiseThresholdSlider;
	@FXML
	private Label noiseThresholdValue;
	@FXML
	private CheckBox useMultisetCheck;

	private MiningParameters parameters;

	public void initialize(MiningParameters parameters) {
		this.parameters = parameters;

		// Initialise UI
		noiseThresholdSlider.setValue(parameters.getNoiseThreshold());
		useMultisetCheck.setSelected(parameters.isUseMultithreading());

		// Update label
		updateNoiseThresholdValue();

		// Add listener
		noiseThresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			updateNoiseThresholdValue();
		});
	}

	private void updateNoiseThresholdValue() {
		noiseThresholdValue.setText(String.format("%.2f", noiseThresholdSlider.getValue()));
	}

	public void updateSettings() {
		parameters.setNoiseThreshold((float) noiseThresholdSlider.getValue());
		parameters.setUseMultithreading(useMultisetCheck.isSelected());
	}

	public MiningParameters getParameters() {
		return this.parameters;
	}

	public void resetToDefaults() {
		// Reset all fields to default values
		noiseThresholdSlider.setValue(0.2f);
		useMultisetCheck.setSelected(false);

		// Update the parameters object
		updateSettings();
	}
}