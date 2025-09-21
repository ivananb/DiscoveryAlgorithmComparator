package main.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.algorithms.MiningAlgorithm;
import main.algorithms.MiningAlgorithmSelector;
import main.utils.MurataReduction;
import main.utils.Utils;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javafx.util.Callback;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import com.raffaeleconforti.marking.MarkingDiscoverer;
//import au.edu.qut.bpmn.metrics.ComplexityCalculator;
//import au.edu.qut.petrinet.tools.SoundnessChecker;
import nl.tue.astar.AStarException;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.multietc.plugins.MultiETCPlugin;
import org.processmining.plugins.multietc.res.MultiETCResult;
import org.processmining.plugins.multietc.sett.MultiETCSettings;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;


public class StatisticsController {

    @FXML private TableView<AlgorithmResult> resultsTableView;
    @FXML private TableColumn<AlgorithmResult, String> algorithmColumn;
    @FXML private TableColumn<AlgorithmResult, Long> timeColumn;
    @FXML private TableColumn<AlgorithmResult, Integer> placesColumn;
    @FXML private TableColumn<AlgorithmResult, Integer> transitionsColumn;
    @FXML private TableColumn<AlgorithmResult, Integer> arcsColumn;
    @FXML private TableColumn<AlgorithmResult, Integer> fitnessColumn;
    @FXML private TableColumn<AlgorithmResult, Integer> precisionColumn;
    @FXML private TableColumn<AlgorithmResult, Integer> fMeasureColumn;
    @FXML private TableColumn<AlgorithmResult, Double> overallScoreColumn;
    @FXML private TableColumn<AlgorithmResult, String> winnerColumn;
    
    @FXML private TextField fitnessWeightField;
    @FXML private TextField precisionWeightField;
    @FXML private TextField timeWeightField;
    @FXML private TextField fMeasureWeightField;

    @FXML private Label fileNameLabel;
    @FXML private Label fileStatsLabel;
    @FXML private Label bestAlgorithmLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private CheckBox removeSilentCheckbox;
    @FXML private HBox buttonBox;
    @FXML private VBox weightsContainer;
    
    private File pnmlFile;
    private Petrinet originalModel;
    private XLog generatedLog;
    private MiningController miningController;
    private List<AlgorithmResult> algorithmResults = new ArrayList<>();
    private UIPluginContext pluginContext;
    
    private double fitnessWeight = 0.4;
    private double precisionWeight = 0.4;
    private double timeWeight = 0.1;
    private double fMeasureWeight = 0.1;

    public void initialize() {
        setupTableView();
        
        resultsTableView.setSelectionModel(null); // Disable row selection
    
        UIContext uiContext = new UIContext();
        pluginContext = uiContext.getMainPluginContext();
    }

    private void setupTableView() {
        // Make columns more compact
        algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));
        algorithmColumn.setMinWidth(180);
        algorithmColumn.setMaxWidth(220);
        
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("executionTime"));
        timeColumn.setMinWidth(60);
        timeColumn.setMaxWidth(80);
        
        placesColumn.setCellValueFactory(new PropertyValueFactory<>("placesCount"));
        placesColumn.setMinWidth(60);
        placesColumn.setMaxWidth(80);
        
        transitionsColumn.setCellValueFactory(new PropertyValueFactory<>("transitionsCount"));
        transitionsColumn.setMinWidth(90);
        transitionsColumn.setMaxWidth(110);
        
        arcsColumn.setCellValueFactory(new PropertyValueFactory<>("arcsCount"));
        arcsColumn.setMinWidth(50);
        arcsColumn.setMaxWidth(90);
        
        fitnessColumn.setCellValueFactory(new PropertyValueFactory<>("fitnessScore"));
        fitnessColumn.setMinWidth(70);
        fitnessColumn.setMaxWidth(90);
        
        precisionColumn.setCellValueFactory(new PropertyValueFactory<>("precisionScore"));
        precisionColumn.setMinWidth(90);
        precisionColumn.setMaxWidth(110);
        
        fMeasureColumn.setCellValueFactory(new PropertyValueFactory<>("fMeasureScore"));
        fMeasureColumn.setMinWidth(100);
        fMeasureColumn.setMaxWidth(120);
        
        overallScoreColumn.setCellValueFactory(new PropertyValueFactory<>("overallScore"));
        overallScoreColumn.setMinWidth(100);
        overallScoreColumn.setMaxWidth(130);
        
        overallScoreColumn.setCellFactory(new Callback<TableColumn<AlgorithmResult, Double>, TableCell<AlgorithmResult, Double>>() {
            @Override
            public TableCell<AlgorithmResult, Double> call(TableColumn<AlgorithmResult, Double> param) {
                return new TableCell<AlgorithmResult, Double>() {
                    @Override
                    protected void updateItem(Double score, boolean empty) {
                        super.updateItem(score, empty);
                        if (empty || score == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            // Format to 2 decimal places
                            setText(String.format("%.2f", score));
                            
                        }
                    }
                };
            }
        });
        
        winnerColumn.setCellValueFactory(new PropertyValueFactory<>("winnerStatus"));
        winnerColumn.setMinWidth(60);
        winnerColumn.setMaxWidth(80);
        
        // Add style for winner column
        winnerColumn.setCellFactory(column -> new TableCell<AlgorithmResult, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("WINNER".equals(item)) {
                        setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Set fixed row height
        resultsTableView.setFixedCellSize(35);
    }

    public void setData(File pnmlFile, Petrinet originalModel, XLog generatedLog, MiningController miningController) {
        this.pnmlFile = pnmlFile;
        this.originalModel = originalModel;
        this.generatedLog = generatedLog;
        this.miningController = miningController;
        
        updateFileInfo();
    }

    private void updateFileInfo() {
        if (pnmlFile != null) {
            fileNameLabel.setText("File: " + pnmlFile.getName());
            
            if (originalModel != null) {
                int places = originalModel.getPlaces().size();
                int transitions = originalModel.getTransitions().size();
                int arcs = originalModel.getEdges().size();
                fileStatsLabel.setText(String.format("Model: %d Places, %d Transitions, %d Arcs", places, transitions, arcs));
            }
        }
    }

    @FXML
    private void handleRunComparison() {
    	// Collapse weights container when starting computation
        weightsContainer.setVisible(false);
        weightsContainer.setManaged(false);
              
        // Read and validate weights
        try {
            fitnessWeight = Double.parseDouble(fitnessWeightField.getText());
            precisionWeight = Double.parseDouble(precisionWeightField.getText());
            timeWeight = Double.parseDouble(timeWeightField.getText());
            fMeasureWeight = Double.parseDouble(fMeasureWeightField.getText());
            
            // Validate weights sum to 1.0
            double total = fitnessWeight + precisionWeight + timeWeight + fMeasureWeight;
            if (Math.abs(total - 1.0) > 0.001) {
            	Utils.showWeightErrorAlert("Weights must sum to 1.0. Current sum: " + String.format("%.3f", total));
                return;
            }
            
            // Validate weights are positive
            if (fitnessWeight < 0 || precisionWeight < 0 || timeWeight < 0 || fMeasureWeight < 0) {
                Utils.showWeightErrorAlert("All weights must be positive values.");
                return;
            }
            
        } catch (NumberFormatException e) {
            Utils.showWeightErrorAlert("Please enter valid numeric values for weights.");
            return;
        }
        
        if (generatedLog == null || generatedLog.isEmpty()) {
        	Utils.showErrorAlert("Generated log is empty or null.");
            return;
        }

        progressIndicator.setVisible(true);
        buttonBox.setDisable(true);
        algorithmResults.clear();
        resultsTableView.getItems().clear();

        boolean removeSilent = removeSilentCheckbox.isSelected();
        
        new Thread(() -> {
            try {
                List<String> algorithms = MiningAlgorithmSelector.getAvailableAlgorithms();
                
                for (String algorithmName : algorithms) {
                    AlgorithmResult result = runAlgorithmComparison(algorithmName, removeSilent);
                    algorithmResults.add(result);
                    
                    // Update UI on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        resultsTableView.getItems().add(result);
                    });
                }
                
                // Determine winner
                AlgorithmResult winner = determineWinner();
                
                javafx.application.Platform.runLater(() -> {
                    if (winner != null) {
                        bestAlgorithmLabel.setText("Best: " + winner.getAlgorithmName());
                        // Highlight winner in table
                        for (AlgorithmResult result : algorithmResults) {
                            result.setWinnerStatus(result.equals(winner) ? "WINNER" : "");
                        }
                        resultsTableView.refresh();
                    }
                    progressIndicator.setVisible(false);
                    buttonBox.setDisable(false);
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    Utils.showErrorMessage(null,null,"Error during comparison: " + e.getMessage(), e);
                    progressIndicator.setVisible(false);
                    buttonBox.setDisable(false);
                });
            }
        }).start();
    }

    private AlgorithmResult runAlgorithmComparison(String algorithmName, boolean removeSilent) {
        AlgorithmResult result = new AlgorithmResult(algorithmName);
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Get algorithm and mine model
            MiningAlgorithm algorithm = MiningAlgorithmSelector.getAlgorithm(algorithmName);
            Petrinet discoveredModel = algorithm.mine(pluginContext, generatedLog);
            
            // Apply silent transition removal if requested
            if (removeSilent && discoveredModel != null) {
                MurataReduction reducer = new MurataReduction(pluginContext);
                Marking initialMarking = new Marking(); // Empty marking for simplicity
                Petrinet reducedModel = reducer.removeSilentTransitions(discoveredModel, initialMarking);
                if (reducedModel != null) {
                    discoveredModel = reducedModel;
                    result.setReduced(true);
                }
            }
            
            long endTime = System.currentTimeMillis();
            result.setExecutionTime(endTime - startTime);
            
            if (discoveredModel != null) {
                // Set individual counts
                result.setPlacesCount(discoveredModel.getPlaces().size());
                result.setTransitionsCount(discoveredModel.getTransitions().size());
                result.setArcsCount(discoveredModel.getEdges().size());
                
                // Calculate all metrics
                double fitness = calculateFitness(discoveredModel);
                double precision = calculatePrecision(discoveredModel);
                double fMeasure = calculateFMeasure(discoveredModel);
//                double sizeComplexity = calculateSizeComplexity(discoveredModel);
                
                result.setFitnessScore((int) (fitness * 100));
                result.setPrecisionScore((int) (precision * 100));
                result.setFMeasureScore((int) (fMeasure * 100));
                //result.setSizeComplexityScore(sizeComplexity);
            }
            
        } catch (Exception e) {
            result.setError(e.getMessage());
        }
        
        return result;
    }
    

    private double calculateFMeasure(Petrinet discoveredModel) {
        try {
        	PetrinetWithMarking petrinetWithMarking = getPetrinetWithMarking(discoveredModel);
            if (petrinetWithMarking == null) {
                return Double.NaN;
            }
            
//            if (!isSound(petrinetWithMarking)) {
//                return Double.NaN;
//            }
            
            // Compute fitness and precision
            double fitness = calculateFitness(discoveredModel);
            double precision = calculatePrecision(discoveredModel);
            
            // Check for NaN values
            if (Double.isNaN(fitness) || Double.isNaN(precision)) {
                return Double.NaN;
            }
            
            // Calculate F-measure (harmonic mean of fitness and precision)
            if (fitness + precision == 0) {
                return 0.0; // Avoid division by zero
            }
            
            return (2 * (fitness * precision)) / (fitness + precision);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Double.NaN;
        }
    }
	
	private double calculateFitness(Petrinet discoveredModel) {
        try {
        	PetrinetWithMarking petrinetWithMarking = getPetrinetWithMarking(discoveredModel);
            if (petrinetWithMarking == null) {
                return Double.NaN;
            }
            
//            if (!isSound(petrinetWithMarking)) {
//                return Double.NaN;
//            }
            
            XEventClassifier xEventClassifier = new XEventNameClassifier();
            PNRepResult pnRepResult = computeAlignment(pluginContext, xEventClassifier, petrinetWithMarking, generatedLog);
            
            double fitness = getAlignmentValue(pnRepResult);
            return fitness;
        } catch (Exception e) {
            e.printStackTrace();
            return Double.NaN;
        }
    }

	private double calculatePrecision(Petrinet discoveredModel) {
	    try {
	    	PetrinetWithMarking petrinetWithMarking = getPetrinetWithMarking(discoveredModel);
	        if (petrinetWithMarking == null) {
	            return Double.NaN;
	        }
	        
//            if (!isSound(petrinetWithMarking)) {
//                return Double.NaN;
//            }
	        
	        XEventClassifier xEventClassifier = new XEventNameClassifier();
	        PNRepResult pnRepResult = computeAlignment(pluginContext, xEventClassifier, petrinetWithMarking, generatedLog);
	        
	        MultiETCPlugin multiETCPlugin = new MultiETCPlugin();
	        MultiETCSettings settings = new MultiETCSettings();
	        settings.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_1);
	        settings.put(MultiETCSettings.REPRESENTATION, MultiETCSettings.Representation.ORDERED);

	        Object[] res = multiETCPlugin.checkMultiETCAlign1(pluginContext, generatedLog, discoveredModel, settings, pnRepResult);
	        MultiETCResult multiETCResult = (MultiETCResult) res[0];
	        
	        double precision = (Double) multiETCResult.getAttribute(MultiETCResult.PRECISION);
	        return precision;
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        return Double.NaN;
	    }
	}
	
//	private double calculateSizeComplexity(Petrinet discoveredModel) {
//			PetrinetWithMarking petrinetWithMarking = getPetrinetWithMarking(discoveredModel);
//		    if (petrinetWithMarking == null) {
//		        return -1.0;
//		    }
//	        
//		    try {
//		        // Convert Petri net to BPMN diagram
//		        BPMNDiagram bpmn = PetriNetToBPMNConverter.convert(
//		            discoveredModel, 
//		            petrinetWithMarking.getInitialMarking(), 
//		            petrinetWithMarking.getFinalMarking(), 
//		            false
//		        );
//		       
//	        // Calculate size complexity using ComplexityCalculator
////	        ComplexityCalculator cc = new ComplexityCalculator(bpmn);
////	        double sizeComplexity = cc.computeSize();
//	        
//		   double sizeComplexity = 1.0;
//	        return sizeComplexity;
//	        
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        return -1.0;
//	    }
//	}
	
	
	private double getAlignmentValue(PNRepResult pnRepResult) {
	    int unreliable = 0;
	    if (pnRepResult == null) return Double.NaN;
	    
	    for (SyncReplayResult srp : pnRepResult) {
	        if (!srp.isReliable()) {
	            unreliable += srp.getTraceIndex().size();
	        }
	    }
	    
	    if (unreliable > pnRepResult.size() / 2) {
	        return Double.NaN;
	    } else {
	        return (Double) pnRepResult.getInfo().get(PNRepResult.TRACEFITNESS);
	    }
	}
	
//	  private boolean isSound(PetrinetWithMarking petrinetWithMarking) {
//	  if (petrinetWithMarking == null) return false;
//	  AcceptingPetriNet acceptingPetriNet = getAcceptingPetriNet(petrinetWithMarking);
//	  try {
//	      SoundnessChecker checker = new SoundnessChecker();
//	      return checker.isSound(acceptingPetriNet);
//	      
//	  } catch (Exception e) {
//	      return false;
//	  }
//	}
//	

	private AlgorithmResult determineWinner() {
        if (algorithmResults.isEmpty()) return null;
        
        AlgorithmResult best = algorithmResults.get(0);
        double bestScore = calculateOverallScore(best, true); // Debug for first
		
        
        for (int i = 1; i < algorithmResults.size(); i++) {
            AlgorithmResult current = algorithmResults.get(i);
            double currentScore = calculateOverallScore(current, false);
            
            if (currentScore > bestScore) {
                best = current;
                bestScore = currentScore;
            }
        }
        
        return best;
    }

    private double calculateOverallScore(AlgorithmResult result, boolean debug) {
        // Handle invalid results
        if (result.getFitnessScore() < 0 || result.getPrecisionScore() < 0 || result.getFMeasureScore() < 0) {
            if (debug) {
                System.out.println("=== DEBUG SCORE CALCULATION ===");
                System.out.println("Algorithm: " + result.getAlgorithmName() + " - INVALID SCORES");
                System.out.println("==============================");
            }
            return Double.NEGATIVE_INFINITY;
        }
        
        double fitnessScore = normalizeScore(result.getFitnessScore(), 0, 100, algorithmResults, "fitness");
        double precisionScore = normalizeScore(result.getPrecisionScore(), 0, 100, algorithmResults, "precision");
        double fMeasureScore = normalizeScore(result.getFMeasureScore(), 0, 100, algorithmResults, "fmeasure");
        double timeScore = normalizeTimeScore(result.getExecutionTime(), algorithmResults);
        
        // Calculate weighted sum
        double overallScore = (fitnessScore * fitnessWeight) + 
               (precisionScore * precisionWeight) + 
               (fMeasureScore * fMeasureWeight) + 
               (timeScore * timeWeight);
        
        // Debug output
        if (debug) {
            System.out.println("=== DEBUG SCORE CALCULATION ===");
            System.out.println("Algorithm: " + result.getAlgorithmName());
            System.out.println("Fitness: " + result.getFitnessScore());
            System.out.println("Precision: " + result.getPrecisionScore());
            System.out.println("F-Measure: " + result.getFMeasureScore());
            System.out.println("Time: " + result.getExecutionTime());
            
            System.out.println("Normalized Fitness: " + fitnessScore);
            System.out.println("Normalized Precision: " + precisionScore);
            System.out.println("Normalized F-Measure: " + fMeasureScore);
            System.out.println("Normalized Time: " + timeScore);
            
            System.out.println("Overall Score: " + overallScore);
            System.out.println("Weights - Fitness: " + fitnessWeight + ", Precision: " + precisionWeight + 
                              ", F-Measure: " + fMeasureWeight + ", Time: " + timeWeight);
            System.out.println("==============================");
        }
        
        result.setOverallScore(overallScore);
        return overallScore;
    }

    // Helper methods for normalisation
    private double normalizeScore(double value, double min, double max, 
                                 List<AlgorithmResult> allResults, String scoreType) {
        if (Double.isNaN(value)) return 0;
        
        // Use actual range from all results if available
        if (allResults != null && !allResults.isEmpty()) {
            double actualMin = Double.MAX_VALUE;
            double actualMax = -Double.MAX_VALUE;
            
            for (AlgorithmResult r : allResults) {
                double currentValue = 0;
                switch (scoreType) {
                    case "fitness": currentValue = r.getFitnessScore(); break;
                    case "precision": currentValue = r.getPrecisionScore(); break;
                    case "fmeasure": currentValue = r.getFMeasureScore(); break;
                }
                
                if (!Double.isNaN(currentValue)) {
                    if (currentValue < actualMin) actualMin = currentValue;
                    if (currentValue > actualMax) actualMax = currentValue;
                }
            }
            
            if (actualMax > actualMin) {
                return (value - actualMin) / (actualMax - actualMin);
            }
        }
        
        // Fallback to provided range
        return (value - min) / (max - min);
    }

    private double normalizeTimeScore(long executionTime, List<AlgorithmResult> allResults) {
        if (allResults == null || allResults.isEmpty()) {
            return Math.max(0, 1 - (executionTime / 10000.0));
        }
        
        // Create sorted list of times
        List<Long> times = new ArrayList<>();
        for (AlgorithmResult r : allResults) {
            times.add(r.getExecutionTime());
        }
        Collections.sort(times);
        
        // Calculate percentile-based score
        int rank = Collections.binarySearch(times, executionTime);
        if (rank < 0) rank = -rank - 1;
        
        return 1.0 - ((double) rank / times.size());
    }
   
    

    public static void showStatisticsWindow(File pnmlFile, Petrinet originalModel, XLog generatedLog, 
            MiningController miningController) {
        try {
            FXMLLoader loader = new FXMLLoader(StatisticsController.class.getResource("/view/statistics.fxml"));
            Parent root = loader.load();
            
            StatisticsController controller = loader.getController();
            controller.setData(pnmlFile, originalModel, generatedLog, miningController);
            
            Stage stage = new Stage();
            stage.setTitle("Algorithm Performance Statistics");
            
            // Create scene and add CSS
            Scene scene = new Scene(root);
            scene.getStylesheets().add(StatisticsController.class.getResource("/css/statistics.css").toExternalForm());
            
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            Utils.showErrorAlert("Error: Could not open statistics window: " + e.getMessage());
        }
    }
    

    public static class AlgorithmResult {
        private String algorithmName;
        private long executionTime;
        private int placesCount;
        private int transitionsCount;
        private int arcsCount;
        private int fitnessScore;
        private int precisionScore;
        private int fMeasureScore;
//        private double sizeComplexityScore;
        private String formattedOverallScore;
        private double overallScore;
        private String winnerStatus;
        private String error;
        private boolean reduced;

        public AlgorithmResult(String algorithmName) {
            this.algorithmName = algorithmName;
            this.winnerStatus = "";
            this.reduced = false;
//            this.sizeComplexityScore = -1; // Initialize as -1 for invalid
        }

        // Getters and setters
        public String getAlgorithmName() { 
            return algorithmName + (reduced ? " (Reduced)" : ""); 
        }
        
        public void setAlgorithmName(String algorithmName) { this.algorithmName = algorithmName; }
        
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        
        public int getPlacesCount() { return placesCount; }
        public void setPlacesCount(int placesCount) { this.placesCount = placesCount; }
        
        public int getTransitionsCount() { return transitionsCount; }
        public void setTransitionsCount(int transitionsCount) { this.transitionsCount = transitionsCount; }
        
        public int getArcsCount() { return arcsCount; }
        public void setArcsCount(int arcsCount) { this.arcsCount = arcsCount; }
        
        public int getFitnessScore() { return fitnessScore; }
        public void setFitnessScore(int fitnessScore) { this.fitnessScore = fitnessScore; }
        
        public int getPrecisionScore() { return precisionScore; }
        public void setPrecisionScore(int precisionScore) { this.precisionScore = precisionScore; }
        
        public int getFMeasureScore() { return fMeasureScore; }
        public void setFMeasureScore(int fMeasureScore) { this.fMeasureScore = fMeasureScore; }
        
//        public double getSizeComplexityScore() { return sizeComplexityScore; }
//        public void setSizeComplexityScore(double sizeComplexityScore) { this.sizeComplexityScore = sizeComplexityScore; }
//        public double getOverallScore() { return overallScore; }

        
        public double getOverallScore() { return overallScore; }
        
        public void setOverallScore(double overallScore) { 
            this.overallScore = overallScore;
            this.formattedOverallScore = String.format("%.2f", overallScore);
        }
        
        public String getFormattedOverallScore() {
            return formattedOverallScore;
        }
        
        public String getWinnerStatus() { return winnerStatus; }
        public void setWinnerStatus(String winnerStatus) { this.winnerStatus = winnerStatus; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public boolean isReduced() { return reduced; }
        public void setReduced(boolean reduced) { this.reduced = reduced; }
        
        
    }
    
    
    private PNRepResult computeAlignment(PluginContext pluginContext, XEventClassifier xEventClassifier, 
                                       PetrinetWithMarking petrinetWithMarking, XLog log) {
        if (petrinetWithMarking == null) return null;

        // Suppress output during computation
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
        }));

        try {
            Petrinet petrinet = petrinetWithMarking.getPetrinet();
            Marking initialMarking = petrinetWithMarking.getInitialMarking();
            Marking finalMarking = petrinetWithMarking.getFinalMarking();

            pluginContext.addConnection(new FinalMarkingConnection(petrinet, finalMarking));

            PetrinetReplayerWithILP replayer = new PetrinetReplayerWithILP();
            XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);

            Map<Transition, Integer> transitions2costs = constructTTCMap(petrinet);
            Map<XEventClass, Integer> events2costs = constructETCMap(petrinet, xEventClassifier, log, dummyEvClass);

            IPNReplayParameter parameters = constructParameters(transitions2costs, events2costs, petrinet, initialMarking, finalMarking);
            TransEvClassMapping mapping = constructMapping(petrinet, xEventClassifier, log, dummyEvClass);

            // Restore output for the actual computation
            System.setOut(originalOut);
            
            return replayer.replayLog(pluginContext, petrinet, log, mapping, parameters);
        } catch (AStarException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            // Always restore output
            System.setOut(originalOut);
        }

        return null;
    }

    private Map<Transition, Integer> constructTTCMap(Petrinet petrinet) {
        Map<Transition, Integer> transitions2costs = new UnifiedMap<Transition, Integer>();

        for (Transition t : petrinet.getTransitions()) {
            if (t.isInvisible()) {
                transitions2costs.put(t, 0);
            } else {
                transitions2costs.put(t, 1);
            }
        }
        return transitions2costs;
    }

    private Map<XEventClass, Integer> constructETCMap(Petrinet petrinet, XEventClassifier xEventClassifier, 
                                                     XLog log, XEventClass dummyEvClass) {
        Map<XEventClass, Integer> costMOT = new UnifiedMap<XEventClass, Integer>();
        XLogInfo summary = XLogInfoFactory.createLogInfo(log, xEventClassifier);

        for (XEventClass evClass : summary.getEventClasses().getClasses()) {
            int value = 1;
            for (Transition t : petrinet.getTransitions()) {
                if (t.getLabel().equals(evClass.getId())) {
                    value = 1;
                    break;
                }
            }
            costMOT.put(evClass, value);
        }

        costMOT.put(dummyEvClass, 1);
        return costMOT;
    }

    private IPNReplayParameter constructParameters(Map<Transition, Integer> transitions2costs, 
                                                  Map<XEventClass, Integer> events2costs, 
                                                  Petrinet petrinet, Marking initialMarking, 
                                                  Marking finalMarking) {
        IPNReplayParameter parameters = new CostBasedCompleteParam(events2costs, transitions2costs);

        parameters.setInitialMarking(initialMarking);
        parameters.setFinalMarkings(finalMarking);
        parameters.setGUIMode(false);
        parameters.setCreateConn(false);
        ((CostBasedCompleteParam) parameters).setMaxNumOfStates(Integer.MAX_VALUE);

        return parameters;
    }

    private TransEvClassMapping constructMapping(Petrinet net, XEventClassifier xEventClassifier, 
                                                XLog log, XEventClass dummyEvClass) {
        TransEvClassMapping mapping = new TransEvClassMapping(xEventClassifier, dummyEvClass);
        XLogInfo summary = XLogInfoFactory.createLogInfo(log, xEventClassifier);

        for (Transition t : net.getTransitions()) {
            boolean mapped = false;

            for (XEventClass evClass : summary.getEventClasses().getClasses()) {
                String id = evClass.getId();

                if (t.getLabel().equals(id)) {
                    mapping.put(t, evClass);
                    mapped = true;
                    break;
                }
            }

            if (!mapped) {
                mapping.put(t, dummyEvClass);
            }
        }

        return mapping;
    }
    
    private PetrinetWithMarking getPetrinetWithMarking(Petrinet discoveredModel) {
        try {
            Marking initialMarking = MarkingDiscoverer.constructInitialMarking(pluginContext, discoveredModel);
            Marking finalMarking = MarkingDiscoverer.constructFinalMarking(pluginContext, discoveredModel);
            
            if (initialMarking == null || finalMarking == null || initialMarking.isEmpty() || finalMarking.isEmpty()) {
                return null;
            }
            
            return new PetrinetWithMarking(discoveredModel, initialMarking, finalMarking);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    

    @FXML
    private void handleResetWeights() {
        fitnessWeightField.setText("0.4");
        precisionWeightField.setText("0.4");
        timeWeightField.setText("0.1");
        fMeasureWeightField.setText("0.1");
    }
    
    
    @FXML
    private void handleToggleWeights() {
        boolean visible = weightsContainer.isVisible();
        weightsContainer.setVisible(!visible);
        weightsContainer.setManaged(!visible);
    }
  
}