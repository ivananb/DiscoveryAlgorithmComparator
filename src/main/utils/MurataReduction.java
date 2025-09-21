package main.utils;

import java.util.Map;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.reduction.Murata;
import org.processmining.plugins.petrinet.reduction.MurataInput;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.reduction.MurataOutput;
import org.processmining.plugins.petrinet.reduction.MurataParameters;

public class MurataReduction {
    private final UIPluginContext pluginContext;
    private Petrinet originalModel;
    private String fallbackDotGraph;

    public MurataReduction(UIPluginContext pluginContext) {
        this.pluginContext = pluginContext;
    }

    public void setOriginalModel(Petrinet originalModel) {
        this.originalModel = originalModel;
    }

    public Petrinet removeSilentTransitions(Petrinet model, Marking initialMarking) {
        try {
            // Create Murata instance
            Murata murata = new Murata();
            
            // Create input with the model and marking
            MurataInput input = new MurataInput(model, initialMarking);
            
            // Set visible transitions as sacred (don't reduce them)
            input.setVisibleSacred(model);
            
            // Configure parameters - preserve behaviour
            MurataParameters parameters = new MurataParameters();
            parameters.setAllowFPTSacredNode(false);
            
            // Apply Murata reduction 
            MurataOutput output;
            if (pluginContext != null) {
                try {
                    output = murata.run(pluginContext, input, parameters);
                } catch (NullPointerException e) {
                    // Fallback: use null context if plugin context causes issues
                    output = murata.run(null, input, parameters);
                }
            } else {
                output = murata.run(null, input, parameters);
            }
            
            
            return output.getNet();
            
        } catch (Exception e) {
            System.err.println("[ERROR] Murata reduction failed: " + e.getMessage());
            e.printStackTrace();
            
            // Generate fallback graph from original model
            fallbackDotGraph = getFallbackDotGraph();
            return null;
        }
    }
    
    public String getStatistics(Petrinet originalModel, Petrinet discoveredModel, Petrinet reducedModel) {
        if (originalModel == null || discoveredModel == null || reducedModel == null) {
            return "Statistics unavailable: null model provided\n";
        }
        
        Map<String, Integer> origStats = Utils.getNetStatistics(originalModel);
        Map<String, Integer> discStats = Utils.getNetStatistics(discoveredModel);
        Map<String, Integer> reducedStats = Utils.getNetStatistics(reducedModel);
        
        StringBuilder stats = new StringBuilder();
        
        //Original vs Discovered
        stats.append("=== Model Comparison ===\n\n");
        stats.append("Reference Model (Original) vs Discovered Model\n\n");
        
        stats.append(String.format("%-15s | %-10s | %-10s%n", "Component", "Original", "Discovered"));
        stats.append("--------------------------------------\n");
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Places", origStats.get("places"), discStats.get("places")));
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Transitions", origStats.get("visibleTransitions"), discStats.get("visibleTransitions")));
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Arcs", origStats.get("edges"), discStats.get("edges")));
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Silent Trans", origStats.get("silentTransitions"), discStats.get("silentTransitions")));
        
        boolean equivalent1 = origStats.get("places").equals(discStats.get("places")) &&
                             origStats.get("visibleTransitions").equals(discStats.get("visibleTransitions")) &&
                             origStats.get("edges").equals(discStats.get("edges"));
        
        stats.append("\nComparison Result: ").append(
                equivalent1 ? "✔ Models are structurally equivalent\n" : "❌ Models have structural differences\n");
        
        // Discovered vs Reduced (Murata reduction results)
        stats.append("\n" + "=".repeat(50) + "\n");
        stats.append("MURATA REDUCTION RESULTS\n");
        stats.append("=".repeat(50) + "\n\n");
        
        stats.append("Discovered Model vs Reduced Model\n\n");
        stats.append(String.format("%-15s | %-10s | %-10s%n", "Component", "Discovered", "Reduced"));
        stats.append("--------------------------------------\n");
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Places", discStats.get("places"), reducedStats.get("places")));
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Transitions", discStats.get("visibleTransitions"), reducedStats.get("visibleTransitions")));
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Arcs", discStats.get("edges"), reducedStats.get("edges")));
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Silent Trans", discStats.get("silentTransitions"), reducedStats.get("silentTransitions")));
        
        //Reduction Summary
        int placesRemoved = discStats.get("places") - reducedStats.get("places");
        int transitionsRemoved = discStats.get("transitions") - reducedStats.get("transitions");
        int arcsRemoved = discStats.get("edges") - reducedStats.get("edges");
        int silentRemoved = discStats.get("silentTransitions") - reducedStats.get("silentTransitions");
        
        stats.append("\n=== Reduction Summary ===\n");
        stats.append(String.format("Places removed:      %d (%.1f%%)%n", placesRemoved, 
                Utils.calculatePercentage(discStats.get("places"), reducedStats.get("places"))));
        stats.append(String.format("Transitions removed: %d (%.1f%%)%n", transitionsRemoved,
                Utils.calculatePercentage(discStats.get("transitions"), reducedStats.get("transitions"))));
        stats.append(String.format("Arcs removed:        %d (%.1f%%)%n", arcsRemoved,
                Utils.calculatePercentage(discStats.get("edges"), reducedStats.get("edges"))));
        stats.append(String.format("Silent transitions:  %d removed (100.0%%)%n", silentRemoved));
        
        //Comparison: Original vs Reduced
        stats.append("\n" + "=".repeat(50) + "\n");
        stats.append("FINAL COMPARISON: Original vs Reduced\n");
        stats.append("=".repeat(50) + "\n\n");
        
        stats.append(String.format("%-15s | %-10s | %-10s%n", "Component", "Original", "Reduced"));
        stats.append("--------------------------------------\n");
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Places", origStats.get("places"), reducedStats.get("places")));
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Transitions", origStats.get("visibleTransitions"), reducedStats.get("visibleTransitions")));
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Arcs", origStats.get("edges"), reducedStats.get("edges")));
        stats.append(String.format("%-15s | %-10d | %-10d%n", "Silent Trans", origStats.get("silentTransitions"), reducedStats.get("silentTransitions")));
        
        boolean equivalentFinal = origStats.get("places").equals(reducedStats.get("places")) &&
                                 origStats.get("visibleTransitions").equals(reducedStats.get("visibleTransitions")) &&
                                 origStats.get("edges").equals(reducedStats.get("edges"));
        
        stats.append("\nFinal Result: ").append(
                equivalentFinal ? "✔ Reduced model matches original structure!\n" : 
                                 "❌ Structural differences remain after reduction\n");
        
        return stats.toString();
    }

    public String getFallbackDotGraph() {
        return fallbackDotGraph != null ? fallbackDotGraph : 
               "digraph G { node [shape=circle]; label=\"No fallback graph available\"; }";
    }
}