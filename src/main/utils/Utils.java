package main.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import java.util.HashMap;
import java.util.Map;

public class Utils {

	public static void showErrorMessage(Label statusLabel, TextArea outputArea, String message, Exception e) {
		// Update status label
		statusLabel.setText("Error: " + message);

		// Build error message for text area
		StringBuilder errorMessage = new StringBuilder("\nERROR: " + message);

		if (e != null) {
			errorMessage.append(", ").append(e.getClass().getSimpleName()).append("\n ")
					.append(e.getMessage() != null ? e.getMessage() : "No details available").append("\n");
			e.printStackTrace();
		} else {
			errorMessage.append("\n");
		}

		outputArea.appendText(errorMessage.toString());
	}
   

	
	public static int parseIntWithDefault(String input, int defaultValue) {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	
	/**
     * Counts silent transitions in a Petri net
     */
    public static int countSilentTransitions(Petrinet net) {
        if (net == null) return 0;
        int count = 0;
        for (Transition transition : net.getTransitions()) {
            if (isSilentTransition(transition)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Checks if a transition is silent
     */
    public static boolean isSilentTransition(Transition transition) {
        if (transition == null) return false;
        String label = transition.getLabel();
        if (label == null) return transition.isInvisible();
        
        String lowerLabel = label.toLowerCase().trim();
        return transition.isInvisible() || 
               lowerLabel.startsWith("tau") ||
               lowerLabel.startsWith("silent") ||
               lowerLabel.matches("t\\d+") ||
               lowerLabel.trim().isEmpty();
    }
    
    /**
     * Counts visible transitions
     */
    public static int countVisibleTransitions(Petrinet net) {
        if (net == null) return 0;
        int count = 0;
        for (Transition transition : net.getTransitions()) {
            if (!isSilentTransition(transition)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Counts edges connected to visible transitions
     */
    public static int countArcs(Petrinet net) {
        if (net == null) return 0;
        int count = 0;
        for (Transition transition : net.getTransitions()) {
            if (!isSilentTransition(transition)) {
                count += net.getInEdges(transition).size();
                count += net.getOutEdges(transition).size();
            }
        }
        return count;
    }
    
    /**
     * Calculates percentage difference
     */
    public static double calculatePercentage(int original, int changed) {
        if (original == 0) return 0;
        return ((double) (changed - original) / original) * 100;
    }
	
	
	/**
     * Gets comprehensive statistics for a Petri net
     */
	public static Map<String, Integer> getNetStatistics(Petrinet net) {
        Map<String, Integer> stats = new HashMap<>();
        if (net == null) {
            stats.put("places", 0);
            stats.put("transitions", 0);
            stats.put("visibleTransitions", 0);
            stats.put("silentTransitions", 0);
            stats.put("edges", 0);
            stats.put("visibleEdges", 0);
            return stats;
        }
        
        stats.put("places", net.getPlaces().size());
        stats.put("transitions", net.getTransitions().size());
        stats.put("visibleTransitions", countVisibleTransitions(net));
        stats.put("silentTransitions", countSilentTransitions(net));
        stats.put("edges", net.getEdges().size());
        stats.put("visibleEdges", countArcs(net));
        
        return stats;
    }
    
    /**
     * Creates a formatted statistics string
     */
    public static String formatStatistics(Petrinet net, String title) {
        Map<String, Integer> stats = getNetStatistics(net);
        
        return String.format(
                "%s:\n" +
                "===============\n" +
                "Places:           %d\n" +
                "Transitions:      %d\n" +
                "- Visible:        %d\n" +
                "- Silent:         %d\n" +
                "Edges:            %d\n" +
                "Visible Edges:    %d\n" +
                "Visible Places:   %d\n",
                title,
                stats.get("places"),
                stats.get("transitions"),
                stats.get("visibleTransitions"),
                stats.get("silentTransitions"),
                stats.get("edges"),
                stats.get("visibleEdges"),
                stats.get("visiblePlaces")
        );
    }
    
    /**
     * Compares two Petri nets and returns difference statistics
     */
    public static Map<String, Object> compareNets(Petrinet net1, Petrinet net2) {
        Map<String, Integer> stats1 = getNetStatistics(net1);
        Map<String, Integer> stats2 = getNetStatistics(net2);
        
        Map<String, Object> comparison = new HashMap<>();
        
        // Absolute differences
        comparison.put("placesDiff", stats2.get("places") - stats1.get("places"));
        comparison.put("transitionsDiff", stats2.get("transitions") - stats1.get("transitions"));
        comparison.put("visibleTransitionsDiff", stats2.get("visibleTransitions") - stats1.get("visibleTransitions"));
        comparison.put("silentTransitionsDiff", stats2.get("silentTransitions") - stats1.get("silentTransitions"));
        comparison.put("edgesDiff", stats2.get("edges") - stats1.get("edges"));
        
        // Percentage differences
        comparison.put("placesPercent", calculatePercentage(stats1.get("places"), stats2.get("places")));
        comparison.put("transitionsPercent", calculatePercentage(stats1.get("transitions"), stats2.get("transitions")));
        comparison.put("silentTransitionsPercent", calculatePercentage(stats1.get("silentTransitions"), stats2.get("silentTransitions")));
        comparison.put("edgesPercent", calculatePercentage(stats1.get("edges"), stats2.get("edges")));
        
        return comparison;
    }
    
    /**
	 * Displays an alert for weight validation errors
	 * @param message
	 */
    public static void showWeightErrorAlert(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Weights");
            alert.setHeaderText("Weight Validation Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
	/**
	 * Displays a general error alert
	 * @param message
	 */
    public static void showErrorAlert(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

}