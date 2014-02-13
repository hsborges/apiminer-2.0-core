package org.apiminer.recommendation.associations;

import java.util.Comparator;
import java.util.Map;

import org.apiminer.entities.example.AssociatedElement;
import org.apiminer.entities.mining.RuleMetrics;

public class AssociationsComparator implements Comparator<AssociatedElement> {

	@Override
	public int compare(AssociatedElement o1, AssociatedElement o2) {
		Map<String, Double> metricsRE1 = o1.getRuleBased().getMetrics();
		Map<String, Double> metricsRE2 = o2.getRuleBased().getMetrics();
		
		Double liftRE1 = metricsRE1.get(RuleMetrics.LIFT.name());
		Double liftRE2 = metricsRE2.get(RuleMetrics.LIFT.name());
		
		if (liftRE1 == liftRE2) {
			Double jaccardRE1 = metricsRE1.get(RuleMetrics.JACCARD.name());
			Double jaccardRE2 = metricsRE2.get(RuleMetrics.JACCARD.name());
			if (jaccardRE1 > jaccardRE2) {
				return 1;
			} else if (jaccardRE1 < jaccardRE2) {
				return -1;
			} else {
				return 0;
			}
		} else {
			if (liftRE1 > liftRE2) {
				return 1;
			} else {
				return -1;
			}
		}
	}


}
