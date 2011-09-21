package facts.diff;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Hashtable;

import facts.ast.TreeNode;
import facts.ast.UniqueTreeBuilder;

/**
 * This is an implementation of the Zhang and Shasha algorithm as
 * described in [FIXME]
 * 
 * SWAN 2007-11-01: I'm pretty sure this code comes from:
 * http://www.cs.queensu.ca/TechReports/Reports/1995-372.pdf and
 * "http://www.inf.unibz.it/dis/teaching/ATA/ata7-handout-1x1.pdf"
 * INSERT-LICENCE-INFO
 */
public class ComparisonZhangShasha
{

    // "Dynamic Programming" Table.
    // use function setFD to access it.
    // Each call to findDistance will change these tables. But each
    // call is independent (and reinitialises this) so the side effect
    // has no real consequence. ie. There are NO public side effects.
    // private Hashtable<String, Hashtable<String, Double>> forestDistance =
    // null; TODO: never read
    private double[][] distance = null;
    private int Size1 = -1;
    private int Size2 = -1;
    private Hashtable<Integer, Hashtable<Integer, Double>> FinalDPTable = new Hashtable<Integer, Hashtable<Integer, Double>>();
    private TreeNode FTree;
    private TreeNode GTree;
    private UniqueTreeBuilder labeler;

    public Transformation findDistance(TreeNode _FTree, TreeNode _GTree, OpsZhangShasha ops,
            StringBuilder out, UniqueTreeBuilder _labeler) {
        FTree = _FTree;
        GTree = _GTree;
        labeler = _labeler;
        // This is initialized to be n+1 * m+1. It should really be n*m
        // but because of java's zero indexing, the for loops would
        // look much more readable if the matrix is extended by one
        // column and row. So, distance[0,*] and distance[*,0] should
        // be permanently zero.
        // distance = new
        // double[FTree.getNodeCount()+1][GTree.getNodeCount()+1];
        Size1 = FTree.CountNodes(FTree.getLabel()) + 1;
        Size2 = GTree.CountNodes(GTree.getLabel()) + 1;
        distance = new double[Size1][Size2];

        FTree.setPostOrdering(0);
        GTree.setPostOrdering(0);
        // Preliminaries
        // 1. Find left-most leaf and key roots
        Hashtable<Integer, Integer> aLeftLeaf = new Hashtable<Integer, Integer>();
        Hashtable<Integer, Integer> bLeftLeaf = new Hashtable<Integer, Integer>();
        ArrayList<Integer> FTreeKeyRoots = new ArrayList<Integer>();
        ArrayList<Integer> GTreeKeyRoots = new ArrayList<Integer>();

        findHelperTables(FTree, aLeftLeaf, FTreeKeyRoots, FTree.GetPostOrderID());
        findHelperTables(GTree, bLeftLeaf, GTreeKeyRoots, GTree.GetPostOrderID());

        // Comparison
        for (Integer aKeyroot : FTreeKeyRoots) { // aKeyroot loop
            for (Integer bKeyroot : GTreeKeyRoots) { // bKeyroot loop
                // Re-initialise forest distance tables
                Hashtable<Integer, Hashtable<Integer, Double>> DPTable = new Hashtable<Integer, Hashtable<Integer, Double>>();
                setFD(aLeftLeaf.get(aKeyroot), bLeftLeaf.get(bKeyroot), 0.0d, DPTable);

                // for all descendents of aKeyroot: i
                for (int i = aLeftLeaf.get(aKeyroot); i <= aKeyroot; i++) {
                    setFD(i,
                            bLeftLeaf.get(bKeyroot) - 1,
                            getFD(i - 1, bLeftLeaf.get(bKeyroot) - 1, DPTable)
                                    + ops.getOp(OpsZhangShasha.DELETE).getCost(i, 0, FTree, GTree),
                            DPTable);
                }

                // for all descendents of bKeyroot: j
                for (int j = bLeftLeaf.get(bKeyroot); j <= bKeyroot; j++) {
                    setFD(aLeftLeaf.get(aKeyroot) - 1,
                            j,
                            getFD(aLeftLeaf.get(aKeyroot) - 1, j - 1, DPTable)
                                    + ops.getOp(OpsZhangShasha.INSERT).getCost(0, j, FTree, GTree),
                            DPTable);
                }

                // for all descendents of aKeyroot: i
                for (int i = aLeftLeaf.get(aKeyroot); i <= aKeyroot; i++) {
                    // System.out.println("i: "+i);

                    // for all descendents of bKeyroot: j
                    for (int j = bLeftLeaf.get(bKeyroot); j <= bKeyroot; j++) {
                        // This min compares del vs ins
                        double min = java.lang.Math.min(
                                getFD(i - 1, j, DPTable)
                                        + ops.getOp(OpsZhangShasha.DELETE).getCost(i, 0, FTree,
                                                GTree),// Option 1: Delete node
                                                       // from FTree
                                getFD(i, j - 1, DPTable)
                                        + ops.getOp(OpsZhangShasha.INSERT).getCost(0, j, FTree,
                                                GTree)// Option 2: Insert node
                                                      // into GTree
                        );

                        if ((aLeftLeaf.get(i) == aLeftLeaf.get(aKeyroot)) &&
                                (bLeftLeaf.get(j) == bLeftLeaf.get(bKeyroot))) {

                            if ((i <= Size1 - 1) && (j <= Size2 - 1))
                                distance[i][j] = min(
                                        min,
                                        getFD(i - 1, j - 1, DPTable)
                                                + ops.getOp(OpsZhangShasha.RENAME).getCost(i, j,
                                                        FTree, GTree));

                            if ((i <= Size1 - 1) && (j <= Size2 - 1))
                                setFD(i, j, distance[i][j], DPTable);
                        }
                        else {
                            setFD(i,
                                    j,
                                    java.lang.Math.min(
                                            min,
                                            getFD(aLeftLeaf.get(i) - 1, bLeftLeaf.get(j) - 1,
                                                    DPTable) + distance[i][j]),
                                    DPTable);
                        }
                    }
                }
                FinalDPTable = DPTable;
            }
        }

        Transformation transform = new Transformation();
        double _cost = distance[FTree.CountNodes(FTree.getLabel())][GTree.CountNodes(GTree
                .getLabel())];
        transform.setCost(_cost);
        return transform;
    }

    public String reportDifferences() {
        int f = Size1 - 1;
        int g = Size2 - 1;
        String diffs = "";
        Boolean ReportBlanks = false;
        while ((f >= 0) && (g >= 0)) {
            String action = "UNKNOWN";
            Double updist = getFD((f - 1), g, FinalDPTable);
            Double leftdist = getFD(f, (g - 1), FinalDPTable);
            Double diagdist = getFD((f - 1), (g - 1), FinalDPTable);
            Double mindist = -1.0;

            // find the minimums
            mindist = diagdist;
            if (updist < mindist)
                mindist = updist;
            if (leftdist < mindist)
                mindist = leftdist;

            if (updist < getFD(f, g, FinalDPTable)) {
                if ((ReportBlanks) || (!FTree.findNode(f).getLabelerValue(labeler).equals(""))) {
                    action = String.format("%s[%s] REMOVED%n", FTree.findNode(f).getLabel(),
                            FTree.findNode(f).getLabelerValue(labeler));
                    diffs = diffs + action;
                }
            }
            else if (diagdist < getFD(f, g, FinalDPTable)) {
                if ((ReportBlanks) || (!FTree.findNode(f).getLabelerValue(labeler).equals(""))
                        || (!GTree.findNode(g).getLabelerValue(labeler).equals(""))) {
                    action = String.format("%s[%s] RENAMED to %s[%s]%n",
                            FTree.findNode(f).getLabel(),
                            FTree.findNode(f).getLabelerValue(labeler),
                            GTree.findNode(g).getLabel(),
                            GTree.findNode(g).getLabelerValue(labeler));
                    diffs = diffs + action;
                }
            }
            else if (leftdist < getFD(f, g, FinalDPTable)) {
                if ((ReportBlanks) || (!GTree.findNode(g).getLabelerValue(labeler).equals(""))) {
                    action = String.format("%s[%s] INSERTED%n", GTree.findNode(g).getLabel(),
                            GTree.findNode(g).getLabelerValue(labeler));
                    diffs = diffs + action;
                }
            }
            // find the minimums
            mindist = diagdist;
            if (updist < mindist)
                mindist = updist;
            if (leftdist < mindist)
                mindist = leftdist;

            if (diagdist <= mindist) {
                f = f - 1;
                g = g - 1;
            }
            else if (updist <= mindist)
                f = f - 1;
            else
                g = g - 1;
        }
        if (!diffs.isEmpty()) {
            // seeFD(FinalDPTable); //trace
            diffs = String.format("FINAL GLORY - > %n%s", diffs);
        }
        else {
            diffs = "FINAL GLORY - > NO DIFFERENCES";
        }
        return diffs;
    }

    /**
     * The initiating call should be to the root node of the tree.
     * It fills in an nxn (hash) table of the leftmost leaf for a
     * given node. It also compiles an array of key roots. The
     * integer values IDs must come from the post-ordering of the
     * nodes in the tree.
     */
    private void findHelperTables(TreeNode someTree, Hashtable<Integer, Integer> leftmostLeaves,
            ArrayList<Integer> keyroots, int aNodeID) {
        findHelperTablesRecurse(someTree, leftmostLeaves, keyroots, aNodeID);

        // add root to keyroots
        keyroots.add(aNodeID);

        // add boundary nodes
        leftmostLeaves.put(0, 0);
    }

    private void findHelperTablesRecurse(TreeNode someTree,
            Hashtable<Integer, Integer> leftmostLeaves, ArrayList<Integer> keyroots, int aNodeID) {
        // System.out.println("findHelperTablesRecurse started");
        // System.out.println("aNodeID="+aNodeID);
        // someTree.PrintSuGTree();
        // System.out.println("someTree.ChildNodes.size()="+someTree.ChildNodes.size());
        // If this is a leaf, then it is the leftmost leaf
        if (someTree.GetChildCount() == 0) {
            leftmostLeaves.put(aNodeID, aNodeID);
        }
        else {
            // System.out.println("AAA");
            boolean seenLeftmost = false;
            for (int count = 0; count < someTree.GetChildCount(); count++)
            // for (Integer child : someTree.getChildrenIDs(aNodeID))
            {
                findHelperTablesRecurse(someTree.GetIthChild(count), leftmostLeaves, keyroots,
                        someTree.GetIthChild(count).GetPostOrderID());
                if (!seenLeftmost) {
                    // System.out.println("someTree.ChildNodes.get(count).GetChildNumber()="+someTree.ChildNodes.get(count).GetChildNumber());
                    // System.out.println("leftmostLeaves.get(someTree.ChildNodes.get(count).GetChildNumber())="+leftmostLeaves.get(someTree.ChildNodes.get(count).GetChildNumber()));
                    // System.out.println("leftmostLeaves.get(someTree.ChildNodes.get(count).GetID())="+leftmostLeaves.get(someTree.ChildNodes.get(count).GetID()));
                    // someTree.PrintSuGTree();
                    leftmostLeaves.put(aNodeID,
                            leftmostLeaves.get(someTree.GetIthChild(count).GetPostOrderID()));
                    // leftmostLeaves.put(aNodeID,
                    // leftmostLeaves.get(someTree.ChildNodes.get(count).GetChildNumber()));
                    seenLeftmost = true;
                }
                else {
                    keyroots.add(someTree.GetIthChild(count).GetPostOrderID());
                }
            }
        }
    }

    /**
     * This returns the value in the cell of the ForestDistance table
     */
    private double getFD(int a, int b,
            Hashtable<Integer, Hashtable<Integer, Double>>
            forestDistance) {
        Hashtable<Integer, Double> rows = null;
        if (!forestDistance.containsKey(a)) {
            // System.out.println("getFD: creating new aStr entry.");
            forestDistance.put(a, new Hashtable<Integer, Double>());
        }

        rows = forestDistance.get(a);
        if (!rows.containsKey(b)) {
            // System.out.println("creating new bStr entry.");
            rows.put(b, 0.0);
        }
        return rows.get(b);
    }

    /**
     * This sets the value in the cell of the ForestDistance table
     */
    private void setFD(int a, int b,
            double aValue,
            Hashtable<Integer, Hashtable<Integer, Double>>
            forestDistance) {

        Hashtable<Integer, Double> rows = null;
        if (!forestDistance.containsKey(a)) {
            // System.out.println("setFD: creating new aStr entry.");
            forestDistance.put(a, new Hashtable<Integer, Double>());
        }

        rows = forestDistance.get(a);
        rows.put(b, aValue);

        // for (String key: forestDistance.keySet()) {
        // System.out.println("FD key: "+key);
        // for (String key2: forestDistance.get(key).keySet()) {
        // System.out.println("FD key2: "+key2);
        // }
        // }
    }
}