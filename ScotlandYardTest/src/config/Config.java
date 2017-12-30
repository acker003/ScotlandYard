package config;

public class Config {

	// Main Factors
	public final double CHAOS_FACTOR;
	public final double SITUATION_FACTOR;
	
	// Situation Factors
	public final double CENTRALITY_FACTOR;
	public final double DEGREE_FACTOR;
	public final double ESCAPE_POTENTIAL_FACTOR;
	public final double DISTANCE_FACTOR;
	public final double RISK_OF_BEING_CIRCLED_FACTOR;
	
	// Chaos factors
	public final double CHAOS_DISTANCE_FACTOR;
	public final double CHAOS_EVALUATION_FACTOR;
	
	public Config(double chaosFactor, double situationFactor, 
			double centralityFactor, double degreeFactor, 
			double escapePotentialFactor, double distanceFactor, 
			double riskBeingCircledFactor, double chaosDistanceFactor, 
			double chaosEvaluationFactor) {
		this.CHAOS_FACTOR = chaosFactor;
		this.SITUATION_FACTOR = situationFactor;
		this.CENTRALITY_FACTOR = centralityFactor;
		this.DEGREE_FACTOR = degreeFactor;
		this.ESCAPE_POTENTIAL_FACTOR = escapePotentialFactor;
		this.DISTANCE_FACTOR = distanceFactor;
		this.RISK_OF_BEING_CIRCLED_FACTOR = riskBeingCircledFactor;
		this.CHAOS_DISTANCE_FACTOR = chaosDistanceFactor;
		this.CHAOS_EVALUATION_FACTOR = chaosEvaluationFactor;
	}
	
	
}
