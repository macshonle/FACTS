package facts.ast;

import java.util.ArrayList;
import java.util.List;

import edu.utsa.strings.Indenter;

public class TreeNode
{
    public TreeNode parent;
    public int id;
    public int postorderid;
    public List<TreeNode> children = new ArrayList<TreeNode>();

    public TreeNode(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public void addChildren(List<TreeNode> newChildren) {
        this.children.addAll(newChildren);
        for (TreeNode child : newChildren) {
            child.parent = this;
        }
    }

    public String prettyPrint(NodeLabeler labeler) {
        StringBuilder buff = new StringBuilder();
        prettyPrint(labeler, this, buff, new Indenter("   "));
        return buff.toString();
    }

    private static void prettyPrint(NodeLabeler labeler, TreeNode treeNode, StringBuilder buff,
            Indenter indenter) {
        buff.append('(');
        buff.append(treeNode.id);
        if (!treeNode.children.isEmpty()) {
            buff.append(':');
            indenter.indent();
            buff.append(indenter.newLine());
            for (TreeNode child : treeNode.children) {
                if (labeler.hasStringRep(child.id)) {
                    buff.append(child.id);
                    buff.append(':');
                    buff.append(labeler.getStringRep(child.id));
                    buff.append(indenter.newLine());
                }
                else {
                    prettyPrint(labeler, child, buff, indenter);
                }
            }
            indenter.unindent();
        }
        buff.append(')');
        buff.append(indenter.newLine());
    }
    

    public TreeNode FindNode(String _label) {
        return this;
    }
    
    public TreeNode FindNode(int _postorderid) {
        return this;
    }

    public int CountNodes() {
        return 1;
    }

    public int CountNodes(String _label) {
        return 1;
    }

    public int GetPostOrderID() {
        return postorderid;
    }

    public int GetChildCount() {
        return 1;
    }

    public TreeNode GetIthChild(int i) {
        return this;
    }

    public String GetLabel() {
        return String.valueOf(id);
    }

}
