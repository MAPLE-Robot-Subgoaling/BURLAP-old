package domain.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import burlap.behavior.policyblocks.AbstractedPolicy;

public class ExperimentUtils {
    public static double sum(List<Double> l) {
	Double sum = 0.;

	for (Double elem : l) {
	    sum += elem;
	}

	return sum;
    }

    public static void main(String[] args) {
	List<String> objects = new ArrayList<String>(10);
	List<String> objects1 = new ArrayList<String>(5);
	List<String> objects2 = new ArrayList<String>(5);
	objects.add("room0");
	objects.add("room1");
	objects.add("room2");
	objects1.add("door0");
	objects1.add("door1");
	objects1.add("door2");
	objects2.add("block0");
	objects2.add("block1");
	objects2.add("block2");
	objects2.add("agent0");

	List<List<String>> stuff = AbstractedPolicy.permutations(objects);

	System.out.println(stuff);
	Collections.shuffle(stuff);
	System.out.println(stuff);
    }
}
