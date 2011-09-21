package facts.ast;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public abstract class TreeBuilder
{
    protected Map<Class<? extends ASTNode>, StructuralPropertyDescriptor[]> lookup;

    {
        // Because we use reflection to get the SPDs for a type, we use a lookup
        // table to reduce the number of reflective calls
        this.lookup = new ConcurrentHashMap<Class<? extends ASTNode>, StructuralPropertyDescriptor[]>();
    }
    
    public abstract TreeNode buildTree(ASTNode node);
    
    protected StructuralPropertyDescriptor[] getProperties(ASTNode node) {
        Class<? extends ASTNode> nodeClass = node.getClass();
        StructuralPropertyDescriptor[] spds = lookup.get(nodeClass);
        if (spds == null) {
            try {
                Method method;
                List properties;
    
                method = nodeClass.getMethod("propertyDescriptors", int.class);
                properties = (List)method.invoke(nodeClass, AST.JLS3);
                spds = toArray(properties);
                lookup.put(nodeClass, spds);
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return spds;
    }

    private static StructuralPropertyDescriptor[] toArray(List list) {
        StructuralPropertyDescriptor[] result;
        result = new StructuralPropertyDescriptor[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i++] = (StructuralPropertyDescriptor)o;
        }
        return result;
    }

    public String prettyPrint(TreeNode treeA) {
        // TODO Auto-generated method stub
        return null;
    }
}
