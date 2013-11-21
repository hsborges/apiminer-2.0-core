package org.apiminer.util.miner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.util.LoggerUtil;
import org.apiminer.util.WekaUtil;

import weka.associations.FPGrowth;
import weka.associations.FPGrowth.AssociationRule;
import weka.associations.FPGrowth.BinaryItem;
import weka.core.Attribute;
import weka.core.Instances;

import com.csvreader.CsvWriter;

//TODO improve it
public class CoverageDataExporter {

	private static final Logger LOGGER = Logger.getLogger(CoverageDataExporter.class);

	private CsvWriter csvWriter = null;

	private int maxNumberOfItens = 4;

	private float decreaseFactor = 0.05f;

	private float[] confidenceValues = new float[]{new Float(0.70), new Float(0.80), new Float(0.90), new Float(0.95)};

	private long minSupport = 10;

	public CoverageDataExporter(OutputStream outputStream) throws IOException {
		csvWriter = new CsvWriter(outputStream, ',', Charset.forName("UTF-8"));
		csvWriter.write("Absolute_Support");
		csvWriter.write("Relative_Support");
		csvWriter.write("Confidence");
		csvWriter.write("#Rules");
		csvWriter.write("#Transactions");
		csvWriter.write("#Distinct_Elements_in_Transactions");
		csvWriter.write("#Premises_Where_Size=1");
		csvWriter.write("#Premises_Where_Size=2");
		csvWriter.write("#Premises_Where_Size=3");
		csvWriter.write("#Premises_Where_Size>3");
		csvWriter.write("Coverage of premisses_of_size_equals_1");
		csvWriter.write("Coverage_of_premisses_of_size_equals_2");
		csvWriter.endRecord();
		csvWriter.flush();
	} 
    
	public void export(long projectId) {
		Arrays.sort(confidenceValues);

		LOGGER.info("Obtendo instência para o weka");
		Instances instances = WekaUtil.getSparseInstanceWeka(null, false);
		
		long numOfValidTransactions = instances.numInstances();

		String[] row = new String[12];
		
		for (float confidence : confidenceValues) {

			long support = numOfValidTransactions;
			while(support >= minSupport) {
				LOGGER.info(String.format("Avaliando para o valor de suporte igual a %d", support));
	
				row[0] = Long.toString(support);
				row[1] = Double.toString((double)support / numOfValidTransactions);
				row[2] = Float.toString(confidence);

				// Parâmetros copiados do original
				FPGrowth growth = new FPGrowth();
				growth.setLowerBoundMinSupport((double)support / numOfValidTransactions);
				growth.setMinMetric(confidence);
				growth.setPositiveIndex(2);
				growth.setMaxNumberOfItems(maxNumberOfItens);
				growth.setNumRulesToFind(Integer.MAX_VALUE);
				growth.setFindAllRulesForSupportLevel(true);

				try {
					growth.buildAssociations(instances);

					row[3] = Integer.toString(growth.getAssociationRules().size());
					row[4] = Integer.toString(instances.numInstances());
					row[5] = Integer.toString(instances.numAttributes());

					int numRulesWherePremiseSizeEqualsTo1 = 0;
					int numRulesWherePremiseSizeEqualsTo2 = 0;
					int numRulesWherePremiseSizeEqualsTo3 = 0;
					int numRulesWherePremiseSizeGreaterThan3 = 0;

					Set<Attribute> apiMethods1 = new HashSet<Attribute>();
					Set<Attribute> apiMethods2 = new HashSet<Attribute>();

					for(AssociationRule ar : growth.getAssociationRules()){
						if (ar.getPremise().size() == 1){
							numRulesWherePremiseSizeEqualsTo1++;
							for (BinaryItem binaryItem : ar.getPremise()) {
								apiMethods1.add(binaryItem.getAttribute());
							}
						}else if (ar.getPremise().size() == 2){
							numRulesWherePremiseSizeEqualsTo2++;
							for (BinaryItem binaryItem : ar.getPremise()) {
								apiMethods2.add(binaryItem.getAttribute());
							}
						}else if (ar.getPremise().size() == 3){
							numRulesWherePremiseSizeEqualsTo3++;
						}else{
							numRulesWherePremiseSizeGreaterThan3++;
						}
					}

					row[6] = Integer.toString(numRulesWherePremiseSizeEqualsTo1);
					row[7] = Integer.toString(numRulesWherePremiseSizeEqualsTo2);
					row[8] = Integer.toString(numRulesWherePremiseSizeEqualsTo3);
					row[9] = Integer.toString(numRulesWherePremiseSizeGreaterThan3);

					int coverageOfRulesWithPremiseSizeEqualsTo1 = apiMethods1.size();
					int coverageOfRulesWithPremiseSizeEqualsTo2 = apiMethods2.size();

					row[10] = Integer.toString(coverageOfRulesWithPremiseSizeEqualsTo1);
					row[11] = Integer.toString(coverageOfRulesWithPremiseSizeEqualsTo2);

					LOGGER.info("Resultado: "+Arrays.toString(row));
					csvWriter.writeRecord(row);
					csvWriter.flush();

				} catch (Throwable e) {
					e.printStackTrace();
					try {
						csvWriter.writeComment(e.getMessage());
						csvWriter.endRecord();
					} catch (IOException e1) {}
					LOGGER.error(e.getLocalizedMessage());
				}

				support *= (1f - decreaseFactor);
//				support--;
			}
			
		}

		LOGGER.info("Processo finalizado com sucesso!");

	}

	@Override
	protected void finalize() throws Throwable {
		csvWriter.flush();
		csvWriter.close();
		super.finalize();
	}

	public static void main(String args[]) throws FileNotFoundException, IOException{
		LoggerUtil.logEvents();
		CoverageDataExporter exporter = new CoverageDataExporter(new FileOutputStream("/home/hudson/output.csv", true));
		exporter.export(1);
	}

}
