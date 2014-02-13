package org.apiminer.util;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;

public class LoggerUtil {

	/**
	 * Enable logging 
	 */
	public synchronized static final void logEvents() {
		try {
			LogManager.getRootLogger().setLevel(Level.DEBUG);
			LogManager.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %5p - %m%n")));
			LogManager.getRootLogger().addAppender(new FileAppender(new PatternLayout("%d{ABSOLUTE} %5p %c{1}:%L - %m%n"), "apiminer-log.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
}
