package com.wwc2.networks.server.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

	private static ExecutorService executorService;

	private static ExecutorService newInstance(){
		if(executorService == null){
			executorService = new ThreadPoolExecutor(
					0, Integer.MAX_VALUE,
					60L, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>()
			);
		}
		return executorService;
	}

	public static void start(Runnable runnable){
		if(executorService == null){
			executorService = newInstance();
		}
		executorService.submit(runnable);
	}

	public static void clearThreadsta(){
		if(executorService != null){
			executorService.shutdown();
			executorService = null;
		}
	}
}
