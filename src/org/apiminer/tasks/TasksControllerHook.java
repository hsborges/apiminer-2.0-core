package org.apiminer.tasks;

import org.apache.log4j.Logger;
import org.apiminer.util.DatabaseUtil;

/**
 * Hook to finalize the controller of tasks.
 * 
 * @author Hudson S. Borges
 *
 */
public class TasksControllerHook implements Runnable {
	
	private static final Logger LOGGER = Logger.getLogger(TasksControllerHook.class);

	@Override
	public void run() {
		LOGGER.info("Shutdown Hook called");
		TasksController.getInstance().stop();
		
		try {
			DatabaseUtil.closeConnections();
		} catch (Exception e) {
			LOGGER.error(e);
		}
		
	}

}
