package main.controller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;

public class HeuristicMinerSettingsController {

	@FXML
	private Slider dependencyThresholdSlider;
	@FXML
	private Label dependencyThresholdValue;
	@FXML
	private Slider andThresholdSlider;
	@FXML
	private Label andThresholdValue;
	@FXML
	private Slider l2lThresholdSlider;
	@FXML
	private Label l2lThresholdValue;
	@FXML
	private Slider relativeThresholdSlider;
	@FXML
	private Label relativeThresholdValue;
	@FXML
	private CheckBox useAllConnectedCheck;
	@FXML
	private CheckBox useLongDistanceCheck;
	@FXML
	private Button resetButton;

	private HeuristicsMinerSettings settings;

	public void initialize(HeuristicsMinerSettings settings) {
		this.settings = settings;
		// Initialise sliders
		dependencyThresholdSlider.setValue(settings.getDependencyThreshold());
		andThresholdSlider.setValue(settings.getAndThreshold());
		l2lThresholdSlider.setValue(settings.getL2lThreshold());
		relativeThresholdSlider.setValue(settings.getRelativeToBestThreshold());

		// Initialise checkboxes
		useAllConnectedCheck.setSelected(settings.isUseAllConnectedHeuristics());
		useLongDistanceCheck.setSelected(settings.isUseLongDistanceDependency());

		// Set initial values
		updateDependencyThresholdValue();
		updateAndThresholdValue();
		updateL2lThresholdValue();
		updateRelativeThresholdValue();

		// Add listeners
		addSliderListener(dependencyThresholdSlider, this::updateDependencyThresholdValue);
		addSliderListener(andThresholdSlider, this::updateAndThresholdValue);
		addSliderListener(l2lThresholdSlider, this::updateL2lThresholdValue);
		addSliderListener(relativeThresholdSlider, this::updateRelativeThresholdValue);
	}

	private void updateDependencyThresholdValue() {
		dependencyThresholdValue.setText(String.format("%.2f", dependencyThresholdSlider.getValue()));
	}

	private void updateAndThresholdValue() {
		andThresholdValue.setText(String.format("%.2f", andThresholdSlider.getValue()));
	}

	private void addSliderListener(Slider slider, Runnable updateMethod) {
		slider.valueProperty().addListener((obs, oldVal, newVal) -> updateMethod.run());
	}

	private void updateL2lThresholdValue() {
		l2lThresholdValue.setText(String.format("%.2f", l2lThresholdSlider.getValue()));
	}

	private void updateRelativeThresholdValue() {
		relativeThresholdValue.setText(String.format("%.2f", relativeThresholdSlider.getValue()));
	}

	public void updateSettings() {
		settings.setDependencyThreshold(dependencyThresholdSlider.getValue());
		settings.setAndThreshold(andThresholdSlider.getValue());
		settings.setL2lThreshold(l2lThresholdSlider.getValue());
		settings.setRelativeToBestThreshold(relativeThresholdSlider.getValue());
		settings.setUseAllConnectedHeuristics(useAllConnectedCheck.isSelected());
		settings.setUseLongDistanceDependency(useLongDistanceCheck.isSelected());
	}

	public HeuristicsMinerSettings getParameters() {
		return settings;
	}

	public void resetToDefaults() {
		// Reset all fields to default values
		dependencyThresholdSlider.setValue(0.9);
		andThresholdSlider.setValue(0.65);
		l2lThresholdSlider.setValue(0.85);
		relativeThresholdSlider.setValue(0.2);
		useAllConnectedCheck.setSelected(true);
		useLongDistanceCheck.setSelected(true);

		updateDependencyThresholdValue();
		updateAndThresholdValue();
		updateL2lThresholdValue();
		updateRelativeThresholdValue();

		// Update the parameters object
		updateSettings();
	}
}