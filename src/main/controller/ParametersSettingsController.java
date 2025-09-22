package main.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main.algorithms.*;
import main.controller.settings.HeuristicMinerSettingsController;
import main.controller.settings.InductiveMinerSettingsController;
import main.controller.settings.SplitMinerSettingsController;
import main.controller.settings.ETMParameterStorage;
import main.controller.settings.EvolutionaryTreeMinerSettingsController;
import java.io.IOException;
import java.util.Map;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;

import main.utils.Utils;

/**
 * Controller class for managing the parameters settings UI.
 * Dynamically loads and displays settings for different mining algorithms.
 */
public class ParametersSettingsController {

	@FXML private VBox settingsContainer;
	@FXML private ButtonType saveButton;
	@FXML private ButtonType saveAndMineButton;
	@FXML private ButtonType resetButton;

	private MiningAlgorithm currentAlgorithm;
	private Dialog<ButtonType> dialog;
	private MiningController miningController;
	private Object currentSettingsController;

	public void setAlgorithm(MiningAlgorithm algorithm, Dialog<ButtonType> dialog, MiningController miningController) {
		this.currentAlgorithm = algorithm;
		this.dialog = dialog;
		this.miningController = miningController;

		try {
			settingsContainer.getChildren().clear();

			String fxmlFile = "";
			if (algorithm instanceof HeuristicMinerAlgorithm) {
				fxmlFile = "/view/settings/heuristic-miner-settings.fxml";
			} else if (algorithm instanceof InductiveMinerAlgorithm) {
				fxmlFile = "/view/settings/inductive-miner-settings.fxml";
			} else if (algorithm instanceof EvolutionaryTreeMinerAlgorithm) {
				fxmlFile = "/view/settings/evolutionary-tree-miner-settings.fxml";
			} else if (algorithm instanceof SplitMinerAlgorithm) {
				fxmlFile = "/view/settings/split-miner-settings.fxml";
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
			Node settingsPane = loader.load();
			settingsContainer.getChildren().add(settingsPane);

			// Initialise controller
			Object controller = loader.getController();
			if (controller instanceof HeuristicMinerSettingsController) {
				((HeuristicMinerSettingsController) controller)
						.initialize((HeuristicsMinerSettings) algorithm.getParameters());
			} else if (controller instanceof InductiveMinerSettingsController) {
				((InductiveMinerSettingsController) controller)
						.initialize((MiningParameters) algorithm.getParameters());
			} else if (controller instanceof EvolutionaryTreeMinerSettingsController) {
				// Get the parameter storage from the algorithm
				ETMParameterStorage storage = ((EvolutionaryTreeMinerAlgorithm) algorithm).getParameterStorage();

				// Initialise the controller with the storage
				((EvolutionaryTreeMinerSettingsController) controller).initialize(storage);
			} else if (controller instanceof SplitMinerSettingsController) {
				((SplitMinerSettingsController) controller).initialize((Map<String, Object>) algorithm.getParameters());
			} else {
				Utils.showErrorMessage(miningController.getStatusLabel(), miningController.getMiningResultsTextArea(),
						"Unknown algorithm settings controller", null);
				return;
			}

			currentSettingsController = controller;

			// Configure buttons
			configureDialogButtons();

		} catch (IOException e) {
			e.printStackTrace();
			Utils.showErrorMessage(miningController.getStatusLabel(), miningController.getMiningResultsTextArea(),
					"Failed to load settings view: " + e.getMessage(), null);
		}
	}

	private void updateSettings() {
		if (currentSettingsController instanceof InductiveMinerSettingsController) {
			InductiveMinerSettingsController controller = (InductiveMinerSettingsController) currentSettingsController;
			controller.updateSettings();
			MiningParameters params = controller.getParameters();
			((InductiveMinerAlgorithm) currentAlgorithm).setParameters(params);
		} else if (currentSettingsController instanceof HeuristicMinerSettingsController) {
			HeuristicMinerSettingsController controller = (HeuristicMinerSettingsController) currentSettingsController;
			controller.updateSettings();
			HeuristicsMinerSettings params = controller.getParameters();
			((HeuristicMinerAlgorithm) currentAlgorithm).setParameters(params);
		} else if (currentSettingsController instanceof EvolutionaryTreeMinerSettingsController) {
			EvolutionaryTreeMinerSettingsController controller = (EvolutionaryTreeMinerSettingsController) currentSettingsController;
			controller.updateSettings();

		} else if (currentSettingsController instanceof SplitMinerSettingsController) {
			SplitMinerSettingsController controller = (SplitMinerSettingsController) currentSettingsController;
			controller.updateSettings();
			Map<String, Object> params = controller.getParameters();
			((SplitMinerAlgorithm) currentAlgorithm).setParameters(params);
		} else {
			Utils.showErrorMessage(miningController.getStatusLabel(), miningController.getMiningResultsTextArea(),
					"Unknown algorithm settings controller", null);
		}
	}

	private void printCurrentParameters() {
		if (currentAlgorithm instanceof InductiveMinerAlgorithm) {
			MiningParameters params = (MiningParameters) currentAlgorithm.getParameters();
			System.out.println("[PARAMETER DUMP] Inductive Miner Settings:");
			System.out.println("- Noise threshold: " + params.getNoiseThreshold());
			System.out.println("- Use multithreading: " + params.isUseMultithreading());
		} else if (currentAlgorithm instanceof HeuristicMinerAlgorithm) {
			HeuristicsMinerSettings params = (HeuristicsMinerSettings) currentAlgorithm.getParameters();
			System.out.println("[PARAMETER DUMP] Heuristic Miner Settings:");
			System.out.println("- Dependency threshold: " + params.getDependencyThreshold());
			System.out.println("- AND threshold: " + params.getAndThreshold());
			System.out.println("- Use all connected: " + params.isUseAllConnectedHeuristics());
		} else if (currentAlgorithm instanceof EvolutionaryTreeMinerAlgorithm) {
		    Object params = currentAlgorithm.getParameters();
		        ETMParameterStorage storage = (ETMParameterStorage) params;
		        System.out.println("[PARAMETER DUMP] Evolutionary Tree Miner Settings:");
		        System.out.println("- Population size: " + storage.getPopulationSize());
		        System.out.println("- Elite size: " + storage.getEliteCount());
		        System.out.println("- Random trees: " + storage.getNrRandomTrees());
		        System.out.println("- Mutation chance: " + storage.getMutationChance());
		        System.out.println("- Crossover chance: " + storage.getCrossOverChance());
		        System.out.println("- Max generations: " + storage.getMaxGenerations());
		        System.out.println("- Fitness limit: " + storage.getFitnessLimit());
		        System.out.println("- Replay fitness weight: " + storage.getReplayFitnessWeight());
		        System.out.println("- Precision weight: " + storage.getPrecisionWeight());
		        System.out.println("- Generalization weight: " + storage.getGeneralizationWeight());
		        System.out.println("- Simplicity weight: " + storage.getSimplicityWeight());
		        System.out.println("- Similarity weight: " + storage.getSimilarityWeight());
		        System.out.println("- Prevent duplicates: " + storage.isPreventDuplicates());
		        System.out.println("- Target fitness: " + storage.getTargetFitness());
		        System.out.println("- Max fitness time: " + storage.getMaxFitnessTime());
		        System.out.println("- CPU cores: " + storage.getCpuCores());

		} else if (currentAlgorithm instanceof SplitMinerAlgorithm) {
			Map<String, Object> params = (Map<String, Object>) currentAlgorithm.getParameters();
			System.out.println("[PARAMETER DUMP] Split Miner Settings:");
			System.out.println("- Frequency threshold: " + params.get("frequencyThreshold"));
			System.out.println("- Parallelism threshold: " + params.get("parallelismThreshold"));
		} else {
			Utils.showErrorMessage(miningController.getStatusLabel(), miningController.getMiningResultsTextArea(),
					"Unknown algorithm type: " + currentAlgorithm.getClass().getSimpleName(), null);
		}
	}


	private void configureDialogButtons() {
		// Get references to the buttons
		Node saveButtonNode = dialog.getDialogPane().lookupButton(saveButton);
		Node resetButtonNode = dialog.getDialogPane().lookupButton(resetButton);

		// Configure Save button
		saveButtonNode.addEventFilter(ActionEvent.ACTION, event -> {
			System.out.println("[DEBUG] Save button clicked");
			updateSettings();
			printCurrentParameters();
		});

		// Configure Reset button
		resetButtonNode.addEventFilter(ActionEvent.ACTION, event -> {
			System.out.println("[DEBUG] Reset button clicked");
			resetToDefaultSettings();
			updateSettings(); // Save the defaults immediately
			event.consume(); // Prevent dialog from closing
		});
	}

	private void resetToDefaultSettings() {
		if (currentSettingsController instanceof InductiveMinerSettingsController) {
			((InductiveMinerSettingsController) currentSettingsController).resetToDefaults();
		} else if (currentSettingsController instanceof HeuristicMinerSettingsController) {
			((HeuristicMinerSettingsController) currentSettingsController).resetToDefaults();
		} else if (currentSettingsController instanceof EvolutionaryTreeMinerSettingsController) {
			((EvolutionaryTreeMinerSettingsController) currentSettingsController).resetToDefaults();
		} else if (currentSettingsController instanceof SplitMinerSettingsController) {
			((SplitMinerSettingsController) currentSettingsController).resetToDefaults();
		} else {
			Utils.showErrorMessage(miningController.getStatusLabel(), miningController.getMiningResultsTextArea(),
					"Unknown algorithm settings controller", null);
		}
	}

}