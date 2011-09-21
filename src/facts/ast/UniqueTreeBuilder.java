package facts.ast;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import edu.utsa.eclipse.SystemUtil;
import edu.utsa.exceptions.PluginError;
import edu.utsa.strings.Indenter;
import edu.utsa.strings.StringUtil;

public class UniqueTreeBuilder extends TreeBuilder
{
    private static final int NULL_ID = 0;
    private static final int LIST_ID = 1;
    private static final int FIRST_ID = 2;

    private int nextID = FIRST_ID;
    // TODO: Determine if all simple values behave well when hashed/compared
    private Map<Object, Integer> simpleValuesIDs = new HashMap<Object, Integer>();
    // We use a String representation for tree nodes for simplicity, otherwise
    // we could have a Map specialized for keys that are int arrays.
    // The key looks like: "<nodeType>:<child1ID>;<child2ID>;", e.g., "5:3;1;"
    private Map<String, Integer> treeNodeIDs = new HashMap<String, Integer>();

    private ArrayList<Object> keyValueLookup = new ArrayList<Object>();
    private ArrayList<String> valueAnnotationLookup = new ArrayList<String>();

    public UniqueTreeBuilder() {
        keyValueLookup.add(NULL_ID, "[null ID]");
        keyValueLookup.add(LIST_ID, "[list of AST nodes]");
        valueAnnotationLookup.add(NULL_ID, "[null ID]");
        valueAnnotationLookup.add(LIST_ID, "[list of AST nodes]");
    }

    // Traverses the AST, labeling each node, from the bottom up, with a unique
    // identifier. Two AST nodes that are syntactically equivalent will have
    // the same ID, e.g. "i++" (no spaces) and "i ++" (with the space) are
    // assigned, say, 5, with the token "i" being assigned 1, and the token
    // "++" being assigned 3.
    //
    // The NodeLabeler keeps track of these assignments.
    //
    // An AST node gets the same ID as an existing one only when:
    // - They have the same AST NodeType
    // - They have the same number of children
    // - All children in all corresponding locations have the same values
    public TreeNode buildTree(ASTNode node) {
        PrintStream out = SystemUtil.getOutStream();
        StructuralPropertyDescriptor[] spds = getProperties(node);

        List<TreeNode> children = new ArrayList<TreeNode>();

        for (StructuralPropertyDescriptor spd : spds) {
            Object property = node.getStructuralProperty(spd);
            // TODO: We can add types we want to skip, such as javadoc comments
            if (spd.isSimpleProperty()) {
                int id;
                if (simpleValuesIDs.containsKey(property)) {
                    id = simpleValuesIDs.get(property);
                }
                else {
                    id = nextID++;
                    simpleValuesIDs.put(property, id);
                    keyValueLookup.add(id, new SimpleProperty(property));
                    valueAnnotationLookup.add(id, "SimpleProperty");
                    out.printf("CREATED simple value id=%d, value=%s%n", id,
                            StringUtil.valueOfAndClass(property));
                }
                TreeNode childTreeNode = new TreeNode(String.valueOf(id));
                children.add(childTreeNode);
            }
            else if (spd.isChildProperty()) {
                ASTNode child = (ASTNode)property;
                TreeNode childTreeNode;
                if (child == null) {
                    childTreeNode = new TreeNode(String.valueOf(NULL_ID));
                }
                else {
                    // TODO: We can short-cut nodes that we want to treat like
                    // token, like qualified names
                    childTreeNode = buildTree(child);
                }
                children.add(childTreeNode);
            }
            else if (spd.isChildListProperty()) {
                List listOfASTs = (List)property;
                List<TreeNode> listChildren = new ArrayList<TreeNode>();
                for (Object obj : listOfASTs) {
                    TreeNode subTree = buildTree((ASTNode)obj);
                    listChildren.add(subTree);
                }
                String listNodeKey = makeKey(LIST_ID, listChildren);
                int id;
                if (treeNodeIDs.containsKey(listNodeKey)) {
                    id = treeNodeIDs.get(listNodeKey);
                }
                else {
                    id = nextID++;
                    treeNodeIDs.put(listNodeKey, id);
                    keyValueLookup.add(id, listNodeKey);
                    valueAnnotationLookup.add(id, "List");
                    out.printf("CREATED list id=%d, key={%s}, value=<%s>%n", id, listNodeKey,
                            property);
                }
                TreeNode listChild = new TreeNode(String.valueOf(id));
                listChild.addChildren(listChildren);
                children.add(listChild);
            }
        }
        int nodeType = node.getNodeType();
        String treeNodeKey = makeKey(nodeType, children);
        int id;
        if (treeNodeIDs.containsKey(treeNodeKey)) {
            id = treeNodeIDs.get(treeNodeKey);
        }
        else {
            id = nextID++;
            treeNodeIDs.put(treeNodeKey, id);
            keyValueLookup.add(id, treeNodeKey);
            valueAnnotationLookup.add(id, node.getClass().getSimpleName());
            out.printf("CREATED list id=%d, key={%s}, value=<%s>%n", id, treeNodeKey,
                    StringUtil.minimizeWhitespace(node));
        }
        TreeNode result = new TreeNode(String.valueOf(id));
        result.addChildren(children);
        return result;
    }

    private String makeKey(int nodeType, List<TreeNode> children) {
        StringBuilder buff = new StringBuilder();
        buff.append(nodeType);
        buff.append(':');
        for (TreeNode child : children) {
            buff.append(child.getLabel());
            buff.append(';');
        }
        String treeNodeKey = buff.toString();
        return treeNodeKey;
    }

    public boolean hasStringRep(int id) {
        if (keyValueLookup.size() > id) {
            Object value = keyValueLookup.get(id);
            return value instanceof SimpleProperty;
        }
        throw new PluginError("Internal programming error");
    }

    public String getStringRep(int id) {
        return ((SimpleProperty)keyValueLookup.get(id)).toString();
    }

    private static class SimpleProperty
    {
        private Object value;

        private SimpleProperty(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            if (value == null) {
                return "<null>";
            }
            else {
                return value.toString();
            }
        }
    }

    public int getNextIndex() {
        return nextID;
    }

    public Object getIndexedItem(int id) {
        Object object = keyValueLookup.get(id);
        return object;
    }

    public String getAnnotatedIndexedItem(int id) {
        Object object = keyValueLookup.get(id);
        if (object instanceof SimpleProperty) {
            return ((SimpleProperty)object).toString();
        }
        else {
            return String.format("%s#%s", valueAnnotationLookup.get(id), object.toString());
        }
    }

    public String prettyPrint(TreeNode treeNode) {
        StringBuilder buff = new StringBuilder();
        prettyPrint(this, treeNode, buff, new Indenter("   "));
        return buff.toString();
    }

    private static void prettyPrint(UniqueTreeBuilder labeler, TreeNode treeNode,
            StringBuilder buff, Indenter indenter) {
        buff.append('(');
        buff.append(treeNode.label);
        if (!treeNode.children.isEmpty()) {
            buff.append(':');
            indenter.indent();
            buff.append(indenter.newLine());
            for (TreeNode child : treeNode.children) {
                Integer parsedLabel = Integer.valueOf(child.label);
                if (labeler.hasStringRep(parsedLabel)) {
                    buff.append(child.label);
                    buff.append(':');
                    buff.append(labeler.getStringRep(parsedLabel));
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
