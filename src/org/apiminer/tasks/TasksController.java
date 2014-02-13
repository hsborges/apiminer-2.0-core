package org.apiminer.tasks;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apiminer.SystemProperties;


/**
 * Class to organize the execution of all tasks of the system. 
 * 
 * @author Hudson S. Borges
 *
 */
public final class TasksController {
		
	private static final Logger LOGGER = Logger.getLogger(TasksController.class);
	
	/**
	 * Number of available processors to the system. 
	 */
	private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
	
	/**
	 * Maximum number of threads to parallel execution.
	 */
	private int maxThreads = SystemProperties.MAX_THREADS;
	
	/**
	 * Boolean value to indicate if the controller is stopped.
	 */
	private boolean stopped = false;

	private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(maxThreads, maxThreads, 1, TimeUnit.HOURS, new ArrayBlockingQueue<Runnable>(maxThreads,true), new ThreadPoolExecutor.AbortPolicy());	
	
	/**
	 *  Queue to store the threads.
	 */
	private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();
	
	/**
	 * Unique instance of controller to the system.
	 */
	private static final TasksController instance = new TasksController();
	
	private TasksController(){
		new Thread(new Robot()).start();
		Runtime.getRuntime().addShutdownHook(new Thread(new TasksControllerHook()));
	}
	
	public synchronized void stop(){
		this.stopped = true;
		this.threadPoolExecutor.shutdownNow();
		this.threadPoolExecutor.purge();
		LOGGER.info("Threads controller ended.");
	}
	
	public synchronized void addTask(final AbstractTask task){
		if ( task == null ) {
			throw new IllegalArgumentException("The task must be not null.");
		}
		
		Runnable runnableTask = new Runnable() {
			@Override
			public void run() {
				task.execute();
			}
			@Override
			public String toString() {
				return task.toString();
			}
		};
		
		this.queue.add(runnableTask);
		LOGGER.debug("New Task added to the queue.");
	}	
		

	public synchronized void addTasks(Collection<AbstractTask> tasks){
		if ( tasks == null ) {
			throw new IllegalArgumentException("The tasks must be not null.");
		}
		
		LinkedList<Runnable> runnables = new LinkedList<Runnable>();
		for (final AbstractTask task : tasks){
			Runnable runnableTask = new Runnable() {
				@Override
				public void run() {
					task.execute();
				}
				@Override
				public String toString() {
					return task.toString();
				}
			};
			runnables.add(runnableTask);
		}

		this.queue.addAll(runnables);
		LOGGER.debug("New Tasks added to the queue.");
		
	}
		
	public synchronized int numberOfRunningTask(){
		return this.threadPoolExecutor.getActiveCount();
	}
	
	public synchronized int numberOfQueuedTasks(){
		return this.queue.size();
	}
	
	/**
	 * 
	 * 
	 * @author Hudson S. Borges
	 *
	 */
	private class Robot implements Runnable {
		@Override
		public void run() {
			
			LOGGER.debug("ThreadExecuter started.");
			while (!stopped) {
				Runnable task = null;
				
				while (task == null && !stopped){
					if (queue.isEmpty()) {
						try {
							Thread.sleep(1*1000);
						} catch (InterruptedException e) {}
					}
					task = queue.poll();
				}
				
				boolean execute = false; 
				while(!execute && !stopped){
					try{
						threadPoolExecutor.execute(task);
						LOGGER.debug("Task \""+task.toString()+"\" inserted in the pool.");
						execute = true;
					}catch(RejectedExecutionException exception){
						execute = false;
					}
				}
				
			}
			
			LOGGER.debug("ThreadExecuter finished.");
		}
		
	}
	
	public synchronized static TasksController getInstance(){
		return instance;
	}

	/**
	 * @return the maxThreads
	 */
	public synchronized int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * @return the stopped
	 */
	public synchronized boolean isStoped() {
		return stopped;
	}
	
	public synchronized boolean hasTasks(){
		return (this.queue.size() + this.threadPoolExecutor.getActiveCount()) > 0;
	}

	public synchronized void setMaxThreads(int maxThreads) {
		if ( maxThreads < 1 || maxThreads > NUM_CORES ) {
			throw new IllegalArgumentException("The number of threads should be between 1 and "+NUM_CORES);
		}
		
		this.maxThreads = maxThreads;
		this.threadPoolExecutor.setCorePoolSize(maxThreads);
		this.threadPoolExecutor.setMaximumPoolSize(maxThreads);
	}

}
