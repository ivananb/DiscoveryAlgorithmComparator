package main.controller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.etm.parameters.ETMParam;

public class EvolutionaryTreeMinerSettingsController {

	// Sliders and labels
	@FXML private Slider populationSizeSlider;
	@FXML private Label populationSizeValue;
	@FXML private Slider eliteSizeSlider;
	@FXML private Label eliteSizeValue;
	@FXML private Slider randomTreesSlider;
	@FXML private Label randomTreesValue;
	@FXML private Slider crossoverChanceSlider;
	@FXML private Label crossoverChanceValue;
	@FXML private Slider mutationChanceSlider;
	@FXML private Label mutationChanceValue;
	@FXML private CheckBox preventDuplicatesCheck;
	@FXML private Slider maxGenerationsSlider;
	@FXML private Label maxGenerationsValue;
	@FXML private Slider targetFitnessSlider;
	@FXML private Label targetFitnessValue;
	@FXML private Slider replyFitnessWeightSlider;
	@FXML private Label replyFitnessWeightValue;
	@FXML private Slider fitnessLimitSlider;
	@FXML private Label fitnessLimitValue;
	@FXML private Slider maxFTimeSlider;
	@FXML private Label maxFTimeValue;
	@FXML private Slider precisionWeightSlider;
	@FXML private Label precisionWeightValue;
	@FXML private Slider generalizationWeightSlider;
	@FXML private Label generalizationWeightValue;
	@FXML private Slider simplicityWeightSlider;
	@FXML private Label simplicityWeightValue;
	@FXML private Slider similarityWeightSlider;
	@FXML private Label similarityWeightValue;
	@FXML private CheckBox noFitnessLimitCheck;

	private ETMParam etmParameters;

	private XLog currentLog;
	private UIPluginContext currentContext;
	private int populationSize = 20;
	private int eliteSize = 5;
	private int nrRandomTrees = 0;
	private double crossOverChance = 0.25;
	private double chanceOfRandomMutation = 0.25;
	private boolean preventDuplicates = true;
	private int maxGen = 1; // generations
	private double targetFitness = 1.0;
	private double replyFitnessWeight = 10.0;
	private double fitnessLimit = -1.0; // -1 means no limit
	private double maxFitnessTime	 = 10.0;
	private double precisionWeight	 = 5.0;
	private double generalizationWeight = 1.0;
	private double simplicityWeight = 2.0;
	private double similarityWeight = 0.0;

	private ETMParameterStorage parameterStorage;

	public void initialize(ETMParameterStorage storage) {
		this.parameterStorage = storage;
		initializeControls();
		setupAllListeners();
	}

	private void initializeControls() {
		populationSizeSlider.setValue(parameterStorage.getPopulationSize());
		populationSizeValue.setText(String.valueOf(parameterStorage.getPopulationSize()));
		eliteSizeSlider.setValue(parameterStorage.getEliteCount());
		eliteSizeValue.setText(String.valueOf(parameterStorage.getEliteCount()));
		randomTreesSlider.setValue(parameterStorage.getNrRandomTrees());
		randomTreesValue.setText(String.valueOf(parameterStorage.getNrRandomTrees()));
		crossoverChanceSlider.setValue(parameterStorage.getCrossOverChance());
		crossoverChanceValue.setText(String.format("%.2f", parameterStorage.getCrossOverChance()));
		mutationChanceSlider.setValue(parameterStorage.getMutationChance());
		mutationChanceValue.setText(String.format("%.2f", parameterStorage.getMutationChance()));
		preventDuplicatesCheck.setSelected(parameterStorage.isPreventDuplicates());
		maxGenerationsSlider.setValue(parameterStorage.getMaxGenerations());
		maxGenerationsValue.setText(String.valueOf(parameterStorage.getMaxGenerations()));
		targetFitnessSlider.setValue(parameterStorage.getTargetFitness());
		targetFitnessValue.setText(String.format("%.2f", parameterStorage.getTargetFitness()));
		replyFitnessWeightSlider.setValue(parameterStorage.getReplayFitnessWeight());
		replyFitnessWeightValue.setText(String.format("%.2f", parameterStorage.getReplayFitnessWeight()));
		boolean hasFitnessLimit = parameterStorage.getFitnessLimit() >= 0;
	    noFitnessLimitCheck.setSelected(!hasFitnessLimit);
	    fitnessLimitSlider.setDisable(!hasFitnessLimit);
	    
	    if (!hasFitnessLimit) {
	        fitnessLimitValue.setText("No Limit");
	        fitnessLimitSlider.setValue(0.9); // Set a reasonable value but keep disabled
	    } else {
	        fitnessLimitSlider.setValue(parameterStorage.getFitnessLimit());
	        fitnessLimitValue.setText(String.format("%.2f", parameterStorage.getFitnessLimit()));
	    }
		maxFTimeSlider.setValue(parameterStorage.getMaxFitnessTime());
		maxFTimeValue.setText(String.format("%.2f", parameterStorage.getMaxFitnessTime()));
		precisionWeightSlider.setValue(parameterStorage.getPrecisionWeight());
		precisionWeightValue.setText(String.format("%.2f", parameterStorage.getPrecisionWeight()));
		generalizationWeightSlider.setValue(parameterStorage.getGeneralizationWeight());
		generalizationWeightValue.setText(String.format("%.2f", parameterStorage.getGeneralizationWeight()));
		simplicityWeightSlider.setValue(parameterStorage.getSimplicityWeight());
		simplicityWeightValue.setText(String.format("%.2f", parameterStorage.getSimplicityWeight()));
		similarityWeightSlider.setValue(parameterStorage.getSimilarityWeight());
		similarityWeightValue.setText(String.format("%.2f", parameterStorage.getSimilarityWeight()));
	}



	private void setupAllListeners() {

		if (populationSizeSlider != null)
			setupIntegerSliderListener(populationSizeSlider, populationSizeValue);
		if (eliteSizeSlider != null)
			setupIntegerSliderListener(eliteSizeSlider, eliteSizeValue);
		if (randomTreesSlider != null)
			setupIntegerSliderListener(randomTreesSlider, randomTreesValue);
		if (maxGenerationsSlider != null)
			setupIntegerSliderListener(maxGenerationsSlider, maxGenerationsValue);
		if (crossoverChanceSlider != null)
			setupDoubleSliderListener(crossoverChanceSlider, crossoverChanceValue);
		if (mutationChanceSlider != null)
			setupDoubleSliderListener(mutationChanceSlider, mutationChanceValue);
		if (targetFitnessSlider != null)
			setupDoubleSliderListener(targetFitnessSlider, targetFitnessValue);
		if (replyFitnessWeightSlider != null)
			setupDoubleSliderListener(replyFitnessWeightSlider, replyFitnessWeightValue);
		// Fitness limit slider listener
	    if (fitnessLimitSlider != null) {
	        fitnessLimitSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
	            if (!noFitnessLimitCheck.isSelected()) { // Only update if not disabled
	                double doubleValue = Math.round(newVal.doubleValue() * 100) / 100.0;
	                fitnessLimitValue.setText(String.format("%.2f", doubleValue));
	            }
	        });
	    }
	    
	    // Fitness limit checkbox listener
	    if (noFitnessLimitCheck != null) {
	        noFitnessLimitCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
	            fitnessLimitSlider.setDisable(newVal);
	            if (newVal) {
	                fitnessLimitValue.setText("No Limit");
	            } else {
	                double value = fitnessLimitSlider.getValue();
	                fitnessLimitValue.setText(String.format("%.2f", value));
	            }
	        });
	    }
		if (maxFTimeSlider != null)
			setupDoubleSliderListener(maxFTimeSlider, maxFTimeValue);
		if (precisionWeightSlider != null)
			setupDoubleSliderListener(precisionWeightSlider, precisionWeightValue);
		if (generalizationWeightSlider != null)
			setupDoubleSliderListener(generalizationWeightSlider, generalizationWeightValue);
		if (simplicityWeightSlider != null)
			setupDoubleSliderListener(simplicityWeightSlider, simplicityWeightValue);
		if (similarityWeightSlider != null)
			setupDoubleSliderListener(similarityWeightSlider, similarityWeightValue);

	}

	private void setupIntegerSliderListener(Slider slider, Label label) {
		slider.valueProperty().addListener((obs, oldVal, newVal) -> {
			int intValue = newVal.intValue();
			label.setText(String.valueOf(intValue));
		});
	}

	private void setupDoubleSliderListener(Slider slider, Label label) {
		
		slider.valueProperty().addListener((obs, oldVal, newVal) -> {
			double doubleValue = Math.round(newVal.doubleValue() * 100) / 100.0;
			label.setText(String.format("%.2f", doubleValue));
		});
	}


	public void updateSettings() {
		if (parameterStorage != null) {
			parameterStorage.setPopulationSize((int) populationSizeSlider.getValue());
			parameterStorage.setEliteCount((int) eliteSizeSlider.getValue());
			parameterStorage.setNrRandomTrees((int) randomTreesSlider.getValue());
			parameterStorage.setCrossOverChance(crossoverChanceSlider.getValue());
			parameterStorage.setMutationChance(mutationChanceSlider.getValue());
			parameterStorage.setPreventDuplicates(preventDuplicatesCheck.isSelected());
			parameterStorage.setMaxGenerations((int) maxGenerationsSlider.getValue());
			parameterStorage.setTargetFitness(targetFitnessSlider.getValue());
			parameterStorage.setReplayFitnessWeight(replyFitnessWeightSlider.getValue());
			//parameterStorage.setFitnessLimit(fitnessLimitSlider.getValue());
			if (noFitnessLimitCheck.isSelected()) {
			    parameterStorage.setFitnessLimit(-1.0);
			} else {
			    parameterStorage.setFitnessLimit(fitnessLimitSlider.getValue());
			}
			parameterStorage.setMaxFitnessTime(maxFTimeSlider.getValue());
			parameterStorage.setPrecisionWeight(precisionWeightSlider.getValue());
			parameterStorage.setGeneralizationWeight(generalizationWeightSlider.getValue());
			parameterStorage.setSimplicityWeight(simplicityWeightSlider.getValue());
			parameterStorage.setSimilarityWeight(similarityWeightSlider.getValue());
		}
	}

	public ETMParam getParameters() {
		updateSettings();
		return etmParameters;
	}

	public void resetToDefaults() {
		// Reset to default values
		//populationSize = 20;
		populationSizeSlider.setValue(20);
		
		//eliteSize = 5;
		eliteSizeSlider.setValue(5);
		
		//nrRandomTrees = 0;
		randomTreesSlider.setValue(0);
		
		//crossOverChance = 0.25;
		crossoverChanceSlider.setValue(0.25);
		
		//chanceOfRandomMutation = 0.25;
		mutationChanceSlider.setValue(0.25);
		
		//preventDuplicates = true;
		preventDuplicatesCheck.setSelected(true);
		
		//maxGen = 50;
		maxGenerationsSlider.setValue(50);
		
		//targetFitness = 1.0;
		targetFitnessSlider.setValue(1.0);
		
		//replyFitnessWeight = 10.0;
		replyFitnessWeightSlider.setValue(10.0);
		
		//fitnessLimit = -1.0;
		noFitnessLimitCheck.setSelected(true);
	    fitnessLimitSlider.setDisable(true);
	    fitnessLimitSlider.setValue(0.9); // Disabled
	    fitnessLimitValue.setText("No Limit");
		
		//maxFitnessTime = 10.0;
		maxFTimeSlider.setValue(10.0);
		
		//precisionWeight = 5.0;
		precisionWeightSlider.setValue(5.0);
		
		//generalizationWeight = 1.0;
		generalizationWeightSlider.setValue(1.0);
		
		//simplicityWeight = 2.0;
		simplicityWeightSlider.setValue(2.0);
		
		//similarityWeight = 0.0;
		similarityWeightSlider.setValue(0.0);

		

		// Update the parameters
		updateSettings();
	}
	

}