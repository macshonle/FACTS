package facts.ast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.utsa.eclipse.SystemUtil;
import edu.utsa.files.FileUtil;
import facts.diff.ComparisonZhangShasha;
import facts.diff.OpsZhangShasha;
import facts.diff.Transformation;

public class Difference
{
    private TreeBuilder treeBuilder;
    private TreeNode treeA;
    private TreeNode treeB;

    public Difference(String filenameA, String filenameB) throws IOException {
        ASTNode astA = parseFile(filenameA);
        ASTNode astB = parseFile(filenameB);

        PrintStream out = SystemUtil.getOutStream();
        out.printf("%s parsed to:%n<<<%s>>>%n%n", filenameA, astA);
        out.printf("%s parsed to:%n<<<%s>>>%n%n", filenameB, astB);
        this.treeBuilder = new UniqueTreeBuilder();
        this.treeA = treeBuilder.buildTree(astA);
        this.treeB = treeBuilder.buildTree(astB);
    }

    public String getResults() {
        StringBuilder out = new StringBuilder();
        out.append(treeBuilder.prettyPrint(treeA));
        out.append(treeBuilder.prettyPrint(treeB));
        ComparisonZhangShasha ZS = new ComparisonZhangShasha();
        OpsZhangShasha costs = new OpsZhangShasha();
        Transformation transform = ZS.findDistance(treeA, treeB, costs, out, (UniqueTreeBuilder)treeBuilder);
        out.append(ZS.reportDifferences());
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
