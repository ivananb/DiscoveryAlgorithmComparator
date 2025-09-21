package main.algorithms;


import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

public class InductiveMinerAlgorithm implements MiningAlgorithm {
	private MiningParameters parameters;
	private UIPluginContext context;
	
	@Override
	public String getAlgorithmName() {
		return "Inductive Miner";
	}

	@Override
	public Object getParameters() {
		return this.parameters; // Return the stored parameters
	}

	@Override
	public void setParameters(Object parameters) {
		if (parameters instanceof MiningParameters) {
			this.parameters = (MiningParameters) parameters;
		}
	}
	
	@Override
    public UIPluginContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(UIPluginContext context) {
        this.context = context;
    }

	public InductiveMinerAlgorithm() {
		this.parameters = createDefaultParameters();
	}

	private MiningParameters createDefaultParameters() {
		MiningParameters params = new MiningParametersIM();
		params.setNoiseThreshold(0.2f); // Default value
		params.setUseMultithreading(false);
		return params;
	}

	@Override
	public Petrinet mine(UIPluginContext context, XLog log) throws Exception {
		if (log == null || log.isEmpty()) {
			throw new IllegalArgumentException("Event log cannot be null or empty");
		}

		try {
			// Convert log to IMLog
			IMLog imLog = new IMLogImpl(log, parameters.getClassifier(), parameters.getLifeCycleClassifier());

			// Mine process tree
			context.getProgress().setIndeterminate(false);
			context.getProgress().setMinimum(0);
			context.getProgress().setMaximum(3);
			context.getProgress().setCaption("Mining with Inductive Miner");

			context.getProgress().inc();
			ProcessTree tree = IMProcessTree.mineProcessTree(imLog, parameters);

			if (tree == null) {
				throw new Exception("Mining failed - no process tree returned");
			}

			context.getProgress().inc();
			// Convert ProcessTree to Petrinet
			PetrinetWithMarkings conversionResult = ProcessTree2Petrinet.convert(tree);

			if (conversionResult == null || conversionResult.petrinet == null) {
				throw new Exception("Conversion failed - no Petri net produced");
			}

			Petrinet net = conversionResult.petrinet;
			context.getProgress().inc(); // Increment progress after conversion


			System.out.println("Mined Petri net with: " + net.getPlaces().size() + " places, "
					+ net.getTransitions().size() + " transitions, " + net.getEdges().size() + " arcs");

			return net;
		} catch (Exception e) {
			context.log("Inductive Miner failed: " + e.getMessage());
			throw new Exception("Inductive Miner failed: " + e.getMessage(), e);
		}
	}

}