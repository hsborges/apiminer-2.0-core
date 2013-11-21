package org.apiminer.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiminer.daos.ApiMethodDAO;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.mining.Transaction;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;

public class WekaUtil {

	/**
	 * Get a sparse instance in Weka format.
	 * 
	 * @param minimumSupport the minimum support value of a method to be included
	 * @param useFullName use the method name instead of the id
	 * @return a sparse instance in Weka format
	 */
	public synchronized static Instances getSparseInstanceWeka(Integer minimumSupport, boolean useFullName) {
		List<ApiMethod> methodIds = new ApiMethodDAO().findUsedMethods(minimumSupport);

		FastVector attributeVector = new FastVector(methodIds.size());
		Map<Long, Attribute> attributeMap = new HashMap<Long, Attribute>();

		for (ApiMethod apm : methodIds) {
			FastVector fastVector = new FastVector();
			fastVector.addElement("0");
			fastVector.addElement("1");

			String attributeName = useFullName ? apm.getFullName() : Long.toString(apm.getId());
			Attribute attribute = new Attribute(attributeName, fastVector);

			attributeVector.addElement(attribute);
			attributeMap.put(apm.getId(), attribute);
		}

		Instances instances = new Instances("API Usage Info", attributeVector, attributeVector.size());

		List<Transaction> transactions = new ProjectDAO().findTransactions(2);

		for (Transaction transaction : transactions) {
			SparseInstance instance = new SparseInstance(1, new double[attributeVector.size()]);
			instance.setDataset(instances);

			for (ApiMethod invocation : transaction.getInvocations()) {
				Attribute atr = attributeMap.get(invocation.getId());
				if (atr != null) {
					instance.setValue(atr, "1");
				}
			}

			instances.add(instance);
		}

		return instances;
	}

}
