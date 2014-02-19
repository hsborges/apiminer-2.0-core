package org.apiminer.util.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apiminer.entities.api.Repository;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RepositoryUtil {
	
	@SuppressWarnings("unchecked")
	public static List<Repository> parse(String inputFile) throws FileNotFoundException {
		final File file = new File(inputFile);
		if (!file.exists() || file.isDirectory()) {
			throw new FileNotFoundException();
		}
		
		try {
			return new ObjectMapper().readValue(file, new LinkedList<Repository>().getClass());
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void export(String outputFile, List<Repository> repositories) {
		if (new File(outputFile).exists()) {
			throw new IllegalArgumentException("The output file already exists!");
		}
		
		try {
			final FileOutputStream out = new FileOutputStream(outputFile, false);
			new ObjectMapper().writeValue(out, repositories);
			out.flush();
			out.close();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
