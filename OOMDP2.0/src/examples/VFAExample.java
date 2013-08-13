package examples;


import domain.lunarlander.LLStateParser;
import domain.lunarlander.LLVisualizer;
import domain.lunarlander.LunarLanderDomain;
import oomdptb.behavior.EpisodeAnalysis;
import oomdptb.behavior.EpisodeSequenceVisualizer;
import oomdptb.behavior.learning.tdmethods.vfa.GradientDescentSarsaLam;
import oomdptb.behavior.vfa.ValueFunctionApproximation;
import oomdptb.behavior.vfa.cmac.CMACFeatureDatabase;
import oomdptb.behavior.vfa.cmac.CMACFeatureDatabase.TilingArrangement;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.PropositionalFunction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.StateParser;
import oomdptb.oomdp.TerminalFunction;
import oomdptb.oomdp.common.SinglePFTF;
import oomdptb.oomdp.visualizer.Visualizer;

public class VFAExample {

	
	protected LunarLanderDomain		lld;
	protected Domain				domain;
	protected RewardFunction		rf;
	protected TerminalFunction		tf;
	protected StateParser			sp;
	protected State					initialState;
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		VFAExample example = new VFAExample();
		String outputPath = "output"; //directory to record results
		
		example.runCMACVFA(outputPath);
		example.visualize(outputPath);
		

	}
	
	
	
	
	public VFAExample() {
		
		lld = new LunarLanderDomain();
		domain = lld.generateDomain();
		rf = new LLRF(domain);
		tf = new SinglePFTF(domain.getPropFunction(LunarLanderDomain.PFONPAD));
		sp = new LLStateParser(domain);
		
		initialState = LunarLanderDomain.getCleanState(domain, 1);
		LunarLanderDomain.setAgent(initialState, 0., 5.0, 0.0);
		LunarLanderDomain.setPad(initialState, 75., 95., 0., 10.);
		
	}
	
	
	public void visualize(String outputPath){
		Visualizer v = LLVisualizer.getVisualizer(lld);
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, outputPath);
	}
	
	public void runCMACVFA(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		int nTilings = 5;
		CMACFeatureDatabase cmac = new CMACFeatureDatabase(nTilings, TilingArrangement.RANDOMJITTER);
		double resolution = 10.;
		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS, domain.getAttribute(LunarLanderDomain.AATTNAME), 2*lld.getAngmax() / resolution);
		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS, domain.getAttribute(LunarLanderDomain.XATTNAME), (lld.getXmax() - lld.getXmin()) / resolution);
		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS, domain.getAttribute(LunarLanderDomain.YATTNAME), (lld.getYmax() - lld.getYmin()) / resolution);
		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS, domain.getAttribute(LunarLanderDomain.VXATTNAME), 2*lld.getVmax() / resolution);
		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS, domain.getAttribute(LunarLanderDomain.VYATTNAME), 2*lld.getVmax() / resolution);
		
		double defaultQ = 0.5;
		
		ValueFunctionApproximation vfa = cmac.generateVFA(defaultQ/nTilings);
		
		GradientDescentSarsaLam agent = new GradientDescentSarsaLam(domain, rf, tf, 0.99, vfa, 0.02, 10000, 0.5);
		
		for(int i = 0; i < 5000; i++){
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState); //run learning episode
			ea.writeToFile(String.format("%se%04d", outputPath, i), sp); //record episode to a file
			System.out.println(i + ": " + ea.numTimeSteps()); //print the performance of this episode
		}
		
		
		
	}
	
	
	
	class LLRF implements RewardFunction{

		
		double							goalReward = 1000.0;
		double							collisionReward = -100.0;
		double							defaultReward = -1.0;
		
		PropositionalFunction			onGround;
		PropositionalFunction			touchingSurface;
		PropositionalFunction			touchingPad;
		PropositionalFunction			onPad;
		
		
		public LLRF(Domain domain){
			
			this.onGround = domain.getPropFunction(LunarLanderDomain.PFONGROUND);
			this.touchingSurface = domain.getPropFunction(LunarLanderDomain.PFTOUCHSURFACE);
			this.touchingPad = domain.getPropFunction(LunarLanderDomain.PFTPAD);
			this.onPad = domain.getPropFunction(LunarLanderDomain.PFONPAD);
			
		}
		
		
		public LLRF(Domain domain, double goalReward, double collisionReward, double defaultReward){
			this.goalReward = goalReward;
			this.collisionReward = collisionReward;
			this.defaultReward = defaultReward;
			
			this.onGround = domain.getPropFunction(LunarLanderDomain.PFONGROUND);
			this.touchingSurface = domain.getPropFunction(LunarLanderDomain.PFTOUCHSURFACE);
			this.touchingPad = domain.getPropFunction(LunarLanderDomain.PFTPAD);
			this.onPad = domain.getPropFunction(LunarLanderDomain.PFONPAD);
		}
		
		
		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			
			if(sprime.somePFGroundingIsTrue(onPad)){
				return goalReward;
			}
			
			if(sprime.somePFGroundingIsTrue(onGround) || sprime.somePFGroundingIsTrue(touchingPad) || sprime.somePFGroundingIsTrue(touchingSurface)){
				return collisionReward;
			}
			
			return defaultReward;
		}
		
		
		
		
	}

	

}
