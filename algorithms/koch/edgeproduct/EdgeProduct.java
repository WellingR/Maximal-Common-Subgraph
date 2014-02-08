package algorithms.koch.edgeproduct;

public class EdgeProduct<E>{
    private E right;
	private E left;

    public E getLeft() {
        return left;
    }

    public E getRight() {
        return right;
    }

    public EdgeProduct(E left, E right){
        this.left = left;
        this.right = right;
    }

    public String toString(){
        return "[" + left + "," + right + "]";
    }



}