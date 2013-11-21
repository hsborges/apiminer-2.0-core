package org.apiminer.util.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apiminer.daos.ApiMethodDAO;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.mining.Transaction;
import org.apiminer.util.WekaUtil;

import weka.core.converters.ArffSaver;

import com.csvreader.CsvWriter;

public class TransactionsUtil {
	
	private static final Comparator<ApiMethod> METHOD_ID_COMPARATOR = new Comparator<ApiMethod>() {
		@Override
		public int compare(ApiMethod o1, ApiMethod o2) {
			return o1.getId().compareTo(o2.getId());
		}
	};

	/**
	 * Export in Arff Weka format.
	 * 
	 * @param outputStream
	 * @throws IOException
	 */
	public static synchronized void exportToArff(final OutputStream outputStream) throws IOException{
		ArffSaver arffSaver = new ArffSaver();
		arffSaver.setInstances(WekaUtil.getSparseInstanceWeka(null, true));
		arffSaver.setDestination(outputStream);
		arffSaver.writeBatch();
	}
	
	/**
	 * Export in CSV format.
	 * 
	 * @param outputStream
	 * @throws IOException
	 */
	public static synchronized void exportToCSV(final OutputStream outputStream) throws IOException{
		CsvWriter csvWriter = new CsvWriter(outputStream,',',Charset.forName("UTF-8"));
		ArrayList<ApiMethod> apiMethodsList = new ArrayList<ApiMethod>(new ApiMethodDAO().findUsedMethods(1));

		String[] row = new String[apiMethodsList.size()];
		for (int i = 0; i < apiMethodsList.size(); i++) {
			row[i] = apiMethodsList.get(i).getFullName();
		}
		
		csvWriter.writeRecord(row);
		
		List<Project> apiClients = new ProjectDAO().findAllClients(DatabaseType.PRE_PROCESSING);
		for(Project client : apiClients ){
			for (ApiClass apc : client.getApiClass()) {
				for (ApiMethod apm : apc.getApiMethods()) {
					if ( apm.getTransactions() == null || apm.getTransactions().isEmpty()) {
						continue;
					}
					
					for (Transaction transaction : apm.getTransactions()) {
						row = new String[apiMethodsList.size()];
						Arrays.fill(row,"");
						
						for ( ApiMethod invocation : transaction.getInvocations() ) {
							int pos = Collections.binarySearch(apiMethodsList, invocation, METHOD_ID_COMPARATOR);
							if ( pos >= 0 ) {
								row[pos] = Boolean.toString(true);
							}
						}
						
						csvWriter.writeRecord(row);
					}
				}
			}
		}
		
		csvWriter.close();
	}
	
}
