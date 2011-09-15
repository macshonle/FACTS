package facts.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import facts.ast.NodeLabeler;
import facts.ast.TreeNode;


/** This is an implementation of the Zhang and Shasha algorithm as
 * described in [FIXME]
 *
 * SWAN 2007-11-01: I'm pretty sure this code comes from:
 * http://www.cs.queensu.ca/TechReports/Reports/1995-372.pdf and
 *"http://www.inf.unibz.it/dis/teaching/ATA/ata7-handout-1x1.pdf"
 * INSERT-LICENCE-INFO
 */
public class ComparisonZhangShasha 
{  

    //"Dynamic Programming" Table.
    //use function setFD to access it.
    //Each call to findDistance will change these tables.  But each
    //call is independent (and reinitialises this) so the side effect
    //has no real consequence.  ie.  There are NO public side effects.
//    private Hashtable<String, Hashtable<String, Double>> forestDistance = null; TODO: never read
    private double[][] distance = null;

    public Transformation findDistance (TreeNode FTree, TreeNode GTree, OpsZhangShasha ops, StringBuilder out, NodeLabeler labeler) 
    {  
        //This is initialized to be n+1 * m+1.  It should really be n*m
        //but because of java's zero indexing, the for loops would
        //look much more readable if the matrix is extended by one
        //column and row.  So, distance[0,*] and distance[*,0] should
        //be permanently zero.
        // distance = new double[FTree.getNodeCount()+1][GTree.getNodeCount()+1];
        int Size1 = FTree.CountNodes(FTree.getLabel())+1;
        int Size2 = GTree.CountNodes(GTree.getLabel())+1;
        int FullSize = Size1 + Size2;
//        distance = new double[FTree.CountNodes(FTree.GetToken(), false)+1][GTree.CountNodes(GTree.GetToken(), false)+1];
        distance = new double[Size1][Size2];

        //Preliminaries
        //1. Find left-most leaf and key roots
        Hashtable<Integer, Integer> aLeftLeaf = new Hashtable<Integer, Integer>();
        Hashtable<Integer, Integer> bLeftLeaf = new Hashtable<Integer, Integer>();
        ArrayList<Integer> FTreeKeyRoots = new ArrayList<Integer>();
        ArrayList<Integer> GTreeKeyRoots = new ArrayList<Integer>();

        findHelperTables(FTree, aLeftLeaf, FTreeKeyRoots, FTree.GetPostOrderID());

        findHelperTables(GTree, bLeftLeaf, GTreeKeyRoots, GTree.GetPostOrderID());

//System.out.println("There are "+aLeftLeaf.size()+ " elements in aLeftLeaf");

Collection ca = aLeftLeaf.values();
//System.out.println("Values of Collection created from Hashtable are :");
//iterate through the collection
Iterator itra = ca.iterator();
//while(itra.hasNext())
//    System.out.println(itra.next());
//System.out.println("There are "+bLeftLeaf.size()+ " elements in bLeftLeaf");
Collection cs = bLeftLeaf.values();
//System.out.println("Values of Collection created from Hashtable are :");
//iterate through the collection
Iterator itrs = cs.iterator();
//while(itrs.hasNext())
//    System.out.println(itrs.next());

//System.out.println("There are "+FTreeKeyRoots.size()+ " elements in FTreeKeyRoots");
//Collection csk = FTreeKeyRoots.values();
//System.out.println("Values of Collection created from Hashtable are :");
//iterate through the collection
Iterator itrsk = FTreeKeyRoots.iterator();
//while(itrsk.hasNext())
//    System.out.println(itrsk.next());

//System.out.println("There are "+GTreeKeyRoots.size()+ " elements in GTreeKeyRoots");
//Collection csk2 = GTreeKeyRoots.values();
//System.out.println("Values of Collection created from Hashtable are :");
//iterate through the collection
Iterator itrsk2 = GTreeKeyRoots.iterator();
//while(itrsk2.hasNext())
//    System.out.println(itrsk2.next());


//   System.out.println("FTree Keyroots");
//    for (Integer aKeyroot : FTreeKeyRoots) {
//       System.out.println(aKeyroot);
//   }

//   System.out.println("l(FTree)");
//    for (Integer key : new TreeSet<Integer>(aLeftLeaf.keySet())) {
//       System.out.println(key+": "+aLeftLeaf.get(key));
//   }

//   System.out.println("GTree Keyroots");
//   for (Integer bKeyroot : GTreeKeyRoots) {
//       System.out.println(bKeyroot);
//   }

//   System.out.println("l(GTree)");
//    for (Integer key : new TreeSet<Integer>(bLeftLeaf.keySet())) {
//       System.out.println(key+": "+bLeftLeaf.get(key));
//   }

        //Comparison
        for (Integer aKeyroot : FTreeKeyRoots) 
        { //aKeyroot loop
//         System.out.println("aKeyroot: "+aKeyroot);

            for (Integer bKeyroot : GTreeKeyRoots) 
            { //bKeyroot loop

                //Re-initialise forest distance tables
                Hashtable<Integer, Hashtable<Integer, Double>> DPTable = new Hashtable<Integer, Hashtable<Integer, Double>>();

//        for (Integer aKeyrootX : FTreeKeyRoots) 
//          System.out.println("aKeyrootX="+aKeyrootX + " aLeftLeaf.get(aKeyrootX)="+aLeftLeaf.get(aKeyrootX));
//        for (Integer bKeyrootX : GTreeKeyRoots) 
//          System.out.println("bKeyrootX="+bKeyrootX + " bLeftLeaf.get(bKeyrootX)="+bLeftLeaf.get(bKeyrootX));

//          System.out.println("aLeftLeaf="+aLeftLeaf);
//          System.out.println("bLeftLeaf="+bLeftLeaf);

//System.out.println("aKeyroot="+aKeyroot);
//System.out.println("bKeyroot="+bKeyroot);
                setFD(aLeftLeaf.get(aKeyroot),bLeftLeaf.get(bKeyroot),0.0d, DPTable);
//System.out.println("111");
//seeFD(DPTable);

                //for all descendents of aKeyroot: i
                for (int i=aLeftLeaf.get(aKeyroot);i <= aKeyroot;i++) 
                {
                    setFD(i,
                          bLeftLeaf.get(bKeyroot)-1,
                          getFD(i-1,bLeftLeaf.get(bKeyroot)-1,DPTable)+ops.getOp(OpsZhangShasha.DELETE).getCost(i,0,FTree,GTree),
                         DPTable);
//System.out.println("222");
//seeFD(DPTable);

//         //trace
//         seeFD(i,bLeftLeaf.get(bKeyroot)-1,fD);
                }

                //for all descendents of bKeyroot: j
//System.out.println("+++++++++++++++++++++++++++++++++++++");
//System.out.println("bLeftLeaf.get(bKeyroot)="+bLeftLeaf.get(bKeyroot)+" bKeyroot="+bKeyroot);
//System.out.println("+++++++++++++++++++++++++++++++++++++");
                
                for (int j=bLeftLeaf.get(bKeyroot);j <= bKeyroot;j++) 
                {
                    setFD(aLeftLeaf.get(aKeyroot)-1,
                          j,
                          getFD(aLeftLeaf.get(aKeyroot)-1,j-1,DPTable)+ops.getOp(OpsZhangShasha.INSERT).getCost(0,j,FTree,GTree),
                          DPTable);
//System.out.println("333");
//seeFD(DPTable);

//         //trace
//         seeFD(aLeftLeaf.get(aKeyroot)-1,j,fD);
                }
    
//     System.out.println("BEFORE cross checks: ");
//     seeFD(fD);

    //for all descendents of aKeyroot: i
                    for (int i=aLeftLeaf.get(aKeyroot);i<=aKeyroot;i++) 
                    {
//           System.out.println("i: "+i);

        //for all descendents of bKeyroot: j
                        for (int j=bLeftLeaf.get(bKeyroot);j<=bKeyroot;j++) 
                        {
//          System.out.println("j: "+j);

       //Start Trace
//        System.out.println
//            ("DEL: "+
//             (getFD(i-1,j,fD)+
//             ops.getOp(OpsZhangShasha.DELETE)
//              .getCost(i,0,FTree,GTree)));

//        System.out.println
//            ("INS: "+
//             (getFD(i,j-1,fD)+
//             ops.getOp(OpsZhangShasha.INSERT)
//              .getCost(0,j,FTree,GTree)));

       //End Trace
         //This min compares del vs ins
                                    
                        double min = java.lang.Math.min(getFD(i-1,j,DPTable)+ops.getOp(OpsZhangShasha.DELETE).getCost(i,0,FTree,GTree),//Option 1: Delete node from FTree
                                                        getFD(i,j-1,DPTable)+ops.getOp(OpsZhangShasha.INSERT).getCost(0,j,FTree,GTree)//Option 2: Insert node into GTree
                                                       );
            
    
//        System.out.println("Min: "+min);
       
                        if ((aLeftLeaf.get(i) == aLeftLeaf.get(aKeyroot)) && 
                            (bLeftLeaf.get(j) == bLeftLeaf.get(bKeyroot))) 
                        {

//           System.out.println("This is a Left-branch node.");

//         System.out.println
//             ("REN: "+
//              (getFD(i-1,j-1,fD) +
//              ops.getOp(OpsZhangShasha.RENAME)
//               .getCost(i,j,FTree,GTree)));

if ((i <= Size1-1) && (j <= Size2-1))
//System.out.println("i="+i+" j="+j+" Size1="+Size1+" Size2="+Size2+" distance[i][j]="+distance[i][j]);
                            distance[i][j] = java.lang.Math.min(min, getFD(i-1,j-1,DPTable) + ops.getOp(OpsZhangShasha.RENAME).getCost(i,j,FTree,GTree));

//          System.out.println("D["+i+"]["+j+"]:"+
//                 distance[i][j]);

if ((i <= Size1-1) && (j <= Size2-1))
                            setFD(i,j,distance[i][j],DPTable);
//System.out.println("444");
//seeFD(DPTable);

        //trace
//         seeFD(i,j, fD);
                        }
                        else 
                        {
          
//            System.out.println("Forest Situation");

//           System.out.println
//         ("REN: "+
//          (getFD(aLeftLeaf.get(i)-1,
//           bLeftLeaf.get(j)-1,fD)+
//           distance[i][j] ));
        
                            setFD(i,
                                  j,
                                  java.lang.Math.min(min,getFD(aLeftLeaf.get(i)-1,bLeftLeaf.get(j)-1,DPTable)+distance[i][j]),
                                  DPTable
                                 );
//System.out.println("555");
//seeFD(DPTable);

//           seeFD(i, j, fD);
                        }
                    }
                }

//     System.out.println("*   aKeyroot: "+aKeyroot+"   bKeyroot: "+bKeyroot);
//System.out.println("666");
//     seeFD(DPTable); //trace
                   int f = Size1-1;
   int g = Size2-1;
   String diffs = "";
   while ((f>=0)&&(g>=0))
   {
//System.out.println("f="+f+" g="+g);
       String action = "UNKNOWN";
       Double updist = getFD((f-1),g,DPTable);
       Double leftdist = getFD(f,(g-1),DPTable);
       Double diagdist = getFD((f-1),(g-1),DPTable);
       Double mindist = -1.0;
//System.out.println("dist="+getFD(f,g,DPTable)+" updist="+updist+" leftdist="+leftdist+" diagdist="+diagdist);

       // find the minimums
       mindist = diagdist;
       if (updist < mindist)
           mindist = updist;
       if (leftdist < mindist)
           mindist = leftdist;
       
       if (updist < getFD(f,g,DPTable))
       {
          action = FTree.findNode(f).getLabel() + " REMOVED("+f+"), ";
          diffs = diffs + action;
       }
       else if (diagdist < getFD(f,g,DPTable))
       {
          action = FTree.findNode(f).getLabel() + " RENAMED to " + GTree.findNode(g).getLabel() + " ("+f+","+g+"), ";
          diffs = diffs + action;
       }
       else if (leftdist < getFD(f,g,DPTable))
       {
          action = GTree.findNode(g).getLabel() + " INSERTED ("+g+"), ";
          diffs = diffs + action;
       }
       
/*       if ((updist < leftdist) && (updist < diagdist))
       {
//System.out.println(" up f="+f+" g="+g);
          mindist = updist;
          if (mindist < getFD(f,g,DPTable))
          {
          action = FTree.FindNode(f).GetToken() + " REMOVED("+f+"), ";
          diffs = diffs + action;
          }
          f = f - 1;
       }
       else if ((leftdist < updist) && (leftdist < diagdist))
       {
//System.out.println(" left f="+f+" g="+g);
          mindist = leftdist;
          if (mindist < getFD(f,g,DPTable))
          {
          action = GTree.FindNode(g).GetToken() + " INSERTED ("+g+"), ";
          diffs = diffs + action;
          }
          g = g - 1;
       }
       else if ((diagdist < updist) && (diagdist < leftdist))
       {
//System.out.println(" diag f="+f+" g="+g);
          mindist = diagdist;
          if (mindist < getFD(f,g,DPTable))
          {
            action = FTree.FindNode(f).GetToken() + " RENAMED to " + GTree.FindNode(g).GetToken() + " ("+f+","+g+"), ";
            diffs = diffs + action;
          }
          f = f - 1;
          g = g - 1;
       }
       else*/
       {
          // find the minimums
          mindist = diagdist;
          if (updist < mindist)
              mindist = updist;
          if (leftdist < mindist)
              mindist = leftdist;
          
          if (diagdist <= mindist)
          {
              f = f - 1;
              g = g - 1;
          }
          else if (updist <= mindist)
              f = f - 1;
          else
              g = g - 1;
       }
   }
if (!diffs.equals(""))
{
//     seeFD(DPTable); //trace
   System.out.println("FINAL GLORY - > " + diffs);
   }
            }
        }

//   //Return result
//   for (int i=0;i<distance.length;i++) {
//       for (int j=0;j<distance[i].length;j++) {
//     System.out.print(distance[i][j]+" ");
//       }
//       System.out.println("");
//   }

//    System.out.println("Distance: "
//          +distance[FTree.CountNodes(FTree.GetToken(), false)][GTree.CountNodes(GTree.GetToken(), false)]);

        Transformation transform = new Transformation();
        transform.setCost(distance[FTree.CountNodes(FTree.getLabel())][GTree.CountNodes(GTree.getLabel())]);
        return transform;
    }

    /** The initiating call should be to the root node of the tree.
     * It fills in an nxn (hash) table of the leftmost leaf for a
     * given node.  It also compiles an array of key roots. The
     * integer values IDs must come from the post-ordering of the
     * nodes in the tree.
     */
    private void findHelperTables (TreeNode someTree, Hashtable<Integer, Integer> leftmostLeaves, ArrayList<Integer> keyroots, int aNodeID) 
    {
        findHelperTablesRecurse(someTree, leftmostLeaves, keyroots, aNodeID);

        //add root to keyroots
        keyroots.add(aNodeID);

        //add boundary nodes
        leftmostLeaves.put(0,0); 
    }  

    private void findHelperTablesRecurse(TreeNode someTree, Hashtable<Integer, Integer> leftmostLeaves, ArrayList<Integer> keyroots, int aNodeID) 
    {
//System.out.println("findHelperTablesRecurse started");
//System.out.println("aNodeID="+aNodeID);
//someTree.PrintSuGTree();
//System.out.println("someTree.ChildNodes.size()="+someTree.ChildNodes.size());
        //If this is a leaf, then it is the leftmost leaf
        if (someTree.GetChildCount() == 0) 
        {
            leftmostLeaves.put(aNodeID, aNodeID);
        }
        else 
        {
//System.out.println("AAA");
             boolean seenLeftmost = false;
             for (int count = 0; count < someTree.GetChildCount(); count++)
//            for (Integer child : someTree.getChildrenIDs(aNodeID)) 
            {
                findHelperTablesRecurse(someTree.GetIthChild(count), leftmostLeaves, keyroots, someTree.GetIthChild(count).GetPostOrderID());
                if (!seenLeftmost) 
                {
//System.out.println("someTree.ChildNodes.get(count).GetChildNumber()="+someTree.ChildNodes.get(count).GetChildNumber());
//System.out.println("leftmostLeaves.get(someTree.ChildNodes.get(count).GetChildNumber())="+leftmostLeaves.get(someTree.ChildNodes.get(count).GetChildNumber()));
//System.out.println("leftmostLeaves.get(someTree.ChildNodes.get(count).GetID())="+leftmostLeaves.get(someTree.ChildNodes.get(count).GetID()));
//someTree.PrintSuGTree();
                    leftmostLeaves.put(aNodeID, leftmostLeaves.get(someTree.GetIthChild(count).GetPostOrderID()));
//                    leftmostLeaves.put(aNodeID, leftmostLeaves.get(someTree.ChildNodes.get(count).GetChildNumber()));
                    seenLeftmost = true;
                }
                else 
                {
                    keyroots.add(someTree.GetIthChild(count).GetPostOrderID());
                }
            }
        }
    }

    /** Returns a String for trace writes */
    private void seeFD(int a, int b,
           Hashtable<Integer, Hashtable<Integer,Double>> 
           forestDistance) {

   System.out.println("["+a+"],["+b+"]: "+getFD(a,b,forestDistance));
    }

    /** Returns a String for trace writes */
    private void seeFD(Hashtable<Integer, Hashtable<Integer,Double>> 
           forestDistance) {
  System.out.println("Forest Distance");
  //Return result
  for (Integer i : new TreeSet<Integer>(forestDistance.keySet())) {
      System.out.print(i+": ");
      for (Integer j : new TreeSet<Integer>(forestDistance.get(i).keySet())) {
    System.out.print(forestDistance.get(i).get(j)+"("+j+")  ");
      }
      System.out.println("");
  }
    }

    /** This returns the value in the cell of the ForestDistance table
     */
    private double getFD(int a, int b,
           Hashtable<Integer, Hashtable<Integer,Double>> 
           forestDistance) {

  Hashtable<Integer, Double> rows = null;
  if (!forestDistance.containsKey(a)) {
//        System.out.println("getFD: creating new aStr entry.");
      forestDistance.put(a, new Hashtable<Integer, Double>());
  }

  rows = forestDistance.get(a);
  if (!rows.containsKey(b)) {
//       System.out.println("creating new bStr entry.");
      rows.put(b, 0.0);
  }
  return rows.get(b);
    }


    /** This sets the value in the cell of the ForestDistance table
     */
    private void setFD(int a, int b,
           double aValue, 
           Hashtable<Integer, Hashtable<Integer,Double>> 
           forestDistance ) {

  Hashtable<Integer, Double> rows = null;
  if (!forestDistance.containsKey(a)) {
//        System.out.println("setFD: creating new aStr entry.");
      forestDistance.put(a, new Hashtable<Integer, Double>());
  }

  rows = forestDistance.get(a);
  rows.put(b, aValue);

//    for (String key: forestDistance.keySet()) {
//        System.out.println("FD key: "+key);
//       for (String key2: forestDistance.get(key).keySet()) {
//     System.out.println("FD key2: "+key2);
//       }
//    }
    }

}
