package main.algorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import processmining.splitminer.SplitMiner;
import processmining.splitminer.ui.dfgp.DFGPUIResult;
import java.util.HashMap;
import java.util.Map;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;

import org.processmining.models.graphbased.directed.petrinet.elements.Place;

public class SplitMinerAlgorithm implements MiningAlgorithm {
	
	private UIPluginContext context;
	private double frequencyThreshold;
	private double parallelismThreshold;
	private DFGPUIResult.FilterType filterType;
	private boolean replaceORs;
	private boolean removeLoopActivities;
	private boolean useInclusiveSemantics;

	public SplitMinerAlgorithm() {
		this( // default parameters
			0.4, // frequency threshold
			0.1, // parallelism threshold
			DFGPUIResult.FilterType.FWG, // filter type
			false, // replace ORs
			false, // remove loop activities
			false); // use inclusive semantics
	}

	public SplitMinerAlgorithm(double freqThreshold, double parallelThreshold, DFGPUIResult.FilterType filterType,
			boolean replaceORs, boolean removeLoopActivities, boolean useInclusiveSemantics) {
		this.frequencyThreshold = freqThreshold;
		this.parallelismThreshold = parallelThreshold;
		this.filterType = filterType;
		this.replaceORs = replaceORs;
		this.removeLoopActivities = removeLoopActivities;
		this.useInclusiveSemantics = useInclusiveSemantics;
	}

	@Override
	public Object getParameters() {
		// Return a new mutable HashMap
		Map<String, Object> params = new HashMap<>();
		params.put("frequencyThreshold", frequencyThreshold);
		params.put("parallelismThreshold", parallelismThreshold);
		params.put("filterType", filterType);
		params.put("replaceORs", replaceORs);
		params.put("removeLoopActivities", removeLoopActivities);
		params.put("useInclusiveSemantics", useInclusiveSemantics);
		return params;
	}

	@Override
	public void setParameters(Object parameters) {
		if (parameters instanceof Map) {
			Map<String, Object> params = (Map<String, Object>) parameters;
			if (params.containsKey("frequencyThreshold")) {
				this.frequencyThreshold = (double) params.get("frequencyThreshold");
			}
			if (params.containsKey("parallelismThreshold")) {
				this.parallelismThreshold = (double) params.get("parallelismThreshold");
			}
			if (params.containsKey("filterType")) {
				this.filterType = (DFGPUIResult.FilterType) params.get("filterType");
			}
			if (params.containsKey("replaceORs")) {
				this.replaceORs = (boolean) params.get("replaceORs");
			}
			if (params.containsKey("removeLoopActivities")) {
				this.removeLoopActivities = (boolean) params.get("removeLoopActivities");
			}
			if (params.containsKey("useInclusiveSemantics")) {
				this.useInclusiveSemantics = (boolean) params.get("useInclusiveSemantics");
			}
		} else {
			throw new IllegalArgumentException("Parameters must be of type Map<String, Object>");
		}
	}

	@Override
	public String getAlgorithmName() {
		return "Split Miner";
	}
	
	@Override
    public UIPluginContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(UIPluginContext context) {
        this.context = context;
    }

	@Override
	public Petrinet mine(UIPluginContext context, XLog log) throws Exception {
		try {
			SplitMiner splitMiner = new SplitMiner();
			XEventClassifier classifier = new XEventNameClassifier();
			BPMNDiagram bpmn = splitMiner.mineBPMNModel(log, classifier, frequencyThreshold, parallelismThreshold,
					filterType, replaceORs, removeLoopActivities, useInclusiveSemantics, null);

            
			// Debug: Print BPMN info
			System.out.println("BPMN Nodes: " + bpmn.getNodes().size());
			System.out.println("BPMN Edges: " + bpmn.getEdges().size());

			Petrinet net = convertBPMNtoPetriNet(bpmn);

			// Debug: Print Petri net info
			System.out.println("PetriNet Places: " + net.getPlaces().size());
			System.out.println("PetriNet Transitions: " + net.getTransitions().size());
			System.out.println("PetriNet Edges: " + net.getEdges().size());

			// Print some transition labels
			int count = 0;
			for (Transition t : net.getTransitions()) {
				if (count++ < 5) { // Print first 5 transitions
					System.out.println("Transition: " + t.getLabel() + " (invisible: " + t.isInvisible() + ")");
				}
			}

			return net;
		} catch (Exception e) {
			throw new Exception("Conversion failed: " + e.getMessage(), e);
		}
	}
	
	private Petrinet convertBPMNtoPetriNet(BPMNDiagram bpmn) {
		try {
			Object[] result = BPMNToPetriNetConverter.convert(bpmn);

			if (result == null || result.length == 0 || !(result[0] instanceof Petrinet)) {
				throw new Exception("Invalid conversion result");
			}

			Petrinet net = (Petrinet) result[0];

			// Ensure all gateway transitions are silent
			for (Transition t : net.getTransitions()) {
				if (t.getLabel() != null && (t.getLabel().contains("Gateway") || t.getLabel().contains("XOR")
						|| t.getLabel().contains("AND") || t.getLabel().contains("OR"))) {
					t.setInvisible(true);
				}
			}

			for (Place place : net.getPlaces()) {
				if (place.getLabel() == null || place.getLabel().isEmpty()) {
					// Assign a simple label if none exists
					place.getAttributeMap().put("ProM_Vis_attr_label", "p_tmp" + place.getId().toString());

				}
			}
			
			System.out.println("Converted Petri net has:");
			System.out.println("- Places: " + net.getPlaces().size());
			System.out.println("- Transitions: " + net.getTransitions().size());
			System.out.println("- Edges: " + net.getEdges().size());

			return net;
		} catch (Exception e) {
			throw new RuntimeException("BPMN to Petri net conversion failed: " + e.getMessage(), e);
		}
	}

}