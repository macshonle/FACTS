package facts.ast;

import java.util.ArrayList;
import java.util.List;

import edu.utsa.strings.Indenter;

public class TreeNode
{
    public TreeNode parent;
    public int id;
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
}
