package facts.diff;

import facts.ast.TreeNode;

/* Implements a basic insert operation with cost 1.
 */
public class BasicInsert extends TreeEditOperation 
{
    public BasicInsert() 
    {
  	    super.opName = "INSERT";
    }

    public double getCost(int aNodeID, int bNodeID, TreeNode aTree, TreeNode bTree) 
    {
	      return 1;
    }
}
