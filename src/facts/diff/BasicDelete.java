package facts.diff;

import facts.ast.TreeNode;

/* Implements a basic delete operation with cost 1.
 */
public class BasicDelete extends TreeEditOperation 
{

    public BasicDelete() 
    {
	      super.opName = "DELETE";
    }

    public Integer getCost(int aNodeID, int bNodeID, TreeNode aTree, TreeNode bTree) 
    {
	      return 1;
    }
}
