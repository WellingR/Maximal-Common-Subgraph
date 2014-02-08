package database.generator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.jgrapht.Graph;

import shared.StringLabeledObject;

import database.importexport.ImportExport;

import generator.GraphGenerator;
import generator.mesh.RegularMeshGenerator;
import generator.randomlyconnected.RandomlyConnectedGraphGenerator;

public class DatabaseCreator {
	
	private static int graphNo=0;
	
	public static void main(String[] args){
		File databaseDir = new File("C:\\database\\");
		databaseDir.mkdirs();
		
		int[] totalsizes = {10, 20, 30};
		double[] subgraphpercentages = {0.2, 0.4, 0.7, 0.9};
		double[] edgedensities = {0.1, 0.25, 0.5};
		int[] alphabetsizes = {10, 20, 40, 60};
		int[] dimensions = {2,3,4};
		
		boolean directed = true;
		
		GraphGenerator g;
		int numberOfPairs = 10;
		File graphdir;
		for(int totalsize  : totalsizes){
			for(double subgraphpercentage : subgraphpercentages){
				if((totalsize * subgraphpercentage) < 2) continue;
				for(int alphabetsize : alphabetsizes){
					//generate randomly connected graphs
					for(double edgedensity : edgedensities){
						try{
							g = new RandomlyConnectedGraphGenerator(directed, totalsize, subgraphpercentage, edgedensity, alphabetsize, true);
						} catch (IllegalArgumentException e) {
							System.err.println("Could not generate graph pair");
							continue;
						}
						graphdir = new File(databaseDir, g.getSettingString() + "\\");
						graphdir.mkdirs();
						generateGraphs(g, numberOfPairs, graphdir);
					}
					//generate regular meshes
					for(int dimension : dimensions){
						g = new RegularMeshGenerator(directed, totalsize, subgraphpercentage, alphabetsize, dimension, true);
						graphdir = new File(databaseDir, g.getSettingString() + "\\");
						graphdir.mkdirs();
						generateGraphs(g, numberOfPairs, graphdir);
					}
				}
			}
		}
		
	}
	
	public static void generateGraphs(GraphGenerator generator, int numberOfPairs, File directory){
		System.out.print("generating " + generator.getSettingString());
		if(generator == null || numberOfPairs < 1 || directory == null || !directory.isDirectory()){
			System.err.println("" +generator+ " " + numberOfPairs + " " + directory + " " + !directory.isDirectory());
			throw new IllegalArgumentException();
		}
		if(!directory.exists()){
			directory.mkdirs();
		}
				
		Graph<StringLabeledObject,StringLabeledObject> g1, g2;
		
		for(int i=0;i<numberOfPairs;i++){
			generator.resetAndGenerate();
			g1 = generator.getSupergraph1();
			g2 = generator.getSupergraph2();
			
			try {
				ImportExport.exportGraph(g1, getGmlFile(directory, 1, graphNo));
				ImportExport.exportGraph(g2, getGmlFile(directory, 2, graphNo));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			graphNo++;
			System.out.print(".");
		}
		System.out.println();
	}
	
	private static File getGmlFile(File directory, int supergraphno, int pairnumber){
		return new File(directory, pairnumber + "_s" + + supergraphno + ".gml");
	}
	
	public static String getTimeString() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(("yyyy.MM.dd 'at' HH:mm:ss"));
	    return sdf.format(cal.getTime());
	}
}
