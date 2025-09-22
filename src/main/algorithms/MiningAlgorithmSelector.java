package main.algorithms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;

import main.controller.settings.ETMParameterStorage;

/**
 * Factory class to select and manage instances of different mining algorithms.
 */
public class MiningAlgorithmSelector {
	private static Map<String, MiningAlgorithm> algorithmInstances = new HashMap<>();

	public static MiningAlgorithm getAlgorithm(String algorithmName) {
		// Return existing instance if available
		if (algorithmInstances.containsKey(algorithmName)) {
			MiningAlgorithm algo = algorithmInstances.get(algorithmName);
			System.out.println("[ALGO SELECTOR] Returning existing " + algorithmName + " instance: "
					+ System.identityHashCode(algo));
			printCurrentParameters(algo);
			return algo;
		}

		// Create new instance if needed
		MiningAlgorithm algorithm;
		switch (algorithmName) {
		case "Inductive Miner":
			algorithm = new InductiveMinerAlgorithm();
			break;
		case "Evolutionary Tree Miner":
			algorithm = new EvolutionaryTreeMinerAlgorithm();
			break;
		case "Split Miner":
			algorithm = new SplitMinerAlgorithm();
			break;
		case "Heuristics Miner":
			algorithm = new HeuristicMinerAlgorithm();
			break;
		default:
			throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
		}

		System.out.println(
				"[ALGORITHM SELECTOR] Created new " + algorithmName + " instance: " + System.identityHashCode(algorithm));
		algorithmInstances.put(algorithmName, algorithm);
		printCurrentParameters(algorithm);
		return algorithm;
		
	}

	private static void printCurrentParameters(MiningAlgorithm algorithm) {
		System.out.println("[PARAMETERS] Current values for " + algorithm.getAlgorithmName() + ":");

		if (algorithm instanceof InductiveMinerAlgorithm) {
			MiningParameters params = (MiningParameters) algorithm.getParameters();
			System.out.println("- Noise threshold: " + params.getNoiseThreshold());
			System.out.println("- Use multiset: " + params.isUseMultithreading());
		} else if (algorithm instanceof EvolutionaryTreeMinerAlgorithm) {
	        Object params = algorithm.getParameters();
            ETMParameterStorage storage = (ETMParameterStorage) params;
            System.out.println("[PARAMETER DUMP] Evolutionary Tree Miner Settings:");
            System.out.println("- Population size: " + storage.getPopulationSize());
            System.out.println("- Elite size: " + storage.getEliteCount());
            System.out.println("- Random trees: " + storage.getNrRandomTrees());
            System.out.println("- Mutation chance: " + storage.getMutationChance());
            System.out.println("- Crossover chance: " + storage.getCrossOverChance());
            System.out.println("- Max generations: " + storage.getMaxGenerations());
            System.out.println("- Fitness limit: " + storage.getFitnessLimit());
            System.out.println("- Replay fitness weight: " + storage.getReplayFitnessWeight());
            System.out.println("- Precision weight: " + storage.getPrecisionWeight());
            System.out.println("- Generalization weight: " + storage.getGeneralizationWeight());
            System.out.println("- Simplicity weight: " + storage.getSimplicityWeight());
            System.out.println("- Similarity weight: " + storage.getSimilarityWeight());
            System.out.println("- Prevent duplicates: " + storage.isPreventDuplicates());
            System.out.println("- Target fitness: " + storage.getTargetFitness());
            System.out.println("- Max fitness time: " + storage.getMaxFitnessTime());
            System.out.println("- CPU cores: " + storage.getCpuCores());
	    } else if (algorithm instanceof HeuristicMinerAlgorithm) {
			HeuristicsMinerSettings params = (HeuristicsMinerSettings) algorithm.getParameters();
			System.out.println("- Dependency threshold: " + params.getDependencyThreshold());
			System.out.println("- AND threshold: " + params.getAndThreshold());
		} else if (algorithm instanceof SplitMinerAlgorithm) {
			Map<String, Object> params = (Map<String, Object>) algorithm.getParameters();
			System.out.println("- Frequency threshold: " + params.get("frequencyThreshold"));
			System.out.println("- Parallelism threshold: " + params.get("parallelismThreshold"));
		} else {
			System.out.println("- No specific parameters available for this algorithm");
		}
	}

	public static List<String> getAvailableAlgorithms() {
		return Arrays.asList("Inductive Miner", "Evolutionary Tree Miner", "Split Miner", "Heuristics Miner");
	}

}