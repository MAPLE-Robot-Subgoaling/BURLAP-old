package domain.sokoban;

public class TrainingElement {
	public String 			command;
	public Trajectory		trajectory;
	
	public TrainingElement(String c, Trajectory t){
		command = c;
		trajectory = t;
	}
}
