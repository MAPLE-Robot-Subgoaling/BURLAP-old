package edu.umbc.cs.maple.oomdp;

import edu.umbc.cs.maple.oomdp.Attribute.AttributeType;


public class Value {

	private Attribute			attribute;		//defines the attribute kind of this value
	private int					discVal;		//the value of the attribute if it is a discrete attribute
	private double				realVal;		//the value of the attribute if it is a real value
	
	
	
	static public Value copyValueAttribute(Value v){
		
		return new Value(v.attribute);
		
	}
	
	
	public Value(Attribute attribute){
		
		this.attribute = attribute;
		
		discVal = -1;
		realVal = Double.NaN;
		
	}
	
	public Value(Value v){
		
		attribute = v.attribute;
		
		discVal = v.discVal;
		realVal = v.realVal;
		
	}
	
	public Value copy(){
		return new Value(this);
	}
	
	public Attribute getAttribute(){
		return attribute;
	}
	
	public void setValue(int v){
		if(attribute.type == Attribute.AttributeType.DISC){
			discVal = v;
		}
		else{
			realVal = (double)v;
		}
	}
	
	public void setValue(double v){
		if(attribute.type == Attribute.AttributeType.DISC){
			discVal = (int)v;
		}
		else{
			realVal = v;
		}
	}
	
	public void setValue(String v){
		if(attribute.type == Attribute.AttributeType.DISC){
			this.setDiscValue(v);
		}
		else{
			realVal = Double.valueOf(v);
		}
	}
	
	public void setDiscValue(int v){
		discVal = v;
	}
	
	public void setDiscValue(String v){
		int intv = attribute.discValuesHash_.get(v);
		discVal = intv;
	}
	
	public void setRealValue(double v){
		realVal = v;
	}
	
	
	public String attName(){
		return attribute.name_;
	}
	
	public int getDiscVal(){
		
		if(attribute.type == Attribute.AttributeType.DISC)
			return discVal;
		return -1;
	}
	
	public double getRealVal(){
		if(attribute.type == Attribute.AttributeType.REAL  || attribute.type == Attribute.AttributeType.REALUNBOUND)
			return realVal;
		return Double.NaN;
	}
	
	public String getStringVal(){
		if(attribute.type == Attribute.AttributeType.DISC){
			if (discVal == -1){
				System.out.println("PROBLEM!");
			}
			return attribute.discValues_.get(discVal);
			
		}
		else if(attribute.type == Attribute.AttributeType.REAL || attribute.type == Attribute.AttributeType.REALUNBOUND){
			return Double.toString(realVal);
		}
		
		return null;
	}
	
	public double getNumericVal(){
		if(attribute.type == Attribute.AttributeType.DISC)
			return (double)discVal;
		return realVal;
	}
	
	public int getDiscreteDimensionality(){
		return attribute.discValues_.size();
	}
	
	public boolean equals(Object obj){
		Value op = (Value)obj;
		if(!op.attribute.equals(attribute)){
			return false;
		}
		if(op.attribute.type == AttributeType.DISC){
			return discVal == op.discVal;
		}
		return realVal == op.realVal;
		
		
	}
	
	public int hashCode(){
		return attribute.hashCode();
	}
	
	
}
