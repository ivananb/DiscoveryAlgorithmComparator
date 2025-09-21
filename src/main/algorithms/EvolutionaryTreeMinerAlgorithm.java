package main.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.plugins.etm.ETM;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.plugins.etm.parameters.ETMParam;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import main.controller.settings.ETMParameterStorage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class EvolutionaryTreeMinerAlgorithm implements MiningAlgorithm {

	private UIPluginContext context;
	private static final String NATIVE_LIB_DIR = "C:/Users/ivana/Projects/Tesi/Mining/lib/win64/";
    
    static {
        loadNativeLibraries();
    }
    
    private static void loadNativeLibraries() {
        loadLibrary("lpsolve55");
        loadLibrary("bfp_etaPFI");
        loadLibrary("bfp_GLPK");
        loadLibrary("bfp_LUSOL");
//        loadLibrary("xli_CPLEX");
//        loadLibrary("xli_DIMACS");
//        loadLibrary("xli_LINDO");
//        loadLibrary("xli_MathProg");
//        loadLibrary("xli_XPRESS");
    }
    
    private static void loadLibrary(String libName) {
        try {
            // First try loading from java.library.path
            System.loadLibrary(libName);
            System.out.println("Successfully loaded library: " + libName);
        } catch (UnsatisfiedLinkError e) {
            // Fall back to loading from our known directory
            try {
                String fullPath = NATIVE_LIB_DIR + libName + ".dll";
                System.load(fullPath);
                System.out.println("Successfully loaded library from application path: " + fullPath);
            } catch (UnsatisfiedLinkError e2) {
                System.err.println("Failed to load library: " + libName);
                System.err.println("Error: " + e2.getMessage());
                System.err.println("Java library path: " + System.getProperty("java.library.path"));
                throw new RuntimeException("Native library loading failed. Please ensure the DLLs are in the correct location.", e2);
            }
        }
    }
    
    @Override
    public Object getParameters() {
        // Return the storage object that contains all parameter values
        return parameterStorage;
    }
    
    @Override
    public void setParameters(Object parameters) {
        if (parameters instanceof ETMParameterStorage) {
            this.parameterStorage = (ETMParameterStorage) parameters;
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

	private ETMParam parameters;

	private ETMParameterStorage parameterStorage;

	public EvolutionaryTreeMinerAlgorithm() {
		this.parameterStorage = new ETMParameterStorage();
	}


	public ETMParameterStorage getParameterStorage() {
		return parameterStorage;
	}

	private ETMParam createMiningParameters(XLog log, UIPluginContext context) {
		ETMParam param = ETMParamFactory.buildParam(log, context, parameterStorage.getPopulationSize(),
				parameterStorage.getEliteCount(), parameterStorage.getNrRandomTrees(),
				parameterStorage.getCrossOverChance(), parameterStorage.getMutationChance(),
				parameterStorage.isPreventDuplicates(), parameterStorage.getMaxGenerations(),
				parameterStorage.getTargetFitness(), parameterStorage.getReplayFitnessWeight(),
				parameterStorage.getFitnessLimit(), parameterStorage.getMaxFitnessTime(),
				parameterStorage.getPrecisionWeight(), parameterStorage.getGeneralizationWeight(),
				parameterStorage.getSimplicityWeight());
		
		int optimalThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
	    param.setMaxThreads(optimalThreads);
	    
	    // Additional performance optimizations
	    try {
	        // Set evaluation parallelism if available
	        Field evaluationParallelism = param.getClass().getDeclaredField("evaluationParallelism");
	        evaluationParallelism.setAccessible(true);
	        evaluationParallelism.set(param, true);
	    } catch (Exception e) {
	        System.out.println("Evaluation parallelism setting not available");
	    }
	    
		return param;
	}


	@Override
	public Petrinet mine(UIPluginContext context, XLog log) throws Exception {

		ETMParam currentParameters = createMiningParameters(log, context);

		if (log == null || log.isEmpty()) {
			throw new IllegalArgumentException("Event log cannot be null or empty");
		}

		try {

			// Run ETM synchronously (since we need to return the Petrinet)
			ETM etm = new ETM(currentParameters);
			etm.run();

			// Get the best discovered tree
			NAryTree resultTree = etm.getResult();
			if (resultTree == null) {
				throw new Exception("Evolutionary mining did not produce a valid result");
			}

			// Convert to ProcessTree
			ProcessTree processTree = NAryTreeToProcessTree.convert(
					currentParameters.getCentralRegistry().getEventClasses(), resultTree, "Discovered Process Tree");

			// Convert to Petri net
			Petrinet net = ProcessTree2Petrinet.convert(processTree).petrinet;

			// Post-processing
			net = postProcessNet(net);

			return net;

		} catch (Exception e) {
			throw new Exception("Evolutionary mining failed: " + e.getMessage(), e);
		}

	}
  
	
	private Petrinet postProcessNet(Petrinet net) {
		//Remove duplicate transitions (same label)
		Map<String, Transition> labelToTransition = new HashMap<String, Transition>();
		Set<Transition> toRemove = new HashSet<Transition>();

		//identify duplicates
		for (Transition t : net.getTransitions()) {
			String cleanLabel = t.getLabel().replace("+complete", "");
			if (labelToTransition.containsKey(cleanLabel)) {
				toRemove.add(t);
			} else {
				labelToTransition.put(cleanLabel, t);
				t.getAttributeMap().put("label", cleanLabel);
			}
		}

		// redirect edges and remove duplicates
		for (Transition t : toRemove) {
			String cleanLabel = t.getLabel().replace("+complete", "");
			Transition mainT = labelToTransition.get(cleanLabel);

			// Redirect input edges
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges = new ArrayList<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>(
					net.getInEdges(t));
			for (PetrinetEdge<?, ?> edge : inEdges) {
				net.removeEdge(edge);
				if (edge.getSource() instanceof Place) {
					net.addArc((Place) edge.getSource(), mainT);
				}
			}

			// Redirect output edges
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = new ArrayList<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>(
					net.getOutEdges(t));
			for (PetrinetEdge<?, ?> edge : outEdges) {
				net.removeEdge(edge);
				if (edge.getTarget() instanceof Place) {
					net.addArc(mainT, (Place) edge.getTarget());
				}
			}

			net.removeTransition(t);
		}

		//Ensure start/end places are properly connected
		Place start = null;
		Place end = null;

		// Find start place (no incoming edges)
		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty()) {
				start = p;
				break;
			}
		}

		// Find end place (no outgoing edges)
		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty()) {
				end = p;
				break;
			}
		}

		// Connect start place to first visible transition if needed
		if (start != null) {
			boolean hasOutgoing = false;
			for (PetrinetEdge<?, ?> edge : net.getOutEdges(start)) {
				if (edge.getTarget() instanceof Transition) {
					hasOutgoing = true;
					break;
				}
			}

			if (!hasOutgoing) {
				Transition firstVisible = null;
				for (Transition t : net.getTransitions()) {
					if (!t.isInvisible()) {
						firstVisible = t;
						break;
					}
				}
				if (firstVisible != null) {
					net.addArc(start, firstVisible);
				}
			}
		}

		// Connect last visible transition to end place if needed
		if (end != null) {
			boolean hasIncoming = false;
			for (PetrinetEdge<?, ?> edge : net.getInEdges(end)) {
				if (edge.getSource() instanceof Transition) {
					hasIncoming = true;
					break;
				}
			}

			if (!hasIncoming) {
				Transition lastVisible = null;
				for (Transition t : net.getTransitions()) {
					if (!t.isInvisible()) {
						lastVisible = t;
					}
				}
				if (lastVisible != null) {
					net.addArc(lastVisible, end);
				}
			}
		}

		return net;
	}

	@Override
	public String getAlgorithmName() {
		return "Evolutionary Tree Miner";
	}

}