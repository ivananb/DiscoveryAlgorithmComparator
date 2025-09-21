package main.algorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.HeuristicsMiner;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;
import org.processmining.plugins.heuristicsnet.miner.heuristics.converter.HeuristicsNetToPetriNetConverter;
import org.processmining.models.heuristics.HeuristicsNet;

public class HeuristicMinerAlgorithm implements MiningAlgorithm {

	private HeuristicsMinerSettings settings;
	private UIPluginContext context;

	public HeuristicMinerAlgorithm() {
		this.settings = createDefaultParameters();
	}

	private HeuristicsMinerSettings createDefaultParameters() {
		HeuristicsMinerSettings settings = new HeuristicsMinerSettings();
		settings.setClassifier(new XEventNameClassifier());
		settings.setDependencyThreshold(0.9);
		settings.setAndThreshold(0.65);
		settings.setL2lThreshold(0.85);
		settings.setUseAllConnectedHeuristics(true);
		settings.setRelativeToBestThreshold(0.2);
		settings.setPositiveObservationThreshold(1);
		settings.setLongDistanceThreshold(0.8);
		settings.setUseLongDistanceDependency(true);
		return settings;
	}

	@Override
	public String getAlgorithmName() {
		return "Heuristics Miner";
	}

	@Override
	public Object getParameters() {
		return this.settings;
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
	public void setParameters(Object parameters) {
		if (parameters instanceof HeuristicsMinerSettings) {
			this.settings = (HeuristicsMinerSettings) parameters;
		} else {
			throw new IllegalArgumentException("Parameters must be of type HeuristicsMinerSettings");
		}
	}

	@Override
	public Petrinet mine(UIPluginContext context, XLog log) throws Exception {
		// Basic validation
		if (log == null)
			throw new IllegalArgumentException("Event log cannot be null");
		if (log.isEmpty())
			throw new IllegalArgumentException("Event log is empty");

		try {
			// 1. Set up classifier - using standard name classifier
			XEventClassifier classifier = new XEventNameClassifier();

			// 2. Create log info with proper event classes
			XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, classifier);
			if (logInfo.getEventClasses(classifier) == null) {
				throw new Exception("Failed to create event classes for the log");
			}

			// 3. Configure settings with the classifier
			HeuristicsMinerSettings settings = new HeuristicsMinerSettings();
			settings.setClassifier(classifier);
			configureGatewaySettings(settings);

			// 4. Run Heuristics Miner with all required parameters
			HeuristicsMiner miner = new HeuristicsMiner(context, log, logInfo, settings);
			HeuristicsNet heuristicsNet = miner.mine();

			if (heuristicsNet == null) {
				throw new Exception("Failed to mine Heuristics Net");
			}

            // 5. Convert to Petri net
            Petrinet net = convertHeuristicsNetToPetriNet(context, heuristicsNet);
            
            // 6. Return the Petri net
            return net;

		} catch (Exception e) {
			throw new Exception("Mining failed: " + e.getMessage(), e);
		}
	}

	private void configureGatewaySettings(HeuristicsMinerSettings settings) {
		settings.setDependencyThreshold(0.9);
		settings.setAndThreshold(0.65);
		settings.setL2lThreshold(0.85);
		settings.setUseAllConnectedHeuristics(true);
		settings.setRelativeToBestThreshold(0.2);
	}

	private Petrinet convertHeuristicsNetToPetriNet(UIPluginContext context, HeuristicsNet heuristicsNet)
			throws Exception {
		Object[] result = HeuristicsNetToPetriNetConverter.converter(context, heuristicsNet);

		if (result == null || result.length == 0 || !(result[0] instanceof Petrinet)) {
			throw new Exception("Conversion to Petri net failed");
		}

		return (Petrinet) result[0];
	}

}