package database;

import generator.GraphGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.jgrapht.Graph;

import database.importexport.ImportExport;

import shared.StringLabeledObject;

public class DatabasePair {
	private Graph<StringLabeledObject, StringLabeledObject> g1, g2;
	private File sourcedir;
	private int number, supergraphsize=-1, alphabetsize=-1;
	private double subgraphpercent=-1;
	private String graphType = "UNKNOWN";
	private boolean directed = false;
	private String aditionalParameters = "";
	
	
	protected DatabasePair(Graph<StringLabeledObject, StringLabeledObject> g1, Graph<StringLabeledObject, StringLabeledObject> g2, File sourcedir, int pairnumber){
		this.g1 = g1;
		this.g2 = g2;
		this.sourcedir = sourcedir;
		this.number = pairnumber;
		parseFolderString(sourcedir.getName());
	}
	
	protected DatabasePair(File g1, File g2, File sourcedir, int numberInDirectory) throws FileNotFoundException{
		this(ImportExport.importGraph(g1), ImportExport.importGraph(g2), sourcedir, numberInDirectory);
	}
	
	private void parseFolderString(String s){
		Scanner scan = new Scanner(s);
		scan.useDelimiter("_");
		String type = scan.next();
		if(type.equals(GraphGenerator.RANDOMLYCONNECTEDGRAPH) || type.equals(GraphGenerator.REGULARMESH)){
			graphType = type;
			directed = scan.next().equals("t");
			supergraphsize = Integer.parseInt(scan.next());
			subgraphpercent = Double.parseDouble(scan.next());
			alphabetsize = Integer.parseInt(scan.next());
			while(scan.hasNext()){
				aditionalParameters += scan.next();
				if(scan.hasNext()){
					aditionalParameters += "_";
				}
			}
		} else {
			System.err.println("Graph type not recognized from folder name");
		}
		
	}
	
	/**
	 * 
	 * @return the aditional parameters for this graph type, or an empty string if there are none
	 */
	public String getAditionalParameters(){
		return aditionalParameters;
	}
	
	public int getSupergraphSize(){
		return supergraphsize;
	}
	
	public double getSubgraphPercent(){
		return subgraphpercent;
	}
	
	public int getAlphabetSize(){
		return alphabetsize;
	}
	
	public boolean getDirected(){
		return directed;
	}
	
	/**
	 * 
	 * @return a string with the graph type name, this sting contains "UNKNOWN" if the type is not known
	 */
	public String getGraphType(){
		return graphType;
	}
	
	public Graph<StringLabeledObject, StringLabeledObject> getGraph1() {
		return g1;
	}

	public Graph<StringLabeledObject, StringLabeledObject> getGraph2() {
		return g2;
	}
	
	public File getSourceDirectory(){
		return sourcedir;
	}

	public int getGraphPairNumber() {
		return number;
	}
}
