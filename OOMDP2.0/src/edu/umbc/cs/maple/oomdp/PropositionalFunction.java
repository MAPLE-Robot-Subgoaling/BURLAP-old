package edu.umbc.cs.maple.oomdp;


public abstract class PropositionalFunction {

	protected String					name_;					//name of the propositional function
	protected Domain					domain_;				//domain that hosts this function
	protected String []					parameterClasses_;		//list of class names for each parameter of the function
	protected String []					replacedClassNames_;	//list of class names to which each parameter object should be paramaterized
	protected String					pfClass_;
	
	
	//parameterClasses is expected to be comma delimited with no unnecessary spaces
	public PropositionalFunction(String name, Domain domain, String parameterClasses){
		
		String [] pClassArray;
		if(parameterClasses.equals("")){
			pClassArray = new String[0];
		}
		else{
			pClassArray = parameterClasses.split(",");
		}
		
		String [] rcn = new String[pClassArray.length];
		for(int i = 0; i < rcn.length; i++){
			rcn[i] = name + ".P" + i;
		}
		
		this.init(name, domain, pClassArray, rcn, name);
		
	}
	
	public PropositionalFunction(String name, Domain domain, String parameterClasses, String pfClassName){
		
		String [] pClassArray;
		if(parameterClasses.equals("")){
			pClassArray = new String[0];
		}
		else{
			pClassArray = parameterClasses.split(",");
		}
		
		String [] rcn = new String[pClassArray.length];
		for(int i = 0; i < rcn.length; i++){
			rcn[i] = name + ".P" + i;
		}
		
		this.init(name, domain, pClassArray, rcn, pfClassName);
		
	}
	
	public PropositionalFunction(String name, Domain domain, String [] parameterClasses){
		
		String [] rcn = new String[parameterClasses.length];
		for(int i = 0; i < rcn.length; i++){
			rcn[i] = name + ".P" + i;
		}
		
		this.init(name, domain, parameterClasses, rcn, name);
		
	}
	
	public PropositionalFunction(String name, Domain domain, String [] parameterClasses, String pfClassName){
		
		String [] rcn = new String[parameterClasses.length];
		for(int i = 0; i < rcn.length; i++){
			rcn[i] = name + ".P" + i;
		}
		
		this.init(name, domain, parameterClasses, rcn, pfClassName);
		
	}
	
	public PropositionalFunction(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames){
		this.init(name, domain, parameterClasses, replacedClassNames, name);
	}
	
	public PropositionalFunction(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames, String pfClassName){
		this.init(name, domain, parameterClasses, replacedClassNames, pfClassName);
	}
	
	public final void init(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames, String pfClass){
		name_ = name;
		domain_ = domain;
		domain_.addPropositionalFunction(this);
		parameterClasses_ = parameterClasses;
		replacedClassNames_ = replacedClassNames;
		pfClass_ = pfClass;
	}
	
	public final String getName(){
		return name_;
	}
	
	
	public final String[] getParameterClasses(){
		return parameterClasses_;
	}
	
	public final String[] getReplacedClasses(){
		return replacedClassNames_;
	}
	
	public final void setClassName(String cn){
		pfClass_ = cn;
	}
	
	public final String getClassName(){
		return pfClass_;
	}
	
	//params is expected to be comma delimited with no unnecessary spaces
	public final boolean isTrue(State st, String params){
		return isTrue(st, params.split(","));
	}
	
	
	
	public abstract boolean isTrue(State st, String [] params);
	
	
	public boolean equals(Object obj){
		PropositionalFunction op = (PropositionalFunction)obj;
		if(op.name_.equals(name_))
			return true;
		return false;
	}
	
	public String toString() {
		return this.name_;
	}

	public int hashCode(){
		return name_.hashCode();
	}
	
	
}
