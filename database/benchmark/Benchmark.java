package database.benchmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.Timer;

import org.jgrapht.Graph;

import algorithms.koch.Koch;
import algorithms.mcgregor.Mcgregor;

import shared.StringLabeledObject;

import database.DatabasePair;
import database.DatabaseReader;
import database.generator.DatabaseCreator;

public class Benchmark {
	
	public static final long MILLIS_IN_MINUTE = 1000 * 60;
	
	private int repetitions;
	private Writer csv;
	private double maxRelativeStandardDeviation;
	Algorithm currentAlgorithm;
	private Timer timer;
	private long timeout;
	
	protected enum Algorithm{ KOCH, MCGREGOR};
	
	private int[] retryKoch = null;
	private int[] retryMcgregor = null;//{901,902,904,930,626,627,629,632,634,635,637,639,647,710,718,720,723,812,813,814,817,822,823,836,919,926};
	
	public static void main(String[] args){
		new Benchmark(10, 0.2, 3 * MILLIS_IN_MINUTE).startBenchmark();
	}
	
	public Benchmark(int repetitions, double maxRelativeStandardDeviation, long timeout){
		if(repetitions < 1) throw new IllegalArgumentException("repetitions < 1");
		if(maxRelativeStandardDeviation <= 0) throw new IllegalArgumentException("maxRelativeStandardDeviation <= 0");
		this.repetitions = repetitions;
		this.maxRelativeStandardDeviation = maxRelativeStandardDeviation;
		String write = "";
		int i;
		this.timeout = timeout;
		csv = null;
		
		try {
			File resultsFile = new File(DatabaseReader.databaseDir, "results.csv");
			if(!resultsFile.exists()){
				write = "Algorithm;connected;GraphType;number;directed;label alphabet size;Supergraph Size(edges);"
					+"Generated Subgraph Size(%);Aditional parameters";
				for(i=0; i<repetitions;i++){
					write+=";time (ns)";
				}
				write +=";average;std deviation;relative standard deviation";
				write +="\r\n";
			}
			csv = new OutputStreamWriter(new FileOutputStream(resultsFile, true));
			csv.write(write);
			csv.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
	}
	
	public void startBenchmark(){
		boolean retry = (retryKoch != null && retryKoch.length > 0) || (retryMcgregor != null && retryMcgregor.length > 0);
		if(retry){
			System.err.println("Retry mode enabled");
			if(retryKoch == null){
				retryKoch = new int[0];
			}
			else if (retryMcgregor == null){
				retryMcgregor = new int[0];
			}
			Arrays.sort(retryKoch);
			Arrays.sort(retryMcgregor);
		} else {
			System.err.println("Running full benchmark");
		}
		for(DatabasePair pair: new DatabaseReader()){
			if(!retry){
				System.out.println(pair.getSourceDirectory().getName() + " " + pair.getGraphPairNumber());
				for(Algorithm a : Algorithm.values()){
					benchmarkPair(pair, a);
				}
			} else {
				if(Arrays.binarySearch(retryKoch, pair.getGraphPairNumber()) >= 0){
					System.out.println(pair.getSourceDirectory().getName() + " " + pair.getGraphPairNumber());
					benchmarkPair(pair, Algorithm.KOCH);
				}
				if(Arrays.binarySearch(retryMcgregor, pair.getGraphPairNumber()) >= 0){
					System.out.println(pair.getSourceDirectory().getName() + " " + pair.getGraphPairNumber());
					benchmarkPair(pair, Algorithm.MCGREGOR);
				}
			}
		}
	}
	
	private void benchmarkPair(DatabasePair pair, Algorithm algorithm){
		currentAlgorithm = algorithm;
		System.out.println(DatabaseCreator.getTimeString());
		Graph<StringLabeledObject, StringLabeledObject> subgraph=null;
		long[] time = new long[repetitions];
		String write;
		int i;
		for(i=0;i< repetitions;i++){
			time[i] = -1;
		}
		for(i=0;i< repetitions;i++){
			subgraph = null;
			System.gc();
			startTimer();
			time[i] = System.nanoTime();
			switch (algorithm){
			case KOCH:
				subgraph = Koch.maxCommonSubgraph(pair.getGraph1(), pair.getGraph2(), true);
				break;
			case MCGREGOR:
				subgraph = Mcgregor.maxCommonSubgraph(pair.getGraph1(), pair.getGraph2(), true);
				break;
			default:
				throw new UnsupportedOperationException(algorithm.name());
					
			}
			time[i] = System.nanoTime() - time[i];
			stopTimer();
			if(subgraph == null){
				time[i] = -1; //time is invalid, since no proper result was returned
				break;
			}
			System.out.print(".");
		}
		System.out.println();
		write = algorithm.name() + ";true;" + pair.getGraphType() + ";" + pair.getGraphPairNumber() + ";" + pair.getDirected() + ";"
		+ pair.getAlphabetSize() + ";" + pair.getSupergraphSize() + ";" + pair.getSubgraphPercent() + ";"
		+ pair.getAditionalParameters();
		for(i=0;i< repetitions;i++){
			if(time[i] == -1){
				write += ";out of time";
			} else{
				write += ";" + time[i];
			}
		}
		if(subgraph == null){
			write+=";-;-;-\r\n";
		} else {
			long average = average(time);
			long standarddeviation = standarddeviation(time);
			double relativeStdev = standarddeviation/(double)average;
			if(relativeStdev > maxRelativeStandardDeviation){
				System.err.println("RSD too high: " + relativeStdev + " restarting this benchmark (" + pair.getGraphPairNumber() + ")");
				benchmarkPair(pair, algorithm);
				return;
			}
			write+=";" + average + ";" + standarddeviation + ";" + relativeStdev + "\r\n";
		}
		try{
			csv.write(write);
			csv.flush();
		} catch (IOException e){
			throw new RuntimeException(e);
		}
	}
	
	private void startTimer(){
		timer = new Timer();
		timer.schedule(getTimerTask(), timeout);
	}
	
	private TimerTask getTimerTask(){
		return new TimerTask(){
			@Override
			public void run() {
				System.err.println("Out of time");
				switch(currentAlgorithm){
				case KOCH:
					Koch.interruptAllInstances();
					break;
				case MCGREGOR:
					Mcgregor.interuptAllInstances();
					break;
				}
				
			}
		};
	}
	
	private void stopTimer(){
		timer.cancel();
	}
	
	private static long average(long... longs){
		long average=0;
		for(long l : longs){
			average +=l;
		}
		average = average / longs.length;
		return average;
	}
	
	/**
	 * Caclulates the standard deviation. The algorithms uses doubles, so it may not be fuly precise
	 * @param longs the array of longs to calculate the variance for
	 * @return the standard diviation of the array longs
	 */
	private static long standarddeviation(long... longs){
		double average = average(longs);
		double variance = 0;
		for(long l : longs){
			variance +=(l-average)*(l-average);
		}
		variance = variance / longs.length;
		return (long) Math.sqrt(variance);
	}
}
