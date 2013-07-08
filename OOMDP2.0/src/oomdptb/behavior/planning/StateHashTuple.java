package oomdptb.behavior.planning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.ObjectClass;
import oomdptb.oomdp.ObjectInstance;
import oomdptb.oomdp.State;

public class StateHashTuple {

	public State								s;
	public int									hashCode;
	private Map <String, List<Attribute>>		attributesForHashCode;
	private boolean								needToRecomputeHashCode;
	
	
	
	public StateHashTuple(State s){
		this.s = s;
		attributesForHashCode = null; //defaults hashCode to using all attributes for all classes
		needToRecomputeHashCode = true;
		
	}
	
	public StateHashTuple(State s, Map<String, List<Attribute>> attributesForHashCode){
		this.s = s;
		this.attributesForHashCode = attributesForHashCode;
		needToRecomputeHashCode = true;
	}
	
	public void setAttributesForHashCode(Map<String, List<Attribute>> attributesForHashCode){
		this.attributesForHashCode = attributesForHashCode;
		needToRecomputeHashCode = true;
	}
	
	public void setAttributesForClass(String classname, List <Attribute> atts){
		if(attributesForHashCode == null){
			attributesForHashCode = new HashMap<String, List<Attribute>>();
		}
		attributesForHashCode.put(classname, atts);
		needToRecomputeHashCode = true;
	}
	
	public void addAttributeForClass(String classname, Attribute att){
		if(attributesForHashCode == null){
			attributesForHashCode = new HashMap<String, List<Attribute>>();
		}
		List <Attribute> atts = attributesForHashCode.get(classname);
		if(atts == null){
			atts = new ArrayList<Attribute>();
			attributesForHashCode.put(classname, atts);
		}
		//check if already there or not
		for(Attribute attInList : atts){
			if(attInList.name.equals(att.name)){
				return ;
			}
		}
		//if reached here then this att is not already added
		atts.add(att);
		needToRecomputeHashCode = true;
	}
	
	
	public void computeHashCode(){
		
		List <String> objectClasses = this.getOrderedClasses();
		int totalVol = 1;
		hashCode = 0;
		for(String oclass : objectClasses){
			List <ObjectInstance> obs = s.getObjectsOfTrueClass(oclass);
			ObjectClass oc = obs.get(0).getObjectClass();
			int vol = this.computeVolumeForClass(oc);
			
			//too ensure object order invariance, the hash values must first be sorted by their object-wise hashcode
			int [] obHashCodes = new int[obs.size()];
			for(int i = 0; i < obs.size(); i++){
				obHashCodes[i] = this.getIndexValue(obs.get(i), oc);
			}
			Arrays.sort(obHashCodes);
			
			//multiply in reverse (for smaller total hash codes)
			for(int i = obHashCodes.length-1; i >= 0; i--){
				hashCode += obHashCodes[i]*totalVol;
				totalVol *= vol;
			}
			
		}
		
		needToRecomputeHashCode = false;
		
		
	}
	
	
	//this method will assume that attributes are all discrete
	private int getIndexValue(ObjectInstance o, ObjectClass oc){
		
		List <Attribute> attributes = this.getAttributesForClass(oc);
		int index = 0;
		int vol = 1;
		for(Attribute att : attributes){
			index += o.getDiscValForAttribute(att.name)*vol;
			vol *= att.discValues.size();
		}
		
		return index;
		
	}
	
	//this method will assume that attributes are all discrete
	private int computeVolumeForClass(ObjectClass oclass){
		
		List <Attribute> attributes = this.getAttributesForClass(oclass);
		int vol = 1;
		for(Attribute att : attributes){
			vol *= att.discValues.size();
		}
		
		return vol;
	}
	
	private List <Attribute> getAttributesForClass(ObjectClass oc){
		if(attributesForHashCode != null){
			List <Attribute> selectedAtts = attributesForHashCode.get(oc.name_);
			if(selectedAtts == null){
				//no definition at all for this class, so return empty list
				return new ArrayList<Attribute>();
			}
			return selectedAtts;
		}
		
		//then default to using all attributes for all object classes
		return oc.attributeList_;
	}
	
	private List <String> getOrderedClasses(){
		List <String> objectClasses = new ArrayList<String>(s.getObjectClassesPresent());
		Collections.sort(objectClasses);
		return objectClasses;
	}
	
	
	@Override
	public boolean equals(Object other){
		if(this == other){
			return true;
		}
		if(!(other instanceof StateHashTuple)){
			return false;
		}
		StateHashTuple o = (StateHashTuple)other;
		return s.equals(o.s);
		
	}
	
	@Override
	public int hashCode(){
		if(needToRecomputeHashCode){
			this.computeHashCode();
		}
		return hashCode;
	}
	
}
