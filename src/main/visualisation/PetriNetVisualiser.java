package main.visualisation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Utility class for visualising Petri nets in DOT format.
 */
public class PetriNetVisualiser {
	
	/**
	 * Generates a DOT format string for visualizing the given Petri net.
	 * @param petriNet The Petri net to visualize.
	 * @param modelId  An identifier for the model, used in the graph name.
	 * @return A string in DOT format representing the Petri net.
	 */
	public static String getVisualisationString(Petrinet petriNet, String modelId) {
		System.out.println("\n=== Generating visualization for model: " + modelId + " ===");

		if (petriNet == null) {
			System.err.println("[ERROR] Petrinet is null!");
			return "";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("digraph " + modelId.replaceAll("[^a-zA-Z0-9]", "_") + " {"); // Sanitise modelId
		sb.append("rankdir=LR ");
		sb.append("graph [id=\"" + modelId + "\"]; ");

		try {

			// Debug model statistics
			Collection<Place> allPlaces = petriNet.getPlaces();
			Collection<Transition> allTransitions = petriNet.getTransitions();
			Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> allEdges = petriNet.getEdges();

			System.out.printf("[DEBUG] Model contains - Places: %d, Transitions: %d, Edges: %d\n", allPlaces.size(),
					allTransitions.size(), allEdges.size());

			// 1. Handle transitions
			System.out.println("[DEBUG] Processing transitions...");
			sb.append("node [shape=box];");
			int visibleCount = 0;
			for (Transition t : allTransitions) {
				if (!t.getLabel().isBlank() && !t.isInvisible()) {
					String cleanLabel = sanitizeLabel(t.getLabel());
					sb.append(cleanLabel + "; ");
					visibleCount++;
				}
			}
			System.out.printf("[DEBUG] Added %d visible transitions\n", visibleCount);

			// 2. Handle silent transitions
			sb.append("node [shape=rect, style=filled, fillcolor=black; width=0.15, label=\"\"]; ");
			HashMap<NodeID, String> silentTransMap = new HashMap<>();
			int silentCount = 0;
			for (Transition t : allTransitions) {
				if (t.isInvisible()) {
					String silentId = "t" + silentCount++;
					silentTransMap.put(t.getId(), silentId);
					sb.append(silentId + "; ");
				}
			}
			System.out.printf("[DEBUG] Added %d silent transitions\n", silentCount);

			// 3. Handle places
			System.out.println("[DEBUG] Processing places...");
			sb.append("node [shape=circle, fillcolor=white]; ");

			Map<String, String> placeNameMapping = new HashMap<>();
			int placeNumber = 1;
			for (Place p : allPlaces) {
				String newName = "p" + placeNumber++;
				placeNameMapping.put(p.getLabel(), newName);
				sb.append(newName).append(" [label=\"").append(newName).append("\"]; ");
				System.out.println("[PLACE] " + p.getLabel() + " → " + newName);
			}

			// 4. Handle edges (arcs)
			int edgeCount = 0;
			System.out.println("[DEBUG] Processing edges...");
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : allEdges) {
				String source = getNodeRepresentation(edge.getSource(), silentTransMap, placeNameMapping);
				String target = getNodeRepresentation(edge.getTarget(), silentTransMap, placeNameMapping);
				sb.append(source).append(" -> ").append(target).append(";");
			}
			System.out.printf("[DEBUG] Added %d edges\n", edgeCount);

			sb.append("}");

			System.out.println("[DEBUG] Generated DOT string:\n" + "digraph \"\" " + sb);

			// Print final DOT string
			String dotString = sb.toString().replace("'", "\\'");
			System.out.println("[DEBUG] Final DOT string: " + dotString);
			return dotString;

		} catch (Exception e) {
			System.err.println("[ERROR] Exception during visualization: " + e.getMessage());
			return "";
		}
	}
	
	/**
	 * Returns a string representation of a Petri net node for DOT format.
	 * @param node The Petri net node (Place or Transition).
	 * @param silentTransMap Mapping of silent transition IDs to their DOT names.
	 * @param placeNameMapping Mapping of original place labels to their new names.
	 * @return A string representing the node in DOT format.
	 */
	private static String getNodeRepresentation(PetrinetNode node, HashMap<NodeID, String> silentTransMap,
			Map<String, String> placeNameMapping) {
		try {
			if (node instanceof Transition && ((Transition) node).isInvisible()) {
				return silentTransMap.get(node.getId());
			}

			// Handle place renaming
			if (node instanceof Place) {
				return placeNameMapping.getOrDefault(node.getLabel(), node.getLabel());
			}

			// return node.getLabel();
			return sanitizeLabel(node.getLabel());
		} catch (Exception e) {
			System.err.println("[ERROR] Failed to process node: " + e.getMessage());
			return "error_node";
		}
	}
	
	/**
	 * Sanitizes a label by removing problematic characters for DOT format.
	 * @param label The original label.
	 * @return The sanitized label.
	 */
	private static String sanitizeLabel(String label) {
		if (label == null)
			return "";
		// Remove +complete suffix and replace problematic characters
		return label.replace("+complete", "").replace("+", "").replace("-", "");
	}

}