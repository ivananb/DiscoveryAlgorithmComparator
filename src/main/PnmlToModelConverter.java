package main;

import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XesXmlParser;
import main.algorithms.MiningAlgorithm;
import main.algorithms.MiningAlgorithmSelector;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.processmining.plugins.stochasticpetrinet.simulator.PNSimulator;
import org.processmining.plugins.stochasticpetrinet.simulator.PNSimulatorConfig;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import main.utils.Utils;

/**
 * Utility class for converting PNML files to Petri net models and generating event logs.
 */
public class PnmlToModelConverter {

	public Petrinet loadPetriNetFromPnml(File pnmlFile) throws Exception {
		PetrinetImpl net = new PetrinetImpl(pnmlFile.getName());
		Map<String, Place> places = new HashMap<>();
		Map<String, Transition> transitions = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(pnmlFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// Parse places
				if (line.contains("<place id=\"")) {
					String id = extractAttribute(line, "id");
					String name = extractName(reader);
					Place p = net.addPlace(name != null ? name : id);
					places.put(id, p);
				}
				// Parse transitions
				else if (line.contains("<transition id=\"")) {
					String id = extractAttribute(line, "id");
					String name = extractName(reader);
					Transition t = net.addTransition(name != null ? name : id);
					transitions.put(id, t);
				}
				// Parse arcs
				else if (line.contains("<arc ")) {
					String source = extractAttribute(line, "source");
					String target = extractAttribute(line, "target");

					if (places.containsKey(source) && transitions.containsKey(target)) {
						net.addArc(places.get(source), transitions.get(target));
					} else if (transitions.containsKey(source) && places.containsKey(target)) {
						net.addArc(transitions.get(source), places.get(target));
					}
				}
			}

			System.out.println("Loaded Petri net with:");
			System.out.println("- Places: " + places.size());
			System.out.println("- Transitions: " + transitions.size());
			return net;
		} catch (Exception e) {
			throw new Exception("Failed to parse PNML file: " + e.getMessage(), e);
		}
	}

	private String extractAttribute(String line, String attrName) {
		Pattern pattern = Pattern.compile(attrName + "=\"([^\"]+)\"");
		Matcher matcher = pattern.matcher(line);
		return matcher.find() ? matcher.group(1) : null;
	}

	private String extractName(BufferedReader reader) throws Exception {
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.contains("<text>")) {
				return line.replace("<text>", "").replace("</text>", "").trim();
			} else if (line.contains("</name>")) {
				break;
			}
		}
		return null;
	}

	public XLog generateLogFromPnml(Petrinet model, File pnmlFile, int numberOfTraces, int maxTraceLength)
			throws Exception {
		UIPluginContext context = new UIContext().getMainPluginContext();
		Marking initialMarking = parseInitialMarking(pnmlFile, model);

		return simulateWithStochasticPlugin(context, model, initialMarking, numberOfTraces, maxTraceLength); // stochastic
																												// plugin
																												// simulation
	}
	
	/**
	 * Simulates the given Petri net model using the Stochastic Petri Net plugin to generate an event log.
	 *
	 * @param context         The UI plugin context.
	 * @param model           The Petri net model to simulate.
	 * @param initialMarking  The initial marking for the simulation.
	 * @param numberOfTraces  The number of traces to generate.
	 * @param maxTraceLength  The maximum length of each trace.
	 * @return The generated event log as an XLog object.
	 * @throws Exception If the simulation fails.
	 */
	private XLog simulateWithStochasticPlugin(UIPluginContext context, Petrinet model, Marking initialMarking,
			int numberOfTraces, int maxTraceLength) throws Exception {
		try {
			PNSimulator simulator = new PNSimulator();
			PNSimulatorConfig config = new PNSimulatorConfig(numberOfTraces);

			// Get markings with non-lambda approach
			Marking finalMarking = StochasticNetUtils.getFinalMarking(context, model);
			if (finalMarking == null) {
				finalMarking = new Marking();
			}

			Marking initialMarking2 = StochasticNetUtils.getInitialMarking(context, model);
			if (initialMarking2 == null) {
				initialMarking2 = detectInitialMarking(model);
			}

			// Debug output
			System.out.println("Initial marking places:");
			for (Place p : initialMarking2.baseSet()) {
				System.out.println("- " + p.getLabel() + ": " + initialMarking2.occurrences(p));
			}

			XLog log = simulator.simulate(context, model, StochasticNetUtils.getSemantics(model), config,
					initialMarking2, finalMarking);

			return limitTraceLengths(log, maxTraceLength);
		} catch (Exception e) {
			throw new Exception("Stochastic simulation failed: " + e.getMessage(), e);
		}
	}

	private XLog limitTraceLengths(XLog log, int maxLength) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog limitedLog = factory.createLog(log.getAttributes());

		for (XTrace trace : log) {
			if (trace.size() <= maxLength) {
				limitedLog.add(trace);
			} else {
				XTrace shortened = factory.createTrace(trace.getAttributes());
				for (int i = 0; i < maxLength && i < trace.size(); i++) {
					shortened.add(trace.get(i));
				}
				limitedLog.add(shortened);
			}
		}
		return limitedLog;
	}

	// Helper method to detect initial marking from net structure
	private Marking detectInitialMarking(Petrinet net) {
		Marking marking = new Marking();

		// Iterate through all places in the net
		for (Place place : net.getPlaces()) {
			// Check if this place has no incoming edges (source place)
			if (net.getInEdges(place).isEmpty()) {
				// Add one token to this place
				marking.add(place, 1);
			}
		}
		return marking;
	}

	private Marking parseInitialMarking(File pnmlFile, Petrinet net) throws Exception {
		Marking marking = new Marking();
		return marking;
	}

	public String generateModelStatistics(XLog eventLog, String algorithmName) throws Exception {
		UIPluginContext context = createPluginContext();
		MiningAlgorithm algorithm = MiningAlgorithmSelector.getAlgorithm(algorithmName);

		Petrinet petriNet = algorithm.mine(context, eventLog);
		return formatModelReport(petriNet, algorithmName);
	}

	// Creates a new plugin context for mining operations
	private UIPluginContext createPluginContext() {
		return new UIContext().getMainPluginContext();
	}

	public static XLog convertToXlog(File logFile) {
		try {
			// Handles XES format
			if (logFile.getName().toLowerCase().endsWith(".xes")) {
				XesXmlParser parser = new XesXmlParser();
				if (parser.canParse(logFile)) {
					return parser.parse(logFile).get(0);
				}
			}
			// Handles MXML format
			else if (logFile.getName().toLowerCase().endsWith(".mxml")) {
				XMxmlParser parser = new XMxmlParser();
				if (parser.canParse(logFile)) {
					return parser.parse(logFile).get(0);
				}
			}

		} catch (Exception e) {
			System.err.println("Error parsing log file:");
			e.printStackTrace();
		}
		return null; // If parsing fails
	}

	public Petrinet mineModelFromLog(XLog log, String algorithmName) throws Exception {
		UIPluginContext context = new UIContext().getMainPluginContext();
		MiningAlgorithm algorithm = MiningAlgorithmSelector.getAlgorithm(algorithmName);
		return algorithm.mine(context, log);
	}

	// for xes log
	private String formatModelReport(Petrinet petriNet, String algorithmName) {
		int visibleTransitions = Utils.countVisibleTransitions(petriNet);
		int visibleArcs = Utils.countArcs(petriNet);
		

		return String.format(
				"Process Discovery Results using %s:\n" + "=================================\n" + "Model Structure:\n"
						+ "• Places (states): %d\n" + "• Visible Transitions: %d\n" + "• Visible Connections: %d\n"
						+ "=================================\n",
				algorithmName, petriNet.getPlaces().size(), visibleTransitions, visibleArcs);
	}
	
	public String compareModels(Petrinet referenceModel, Petrinet discoveredModel) {
	    Map<String, Integer> refStats = Utils.getNetStatistics(referenceModel);
	    Map<String, Integer> discStats = Utils.getNetStatistics(discoveredModel);
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("=== Model Comparison ===\n\n");
	    sb.append("Reference Model (Original) vs Discovered Model\n\n");
	    
	    sb.append(String.format("%-15s | %-10s | %-10s%n", "Component", "Original", "Discovered"));
	    sb.append("--------------------------------------\n");
	    sb.append(String.format("%-15s | %-10d | %-10d%n", "Places", refStats.get("places"), discStats.get("places")));
	    sb.append(String.format("%-15s | %-10d | %-10d%n", "Transitions", refStats.get("visibleTransitions"), discStats.get("visibleTransitions")));
	    sb.append(String.format("%-15s | %-10d | %-10d%n", "Arcs", refStats.get("edges"), discStats.get("edges")));
	    sb.append(String.format("%-15s | %-10d | %-10d%n", "Silent Trans", refStats.get("silentTransitions"), discStats.get("silentTransitions")));
	    
	    boolean equivalent = refStats.get("places").equals(discStats.get("places")) &&
	                        refStats.get("visibleTransitions").equals(discStats.get("visibleTransitions")) &&
	                        refStats.get("edges").equals(discStats.get("edges"));
	    
	    sb.append("\nComparison Result: ").append(
	            equivalent ? "✔ Models are structurally equivalent\n" : "[X] Models have structural differences\n");
	    
	    return sb.toString();
	}

	public XLog importXesLog(File xesFile) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		if (parser.canParse(xesFile)) {
			return parser.parse(xesFile).get(0);
		} else {
			throw new Exception("Cannot parse the provided XES file: " + xesFile.getName());
		}

	}
}