package domain.experiments;

import java.util.List;

public class ExperimentUtils {
    public static double sum(List<Double> l) {
	Double sum = 0.;
	
	for(Double elem: l) {
	    sum += elem;
	}
	
	return sum;
    }
}
