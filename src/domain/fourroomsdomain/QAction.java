package domain.fourroomsdomain;

import burlap.oomdp.singleagent.GroundedAction;

/**
 * QAction Class - a simple data structure that holds the GroundedAction
 * And the related QValyue
 * @author Tenji Tembo
 *
 */
public class QAction {
	
	//Variables
	public GroundedAction action;
	public double value;
	
	/**
	 * Constructor
	 * @param action
	 * @param qValue
	 */
	public QAction(GroundedAction action, Double qValue){
		this.action = action;
		this.value = qValue;
	}
	
	/**
	 * getter
	 * @return - q value
	 */
	public double getQVal(){
		return value;
	}
	
	/**
	 * setter
	 * @param qVal - new q value
	 */
	public void setQVal(Double qVal){
		this.value = qVal;
	}
}
