package main.controller.settings;

public class ETMParameterStorage {
	
	//DEFAULT
	private int populationSize = 20;
	private int eliteCount = 5;
	private int nrRandomTrees = 0;
	private double crossOverChance = 0.25;
	private double mutationChance = 0.25;
	private boolean preventDuplicates = true;
	private int maxGenerations = 50; // generations
	private double targetFitness = 1.0;
	private double replayFitnessWeight = 10.0;
	private double fitnessLimit = -1.0; // -1 means no limit
	private double maxFitnessTime = 10.0;
	private double precisionWeight = 5.0;
	private double generalizationWeight = 1.0;
	private double simplicityWeight = 2.0;
	private double similarityWeight = 0.0;

	private int cpuCores = Runtime.getRuntime().availableProcessors(); // Default to all available cores

	// Getters and setters for all parameters
	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int size) {
		this.populationSize = size;
	}

	public int getEliteCount() {
		return eliteCount;
	}

	public void setEliteCount(int count) {
		this.eliteCount = count;
	}

	public int getNrRandomTrees() {
		return nrRandomTrees;
	}

	public void setNrRandomTrees(int count) {
		this.nrRandomTrees = count;
	}

	public double getCrossOverChance() {
		return crossOverChance;
	}

	public void setCrossOverChance(double chance) {
		this.crossOverChance = chance;
	}

	public double getMutationChance() {
		return mutationChance;
	}

	public void setMutationChance(double chance) {
		this.mutationChance = chance;
	}

	public boolean isPreventDuplicates() {
		return preventDuplicates;
	}

	public void setPreventDuplicates(boolean prevent) {
		this.preventDuplicates = prevent;
	}

	public int getMaxGenerations() {
		return maxGenerations;
	}

	public void setMaxGenerations(int generations) {
		this.maxGenerations = generations;
	}

	public double getTargetFitness() {
		return targetFitness;
	}

	public void setTargetFitness(double fitness) {
		this.targetFitness = fitness;
	}

	public double getReplayFitnessWeight() {
		return replayFitnessWeight;
	}

	public void setReplayFitnessWeight(double weight) {
		this.replayFitnessWeight = weight;
	}

	public double getFitnessLimit() {
		return fitnessLimit;
	}

	public void setFitnessLimit(double limit) {
		this.fitnessLimit = limit;
	}

	public double getMaxFitnessTime() {
		return maxFitnessTime;
	}

	public void setMaxFitnessTime(double time) {
		this.maxFitnessTime = time;
	}

	public double getPrecisionWeight() {
		return precisionWeight;
	}

	public void setPrecisionWeight(double weight) {
		this.precisionWeight = weight;
	}

	public double getGeneralizationWeight() {
		return generalizationWeight;
	}

	public void setGeneralizationWeight(double weight) {
		this.generalizationWeight = weight;
	}

	public double getSimplicityWeight() {
		return simplicityWeight;
	}

	public void setSimplicityWeight(double weight) {
		this.simplicityWeight = weight;
	}
 
	public double getSimilarityWeight() {
		return similarityWeight;
	}

	public void setSimilarityWeight(double weight) {
		this.similarityWeight = weight;
	}

	public int getCpuCores() {
		return cpuCores;
	}

	public void setCpuCores(int cores) {
		this.cpuCores = Math.min(Math.max(1, cores), Runtime.getRuntime().availableProcessors());
	}

	public void copyFrom(ETMParameterStorage other) {
		this.populationSize = other.populationSize;
		this.eliteCount = other.eliteCount;
		this.nrRandomTrees = other.nrRandomTrees;
		this.crossOverChance = other.crossOverChance;
		this.mutationChance = other.mutationChance;
		this.preventDuplicates = other.preventDuplicates;
		this.maxGenerations = other.maxGenerations;
		this.targetFitness = other.targetFitness;
		this.replayFitnessWeight = other.replayFitnessWeight;
		this.fitnessLimit = other.fitnessLimit;
		this.maxFitnessTime = other.maxFitnessTime;
		this.precisionWeight = other.precisionWeight;
		this.generalizationWeight = other.generalizationWeight;
		this.simplicityWeight = other.simplicityWeight;
		this.similarityWeight = other.similarityWeight;

		this.cpuCores = other.cpuCores;

	}

}