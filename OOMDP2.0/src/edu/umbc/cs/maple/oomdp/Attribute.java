package edu.umbc.cs.maple.oomdp;

import java.util.*;


public class Attribute {
	
	public enum AttributeType{
		NOTYPE(-1),
		DISC(0),
		REAL(1),
		REALUNBOUND(2);
		
		private final int intVal;
		
		AttributeType(int i){
			this.intVal = i;
		}
		
		public int toInt(){
			return intVal;
		}
		
		public static AttributeType fromInt(int i){
			switch(i){
				case 0:
					return DISC;
				case 1:
					return REAL;
				case 2:
					return REALUNBOUND;
				default:
					return NOTYPE;
			}
		}
	}

	public String						name_;				//name of the attribute
	public AttributeType				type_;				//type of values attribute holds
	public Domain						domain_;			//domain that holds this attribute
	public double						lowerLim_;			//lowest value for a bounded real attribute
	public double						upperLim_;			//highest value for a bounded real attribute
	public Map <String, Integer>		discValuesHash_;	//maps names of discrete values to int values 
	public List <String>				discValues_;		//list of discrete value names by their int value
	public boolean						hidden_;			//whether this value is part of the state representation or is hidden from the agent
	
	
	public Attribute(Domain domain, String name){
		
		domain_ = domain;
		name_ = name;
		
		type_ = AttributeType.NOTYPE;
		discValuesHash_ = new HashMap <String, Integer>(0);
		discValues_ = new ArrayList <String>(0);
		
		lowerLim_ = 0.0;
		upperLim_ = 0.0;
		
		hidden_ = false;
		
		
		domain_.addAttribute(this);
		
	}
	
	public Attribute(Domain domain, String name, AttributeType type){
		
		domain_ = domain;
		name_ = name;
		
		type_ = type;
		discValuesHash_ = new HashMap <String, Integer>(0);
		discValues_ = new ArrayList <String>(0);
		
		lowerLim_ = 0.0;
		upperLim_ = 0.0;
		
		hidden_ = false;
		
		
		domain_.addAttribute(this);
		
	}
	
	
	public Attribute(Domain domain, String name, int type){
		
		domain_ = domain;
		name_ = name;
		
		type_ = AttributeType.fromInt(type);
		discValuesHash_ = new HashMap <String, Integer>(0);
		discValues_ = new ArrayList <String>(0);
		
		lowerLim_ = 0.0;
		upperLim_ = 0.0;
		
		
		domain_.addAttribute(this);
		
	}
	
	public void setLims(double lower, double upper){
		lowerLim_ = lower;
		upperLim_ = upper;
	}
	
	
	
	public void setType(int itype){
		type_ = AttributeType.fromInt(itype);
	}
	
	public void setType(AttributeType type){
		type_ = type;
	}
	
	
	public void setDiscValues(List <String> vals){
		discValues_ = new ArrayList <String> (vals);
		discValuesHash_ = new HashMap<String, Integer>();
		for(int i = 0; i < discValues_.size(); i++){
			discValuesHash_.put(vals.get(i), new Integer(i));
		}
		
		//set range
		lowerLim_ = 0.0;
		upperLim_ = discValues_.size()-1;
	}
	
	
	public void setDiscValuesForRange(int low, int high, int step){
	
		discValues_ = new ArrayList <String>();
		discValuesHash_ = new HashMap<String, Integer>();
		
		int counter = 0;
		for(int i = low; i <= high; i += step){
		
			String s = Integer.toString(i);
			
			discValues_.add(s);
			discValuesHash_.put(s, counter);
			
			counter++;
		}
		
		//set range
		lowerLim_ = 0.0;
		upperLim_ = discValues_.size()-1;
	
	}
	
	
	public boolean equals(Object obj){
		Attribute op = (Attribute)obj;
		if(op.name_.equals(name_))
			return true;
		return false;
	}
	
	public int hashCode(){
		return name_.hashCode();
	}
	
	
	
	
}
