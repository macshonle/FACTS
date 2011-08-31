package facts.diff;

import facts.ast.TreeNode;

/* Implements a basic rename operation.  If node labels are the same
 * then the cost is 0, otherwise the cost is 1.
 *
 * INSERT-LICENCE-INFO
 */
public class BasicRename extends TreeEditOperation 
{

    public BasicRename() 
    {
	      super.opName = "RENAME";
    }

    public double getCost(int aNodeID, int bNodeID, TreeNode aTree, TreeNode bTree) 
    {
	      String aString = aTree.FindNode(aNodeID).GetLabel();
        String bString = bTree.FindNode(bNodeID).GetLabel();
//	      int aDiv = aString.lastIndexOf(":");
//	      int bDiv = bString.lastIndexOf(":");
//	      if (aDiv != -1) 
//        {
//	          aString = aString.substring(0,aDiv);
//	      }
//        if (bDiv != -1) 
//        {
//	          bString = bString.substring(0,bDiv);
//	      }
	      if (aString.equals(bString)) 
        {
	          return 0;
	      }
        return 1;
    }
}
