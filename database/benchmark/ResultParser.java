package database.benchmark;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ResultParser {
	
	public static final String RANDOMLYCONNECTED = "RandomlyConnectedGraph";
	public static final String REGULARMESH = "RegularMesh";
	
	public static final String KOCH = "KOCH";
	public static final String MCGREGOR = "MCGREGOR";
	
	public static final String ALGORITHM = "Algorithm";
	public static final String CONNECTED = "connected";
	public static final String GRAPHTYPE = "GraphType";
	public static final String NUMBER = "number";
	public static final String LABELALPHABET = "label alphabet size";
	public static final String TOTALSIZE = "Supergraph Size(edges)";
	public static final String SUBGRAPHPERCENT = "Generated Subgraph Size(%)";
	public static final String AVERAGE = "average";
	public static final String ADDITIONALPARAM = "Aditional parameters";
	
	public static final String DELIMITER =  ";";
	
	public static final long THREEMINUTES = 180000000000L;
	
	int[] totalsizes = {10, 20, 30};
	double[] subgraphpercentages = {0.2, 0.4, 0.7, 0.9};
	int[] alphabetsizes = {10, 20, 40, 60};
	String[] edgedensities = {"0.1", "0.25", "0.5"};
	String[] dimensions = {"2","3","4"};
	
	BenchmarkResult current = null;
	
	private int algorithmloc=-1, connectedloc=-1, graphtypeloc=-1, numberloc=-1,labelalphabetloc=-1,
			totalsizeloc=-1, subgraphpercentloc=-1, averageloc=-1, aditionalparamloc=-1, imax=-1;
	
	public static void main(String[] args){
		ResultParser parse = new ResultParser();
		parse.printTableNL(MCGREGOR, REGULARMESH, "3", 0.4);
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		parse = new ResultParser();
//		parse.printTableNS(MCGREGOR, REGULARMESH, "2", 20);
	}
	Scanner file, line;
	
	public ResultParser(){
		try {
			file = new Scanner(new FileInputStream("C:\\database\\results.csv"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		setLocations();
	}
	
	private void printTableNS(String algorithm, String graphType, String additionalParam, long labelSize){
		ArrayList<BenchmarkResult> results = new ArrayList<BenchmarkResult>();
		while(parseNextLine()){
			if(current.graphtype.equals(graphType) && current.additionalparam.equals(additionalParam)
					&& current.labelalphabet == labelSize){
				boolean found = false;
				for(BenchmarkResult b : results){
					if(b.sameParameters(current)){
						b.addResult(current.getMean());
						found = true;
						break;
					}
				}
				if(!found){
					results.add(current);
				}
			}
		}
		System.out.println(results.size() + "\n");
		
		for(double subgraphpercent : subgraphpercentages){
			int percent = (int) (subgraphpercent*100);
			System.out.print(percent + "\\%");
			for(long totalsize : totalsizes){
				printmicros(results, graphType, totalsize, subgraphpercent, labelSize, additionalParam, algorithm);
			}
			System.out.println("\\\\ \\hline");
		}
	}
	
	private void printTableNL(String algorithm, String graphType, String additionalParam, double subgraphpercent){
		ArrayList<BenchmarkResult> results = new ArrayList<BenchmarkResult>();
		while(parseNextLine()){
			if(current.graphtype.equals(graphType) && current.additionalparam.equals(additionalParam)
					&& current.subgraphpercent == subgraphpercent){
				boolean found = false;
				for(BenchmarkResult b : results){
					if(b.sameParameters(current)){
						b.addResult(current.getMean());
						found = true;
						break;
					}
				}
				if(!found){
					results.add(current);
				}
			}
		}
		System.out.println(results.size() + "\n");
		
		for(long labelsize : alphabetsizes){
			System.out.print(labelsize);
			for(long totalsize : totalsizes){
				printmicros(results, graphType, totalsize, subgraphpercent, labelsize, additionalParam, algorithm);
			}
			System.out.println("\\\\ \\hline");
		}
	}
	
	private void printmicros(List<BenchmarkResult> results, String graphType, long totalsize,
			double subgraphpercent, long labelsize, String additionalParameter, String algorithm){
		System.out.print("&");
		boolean found = false;
		for(BenchmarkResult r : results){
			if(r.graphtype.equals(graphType) && r.totalsize == totalsize && r.subgraphpercent == subgraphpercent
				&& r.labelalphabet == labelsize && r.getAdditionalparam().equals(additionalParameter)
				&& r.getAlgoritm().equals(algorithm)){
				long millis = r.getMedian() / 1000;
				String print = " " + millis;
				System.out.print(print);
				found = true;
			}
		}
		if(!found){
			System.out.print("-");
		}
	}
	
	private void printRatioTable(String graphType){
		String[] additionalParams;
		if(graphType.equals(RANDOMLYCONNECTED)){
			additionalParams = edgedensities;
		} else if (graphType.equals(REGULARMESH)){
			additionalParams = dimensions;
		} else{
			throw new IllegalArgumentException("Unrecognized graphtype argument: " + graphType);
		}
		ArrayList<BenchmarkResult> results = new ArrayList<BenchmarkResult>();
		while(parseNextLine()){
			if(current.graphtype.equals(graphType)){
				boolean found = false;
				for(BenchmarkResult b : results){
					if(b.sameParameters(current)){
						b.addResult(current.getMean());
						found = true;
						break;
					}
				}
				if(!found){
					results.add(current);
				}
			}
		}
		//results now contains all results for graphType
		Map<BenchmarkResult,Double> ratiomap = new LinkedHashMap<BenchmarkResult,Double>();
		while(!results.isEmpty()){
			BenchmarkResult a = results.remove(0);
			BenchmarkResult b = null;
			for(BenchmarkResult r : results){
				if(r.sameParametersExcludingAlgorithm(a)){
					b = r;
					break;
				}
			}
			if(b == null){
				System.err.println("No other result found");
			} else {
				double ratio=1;
				if(a.getMedian() == THREEMINUTES || b.getMedian() == THREEMINUTES){
					ratio = -1; //flag the ratio that it is an minimum estimate by making it negative
				}
				if(a.getAlgoritm().equals(KOCH)){
					ratio = b.getMedian()/(double)a.getMedian();
					System.out.println(b.getAlgoritm()+"/"+a.getAlgoritm());
				} else {
					ratio = a.getMedian()/(double)b.getMedian();
					System.out.println(b.getAlgoritm()+"/"+a.getAlgoritm());
				}
				ratiomap.put(a, ratio);
				results.remove(b);
			}
		}
		//ratiomap should now contain all ratio's
		for(String additionalparam : additionalParams){
			System.out.print("\\multirow{"+alphabetsizes.length+"}{*}{"+additionalparam+"}");
			for(long label : alphabetsizes){
				System.out.print("&" + label);
				for(long totalsize : totalsizes){
					for(double subgraphpercent : subgraphpercentages){
						printratio(ratiomap, graphType, totalsize, subgraphpercent, label, additionalparam);
					}
				}
				String line;
				if(label == alphabetsizes[alphabetsizes.length-1]){
					line = "\\hline";
				} else {
					line = "\\cline{3-"+ (2+totalsizes.length*subgraphpercentages.length)+"}";
				}
				System.out.println("\\\\ "+ line);
			}
		}
		
		
	}
	
	private void printratio(Map<BenchmarkResult,Double> ratiomap, String graphtType, long totalsize,
			double subgraphpercent, long labelsize, String additionalParameter){
		System.out.print("&");
		boolean found = false;
		int maxdecimals = 3;
		for(BenchmarkResult r : ratiomap.keySet()){
			if(r.graphtype.equals(graphtType) && r.totalsize == totalsize && r.subgraphpercent == subgraphpercent
				&& r.labelalphabet == labelsize && r.getAdditionalparam().equals(additionalParameter)){
				double ratio = ratiomap.get(r);
				boolean estimateFlag = false;
				if(ratio < 0){
					estimateFlag = true;
					ratio = -ratio;
				}
				int decimalindex = (""+ratio).indexOf(".");
				double roundadd = 0.5;
				for(int i=0;i<maxdecimals+1-decimalindex;i++){
					roundadd/=10;
				}
				ratio+= roundadd;
				String print = "" + ratio;
				if(decimalindex < maxdecimals+1){
					print = print.substring(0,maxdecimals+2);
					int decimalchars = print.length() - decimalindex;
					if(decimalchars<maxdecimals+1){
						print += "\\phantom{";
						while(decimalchars<maxdecimals+1){
							print+="0";
							decimalchars++;
						}
						print+="}";
					}
				} else{
					print = print.substring(0,decimalindex);
					print+="\\phantom{.";
					for(int i=0;i<maxdecimals;i++){
						print+="0";
					}
					print+="}";
				}
				if(estimateFlag){
					if(ratio < 1){
						print = "<" + print;
					} else{
						print = ">" + print;
					}
				}
				if(ratio < 1){
					print+="\\cellcolor[gray]{0.8}";
				}
				System.out.print(print);
				found = true;
			}
		}
		if(!found){
			System.out.print("-");
		}
	}
	
	private void setLocations(){
		if(!setNextLine()){
			throw new RuntimeException("File has no content");
		}
		for(int i = 0;line.hasNext();i++){
			String s = line.next();
			if(s.equals(ALGORITHM)){
				algorithmloc = i;
				imax = i;
			} else if(s.equals(CONNECTED)){
				connectedloc = i;
				imax = i;
			} else if(s.equals(NUMBER)){
				numberloc = i;
				imax = i;
			} else if(s.equals(GRAPHTYPE)){
				graphtypeloc = i;
				imax = i;
			} else if(s.equals(LABELALPHABET)){
				labelalphabetloc = i;
				imax = i;
			} else if(s.equals(SUBGRAPHPERCENT)){
				subgraphpercentloc = i;
				imax = i;
			} else if(s.equals(TOTALSIZE)){
				totalsizeloc = i;
				imax = i;
			} else if(s.equals(AVERAGE)){
				averageloc = i;
				imax = i;
			} else if(s.equals(ADDITIONALPARAM)){
				aditionalparamloc = i;
				imax = i;
			}
		}
		if(algorithmloc==-1 || connectedloc==-1 || graphtypeloc==-1 || numberloc==-1 ||labelalphabetloc==-1
			||totalsizeloc==-1 || subgraphpercentloc==-1 || averageloc==-1 || aditionalparamloc==-1){
			throw new RuntimeException("Unable to find all headers");
		}
	}
	
	private boolean parseNextLine(){
		boolean result = setNextLine();
		if(result){
			String algorithm="", additionalparam="", graphtype="";
			boolean connected = false;
			long number=-1, totalsize=-1, labelalphabet=-1, average=-1;
			double subgraphpercent=-1;
			for(int i=0;i<=imax;i++){
				String s = line.next();
				if(i == algorithmloc){
					algorithm = s;
				} else if(i == connectedloc){
					connected = s.equalsIgnoreCase("true");
				} else if(i == graphtypeloc){
					graphtype = s;
				} else if(i == numberloc){
					number = Long.parseLong(s);
				} else if(i == labelalphabetloc){
					labelalphabet = Long.parseLong(s);
				} else if(i == totalsizeloc){
					totalsize = Long.parseLong(s);
				} else if(i == subgraphpercentloc){
					subgraphpercent = Double.parseDouble(s);
				} else if(i == averageloc){
					if(!s.equals("-")){
						average = Long.parseLong(s);
					}
				} else if(i == aditionalparamloc){
					additionalparam = s;
				}
			}
			current = new BenchmarkResult(algorithm, additionalparam, graphtype, connected, number,
					totalsize, average, labelalphabet, subgraphpercent);
		}
		return result;
	}
	
	private boolean setNextLine(){
		if(file.hasNextLine()){
			line = new Scanner(file.nextLine());
			line.useDelimiter(DELIMITER);
			return true;
		}
		return false;
	}
	
	private class BenchmarkResult{
		private String algoritm, additionalparam, graphtype;
		private boolean connected;
		private long number, totalsize, labelalphabet;
		private double subgraphpercent;
		private ArrayList<Long> results = new ArrayList<Long>();
		
		protected BenchmarkResult(String algorithm, String additionalparam, String graphtype, boolean connected,
				long number, long totalsize, long average, long labelalphabet, double subgraphpercent){
			this.algoritm =algorithm;
			this.additionalparam = additionalparam;
			this.graphtype = graphtype;
			this.connected = connected;
			this.number = number;
			this.totalsize = totalsize;
			this.labelalphabet = labelalphabet;
			this.subgraphpercent = subgraphpercent;
			addResult(average);
		}
		
		public String getAlgoritm() {
			return algoritm;
		}

		public String getAdditionalparam() {
			return additionalparam;
		}

		public String getGraphtype() {
			return graphtype;
		}

		public boolean getConnected() {
			return connected;
		}

		public long getNumber() {
			return number;
		}

		public long getTotalsize() {
			return totalsize;
		}

		public long getLabelalphabet() {
			return labelalphabet;
		}

		public double getSubgraphpercent() {
			return subgraphpercent;
		}

		public long getMean() {
			if(results.size()==0) return -1;
			double average=0;
			for(long l : results){
				average += l;
			}
			average /= results.size();
			return (long) average;
		}
		
		public long getMedian() {
			if(results.size()==0) return -1;
			Collections.sort(results);
			if(results.size()%2==1){
				return results.get(results.size()/2);
			} else {
				long lower = results.get(results.size()/2-1);
				long upper = results.get(results.size()/2);
			 
				return (lower + upper) / 2;
			}
		}
		
		public void addResult(long l){
			if(l==-1){
				System.err.println("Result -1 added as 3minutes in pair " + getNumber() + " "+ getAlgoritm() + " "+ getAdditionalparam()
						+ " "+ getGraphtype() + " "+ getConnected() + " "+ getTotalsize() + " "+ getLabelalphabet() + " "+ getSubgraphpercent());
				results.add(THREEMINUTES);
			} else {
				results.add(l);
			}
		}
		
		/**
		 * Checks of the other was generated using the same parameters
		 * @param other a Benchmarkresult
		 * @return true if the parameters are the same
		 */
		public boolean sameParameters(BenchmarkResult other){
			return other.algoritm.equals(this.algoritm) && other.additionalparam.equals(this.additionalparam)
			&& other.graphtype.equals(this.graphtype) && other.labelalphabet == this.labelalphabet
			&&other.connected == this.connected && other.subgraphpercent == this.subgraphpercent
			&&other.totalsize == this.totalsize;
		}
		
		/**
		 * Checks of the other was generated using the same parameter (excluding the algorithm parameter)
		 * @param other a Benchmarkresult
		 * @return true if the parameters are the same (excluding the algorithm parameter)
		 */
		public boolean sameParametersExcludingAlgorithm(BenchmarkResult other){
			return other.getAdditionalparam().equals(this.getAdditionalparam())
			&& other.getGraphtype().equals(this.getGraphtype()) && other.labelalphabet == this.labelalphabet
			&&other.connected == this.connected && other.subgraphpercent == this.subgraphpercent
			&&other.totalsize == this.totalsize;
		}
	}
}
