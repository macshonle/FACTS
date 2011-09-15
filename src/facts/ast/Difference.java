package facts.ast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.utsa.eclipse.SystemUtil;
import edu.utsa.files.FileUtil;
import facts.diff.ComparisonZhangShasha;
import facts.diff.OpsZhangShasha;
import facts.diff.Transformation;
import facts.diff.TreeEditOperation;

public class Difference
{
    private NodeLabeler labeler;
    private TreeNode treeA;
    private TreeNode treeB;
    private int indexAfterA;
    private int indexAfterB;

    public Difference(String filenameA, String filenameB) throws IOException {
        ASTNode astA = parseFile(filenameA);
        ASTNode astB = parseFile(filenameB);

        PrintStream out = SystemUtil.getOutStream();
        out.printf("%s parsed to:%n<<<%s>>>%n%n", filenameA, astA);
        out.printf("%s parsed to:%n<<<%s>>>%n%n", filenameB, astB);
        this.labeler = new NodeLabeler();
        this.treeA = labeler.label(astA);
        this.indexAfterA = labeler.getNextIndex();
        this.treeB = labeler.label(astB);
        this.indexAfterB = labeler.getNextIndex();
    }

    public String getResults() {
        StringBuilder out = new StringBuilder();
        out.append(treeA.prettyPrint(labeler));
        out.append(String.format("%nIndex after A: %d%nIndex after B: %d%n",
                indexAfterA, indexAfterB));
        out.append(String.format("%nNew indecies for tree B:%n"));
        for (int i = indexAfterA; i < indexAfterB; ++i) {
            out.append(String.format(" + %d: %s%n", i, labeler.getAnnotatedIndexedItem(i)));
        }
        out.append("THIS IS A TEST");
        ComparisonZhangShasha ZS = new ComparisonZhangShasha();
        OpsZhangShasha costs = new OpsZhangShasha();
        Transformation transform = ZS.findDistance(treeA, treeB, costs, out, labeler);
        for (TreeEditOperation operation : transform.getOperations()) {
            
        }
        return out.toString();
    }

    private ASTNode parseFile(String filename) throws IOException {
        File fileA = new File(filename);
        InputStream is = new FileInputStream(fileA);
        String contents = FileUtil.readStream(is);
        char[] sourceA = contents.toCharArray();

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(sourceA);
        CompilationUnit result = (CompilationUnit)parser.createAST(null);
        return result;
    }
}
