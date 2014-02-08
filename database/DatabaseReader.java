package database;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Set;


public class DatabaseReader implements Iterator<DatabasePair>, Iterable<DatabasePair>{
	
	public static final File databaseDir = new File("C:\\database\\");
	
	private Iterator<File> folderIt;
	private File currentFolder;
	private TreeSet<File> graphFileSet = null;
	
	private DatabasePair nextPair = null;
	
	private FileFilter filter = new FileFilter(){
		public boolean accept(File arg0) {
			return arg0.getName().endsWith(".gml");
		};
	};
	
	public DatabaseReader(){
		Set<File> folders = new TreeSet<File>();
		for(File f : databaseDir.listFiles()){
			if(f.isDirectory()){
				folders.add(f);
			}
		}
		folderIt = folders.iterator();
	}
	
	private boolean nextFolder(){
		if(folderIt != null && folderIt.hasNext()){
			currentFolder = folderIt.next();
			graphFileSet = new TreeSet<File>();
			for(File f : currentFolder.listFiles(filter)){
				graphFileSet.add(f);
			}
			if(graphFileSet.isEmpty()){
				return nextFolder();
			} else return true;
		} else {
			folderIt = null;
			return false;
		}
	}
	
	

	@Override
	public boolean hasNext() {
		if(nextPair == null) setNextPair();
		return nextPair != null;
	}
	
	private void setNextPair(){
		if(nextPair == null){
			if(graphFileSet == null || graphFileSet.isEmpty()){
				if(!nextFolder()) return;
			}
			
			//graphs.isEmpty() == false at this point
			File f = graphFileSet.first();
			File other = null;
			if(f.getName().endsWith("_s1.gml")){
				other = getFileFromGraphs(f.getName().replaceFirst("_s1.gml", "_s2.gml"));
			} else if(f.getName().endsWith("_s2.gml")){
				other = getFileFromGraphs(f.getName().replaceFirst("_s2.gml", "_s1.gml"));
			}
			boolean failure = false;
			if(other != null){
				int graphno;
				String str = f.getName();
				int index = str.indexOf("_s");
				if(index == -1){
					graphno = -1;
				}
				str = str.substring(0, index);
				graphno = Integer.parseInt(str);
				try {
					nextPair = new DatabasePair(f, other, currentFolder, graphno);
				} catch (FileNotFoundException e) {
					System.err.println("File not found: " + e.getMessage());
					failure = true;
				}
				graphFileSet.remove(f);
				graphFileSet.remove(other);
			} else {
				System.err.println("Could not find other graph for: " + f.getName());
				failure = true;
				graphFileSet.remove(f);
			}
			if(failure){
				setNextPair(); //try again
			}
		}
	}
	
	/**
	 * 
	 * @param endsWith
	 * @return a File whose name ends with 'suffix' or null if there is no such file in the current folder
	 */
	private File getFileFromGraphs(String suffix){
		for(File f : graphFileSet){
			if(f.getName().endsWith(suffix)) return f;
		}
		
		
		return null;
	}

	@Override
	public DatabasePair next() {
		if(!hasNext()) throw new NoSuchElementException();
		DatabasePair result = nextPair;
		nextPair = null;
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<DatabasePair> iterator() {
		return this;
	}
	
}
