package facts.ast;

import java.util.ArrayList;
import java.util.List;

import edu.utsa.strings.Indenter;

public class TreeNode
{
    public TreeNode parent;
    public String label;
    public int postOrderID;
    public List<TreeNode> children = new ArrayList<TreeNode>();
    private int treeSize = -1 /*our memoized size*/;

    public TreeNode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void addChildren(List<TreeNode> newChildren) {
        this.children.addAll(newChildren);
        for (TreeNode child : newChildren) {
            child.parent = this;
        }
    }

    public TreeNode findNode(int postOrderID) {
        if (this.postOrderID == postOrderID) {
            return this;
        }
        else {
            for (TreeNode child : children) {
                TreeNode node = child.findNode(postOrderID);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }

    public TreeNode findNode(String label) {
        if (this.label.equals(label)) {
            return this;
        }
        else {
            for (TreeNode child : children) {
                TreeNode node = child.findNode(label);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }
    
    public int CountNodes(String label) {
        TreeNode foundNode = findNode(label);
        if (foundNode == null) {
            return 0;
        }
        else {
            return foundNode.getSize();
        }
    }
    
    public int getSize() {
        if (treeSize == -1) {
            this.treeSize = 1;
            for (TreeNode child : children) {
                this.treeSize += child.getSize();
            }
        }
        return treeSize;
    }

    public int GetPostOrderID() {
        return postOrderID;
    }

    public int GetChildCount() {
        return children.size();
    }

    public TreeNode GetIthChild(int i) {
        return children.get(i);
    }
    
    public int setPostOrdering(int counter) {
        int internalCounter = counter;

      	//examine children
        for (TreeNode child : children) {
      	    internalCounter = child.setPostOrdering(internalCounter);
        }

      	//set new postOrderID for this node (set to counter+1)
        postOrderID = internalCounter+1;
      	return internalCounter+1;
    }

    public String getLabelerValue(UniqueTreeBuilder labeler) {
        StringBuilder buff = new StringBuilder();
        Integer parsedLabel = Integer.valueOf(label);
        if (labeler.hasStringRep(parsedLabel))
        	buff.append(labeler.getStringRep(parsedLabel));
        return buff.toString();
    }

}
