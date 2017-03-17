package cspSolver;

public class ArcPair {

	  private final Variable left;
	  private final Variable right;

	  public ArcPair(Variable left, Variable right) {
	    this.left = left;
	    this.right = right;
	  }

	  public Variable getLeft() { return left; }
	  public Variable getRight() { return right; }

	  
	  @Override
	  public boolean equals(Object o) {
	    if (!(o instanceof ArcPair)) 
	    	return false;
	    ArcPair pairo = (ArcPair) o;
	    return this.left.equals(pairo.getLeft()) &&
	           this.right.equals(pairo.getRight());
	           
//	           ||
//	           pairo.getLeft().equals(left) &&
//	           pairo.getRight().equals(right);
	  }

}