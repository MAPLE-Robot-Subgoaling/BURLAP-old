package burlap.behavior.singleagent.pod;

import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Attribute;

public class AbstractedTest {
    public static void main(String[] args) {
	final short RED = 1;
	final short BLUE = 2;
	final short GREEN = 3;
	
	final short CIRCLE = 1;
	final short SQUARE = 2;
	final short TRIANGLE = 3;
	
	State s1 = new State();
	
	ObjectClass blockClass = new ObjectClass(null, "block");
	blockClass.addAttribute(new Attribute(null, "x", AttributeType.REAL));
	blockClass.addAttribute(new Attribute(null, "y", AttributeType.REAL));
	blockClass.addAttribute(new Attribute(null, "color", AttributeType.DISC));
	blockClass.addAttribute(new Attribute(null, "shape", AttributeType.DISC));
	
	ObjectInstance o1s1 = new ObjectInstance(blockClass, "block1");
	
	ObjectInstance o2s1 = new ObjectInstance(blockClass, "block2");
	
	s1.addObject(o1s1);
	s1.addObject(o2s1);
    }
}
