package generator.mesh;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Coordinate {

	private int[] coords;
	
	public Coordinate(int... coordinates){
		coords = coordinates;
	}
	
	public static Coordinate getOriginCoordinate(int dimension){
		if(dimension < 1) throw new RuntimeException("dimension < 1");
		int[] coords = new int[dimension];
		for(int i=0;i<dimension;i++){
			coords[i]=0;
		}
		return new Coordinate(coords);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(coords);
		return result;
	}

	public boolean equals(Object o){
		if(o instanceof Coordinate){
			Coordinate other = (Coordinate) o;
			if(other.coords.length == this.coords.length){
				boolean result = true;
				for(int i=0;i<coords.length;i++){
					result &= other.coords[i]==this.coords[i];
				}
				return result;
			} else return false;
		} else return false;
		
	}
	
	/**
	 * @return a set of coordinates that are the neighbours of this coordinate. 
	 * These Coordinates have one of their numbers incremented or decremented by 1
	 */
	public Set<Coordinate> getNeighbours(){
		Set<Coordinate> result = new HashSet<Coordinate>();
		int[] coordsCopy;
		for(int i=0;i<coords.length;i++){
			coordsCopy = coords.clone();
			coordsCopy[i]--;
			result.add(new Coordinate(coordsCopy));
			coordsCopy = coords.clone();
			coordsCopy[i]++;
			result.add(new Coordinate(coordsCopy));
		}
		return result;
	}
}
