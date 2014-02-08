package test;

class IntPair{
	private int a, b;
	
	IntPair(int a, int b){
		this.a = a;
		this.b = b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a;
		result = prime * result + b;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IntPair)) {
			return false;
		}
		IntPair other = (IntPair) obj;
		if (a != other.a) {
			return false;
		}
		if (b != other.b) {
			return false;
		}
		return true;
	}
}