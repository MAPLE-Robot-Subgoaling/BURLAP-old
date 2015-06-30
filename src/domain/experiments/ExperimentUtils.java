package domain.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
	Random rand = new Random();
	for (int i = 0; i < 7; i++) {
	    System.out.println("i=" + i);
	    for (int j = 0; j < 10; j++) {
		System.out.println("j=" + j);
		if (i < 4) {
		    // 0[1,5], 1[1,4], 2[1,3], 3[1,2]
		    int r = 50 - (i * 10);
		    System.out.println(r - 1);
		} else {
		    // 4[2, 5], 5[3, 5], 6[4, 5]
		    int k = 10 + (i - 4) * 10;
		    int r = 49 - k;
		    System.out.println(k);
		    System.out.println(k + r);
		}
	    }
	}
    }
}
