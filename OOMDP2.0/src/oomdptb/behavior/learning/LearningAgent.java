package oomdptb.behavior.learning;

import java.util.List;

import oomdptb.behavior.EpisodeAnalysis;
import oomdptb.oomdp.State;

public interface LearningAgent {

	public EpisodeAnalysis runLearningEpisodeFrom(State initialState);
	
	public EpisodeAnalysis getLastLearningEpisode();
	public void setNumEpisodesToStore(int numEps);
	public List<EpisodeAnalysis> getAllStoredLearningEpisodes();
	
}
