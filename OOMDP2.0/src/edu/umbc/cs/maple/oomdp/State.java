package edu.umbc.cs.maple.oomdp;

import java.util.*;

public class State {

	private String 											name_;						//state name for disambiguation
	
	private List <ObjectInstance>							objectInstances_;			//list of observable object instances that define the state
	private List <ObjectInstance>							hiddenObjectInstances_;		//list of hidden object instances that facilitate domain dynamics and infer observable values
	private Map <String, ObjectInstance>					objectMap_;					//map from object names to their instances
	
	private Set <String>									objectClasses_;				//list of object (pseudo)classes that are instantiated in this state
	private Map <String, List <ObjectInstance>>				objectIndexByClass_;		//map of object instances organized by (pseudo)class
	private List <List <ObjectInstance>>					objectsByClass_;			//list of object instances organized by (pseudo)class
	
	private Set <String>									objectTrueClasses_;			//list of object classes that are instantiated in this state
	private Map <String, List <ObjectInstance>>				objectIndexByTrueClass_;	//map of object instances organized by class
	private List <List <ObjectInstance>>					objectsByTrueClass_;		//list of object instances organized by class

	
	
	public State(){
		name_ = "unnamed";
		this.initDataStructures();
	}
	
	public State(String name){
		name_ = name;
		this.initDataStructures();
	}
	
	public State(State s){
		
		name_ = s.name_;
		
		this.initDataStructures();
		
		for(ObjectInstance o : s.objectInstances_){
			this.addObject(o.copy());
		}
		
		for(ObjectInstance o : s.hiddenObjectInstances_){
			this.addObject(o.copy());
		}
		
	}
	
	public State copy(){
		return new State(this);
	}
	
	public void initDataStructures(){
		
		objectInstances_ = new ArrayList <ObjectInstance>();
		hiddenObjectInstances_ = new ArrayList <ObjectInstance>();
		objectMap_ = new HashMap <String, ObjectInstance>();
		
		objectClasses_ = new HashSet <String>();
		objectIndexByClass_ = new HashMap <String, List <ObjectInstance>>();
		objectsByClass_ = new ArrayList <List <ObjectInstance>>();
		
		objectTrueClasses_ = new HashSet <String>();
		objectIndexByTrueClass_ = new HashMap <String, List <ObjectInstance>>();
		objectsByTrueClass_ = new ArrayList <List <ObjectInstance>>();
	}
	
	public void setName(String name){
		name_ = name;
	}
	
	public String getName(){
		return name_;
	}
	
	public void addObject(ObjectInstance o){
		
		String oname = o.getName();
		
		if(objectMap_.containsKey(oname)){
			return ; //don't add an object that conflicts with another object of the same name
		}
		
		
		objectMap_.put(oname, o);
		
		
		if(o.getObjectClass().hidden_){
			hiddenObjectInstances_.add(o);
		}
		else{
			objectInstances_.add(o);
		}
		
		
		this.addObjectClassIndexing(o);
		
		
	}
	
	private void addObjectClassIndexing(ObjectInstance o){
		
		String oclass = o.getPseudoClass();
		String otclass = o.getTrueClassName();
		
		//manage pseudo indexing
		if(objectClasses_.contains(oclass)){
			objectIndexByClass_.get(oclass).add(o);
		}
		else{
			
			objectClasses_.add(oclass);
			ArrayList <ObjectInstance> classList = new ArrayList <ObjectInstance>();
			classList.add(o);
			objectIndexByClass_.put(oclass, classList);
			objectsByClass_.add(classList);
			
		}
		
		//manage true indexing
		if(objectTrueClasses_.contains(otclass)){
			objectIndexByTrueClass_.get(otclass).add(o);
		}
		else{
			
			objectTrueClasses_.add(otclass);
			ArrayList <ObjectInstance> classList = new ArrayList <ObjectInstance>();
			classList.add(o);
			objectIndexByTrueClass_.put(otclass, classList);
			objectsByTrueClass_.add(classList);
			
		}
		
	}
	
	
	public void removeObject(String oname){
		this.removeObject(objectMap_.get(oname));
	}
	
	
	public void removeObject(ObjectInstance o){
		if(o == null){
			return ;
		}
		
		String oname = o.getName();
		
		if(!objectMap_.containsKey(oname)){
			return ; //make sure we're removing something that actually exists in this state!
		}
		
		if(o.getObjectClass().hidden_){
			hiddenObjectInstances_.remove(o);
		}
		else{
			objectInstances_.remove(o);
		}
		
		objectMap_.remove(oname);
		
		this.removeObjectClassIndexing(o);
		
	}
	
	private void removeObjectClassIndexing(ObjectInstance o){
		
		String oclass = o.getPseudoClass();
		List <ObjectInstance> classList = objectIndexByClass_.get(oclass);
		
		String otclass = o.getTrueClassName();
		List <ObjectInstance> classTList = objectIndexByTrueClass_.get(otclass);
		
		//manage psuedo class
		
		//if this index has more than one entry, then we can just remove from it and be done
		if(classList.size() > 1){
			classList.remove(o);
		}
		else{
			//otherwise we have to remove class entries for it
			//first find it in the list
			for(int i = 0; i < objectsByClass_.size(); i++){
				if(objectsByClass_.get(i).get(0).getPseudoClass().equals(oclass)){
					objectsByClass_.remove(i);
					break;
				}
			}
			objectIndexByClass_.remove(oclass);
			objectClasses_.remove(oclass);
		}
		
		
		//manage true class
		
		//if this index has more than one entry, then we can just remove from it and be done
		if(classTList.size() > 1){
			classTList.remove(o);
		}
		else{
			//otherwise we have to remove class entries for it
			//first find it in the list
			for(int i = 0; i < objectsByTrueClass_.size(); i++){
				if(objectsByTrueClass_.get(i).get(0).getTrueClassName().equals(otclass)){
					objectsByTrueClass_.remove(i);
					break;
				}
			}
			objectIndexByTrueClass_.remove(otclass);
			objectTrueClasses_.remove(otclass);
		}
		
		
		
	}
	
	
	public Map <String, String> getExactStateObjectMatchingTo(State so){
		
		Map <String, String> matching = new HashMap<String, String>();
		
		if(this.numTotalObjets() != so.numTotalObjets()){
			return new HashMap<String, String>(); //states are not equal and therefore cannot be matched
		}
		
		Set<String> matchedObs = new HashSet<String>();
		
		for(List <ObjectInstance> objects : objectsByTrueClass_){
			
			String oclass = objects.get(0).getTrueClassName();
			List <ObjectInstance> oobjects = so.getObjectsOfTrueClass(oclass);
			if(objects.size() != oobjects.size()){
				return new HashMap<String, String>(); //states are not equal and therefore cannot be matched
			}
			
			for(ObjectInstance o : objects){
				boolean foundMatch = false;
				for(ObjectInstance oo : oobjects){
					if(matchedObs.contains(oo.getName())){
						continue; //already matched this one; check another
					}
					if(o.valueEquals(oo)){
						foundMatch = true;
						matchedObs.add(oo.getName());
						matching.put(o.getName(), oo.getName());
						break;
					}
				}
				if(!foundMatch){
					return new HashMap<String, String>(); //states are not equal and therefore cannot be matched
				}
			}
			
		}
		
		return matching;
	}
	
	
	
	@Override
	public boolean equals(Object other){
	
		if(this == other){
			return true;
		}
		
		if(!(other instanceof State)){
			return false;
		}
		
		State so = (State)other;
		
		if(this.numTotalObjets() != so.numTotalObjets()){
			return false;
		}
		
		for(List <ObjectInstance> objects : objectsByTrueClass_){
			
			String oclass = objects.get(0).getTrueClassName();
			List <ObjectInstance> oobjects = so.getObjectsOfTrueClass(oclass);
			if(objects.size() != oobjects.size()){
				return false;
			}
			
			for(ObjectInstance o : objects){
				boolean foundMatch = false;
				for(ObjectInstance oo : oobjects){
					if(o.valueEquals(oo)){
						foundMatch = true;
						break;
					}
				}
				if(!foundMatch){
					return false;
				}
			}
			
		}
		
		
		return true;
	}
	
	
	public State parameterize(String [] params){
		if(params.length == 0){
			return this; //no parameters, it's the same state
		}
		
		//otherwise label them accordingly
		for(int i = 0; i < params.length; i++){
			this.parameterize(params[i], "param" + i);
		}
		
		return this;
		
	}
	
	public State parameterize(String [] params, String [] newClassNames){
		if(params.length == 0){
			return this; //no parameters, it's the same state
		}
		
		//otherwise label them accordingly
		for(int i = 0; i < params.length; i++){
			this.parameterize(params[i], newClassNames[i]);
		}
		
		return this;
		
	}
	
	public State deparameterize(String [] params){
		if(params.length == 0){
			return this; //no parameters, it's the same state
		}
		
		//otherwise label them accordingly
		for(int i = 0; i < params.length; i++){
			this.deparameterize(params[i]);
		}
		
		return this;
		
	}
	
	public void parameterize(String oname, String pseudoClassName){
		
		ObjectInstance o = objectMap_.get(oname);
		
		if(o == null){
			return ;
		}
		
		//first remove the object indexing under its previous class
		this.removeObjectClassIndexing(o);
		
		//now change the class name
		o.pushPseudoClass(pseudoClassName);
		
		//now reorganize the object indexing by its new class
		this.addObjectClassIndexing(o);
		
	}
	
	public void deparameterize(String oname){
		
		ObjectInstance o = objectMap_.get(oname);
		
		if(o == null){
			return ;
		}
		
		//first remove the object indexing under its current class
		this.removeObjectClassIndexing(o);
		
		//now change the class name to be what it was before this last parameterization
		o.popPseudoClass();
		
		//now reorganize the object indexing by its new class
		this.addObjectClassIndexing(o);
		
	}
	
	
	
	public int numTotalObjets(){
		return objectInstances_.size() + hiddenObjectInstances_.size();
	}
	
	public int numObservableObjects(){
		return objectInstances_.size();
	}
	
	public int numHiddenObjects(){
		return hiddenObjectInstances_.size();
	}
	
	public ObjectInstance getObject(String oname){
		return objectMap_.get(oname);
	}
	
	public ObjectInstance getObservableObjectAt(int i){
		if(i > objectInstances_.size()){
			return null;
		}
		return objectInstances_.get(i);
	}
	
	public ObjectInstance getHiddenObjectAt(int i){
		if(i > hiddenObjectInstances_.size()){
			return null;
		}
		return hiddenObjectInstances_.get(i);
	}
	
	public List <ObjectInstance> getObservableObjects(){
		return new ArrayList <ObjectInstance>(objectInstances_);
	}
	
	public List <ObjectInstance> getHiddenObjects(){
		return new ArrayList <ObjectInstance>(hiddenObjectInstances_);
	}
	
	public List <ObjectInstance> getAllObjects(){
		List <ObjectInstance> objects = new ArrayList <ObjectInstance>(objectInstances_);
		objects.addAll(hiddenObjectInstances_);
		return objects;
	}
	
	public List <ObjectInstance> getObjectsOfClass(String oclass){
		List <ObjectInstance> tmp = objectIndexByClass_.get(oclass);
		if(tmp == null){
			return new ArrayList <ObjectInstance>();
		}
		return new ArrayList <ObjectInstance>(tmp);
	}
	
	public List <ObjectInstance> getObjectsOfTrueClass(String oclass){
		List <ObjectInstance> tmp = objectIndexByTrueClass_.get(oclass);
		if(tmp == null){
			return new ArrayList <ObjectInstance>();
		}
		return new ArrayList <ObjectInstance>(tmp);
	}
	
	public List <List <ObjectInstance>> getAllObjectsByClass(){
		return new ArrayList <List <ObjectInstance>>(objectsByClass_);
	}
	
	public Set <String> getObjectClassesPresent(){
		return new HashSet<String>(objectClasses_);
	}
	
	public String getStateDescription(){
		
		String desc = "";
		for(ObjectInstance o : objectInstances_){
			desc = desc + o.getObjectDescription() + "\n";
		}
		
		return desc;
	
	}
	
	public String getCompleteStateDescription(){
		
		String desc = "";
		for(ObjectInstance o : objectInstances_){
			desc = desc + o.getObjectDescription() + "\n";
		}
		for(ObjectInstance o : hiddenObjectInstances_){
			desc = desc + o.getObjectDescription() + "\n";
		}
		
		
		return desc;
		
	}
	
	
	
	
	public List <GroundedAction> getAllGroundedActionsFor(Action a){
		
		List <GroundedAction> res = new ArrayList<GroundedAction>();
		
		if(a.getParameterClasses().length == 0){
			if(a.applicableInState(this, "")){
				res.add(new GroundedAction(a, new String[]{}));
			}
			return res; //no parameters so just the single ga without params
		}
		
		List <List <String>> bindings = this.getPossibleBindingsGivenClassRenames(a.getParameterClasses(), a.getReplacedClasses(), true);
		
		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
			if(a.applicableInState(this, aprams)){
				GroundedAction gp = new GroundedAction(a, aprams);
				res.add(gp);
			}
		}
		
		return res;
	}
	
	
	public List <GroundedProp> getAllGroundedPropsFor(PropositionalFunction pf){
		
		List <GroundedProp> res = new ArrayList<GroundedProp>();
		
		if(pf.getParameterClasses().length == 0){
			res.add(new GroundedProp(pf, new String[]{}));
			return res; //no parameters so just the single gp without params
		}
		
		List <List <String>> bindings = this.getPossibleBindingsGivenClassRenames(pf.getParameterClasses(), pf.getReplacedClasses(), true);
		
		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
			GroundedProp gp = new GroundedProp(pf, aprams);
			res.add(gp);
		}
		
		return res;
	}
	
	
	public List <List <String>> getPossibleBindingsGivenClassRenames(String [] paramClasses, String [] paramRenames, boolean useTrueClass){
		
		List <List <String>> res = new ArrayList <List<String>>();
		List <List <String>> currentBindingSets = new ArrayList <List<String>>();
		List <String> uniqueRenames = this.identifyUniqueClassesInParameters(paramRenames);
		List <String> uniqueParamClases = this.identifyUniqueClassesInParameters(paramClasses);
		
		Map <String, List <ObjectInstance>>	instanceMap = objectIndexByClass_;
		if(useTrueClass){
			instanceMap = objectIndexByTrueClass_;
		}
		
		//first make sure we have objects for each class parameter; if not return empty list
		for(String oclass : uniqueParamClases){
			int n = this.getNumOccurencesOfClassInParameters(oclass, paramClasses);
			List <ObjectInstance> objectsOfClass = instanceMap.get(oclass);
			if(objectsOfClass == null){
				return res;
			}
			if(objectsOfClass.size() < n){
				return res;
			}
		}
		
		this.getPossibleRenameBindingsHelper(res, currentBindingSets, 0, objectInstances_, uniqueRenames, paramClasses, paramRenames, useTrueClass);
		
		
		return res;
		
	}
	
	
	
	
	
	
	
	
	
	private void getPossibleRenameBindingsHelper(List <List <String>> res, List <List <String>> currentBindingSets, int bindIndex,
			List <ObjectInstance> remainingObjects, List <String> uniqueRenameClasses, String [] paramClasses, String [] paramRenames,
			boolean useTrueClass){
		
		if(bindIndex == uniqueRenameClasses.size()){
			//base case, put it all together and add it to the result
			res.add(this.getBindngFromCombinationSet(currentBindingSets, uniqueRenameClasses, paramRenames));
			return ;
		}
		
		//otherwise we're in the recursive case
		
		String r = uniqueRenameClasses.get(bindIndex);
		String c = this.parameterClassAssociatedWithRename(r, paramRenames, paramClasses);
		List <ObjectInstance> cands = this.objectsMatchingClass(remainingObjects, c, useTrueClass);
		int k = this.numOccurencesOfRename(r, paramRenames);
		List <List <String>> combs = this.getAllCombinationsOfObjects(cands, k);
		for(List <String> cb : combs){
			
			List <List<String>> nextBinding = new ArrayList<List<String>>(currentBindingSets.size());
			for(List <String> prevBind : currentBindingSets){
				nextBinding.add(prevBind);
			}
			nextBinding.add(cb);
			List <ObjectInstance> nextObsReamining = this.objectListDifference(remainingObjects, cb);
			
			//recursive step
			this.getPossibleRenameBindingsHelper(res, nextBinding, bindIndex+1, nextObsReamining, uniqueRenameClasses, paramClasses, paramRenames, useTrueClass);
			
		}
		
		
		
	}
	
	
	private List <ObjectInstance> objectListDifference(List <ObjectInstance> objects, List <String> toRemove){
		List <ObjectInstance> remaining = new ArrayList<ObjectInstance>(objects.size());
		for(ObjectInstance oi : objects){
			String oname = oi.getName();
			if(!toRemove.contains(oname)){
				remaining.add(oi);
			}
		}
		return remaining;
	}
	
	private int getNumOccurencesOfClassInParameters(String className, String [] paramClasses){
		int num = 0;
		for(int i = 0; i < paramClasses.length; i++){
			if(paramClasses[i].equals(className)){
				num++;
			}
		}
		return num;
	}
	
	private List <String> identifyUniqueClassesInParameters(String [] paramClasses){
		List <String> unique = new ArrayList <String>();
		for(int i = 0; i < paramClasses.length; i++){
			if(!unique.contains(paramClasses[i])){
				unique.add(paramClasses[i]);
			}
		}
		return unique;
	}
	
	
	
	private int numOccurencesOfRename(String rename, String [] renameParams){
		int num = 0;
		for(int i = 0; i < renameParams.length; i++){
			if(renameParams[i].equals(rename)){
				num++;
			}
		}
		
		return num;
		
	}
	
	private String parameterClassAssociatedWithRename(String rename, String [] renameParams, String [] params){
		for(int i = 0; i < renameParams.length; i++){
			if(renameParams[i].equals(rename)){
				return params[i];
			}
		}
		return "";
	}
	
	
	private List <ObjectInstance> objectsMatchingClass(List <ObjectInstance> sourceObs, String cname, boolean useTrueClass){
		
		List <ObjectInstance> res = new ArrayList<ObjectInstance>(sourceObs.size());
		
		for(ObjectInstance o : sourceObs){
			if(useTrueClass){
				if(o.getTrueClassName().equals(cname)){
					res.add(o);
				}
			}
			else{
				if(o.getPseudoClass().equals(cname)){
					res.add(o);
				}
			}
		}
		
		return res;
		
	}
	
	
	
	//for renaming parameter listing
	//comboSets: is a list of the bindings for each rename class. For instance, if the parameter renames were P, Q, P, Q, R; then there would be three lists
	//			one of the objects that are renamed to P, one for the objects renamed to Q, and one for the object renamed to R
	//renameClassesAssociateWithSet: which rename class each list of bindings in comboSets is for
	//paramRenames: the parameter binding class renames; indicates the index a binding should be for a binding of a specific rename class
	private List <String> getBindngFromCombinationSet(List <List <String>> comboSets, List <String> renameClassesAssociatedWithSet, String [] paramRenames){
		
		List <String> res = new ArrayList <String>(paramRenames.length);
		//add the necessary space first
		for(int i = 0; i < paramRenames.length; i++){
			res.add("");
		}
		
		//apply the parameter bindings for each rename combination
		for(int i = 0; i < comboSets.size(); i++){
			List <String> renameCombo = comboSets.get(i);
			String r = renameClassesAssociatedWithSet.get(i);
			
			//find the parameter indices that match this rename and set a binding accordingly
			int ind = 0;
			for(int j = 0; j < paramRenames.length; j++){
				if(paramRenames[j].equals(r)){
					res.set(j, renameCombo.get(ind));
					ind++;
				}
			}
		}
		
		return res;
	}
	
	
	List <List <String>> getAllCombinationsOfObjects(List <ObjectInstance> objects, int k){
		
		List <List<String>> allCombs = new ArrayList <List<String>>();
		
		int n = objects.size();
		int [] comb = this.initialComb(k, n);
		allCombs.add(this.getListOfBindingsFromCombination(objects, comb));
		while(nextComb(comb, k, n) == 1){
			allCombs.add(this.getListOfBindingsFromCombination(objects, comb));
		}
		
		return allCombs;
		
	}
	
	
	
	List <String> getListOfBindingsFromCombination(List <ObjectInstance> objects, int [] comb){
		List <String> res = new ArrayList <String>(comb.length);
		for(int i = 0; i < comb.length; i++){
			res.add(objects.get(comb[i]).getName());
		}
		return res;
	}
	
	
	private int [] initialComb(int k, int n){
		int [] res = new int[k];
		for(int i = 0; i < k; i++){
			res[i] = i;
		}
		
		return res;
	}
	
	private int nextComb(int [] comb, int k, int n){
		
		int i = k-1;
		comb[i]++;
		
		while(i > 0 && comb[i] >= n-k+1+i){
			i--;
			comb[i]++;
		}
		
		if(comb[0] > n-k){
			return 0;
		}
		
		/* comb now looks like (..., x, n, n, n, ..., n).
		Turn it into (..., x, x + 1, x + 2, ...) */
		for(i = i+1; i < k; i++){
			comb[i] = comb[i-1] + 1;
		}
		
		return 1;
	}
	
	//DEPRICATED BINDING CODE THAT SHOULD NEVER BE USED AGAIN
	//LEFT IN COMMENTS FOR REFERENCE PURPOSES ONLY
	
	/*
	
	//will return a list of all the possible true class parameter bindings that can be made in this state given the parameterized classes
	public List <List <String>> getPossibleBindingsOnTrueClass(String [] paramClasses, boolean combinatorial){
		
		List <List <String>> res = new ArrayList <List <String>>();
		String [] bindings = new String[paramClasses.length];
		
		//first make sure we have objects for each class parameter; if not return empty list
		for(int i = 0; i < paramClasses.length; i++){
			if(!objectTrueClasses_.contains(paramClasses[i])){
				return res;
			}
		}
		
		if(combinatorial){
			return this.getPossibleBindingsCombintorialStarter(paramClasses, objectIndexByTrueClass_);
		}
		else{
			this.getPossibleBindingsHelper(res, bindings, 0, paramClasses, objectIndexByTrueClass_);
		}
		
		return res;
	}
	
	//will return a list of all the possible pseudo class parameter bindings that can be made in this state given the parameterized classes
	public List <List <String>> getPossibleBindingsOnPseudoClass(String [] paramClasses, boolean combinatorial){
		
		List <List <String>> res = new ArrayList <List <String>>();
		String [] bindings = new String[paramClasses.length];
		
		//first make sure we have objects for each class parameter; if not return empty list
		for(int i = 0; i < paramClasses.length; i++){
			if(!objectClasses_.contains(paramClasses[i])){
				return res;
			}
		}
		
		if(combinatorial){
			return this.getPossibleBindingsCombintorialStarter(paramClasses, objectIndexByClass_);
		}
		else{
			this.getPossibleBindingsHelper(res, bindings, 0, paramClasses, objectIndexByClass_);
		}
		
		return res;
	}
	
	*/
	
	/*
	private void getPossibleBindingsHelper(List <List <String>> res, String [] currentBindings, int indexToBind, String [] paramClasses, Map <String, List <ObjectInstance>> classMapping){
		
		//base case is that the currentBindings has been filled completely already, in which case we packaged it up and add it to our list
		if(indexToBind == currentBindings.length){
			
			//do not reuse the current bindings, package it up into a list and add it to our result
			List <String> aBinding = new ArrayList <String>(currentBindings.length);
			for(int i = 0; i < currentBindings.length; i++){
				aBinding.add(currentBindings[i]);
			}
			
			res.add(aBinding);
			return;
			
		}
		else{
			
			//otherwise, consider all the possible bindings for this parameter and couple them recursively with the possible ones
			//for the subsequent parameters
			String className = paramClasses[indexToBind];
			
			//get all the objects of that class
			List <ObjectInstance> objectsOfClass = classMapping.get(className);
			
			//try bindings for each object of this class
			for(ObjectInstance o : objectsOfClass){
				
				//make sure this object hasn't already been bound to a previous parameter
				String oname = o.getName();
				boolean useBinding = true;
				for(int j = indexToBind-1; j >= 0; j--){
					if(currentBindings[j].equals(oname)){
						useBinding = false;
						break;
					}
				}
				if(!useBinding){
					continue;
				}
				
				//set this as a possible binding and find bindings for the rest of the parameters
				currentBindings[indexToBind] = oname;
				this.getPossibleBindingsHelper(res, currentBindings, indexToBind+1, paramClasses, classMapping);
				
			}
			
			
		}
		
	}
	
	private List <List <String>> getPossibleBindingsCombintorialStarter(String [] paramClasses, Map <String, List <ObjectInstance>> classMapping){
		
		List <String> uniqueClasses = this.identifyUniqueClassesInParameters(paramClasses);
		
		List <List <List <String>>> allClassWiseCombos = new ArrayList <List <List <String>>>();
		for(String oclass : uniqueClasses){
			List <ObjectInstance> objects = classMapping.get(oclass);
			List <List <String>> classWiseCombos = this.getAllCombinationsOfObjects(objects, this.getNumOccurencesOfClassInParameters(oclass, paramClasses));
			allClassWiseCombos.add(classWiseCombos);
		}
		
		int [] currentBindings = new int[uniqueClasses.size()];
		List <List <String>> bindings = new ArrayList <List <String>>();
		this.getPossibleBindingsCombintorialHelper(bindings, currentBindings, 0, allClassWiseCombos, uniqueClasses, paramClasses);
		
		return bindings;
	}
	
	private void getPossibleBindingsCombintorialHelper(List <List <String>> res, int [] currentBindings, int indexToBind, List <List <List <String>>> allClassWiseCombos, List <String> classOrder, String [] paramClasses){
		
		//base case is that the currentBindings has been filled completely already, in which case we packaged it up and add it to our list
		if(indexToBind == currentBindings.length){
			
			List <List <String>> comboSets = new ArrayList <List <String>>(currentBindings.length);
			for(int i = 0; i < currentBindings.length; i++){
				List <String> combo = allClassWiseCombos.get(i).get(currentBindings[i]);
				comboSets.add(combo);
			}
			
			//now that we have the binded combo sets, get a full binding
			List <String> binding = this.getBindingFromCombinationSet(comboSets, classOrder, paramClasses);
			res.add(binding);
			
		}
		else{
			
			//otherwise, consider all the possible bindings for this parameter and couple them recursively with the possible ones
			//for the subsequent parameters
			List <List <String>> possibleCombosForClass = allClassWiseCombos.get(indexToBind);
			for(int i = 0; i < possibleCombosForClass.size(); i++){
				currentBindings[indexToBind] = i;
				this.getPossibleBindingsCombintorialHelper(res, currentBindings, indexToBind+1, allClassWiseCombos, classOrder, paramClasses);
			}
			
			
		}
		
		
	}
	*/
	
	
	/*
	private List <String> getBindingFromCombinationSet(List <List <String>> comboSets, List <String> classesAssociatedWithSet, String [] paramClasses){
		
		List <String> binding = new ArrayList<String>(paramClasses.length);
		for(int i = 0; i < classesAssociatedWithSet.size(); i++){
			int ind = 0;
			List <String> classBindings = comboSets.get(i);
			for(int j = 0; j < paramClasses.length; j++){
				if(paramClasses[j].equals(classesAssociatedWithSet.get(i))){
					binding.add(classBindings.get(ind));
					ind++;
				}
			}
		}
		
		return binding;
		
	}
	*/
	
	
	
}
