package main.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public interface MiningAlgorithm {
	Petrinet mine(UIPluginContext context, XLog log) throws Exception;

	String getAlgorithmName();

	Object getParameters();

	void setParameters(Object parameters);
	
	UIPluginContext getContext();

	void setContext(UIPluginContext context);
}