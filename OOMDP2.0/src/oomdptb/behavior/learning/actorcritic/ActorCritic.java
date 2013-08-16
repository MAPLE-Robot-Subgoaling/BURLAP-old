package oomdptb.behavior.learning.actorcritic;

import java.util.LinkedList;
import java.util.List;

import oomdptb.behavior.EpisodeAnalysis;
import oomdptb.behavior.Policy;
import oomdptb.behavior.learning.LearningAgent;
import oomdptb.behavior.planning.OOMDPPlanner;
import oomdptb.oomdp.Action;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;

public class ActorCritic extends OOMDPPlanner implements LearningAgent {

	protected Actor													actor;
	protected Critic												critic;
	
	protected int													numEpisodesForPlanning;
	
	protected LinkedList<EpisodeAnalysis>							episodeHistory;
	protected int													numEpisodesToStore;
	
	public ActorCritic(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, Actor actor, Critic critic) {
		this.actor = actor;
		this.critic = critic;
		numEpisodesForPlanning = 1;
		this.episodeHistory = new LinkedList<EpisodeAnalysis>();
		numEpisodesToStore = 1;
		this.PlannerInit(domain, rf, tf, gamma, null);
	}
	
	@Override
	public void addNonDomainReferencedAction(Action a){
		super.addNonDomainReferencedAction(a);
		this.actor.addNonDomainReferencedAction(a);
		this.critic.addNonDomainReferencedAction(a);
		
	}

	@Override
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState) {
		
		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
		
		State curState = initialState;
		
		this.critic.initializeEpisode(curState);
		
		while(!tf.isTerminal(curState)){
			
			GroundedAction ga = this.actor.getAction(curState);
			State nextState = ga.executeIn(curState);
			double r = this.rf.reward(curState, ga, nextState);
			
			ea.recordTransitionTo(nextState, ga, r);
			
			CritiqueResult critqiue = this.critic.critiqueAndUpdate(curState, ga, nextState);
			this.actor.updateFromCritqique(critqiue);
			
			curState = nextState;
			
		}
		
		this.critic.endEpisode();
		
		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
		}
		episodeHistory.offer(ea);
		
		return ea;
	}

	@Override
	public EpisodeAnalysis getLastLearningEpisode() {
		return episodeHistory.getLast();
	}

	@Override
	public void setNumEpisodesToStore(int numEps) {
		this.numEpisodesToStore = numEps;
	}

	@Override
	public List<EpisodeAnalysis> getAllStoredLearningEpisodes() {
		return this.episodeHistory;
	}

	@Override
	public void planFromState(State initialState) {
		for(int i = 0; i < numEpisodesForPlanning; i++){
			this.runLearningEpisodeFrom(initialState);
		}
	}
	
	public Policy getPolicy(){
		return this.actor;
	}

}
