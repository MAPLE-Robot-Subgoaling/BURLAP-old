package oomdptb.oomdp;

import java.util.*;

public class ObjectClass {
	
	public String							name_;							//name of the object class
	public Domain							domain_;						//back pointer to host domain
	public Map <String, Integer>			attributeIndex_;				//map from attribute name to feature vector index
	public Map <String, Attribute>			attributeMap_;					//map from attribute name to the defining attribute
	public List <Attribute>					attributeList_;					//definitions of object attributes
	public List <Integer>					observableAttributeIndices_;	//feature vector index of only attributes that are observable to the world
	public boolean							hidden_;						//whether this is a hidden object class for facilitating transition dynamics and observable objects
	
	public int								discreteSpace_;					//special variable to facilitate tabular problems; indicates the total observable state space for this class
	public int []							discreteAttributeSpaces_;		//special variable to facilitate tabular problems; indicates the space of each attribute
	
	
	public ObjectClass(Domain domain, String name){
		
		name_ = name;
		domain_ = domain;
		attributeIndex_ = new HashMap <String, Integer>();
		attributeMap_ = new HashMap <String, Attribute>();
		attributeList_ = new ArrayList <Attribute>();
		observableAttributeIndices_ = new ArrayList <Integer>();
		hidden_ = false;
		discreteSpace_ = 1;
		discreteAttributeSpaces_ = new int[0];
		
		domain_.addObjectClass(this);
		
		
	}
	
	public ObjectClass(Domain domain, String name, boolean hidden){
		
		name_ = name;
		domain_ = domain;
		attributeIndex_ = new HashMap <String, Integer>();
		attributeMap_ = new HashMap <String, Attribute>();
		attributeList_ = new ArrayList <Attribute>();
		observableAttributeIndices_ = new ArrayList <Integer>();
		hidden_ = hidden;
		discreteSpace_ = 1;
		discreteAttributeSpaces_ = new int[0];
		domain_.addObjectClass(this);
		
		
	}
	
	
	public void setAttributes(List <Attribute> atts){
		
		attributeList_.clear();
		observableAttributeIndices_.clear();
		attributeMap_.clear();
		attributeIndex_.clear();
		
		for(Attribute att: atts){
			this.addAttribute(att);
		}
		
	}
	
	
	public void addAttribute(Attribute att){
		
		//only add if it is new
		if(this.hasAttribute(att)){
			return ;
		}
		
		int ind = attributeList_.size();
		
		attributeList_.add(att);
		attributeMap_.put(att.name, att);
		attributeIndex_.put(att.name, ind);
		
		if(!att.hidden){
			observableAttributeIndices_.add(ind);
			if(att.type == Attribute.AttributeType.DISC){
				int d = att.discValues.size();
				discreteSpace_ *= d;
				int [] newD = new int[discreteAttributeSpaces_.length+1];
				for(int i = 0; i < discreteAttributeSpaces_.length; i++){
					newD[i] = discreteAttributeSpaces_[i];
				}
				newD[discreteAttributeSpaces_.length] = d;
				discreteAttributeSpaces_ = newD;
			}
		}
		
	}
	
	
	public boolean hasAttribute(Attribute att){
		return this.hasAttribute(att.name);
	}
	
	public boolean hasAttribute(String attName){	
		return attributeMap_.containsKey(attName);
	}
	
	public int attributeIndex(String attName){
		Integer ind = attributeIndex_.get(attName);
		if(ind != null){
			return ind;
		}
		return -1;
	}
	
	public int numAttributes(){
		return attributeList_.size();
	}
	
	
	
	
}
