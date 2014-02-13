package org.apiminer.recommendation.examples;

import java.util.Comparator;

import org.apiminer.entities.example.Example;

public class ExampleComparator implements Comparator<Example> {

	@Override
	public int compare(Example o1, Example o2) {
		int numProblems1 = o1.getProblems().size();
		int numProblems2 = o2.getProblems().size();
		if (numProblems1 != numProblems2) {
			if (numProblems1 < numProblems2) {
				return 1;
			} else {
				return -1;
			}
		} else {
			String[] size1 = o1.getFormattedCodeExample().split("\n");
			String[] size2 = o2.getFormattedCodeExample().split("\n");
			if (size1.length < size2.length) {
				return 1;
			} else if (size1.length > size2.length) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
