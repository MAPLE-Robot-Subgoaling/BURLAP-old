package edu.umbc.cs.maple.oomdp;

import edu.umbc.cs.maple.oomdp.Attribute.AttributeType;


public class Value {

	private Attribute			attribute_;		//defines the attribute kind of this value
	private int					discVal_;		//the value of the attribute if it is a discrete attribute
	private double				realVal_;		//the value of the attribute if it is a real value
	
	
	
	static public Value copyValueAttribute(Value v){
		
		return new Value(v.attribute_);
		
	}
	
	
	public Value(Attribute attribute){
		
		attribute_ = attribute;
		
		discVal_ = -1;
		realVal_ = Double.NaN;
		
	}
	
	public Value(Value v){
		
		attribute_ = v.attribute_;
		
		discVal_ = v.discVal_;
		realVal_ = v.realVal_;
		
	}
	
	public Value copy(){
		return new Value(this);
	}
	
	public Attribute getAttribute(){
		return attribute_;
	}
	
	public void setValue(int v){
		if(attribute_.type_ == Attribute.AttributeType.DISC){
			discVal_ = v;
		}
		else{
			realVal_ = (double)v;
		}
	}
	
	public void setValue(double v){
		if(attribute_.type_ == Attribute.AttributeType.DISC){
			discVal_ = (int)v;
		}
		else{
			realVal_ = v;
		}
	}
	
	public void setValue(String v){
		if(attribute_.type_ == Attribute.AttributeType.DISC){
			this.setDiscValue(v);
		}
		else{
			realVal_ = Double.valueOf(v);
		}
	}
	
	public void setDiscValue(int v){
		discVal_ = v;
	}
	
	public void setDiscValue(String v){
		int intv = attribute_.discValuesHash_.get(v);
		discVal_ = intv;
	}
	
	public void setRealValue(double v){
		realVal_ = v;
	}
	
	
	public String attName(){
		return attribute_.name_;
	}
	
	public int getDiscVal(){
		
		if(attribute_.type_ == Attribute.AttributeType.DISC)
			return discVal_;
		return -1;
	}
	
	public double getRealVal(){
		if(attribute_.type_ == Attribute.AttributeType.REAL  || attribute_.type_ == Attribute.AttributeType.REALUNBOUND)
			return realVal_;
		return Double.NaN;
	}
	
	public String getStringVal(){
		if(attribute_.type_ == Attribute.AttributeType.DISC){
			if (discVal_ == -1){
				System.out.println("PROBLEM!");
			}
			return attribute_.discValues_.get(discVal_);
			
		}
		else if(attribute_.type_ == Attribute.AttributeType.REAL || attribute_.type_ == Attribute.AttributeType.REALUNBOUND){
			return Double.toString(realVal_);
		}
		
		return null;
	}
	
	public double getNumericVal(){
		if(attribute_.type_ == Attribute.AttributeType.DISC)
			return (double)discVal_;
		return realVal_;
	}
	
	public int getDiscreteDimensionality(){
		return attribute_.discValues_.size();
	}
	
	
	
	
	
	public boolean equals(Object obj){
		Value op = (Value)obj;
		if(!op.attribute_.equals(attribute_)){
			return false;
		}
		if(op.attribute_.type_ == AttributeType.DISC){
			return discVal_ == op.discVal_;
		}
		return realVal_ == op.realVal_;
		
		
	}
	
	public int hashCode(){
		return attribute_.hashCode();
	}
	
	
}
