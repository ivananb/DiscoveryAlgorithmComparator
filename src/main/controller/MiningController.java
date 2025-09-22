package main.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import main.PnmlToModelConverter;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import java.io.File;
import main.algorithms.MiningAlgorithm;
import main.algorithms.MiningAlgorithmSelector;
import main.utils.ExportController;
import main.utils.FileHandler;
import main.utils.MurataReduction;
import main.utils.VisualisationController;
import org.deckfour.xes.model.XLog;
import java.io.IOException;
import main.utils.Utils;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Controller for the main mining application UI.
 * Handles loading models, generating logs, mining, and displaying results.
 */
public class MiningController {
	// UI Components
	@FXML private ComboBox<String> miningAlgorithmComboBox;
	@FXML private Label applicationStatusLabel;
	@FXML private TextArea miningResultsTextArea;
	@FXML private Button loadPetriNetButton;
	@FXML private Button generateLogButton;
	@FXML private Button mineModelButton;
	@FXML private Button clearResultsButton;
	@FXML private Button exportLogXesButton;
	@FXML private Button exportPnmlButton;
	@FXML private Button logExplorerButton;
	@FXML private Button showStatisticsButton;
	@FXML private TextField traceCountField;
	@FXML private TextField maxTraceLengthField;
	@FXML private WebView originalModelWebView;
	@FXML private WebView discoveredModelWebView;
	@FXML private HBox pnmlWorkflowBox;
	@FXML private HBox miningSectionBox;
	@FXML private Label fileTypeLabel;
	@FXML private Button importFileButton;
	@FXML private Button settingsButton;
	@FXML private VBox visualizationContainer;
	@FXML private VBox originalModelContainer;
	@FXML private VBox discoveredModelContainer;
	@FXML private Label originalModelLabel;
	@FXML private Label discoveredModelLabel;
	@FXML private CheckBox removeSilentTransitionsCheckbox;
	@FXML private ProgressIndicator progressIndicator;

	private final PnmlToModelConverter converter = new PnmlToModelConverter();
	private Petrinet originalModel;
	private XLog generatedLog;
	private File currentPnmlFile;
	private Petrinet discoveredModel;
	private Petrinet reducedModelCache = null;
	private final VisualisationController visualisationController = new VisualisationController();
	
	private enum FileType {
		NONE, PNML, XES
	}
	private FileType currentFileType = FileType.NONE;
	private final FileHandler fileHandler = new FileHandler(converter);
	@FXML private ToggleGroup workflowModeGroup = new ToggleGroup();
	@FXML
	private VBox pnmlWorkflowPane;
	@FXML
	private VBox xesWorkflowPane;
	@FXML
	private ComboBox<String> xesMiningAlgorithmCombo;
	@FXML private Button loadXesButton;
	@FXML private Button mineFromXesButton;
	@FXML private Button exportXesPnmlButton;
	@FXML private XLog importedXesLog;
	
	private MurataReduction silentTransitionRemover;
	private UIPluginContext pluginContext;
	private Marking initialMarking;

	@FXML
	private void initialize() {
		miningAlgorithmComboBox.getItems().setAll(MiningAlgorithmSelector.getAvailableAlgorithms());
		miningAlgorithmComboBox.getSelectionModel().selectFirst();
		updateApplicationStatus("Ready to load a Petri net or event log");

		// Initialise WebViews
		visualisationController.initializeWebView(originalModelWebView);
		visualisationController.initializeWebView(discoveredModelWebView);

		// Setup context menus
		visualisationController.setupWebViewContextMenus(originalModelWebView, "original");
		visualisationController.setupWebViewContextMenus(discoveredModelWebView, "discovered");

		// Initially hide all workflow sections
		pnmlWorkflowBox.setVisible(false);
		miningSectionBox.setVisible(false);
		settingsButton.setDisable(false);
		
		progressIndicator.setVisible(false);

	}

	
	private void updateButtonStates() {
	    boolean hasLog = (generatedLog != null && !generatedLog.isEmpty())
	            || (importedXesLog != null && !importedXesLog.isEmpty());
	    boolean hasDiscoveredModel = discoveredModel != null;
	    boolean hasAlgorithmSelected = miningAlgorithmComboBox.getValue() != null
	            && !miningAlgorithmComboBox.getValue().isEmpty();
	    boolean hasPnmlAndLog = currentFileType == FileType.PNML && originalModel != null && generatedLog != null;

	    exportLogXesButton.setDisable(!hasLog);
	    mineModelButton.setDisable(!hasLog);
	    exportPnmlButton.setDisable(!hasDiscoveredModel);
	    logExplorerButton.setDisable(!hasLog);
	    settingsButton.setDisable(!hasLog || !hasAlgorithmSelected);
	    removeSilentTransitionsCheckbox.setDisable(!hasDiscoveredModel);
	    showStatisticsButton.setDisable(!hasPnmlAndLog);
	}

	@FXML
	private void handleGenerateLog() {
		if (originalModel == null) {
			Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea,
					"No Petri net loaded to generate log from.", null);
			return;
		}

		try {
			// Get user input for number of traces and max trace length
			int numberOfTraces = Utils.parseIntWithDefault(traceCountField.getText(), 100);
			int maxTraceLength = Utils.parseIntWithDefault(maxTraceLengthField.getText(), 50);

			// Generate log with parameters using the stored PNML file
			generatedLog = converter.generateLogFromPnml(originalModel, currentPnmlFile, numberOfTraces,
					maxTraceLength);

			displayGeneratedLogInformation();
			updateButtonStates();

			// Show confirmation
			updateApplicationStatus(
					"Event log generated with " + numberOfTraces + " traces and max length " + maxTraceLength);

		} catch (Exception e) {
			Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "Failed to generate event log", e);
		}
	}

	@FXML
	private void handleClearResults() {
		// Reset all values to defaults
		traceCountField.setText("100");
		maxTraceLengthField.setText("50");
		miningAlgorithmComboBox.getSelectionModel().selectFirst();
		removeSilentTransitionsCheckbox.setSelected(false);

		// Clear data and UI
		generatedLog = null;
		discoveredModel = null;
		originalModel = null;
		currentPnmlFile = null;
		importedXesLog = null;
		silentTransitionRemover = null; // Use default constructor
		reducedModelCache = null; // Clear the cache

		// Clear visualisations
		visualisationController.clearWebView(originalModelWebView);
	    visualisationController.clearWebView(discoveredModelWebView);
	    miningResultsTextArea.clear();

		// Reset UI state
		pnmlWorkflowBox.setVisible(false);
		miningSectionBox.setVisible(false);
		updateButtonStates();
		updateApplicationStatus("Results cleared. Ready for new model or log.");
	    applicationStatusLabel.getStyleClass().removeAll("status-removing-silent", "status-silent-removed");
	    
		// Set file type to NONE
		currentFileType = FileType.NONE;
		updateUIForFileType();

	}

	private void resetUIForNewModel() {
		// Save the current text content
		String preservedText = miningResultsTextArea.getText();

		// Clear existing data
		generatedLog = null;
		discoveredModel = null;
		importedXesLog = null;

		// Clear visualisations
		visualisationController.clearWebView(originalModelWebView);
	    visualisationController.clearWebView(discoveredModelWebView);
	    miningResultsTextArea.clear();

		// Reset visualisation layout
		originalModelContainer.setVisible(true);
		originalModelContainer.setManaged(true);
		discoveredModelContainer.setMaxHeight(Region.USE_PREF_SIZE);
		discoveredModelWebView.setPrefHeight(300);

		// Reset& Disable buttons
		generateLogButton.setDisable(false);
		mineModelButton.setDisable(true);
		updateButtonStates();

		// Reset trace parameters to defaults
		traceCountField.setText("100");
		maxTraceLengthField.setText("50");

		// Restore the preserved text
		miningResultsTextArea.setText(preservedText);
	}

	private void displayLoadedModelInformation(File modelFile) {
		String modelName = modelFile.getName();
		int places = originalModel.getPlaces().size();
		int transitions = originalModel.getTransitions().size();
		int arcs = originalModel.getEdges().size();

		updateApplicationStatus(
				String.format("Loaded %s (%d places, %d transitions, %d arcs)", modelName, places, transitions, arcs));

		miningResultsTextArea.appendText(String.format("Successfully loaded Petri net:\n" + "- File: %s\n"
				+ "- Places: %d\n" + "- Transitions: %d\n" + "- Arcs: %d\n\n", modelName, places, transitions, arcs));
		clearResultsButton.setDisable(false);
	}

	private void displayGeneratedLogInformation() {
		int traceCount = generatedLog.size();
		int eventCount = generatedLog.stream().mapToInt(trace -> trace.size()).sum();

		miningResultsTextArea.appendText(String.format(
				"\nGenerated event log:\n" + "- Traces: %d\n" + "- Total events: %d\n\n", traceCount, eventCount));
	}

	private void displayMiningResults(String algorithmName, String result) {
		miningResultsTextArea.appendText("\n=== Mining Results using " + algorithmName + " ===\n");
		miningResultsTextArea.appendText(result);
		clearResultsButton.setDisable(false);
	}

	private void updateApplicationStatus(String message) {
		applicationStatusLabel.setText(message);
	}

	private void handleModelLoadingError(Exception e) {
		Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "Failed to load Petri net", e);
		miningResultsTextArea.appendText("Details: " + e.getMessage() + "\n");
		originalModel = null;
		generateLogButton.setDisable(true);
		mineModelButton.setDisable(true);
		e.printStackTrace();
	}

	// Download event log as XES file
	@FXML
	private void handleExportLogXes() {
		if (generatedLog == null) {
			Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "No event log generated to export.",
					null);
			return;
		}
		try {
			ExportController.exportLogAsXes(generatedLog, currentPnmlFile, miningAlgorithmComboBox.getValue(),
					exportLogXesButton.getScene().getWindow());
			updateApplicationStatus("Event log exported successfully");
			miningResultsTextArea.appendText("\nEvent log exported successfully.\n");
		} catch (Exception e) {
			Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "Failed to export event log", e);
		}
	}
	
	@FXML
	private void handleExportPnml() {
	    Petrinet modelToExport = discoveredModel;
	    boolean isReduced = false;
	    
	    // Use reduced model if checkbox is checked and reduced model is available
	    if (removeSilentTransitionsCheckbox.isSelected() && reducedModelCache != null) {
	        modelToExport = reducedModelCache;
	        isReduced = true;
	        miningResultsTextArea.appendText("\nExporting reduced model (silent transitions removed)\n");
	    } else if (removeSilentTransitionsCheckbox.isSelected() && reducedModelCache == null) {
	        // Checkbox is checked but no reduced model available
	        Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, 
	                "No reduced model available. Please apply reduction first by toggling the checkbox.", null);
	        removeSilentTransitionsCheckbox.setSelected(false);
	        return;
	    }
	    
	    if (modelToExport == null) {
	        Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "No model to export.", null);
	        return;
	    }
	    
	    try {
	        ExportController.exportPnml(modelToExport, currentPnmlFile, miningAlgorithmComboBox.getValue(),
	                exportPnmlButton.getScene().getWindow(), isReduced);
	        updateApplicationStatus("Model exported successfully");
	        miningResultsTextArea.appendText("\nModel exported successfully.\n");
	    } catch (Exception e) {
	        Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "Failed to export PNML model", e);
	    }
	}

	@FXML
	private void handleShowTraceBrowser() {
		if (generatedLog == null) {
			Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "No event log generated to view.",
					null);
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/logExplorer.fxml"));
			Parent root = loader.load();

			LogExplorerController controller = loader.getController();
			controller.setLog(generatedLog);

			controller.setOriginalFile(currentPnmlFile);
			controller.setMiningAlgorithm(miningAlgorithmComboBox.getValue());

			Stage stage = new Stage();
			stage.setTitle("Trace Browser");
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "Failed to open trace browser", e);
		}
	}

	// Update your handleLoadPetriNet method
	@FXML
	private void handleLoadPetriNet() {
		File selectedFile = handleImportFile();
		if (selectedFile != null) {
			try {
				resetUIForNewModel();

				currentPnmlFile = selectedFile;
				originalModel = converter.loadPetriNetFromPnml(selectedFile);

				// Display the original model
				visualisationController.displayModelInWebView(originalModelWebView, originalModel, "Original Model");

				System.out.println("Petri net loaded with: " + originalModel.getPlaces().size() + " places, "
						+ originalModel.getTransitions().size() + " transitions");

				displayLoadedModelInformation(selectedFile);
				generateLogButton.setDisable(false);
			} catch (Exception e) {
				handleModelLoadingError(e);
			}
		}
	}

	
	@FXML
	private void handleMineModel() {
	    System.out.println("\n=== Starting model mining ===");
	    
	    progressIndicator.setVisible(true);
	    
	    // Reset silent transitions state
	    removeSilentTransitionsCheckbox.setSelected(false);
	    applicationStatusLabel.getStyleClass().removeAll("status-removing-silent", "status-silent-removed");
	    
	    XLog logToMine = (currentFileType == FileType.XES) ? importedXesLog : generatedLog;
	    if (logToMine == null) {
	        Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea,
	                "No event log available to mine a model from. Please generate or import a log first.", null);
	        return;
	    }

	    try {
	        String selectedAlgorithm = miningAlgorithmComboBox.getValue();
	        System.out.println("[DEBUG] Using algorithm: " + selectedAlgorithm);

	        // Create plugin context first
	        pluginContext = createPluginContextForAlgorithm(selectedAlgorithm);
	        
	        if (pluginContext == null) {
	            throw new Exception("Failed to create plugin context for algorithm: " + selectedAlgorithm);
	        }

	        // Get the algorithm instance with the created context
	        MiningAlgorithm algorithm = MiningAlgorithmSelector.getAlgorithm(selectedAlgorithm);
	        
	        // Mine the model using the algorithm
	        System.out.println("[DEBUG] Starting mining process...");
	        discoveredModel = algorithm.mine(pluginContext, logToMine);
	        
	        // Create an empty marking for now
	        initialMarking = new Marking();
	        System.out.println("[DEBUG] Using empty initial marking");

	        if (discoveredModel == null) {
	            System.err.println("[ERROR] Mining algorithm returned null model");
	            Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea,
	                    "Mining algorithm returned null model", null);
	            return;
	        }

	        System.out.printf("[DEBUG] Discovered model stats - Places: %d, Transitions: %d, Edges: %d\n",
	                discoveredModel.getPlaces().size(), discoveredModel.getTransitions().size(),
	                discoveredModel.getEdges().size());

	        // Initialise the silent transition remover with the algorithm's context
	        silentTransitionRemover = new MurataReduction(pluginContext);

	        // Display the model
	        System.out.println("[DEBUG] Attempting to display discovered model");
	        visualisationController.displayModelInWebView(discoveredModelWebView, discoveredModel,
	                currentFileType == FileType.XES ? "Discovered Model (from XES)" : "Discovered Model");

	        // Different output based on workflow
	        if (currentFileType == FileType.PNML) {
	            // PNML workflow - compare with original model
	            String comparisonResult = converter.compareModels(originalModel, discoveredModel);
	            displayMiningResults(selectedAlgorithm, comparisonResult);
	        } else {
	            // XES workflow - show mining statistics
	            String miningStats = converter.generateModelStatistics(logToMine, selectedAlgorithm);
	            displayMiningResults(selectedAlgorithm, miningStats);
	        }

	        updateApplicationStatus("Mining completed with " + selectedAlgorithm);
	        updateButtonStates();

	    } catch (Exception e) {
	        System.err.println("[ERROR] Mining failed with exception: " + e.getMessage());
	        Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "Mining failed", e);
	    } finally {
	    	// Hide progress indicator when mining is complete and model is displayed
	    	progressIndicator.setVisible(false);
	    }
	}

	private UIPluginContext createPluginContextForAlgorithm(String algorithmName) {
	    try {
	        if (algorithmName == null || algorithmName.isEmpty()) {
	            throw new IllegalArgumentException("Algorithm name cannot be null or empty");
	        }
	        
	        // Create a proper plugin context
	        UIContext uiContext = new UIContext();
	        UIPluginContext pluginContext = uiContext.getMainPluginContext();

	        System.out.println("[DEBUG] Created plugin context for algorithm: " + algorithmName);
	        return pluginContext;
	        
	    } catch (Exception e) {
	        System.err.println("[ERROR] Failed to create plugin context: " + e.getMessage());
	        e.printStackTrace();
	        return null;
	    }
	}
	

	@FXML
	private File handleImportFile() {
		File selectedFile = fileHandler.showFileChooser(importFileButton.getScene().getWindow());
		if (selectedFile != null) {
			resetUIForNewModel();

			try {
				String filename = selectedFile.getName().toLowerCase();
				if (filename.endsWith(".pnml")) {
					loadPnmlFile(selectedFile);
				} else if (filename.endsWith(".xes")) {
					loadXesFile(selectedFile);
				} else {
					Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea,
							"Unsupported file type. Please select a PNML or XES file.", null);
					return null;
				}
			} catch (Exception e) {
				Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "Failed to import file", e);
			}
		}
		return selectedFile;
	}

	private void loadPnmlFile(File pnmlFile) throws Exception {

		// Reset UI for new model
		resetUIForNewModel();

		// show hbox buttons
		pnmlWorkflowBox.setVisible(true);
		miningSectionBox.setVisible(true);
		logExplorerButton.setVisible(true);

		currentPnmlFile = pnmlFile;
		originalModel = converter.loadPetriNetFromPnml(pnmlFile);
		visualisationController.displayModelInWebView(originalModelWebView, originalModel, "Original Model");

		// Update UI state
		currentFileType = FileType.PNML;
		updateUIForFileType();
		displayLoadedModelInformation(pnmlFile);
	}

	private void loadXesFile(File xesFile) throws Exception {
		// Reset UI for new model
		resetUIForNewModel();
		pnmlWorkflowBox.setVisible(false);

		// Show mining controls
		miningSectionBox.setVisible(true);

		// Load the log
		importedXesLog = converter.importXesLog(xesFile);
		currentPnmlFile = xesFile;

		// Update UI state
		currentFileType = FileType.XES;
		updateUIForFileType();
		updateApplicationStatus("Loaded XES log with " + importedXesLog.size() + " traces");

		// Enable controls
		mineModelButton.setDisable(false);
		logExplorerButton.setDisable(false);
		updateButtonStates();

		// Adjust visualisation for XES workflow
		originalModelContainer.setVisible(false);
		originalModelContainer.setManaged(false);
		discoveredModelContainer.setMaxHeight(Double.MAX_VALUE);
		discoveredModelWebView.setPrefHeight(600);

		// Update CSS class for XES workflow
		miningSectionBox.getStyleClass().add("xes-workflow");
	}

	private void updateUIForFileType() {
		switch (currentFileType) {
		case PNML:
			fileTypeLabel.setText("PNML");
			fileTypeLabel.getStyleClass().removeAll("file-type-xes", "file-type-pnml");
			fileTypeLabel.getStyleClass().add("file-type-pnml");
			break;

		case XES:
			fileTypeLabel.setText("XES");
			fileTypeLabel.getStyleClass().removeAll("file-type-xes", "file-type-pnml");
			fileTypeLabel.getStyleClass().add("file-type-xes");
			break;

		case NONE:
			fileTypeLabel.setText("None");
			fileTypeLabel.getStyleClass().removeAll("file-type-xes", "file-type-pnml");
			break;
		}
	}

	@FXML
	private void handleSettingsButton() {
		try {
			// Load the settings dialog
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/parameters-settings.fxml"));
			DialogPane dialogPane = loader.load();
			ParametersSettingsController parametersController = loader.getController();

			// Get current algorithm
			MiningAlgorithm algorithm = MiningAlgorithmSelector.getAlgorithm(miningAlgorithmComboBox.getValue());
			if (algorithm == null) {
				Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea,
						"No valid mining algorithm selected.", null);
				return;
			}

			// Configure the dialog
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setDialogPane(dialogPane);
			dialog.setTitle(miningAlgorithmComboBox.getValue() + " Settings");

			XLog currentLog = (currentFileType == FileType.XES) ? importedXesLog : generatedLog;

			// Initialise with current settings
			parametersController.setAlgorithm(algorithm, dialog, this);

			// Show dialog and wait for response
			dialog.showAndWait().ifPresent(response -> {
				if (response == ButtonType.OK) {
					updateApplicationStatus("Settings saved for " + miningAlgorithmComboBox.getValue());
				}
			});

		} catch (IOException e) {
			Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, "Failed to open settings dialog", e);
		}
	}

	public Label getStatusLabel() {
		return applicationStatusLabel;
	}

	public TextArea getMiningResultsTextArea() {
		return miningResultsTextArea;
	}

	public WebView getOriginalModelWebView() {
		return originalModelWebView;
	}

	public WebView getDiscoveredModelWebView() {
		return discoveredModelWebView;
	}

	public File getCurrentPnmlFile() {
		return currentPnmlFile;
	}

	public Petrinet getOriginalModel() {
		return originalModel;
	}

	public XLog getGeneratedLog() {
		return generatedLog;
	}
	
	public UIPluginContext getPluginContext() {
	    return this.pluginContext;
	}
	

	@FXML
	private void handleRemoveSilentTransitions() {
	    if (discoveredModel == null) {
	        Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, 
	                "No discovered model available to process.", null);
	        removeSilentTransitionsCheckbox.setSelected(false);
	        return;
	    }

	    try {
	        if (removeSilentTransitionsCheckbox.isSelected()) {
	            updateApplicationStatus("Applying Murata reduction...");
	            applicationStatusLabel.getStyleClass().add("status-removing-silent");
	            
	            if (silentTransitionRemover == null) {
	                silentTransitionRemover = new MurataReduction(pluginContext);
	            }
	            
	            // Count silent transitions before removal
	            int originalSilentCount = Utils.countSilentTransitions(discoveredModel);
	            miningResultsTextArea.appendText("\n=== Murata Silent Transition Reduction ===\n");
	            miningResultsTextArea.appendText("Found " + originalSilentCount + " silent transitions\n");
	            
	            
	         // Apply Murata reduction and cache the result
	            reducedModelCache = silentTransitionRemover.removeSilentTransitions(discoveredModel, initialMarking);
	            
	            if (reducedModelCache != null) {
	                // Display the reduced model
	                visualisationController.displayModelInWebView(discoveredModelWebView, reducedModelCache,
	                        currentFileType == FileType.XES ? 
	                        "Discovered Model (Murata Reduced)" : 
	                        "Discovered Model (Murata Reduced)");
	                
	                // Add statistics to results
	                String stats = silentTransitionRemover.getStatistics(originalModel, discoveredModel, reducedModelCache);
	                updateApplicationStatus("Murata reduction completed successfully");
	                applicationStatusLabel.getStyleClass().remove("status-removing-silent");
	                applicationStatusLabel.getStyleClass().add("status-silent-removed");
	                
	            } else {
	                // Murata failed - use fallback
	            	reducedModelCache = null; // Clear cache
	                displayFallbackGraph();
	                miningResultsTextArea.appendText("\nMurata reduction failed - using fallback visualization\n");
	                updateApplicationStatus("Murata failed - fallback displayed");
	                applicationStatusLabel.getStyleClass().remove("status-removing-silent");
	                applicationStatusLabel.getStyleClass().add("status-silent-failed");
	            }
	            
	        } else {
	            // Restore the original discovered model
	        	reducedModelCache = null; // Clear cache
	            visualisationController.displayModelInWebView(discoveredModelWebView, discoveredModel,
	                    currentFileType == FileType.XES ? "Discovered Model (from XES)" : "Discovered Model");
	            
	            updateApplicationStatus("Original model restored");
	            applicationStatusLabel.getStyleClass().removeAll("status-removing-silent", "status-silent-removed", "status-silent-failed");
	            
	            miningResultsTextArea.appendText("\n=== Restored Original Model ===\n");
	        }
	        
	    } catch (Exception e) {
	    	reducedModelCache = null; // Clear cache on error
	        System.err.println("[ERROR] Silent transition removal failed: " + e.getMessage());
	        e.printStackTrace();
	        
	        try {
	            displayFallbackGraph();
	            miningResultsTextArea.appendText("\nError occurred during reduction: " + e.getMessage() + "\n");
	            updateApplicationStatus("Error occurred - fallback displayed");
	            applicationStatusLabel.getStyleClass().remove("status-removing-silent");
	            applicationStatusLabel.getStyleClass().add("status-silent-failed");
	            
	        } catch (Exception fallbackException) {
	            Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea, 
	                    "All silent transition removal methods failed", fallbackException);
	            removeSilentTransitionsCheckbox.setSelected(false);
	            applicationStatusLabel.getStyleClass().removeAll("status-removing-silent", "status-silent-failed");
	        }
	    }
	}
	
	private void displayFallbackGraph() {
	    try {
	        if (silentTransitionRemover != null) {
	            String fallbackDot = silentTransitionRemover.getFallbackDotGraph();
	            // Load the fallback DOT content
	            discoveredModelWebView.getEngine().executeScript("setModel('" + fallbackDot + "')");
	        }
	    } catch (Exception e) {
	        discoveredModelWebView.getEngine().loadContent("<html><body>Error displaying fallback graph</body></html>");
	    }
	}
	
	
	@FXML
	private void handleShowStatistics() {
	    if (currentFileType != FileType.PNML || originalModel == null || generatedLog == null) {
	        Utils.showErrorMessage(applicationStatusLabel, miningResultsTextArea,
	                "Please load a PNML file and generate an event log first.", null);
	        return;
	    }
	    
	    StatisticsController.showStatisticsWindow(currentPnmlFile, originalModel, generatedLog, this);
	}
	
}