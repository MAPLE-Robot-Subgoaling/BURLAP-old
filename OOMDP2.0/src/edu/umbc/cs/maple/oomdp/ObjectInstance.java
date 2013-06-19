package edu.umbc.cs.maple.oomdp;

import java.util.*;


public class ObjectInstance {
	
	private ObjectClass					obClass_;			//object class to which this object belongs
	private String						name_;				//name of the object for disambiguation
	private List <Value>				values_;			//the values for each attribute
	
	private LinkedList <String>			pseudoClass_;		//list of pseudo classes that have been assigned from parameterization
	
	private double [] 					obsFeatureVec_;		//double representation of feature vector for easy manipulation (e.g., kd-trees)
	
	
	
	public ObjectInstance(ObjectClass obClass){
		
		obClass_ = obClass;
		name_ = "noname";
		pseudoClass_ = new LinkedList <String>();
		pseudoClass_.add(obClass_.name_); //base pseudo class is the true class 
		
		this.initializeValueObjects();
		
	}
	
	public ObjectInstance(ObjectClass obClass, String name){
		
		obClass_ = obClass;
		name_ = name;
		pseudoClass_ = new LinkedList <String>();
		pseudoClass_.add(obClass_.name_); //base pseudo class is the true class
		
		this.initializeValueObjects();
		
	}
	
	public ObjectInstance(ObjectInstance o){
		
		obClass_ = o.obClass_;
		name_ = o.name_;
		
		values_ = new ArrayList <Value>(obClass_.numAttributes());
		for(Value v : o.values_){
			values_.add(v.copy());
		}
		
		pseudoClass_ = new LinkedList<String>();
		for(String pc : o.pseudoClass_){
			pseudoClass_.addLast(pc);
		}
		
		obsFeatureVec_ = new double[obClass_.observableAttributeIndices_.size()];
		for(int i = 0; i < o.obsFeatureVec_.length; i++){
			obsFeatureVec_[i] = o.obsFeatureVec_[i];
		}
		
	}
	
	public ObjectInstance copy(){
		return new ObjectInstance(this);
	}
	
	
	
	public void initializeValueObjects(){
		
		obsFeatureVec_ = new double[obClass_.observableAttributeIndices_.size()];
		
		values_ = new ArrayList <Value>(obClass_.numAttributes());
		for(Attribute att : obClass_.attributeList_){
			values_.add(new Value(att));
		}
		
	}
	
	public void setName(String name){
		name_ = name;
	}
	
	public int getDiscreteSpace(){
		return obClass_.discreteSpace_;
	}
	
	public int [] getDiscreteAttributeSpaces(){
		return obClass_.discreteAttributeSpaces_;
	}
	
	public void pushPseudoClass(String c){
		pseudoClass_.addFirst(c);
	}
	
	public void setValue(String attName, String v){
		int ind = obClass_.attributeIndex(attName);
		values_.get(ind).setValue(v);
		this.computeRealVals();
	}
	
	public void setValue(String attName, double v){
		int ind = obClass_.attributeIndex(attName);
		values_.get(ind).setValue(v);
		this.computeRealVals();
	}
	
	public void setValue(String attName, int v){
		int ind = obClass_.attributeIndex(attName);
		values_.get(ind).setValue(v);
		this.computeRealVals();
	}
	
	public void setValues(List <Double> vs){
		
		for(int i = 0; i < vs.size(); i++){
			values_.get(i).setValue(vs.get(i));
		}
		this.computeRealVals();
	}
	
	public void setObservableValues(List <Double> vs){
		
		for(int i = 0; i < vs.size(); i++){
			int ind = obClass_.observableAttributeIndices_.get(i);
			values_.get(ind).setValue(vs.get(i));
		}
		this.computeRealVals();
		
	}
	
	public String getName(){
		return name_;
	}
	
	
	public ObjectClass getObjectClass(){
		return obClass_;
	}
	
	public String getTrueClassName(){
		return obClass_.name_;
	}
	
	public String getPseudoClass(){
		return pseudoClass_.peek();
	}
	
	public String popPseudoClass(){
		//only pop the class if there are more than one
		//because the first element is the true class and can never be removed
		if(pseudoClass_.size() > 1){
			return pseudoClass_.poll();
		}
		return null;
	}
	
	public Value getValueForAttribute(String attName){
		int ind = obClass_.attributeIndex(attName);
		return values_.get(ind);
	}
	
	public double getRealValForAttribute(String attName){
		int ind = obClass_.attributeIndex(attName);
		return values_.get(ind).getRealVal();
	}
	
	public String getStringValForAttribute(String attName){
		int ind = obClass_.attributeIndex(attName);
		return values_.get(ind).getStringVal();
	}
	
	public int getDiscValForAttribute(String attName){
		int ind = obClass_.attributeIndex(attName);
		return values_.get(ind).getDiscVal();
	}
	
	public List <Value> getValues(){
		return this.values_;
	}
	
	public String getObjectDescription(){
		
		String desc = name_ + " (" + this.getPseudoClass() + ")\n";
		for(Value v : values_){
			desc = desc + "\t" + v.attName() + ":\t" + v.getStringVal() + "\n";
		}
		
		return desc;
	
	}
	
	public String getObservableValueStringRep(){
		String res = String.valueOf(obsFeatureVec_[0]);
		for(int i = 1; i < obsFeatureVec_.length; i++){
			res = res + "," + String.valueOf(obsFeatureVec_[i]);
		}
		return res;
	}
	
	public void computeRealVals(){
		
		for(int i = 0; i < obClass_.observableAttributeIndices_.size(); i++){
			
			int ind = obClass_.observableAttributeIndices_.get(i);
			obsFeatureVec_[i] = values_.get(ind).getNumericVal();
			
		}
		
	}
	
	public double[] getObservableFeatureVec(){
		return obsFeatureVec_;
	}
	
	
	
	
	public boolean equals(Object obj){
		ObjectInstance op = (ObjectInstance)obj;
		if(op.name_.equals(name_))
			return true;
		return false;
	}
	
	public boolean valueEquals(ObjectInstance obj){
	
		if(!obClass_.equals(obj.obClass_)){
			return false;
		}
	
		for(Value v : values_){
		
			Value ov = obj.getValueForAttribute(v.attName());
			if(!v.equals(ov)){
				return false;
			}
		
		}
		
		return true;
	
	}
	
	public int hashCode(){
		return name_.hashCode();
	}
	

}
