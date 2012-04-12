package facts.diff;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Hashtable;

import facts.ast.TreeNode;
import facts.ast.UniqueTreeBuilder;
import java.util.TreeSet;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
    private int[][] distance = null;
    private int[][] memoize = null;
    private int[] aLeftLeaf2 = null;
    private int[] bLeftLeaf2 = null;
    private int Size1 = -1;
    private int Size2 = -1;
    // Hashtable<FTree.key, Hashtable<GTree.key, FD from FTree to GTree>>
//    private Hashtable<Integer, Hashtable<Integer, Integer>> FinalDPTable = new Hashtable<Integer, Hashtable<Integer, Integer>>();
    private int[][] FinalDPTable2 = null;
    private TreeNode FTree;
    private TreeNode GTree;
    private UniqueTreeBuilder labeler;
    private Boolean ShowSingleChange = true;
    private Boolean ShowLineChange = true;
    
    public String findDistance(TreeNode _FTree, TreeNode _GTree, OpsZhangShasha ops,
            StringBuilder out, UniqueTreeBuilder _labeler) {
    	String rval = "";
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
        System.out.println("FTree size = "+Size1);
        System.out.println("GTree size = "+Size2);
        distance = new int[Size1][Size2];
//        memoize = new int[Size1][Size2];
        for (int i=0;i<Size1;i++)
        	for (int j=0;j<Size2;j++)
        	{
        		distance[i][j] = -1;
//        		memoize[i][j] = -1;
        	}
        // set the post ordering ids
        FTree.setPostOrdering(0);
        GTree.setPostOrdering(0);
        // Preliminaries
        // the set of left-most leaf for a given node
        // For example:
        //   55=1 means that for the node with post-order ID of 55,
        //           the left most leaf has post-order ID of 1
//        Hashtable<Integer, Integer> aLeftLeaf = new Hashtable<Integer, Integer>();
//        Hashtable<Integer, Integer> bLeftLeaf = new Hashtable<Integer, Integer>();
        aLeftLeaf2 = new int[Size1];
        bLeftLeaf2 = new int[Size2];
        // keyroots are the root and where the node has a left sibling
        ArrayList<Integer> FTreeKeyRoots = new ArrayList<Integer>();
        ArrayList<Integer> GTreeKeyRoots = new ArrayList<Integer>();
        // now find the leftmost leaf and all the keyroots
        System.out.println("ASD");
        findHelperTables(FTree, aLeftLeaf2, FTreeKeyRoots, FTree.GetPostOrderID());
        System.out.println("FGH");
        findHelperTables(GTree, bLeftLeaf2, GTreeKeyRoots, GTree.GetPostOrderID());
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        int keytrigger = 0;

        // for each keyroot combination
        for (Integer aKeyroot : FTreeKeyRoots) { // aKeyroot loop
        	if (aKeyroot > keytrigger){
        		System.out.println("aKeyroot="+aKeyroot+" "+dateFormat.format(new Date()));
        		keytrigger = keytrigger + 1;
        	}
            for (Integer bKeyroot : GTreeKeyRoots) { // bKeyroot loop
                int flabel = Integer.parseInt(FTree.findNode(aKeyroot).getLabel());
                int glabel = Integer.parseInt(GTree.findNode(bKeyroot).getLabel());
                // Re-initialize forest distance tables
//                Hashtable<Integer, Hashtable<Integer, Integer>> DPTable = new Hashtable<Integer, Hashtable<Integer, Integer>>();
                int[][] DPTable2 = new int[Size1][Size2];
//                setFD(aLeftLeaf.get(aKeyroot), bLeftLeaf.get(bKeyroot), 0, DPTable);
                setFD(aLeftLeaf2[aKeyroot], bLeftLeaf2[bKeyroot], 0, DPTable2);

                // for all descendants of aKeyroot: i
                for (int i = aLeftLeaf2[aKeyroot]; i <= aKeyroot; i++) {
                    setFD(i,
                            bLeftLeaf2[bKeyroot] - 1,
                            getFD(i - 1, bLeftLeaf2[bKeyroot] - 1, DPTable2)
                                    + ops.getOp(OpsZhangShasha.DELETE).getCost(i, 0, FTree, GTree),
                            DPTable2);
                }

                // for all descendants of bKeyroot: j
                for (int j = bLeftLeaf2[bKeyroot]; j <= bKeyroot; j++) {
                    setFD(aLeftLeaf2[aKeyroot] - 1,
                            j,
                            getFD(aLeftLeaf2[aKeyroot] - 1, j - 1, DPTable2)
                                    + ops.getOp(OpsZhangShasha.INSERT).getCost(0, j, FTree, GTree),
                            DPTable2);
                }
//            	if (aKeyroot == 1217)
//            		System.out.println("aKeyroot="+aKeyroot + " bKeyroot(2)="+bKeyroot+" "+dateFormat.format(new Date()));


                // for all descendants of aKeyroot: i
                for (int i = aLeftLeaf2[aKeyroot]; i <= aKeyroot; i++) {
                    // System.out.println("i: "+i);

                    // for all descendents of bKeyroot: j
                    for (int j = bLeftLeaf2[bKeyroot]; j <= bKeyroot; j++) {
                    	
                        //if (memoize[flabel][glabel] == -1)
                        //{
                    	///////////////////////////////////////////////
                    	// RIGHT HERE!  SEE IF THERE IS A WAY TO TELL
                    	// IF WE'VE ALREADY CALCULATED SUCH A FD BASED
                    	// UPON THE ROOT NUMBER AND SAVE OURSELVES THIS
                    	// CALCULATION
                    	///////////////////////////////////////////////
                    			
                        // This min compares del vs ins
                        Integer min = (int)java.lang.Math.min(
                                getFD(i - 1, j, DPTable2)
                                        + ops.getOp(OpsZhangShasha.DELETE).getCost(i, 0, FTree,
                                                GTree),// Option 1: Delete node
                                                       // from FTree
                                getFD(i, j - 1, DPTable2)
                                        + ops.getOp(OpsZhangShasha.INSERT).getCost(0, j, FTree,
                                                GTree)// Option 2: Insert node
                                                      // into GTree
                        );

                        if ((aLeftLeaf2[i] == aLeftLeaf2[aKeyroot]) &&
                                (bLeftLeaf2[j] == bLeftLeaf2[bKeyroot])) {

                            if ((i <= Size1 - 1) && (j <= Size2 - 1))
                                distance[i][j] = (int)min(
                                        min,
                                        getFD(i - 1, j - 1, DPTable2)
                                                + ops.getOp(OpsZhangShasha.RENAME).getCost(i, j,
                                                        FTree, GTree));

                            if ((i <= Size1 - 1) && (j <= Size2 - 1))
                                setFD(i, j, distance[i][j], DPTable2);
                        }
                        else {
                            setFD(i,
                                    j,
                                    java.lang.Math.min(
                                            min,
                                            getFD(aLeftLeaf2[i] - 1, bLeftLeaf2[j] - 1,
                                                    DPTable2) + distance[i][j]),
                                    DPTable2);
                    	}
                      //}
                    }
                }

//            	if (aKeyroot == 1217)
//            		System.out.println("aKeyroot="+aKeyroot + " bKeyroot(3)="+bKeyroot+" "+dateFormat.format(new Date()));
//                memoize[flabel][glabel] = getFD(aKeyroot, bKeyroot, DPTable);
//                FinalDPTable = null;
//              	FinalDPTable = DPTable;
              	FinalDPTable2 = DPTable2;
//                DPTable = null;
//              rval = seeFD(FinalDPTable);
            }
        }

//        for (int i=0;i<Size1;i++)
//        {
//            for (int j=0;j<Size2;j++)
//            	System.out.print(memoize[i][j]+ " ");
//        	System.out.println();
//        }
        Transformation transform = new Transformation();
        Integer _cost = distance[FTree.CountNodes(FTree.getLabel())][GTree.CountNodes(GTree
                .getLabel())];
        transform.setCost(_cost);

//               rval = seeFDfile(FinalDPTable2);
        return rval;
    }

    private String[] ConvertFileToString(String InFile)
    {
    	int idx = 0;
    	String[] stringlist = new String[1000];
        if (InFile.equals(""))
            System.out.println("Input file not supplied.  Unable to continue.");
          else
          {
            try 
            {
              BufferedReader in = new BufferedReader(new FileReader(InFile));
              String str;
              Boolean commentstarted = false;
              while ((str = in.readLine()) != null) 
              {
            	  String nstr = str;
            	  if ((nstr.indexOf("/*") != -1) && (nstr.indexOf("*/") != -1))
            	  {
               		  nstr = nstr.substring(0,nstr.indexOf("/*")) + nstr.substring(nstr.indexOf("*/")+2);
            	  }
            	  else if ((nstr.indexOf("/*") != -1))
            	  {
               		  nstr = nstr.substring(0,nstr.indexOf("/*"));
               		  commentstarted = true;
            	  }
            	  else if ((nstr.indexOf("*/") != -1))
            	  {
               		  nstr = nstr.substring(nstr.indexOf("*/")+2);
               		  commentstarted = false;
            	  }
            	  if (nstr.indexOf("//") != -1)
            		  nstr = nstr.substring(0,nstr.indexOf("//"));
            	  nstr = nstr.trim();
//            	  nstr = nstr.replace("true","TRUESPECIAL");
//            	  nstr = nstr.replace("false","FALSESPECIAL");
//            	  nstr = nstr.replace("0","ZEROSPECIAL");
            	  if (!(nstr.equals("")))
       			  {
            		  if (!commentstarted)
            		  {
	            	  stringlist[idx]=nstr;
	            	  idx++;
            		  }
       			  }
              }
          in.close();
            }
	        catch (IOException e) 
	        {
	        }
          } 
    	return stringlist;
    }
    
    public void SetLineNums(TreeNode tree, String[] code, int treesize)
    {
    	int x = 9;
		int codepos = 0;
		String curstr = code[codepos];
		int treepos = 0;
		String[] treelist = new String[4000];
		int treelistsize = 0;
		for (int i = 0; i < treesize; i++) {
			if (tree.findNode(i) != null) {
				if (tree.findNode(i).getLabelerValue(labeler) != null) {
					String lbl = tree.findNode(i).getLabelerValue(labeler);
					if (!(lbl.equals(""))) {
						treelist[treepos] = lbl;
						treepos++;
					}
				}
			}
		}
		treelistsize = treepos;
		treepos = 0;
		for (int i = 0; i < treesize; i++) {
			if (tree.findNode(i) != null) {
				if (tree.findNode(i).getLabelerValue(labeler) != null) {
					String lbl = tree.findNode(i).getLabelerValue(labeler);
					if (!(lbl.equals(""))) {
						// System.out.println(lbl);
						if (lbl.equals(treelist[treepos])) {
							treepos++;
						}
						if (curstr != null) {
							if ((curstr.indexOf(lbl) != -1) &&
								((!lbl.equals("true")) &&
								 (!lbl.equals("false")) && 
								 (!lbl.equals("0")))) {
								tree.findNode(i).lineNum = codepos + 1;
								// System.out.println(lbl+" A lineNum="+(codepos+1));
								curstr = curstr.substring(curstr.indexOf(lbl) + lbl.length());
							} else if ((!lbl.equals("true"))
									&& (!lbl.equals("false"))
									&& (!lbl.equals("0"))) {
								Boolean lblfound = false;
								while ((!lblfound) && (codepos < 1000))
								{
									codepos++;
									if (codepos == 23)
										System.out.println("AAA");
									curstr = code[codepos];
									if (curstr == null)
										codepos = 1000;
									else if (curstr.indexOf(lbl) != -1)
									{
										tree.findNode(i).lineNum = codepos + 1;
										curstr = curstr.substring(curstr.indexOf(lbl) + lbl.length());
										lblfound = true;
									}
								}
							} else if ((lbl.equals("true"))
									|| (lbl.equals("false"))
									|| (lbl.equals("0"))) {
								String nextnondeadtoken = "";
								for (int j = treepos; j < treelistsize; j++)
									if ((treelist[j] != "0") &&
										(treelist[j] != "true") &&
										(treelist[j] != "false")) {
										nextnondeadtoken = treelist[j];
										if (nextnondeadtoken.equals("0"))
											nextnondeadtoken = "";
										else if (nextnondeadtoken.equals("true"))
											nextnondeadtoken = "";
										else if (nextnondeadtoken.equals("false"))
											nextnondeadtoken = "";
										else
											break;
									}
								Boolean deadtokenfound = false;
								Boolean continueprocessing = true;
								String curstr2 = curstr;
								int codepos2 = codepos;
								while (continueprocessing) {
									if (curstr2.indexOf(lbl) != -1){
										deadtokenfound = true;
										continueprocessing = false;
									}
									else if (curstr2.indexOf(nextnondeadtoken) != -1){
										continueprocessing = false;
									}
									else{
										Boolean lbl2found = false;
										while ((!lbl2found) && (codepos2 < 1000))
										{
											codepos2++;
											curstr2 = code[codepos2];
											if (curstr2 == null){
												codepos2 = 1000;
											}
											else if (curstr2.indexOf(lbl) != -1)
											{
												deadtokenfound = true;
												continueprocessing = false;
												lbl2found = true;
											} else if (curstr2.indexOf(nextnondeadtoken) != -1){
												continueprocessing = false;
												lbl2found = true;
											}
										}
									}
								}
								if (deadtokenfound) {
									if (lbl.equals(treelist[treepos])) {
										treepos++;
									}
									if (curstr != null) {
										if (curstr.indexOf(lbl) != -1) {
											tree.findNode(i).lineNum = codepos + 1;
											// System.out.println(lbl+" A lineNum="+(codepos+1));
											curstr = curstr.substring(curstr.indexOf(lbl) + lbl.length());
										} else {
											Boolean lblfound = false;
											while ((!lblfound) && (codepos < 1000))
											{
												codepos++;
												if (codepos == 23)
													x = codepos;
												curstr = code[codepos];
												if (curstr == null)
													codepos = 1000;
												else if (curstr.indexOf(lbl) != -1)
												{
													tree.findNode(i).lineNum = codepos + 1;
													curstr = curstr.substring(curstr.indexOf(lbl) + lbl.length());
													lblfound = true;
												}
											}
										}
									}
								}
							}
						}

					}
				}
			}
		}
		codepos = x + codepos;
		System.out.println(x);
	}

    public String reportDifferences(Boolean ReportBlanks, String fileA, String fileB) {
    	
    	ReportBlanks = true;
        String[] fileAlist = ConvertFileToString(fileA);
        String[] fileBlist = ConvertFileToString(fileB);

        SetLineNums(FTree, fileAlist, Size1);
        SetLineNums(GTree, fileBlist, Size2);
        

    	for (int i=0;i<=Size1;i++)
    		if (GTree.findNode(i) != null)
	    		if (GTree.findNode(i).lineNum > 0)
	    			System.out.println(GTree.findNode(i).lineNum + " * " + GTree.findNode(i).getLabelerValue(labeler));
//        for (int i=0;i<1000;i++)
//        	if (fileAlist[i] != null)
//        		if (!(fileAlist[i].equals("")))
//        			System.out.println(fileAlist[i]);
        // flag on whether debug information should be used
        Boolean debug = false;
    	// the position in the DP table, initialized to the lower right hand corner
    	int f = Size1 - 1;
        int g = Size2 - 1;
        // get whichever is bigger
        int MaxSize = Size1;
        if(Size2 > MaxSize) MaxSize = Size2;
        // the string of all the differences
        String diffs = "";
        // a collection of the differences
        String[] thediffs = new String[MaxSize*100];
        // the count of differences found
        int diffcounter = 0;
        // flags to indicate that we've detected an insert or delete action
        Boolean InsertStarted = false;
        Boolean DeleteStarted = false;
    	String[] modlist = new String[1000];
    	int[] modflist = new int[1000];
    	int[] modglist = new int[1000];
    	int modindex = 0;
    	for (int i=0;i<1000;i++)
    	{
    		modlist[i] = "X";
    		modflist[i] = -3;
    		modglist[i] = -3;
    	}
    	int gprev = -99;
    	int fprev = -99;
    	int gline = 0;
    	int fline = 0;
    	int lastf = -99;
    	int lastg = -99;
        // while we haven't reached the upper left hand corner of the DP table
        while ((f != 0) && (g != 0)) {
        	// the detected change that was made
        	String action = "UNKNOWN";
/*        	if (GTree.findNode(g) != null)
        	{
	        	action = String.format("Z id=%s value=%s ??? <%s:%s:%s>%n", 
	        			GTree.findNode(g).getLabel(),
	                    GTree.findNode(g).getLabelerValue(labeler),
	                    getFD(f, g, FinalDPTable),f,g);
	            thediffs[diffcounter++] = action;
        	}
        	else
        	{
	        	action = String.format("Z id=null value=null ??? <%s:%s:%s>%n", 
	                    getFD(f, g, FinalDPTable),f,g);
	            thediffs[diffcounter++] = action;
        	}*/
        	// from the current cell in the DP table, get the value above, left and diagonal cells 
            Integer updist = getFD((f - 1), g, FinalDPTable2);
            Integer leftdist = getFD(f, (g - 1), FinalDPTable2);
            Integer diagdist = getFD((f - 1), (g - 1), FinalDPTable2);
            if (FTree.findNode(f) != null)
            	fline = FTree.findNode(f).lineNum-1;
            else
            	fline = -1;
            if (GTree.findNode(g) != null)
            	gline = GTree.findNode(g).lineNum-1;
            else
            	gline = -1;
            if (fline < 0) fline = -1;
            if (gline < 0) gline = -1;
            if ((fline >=0) || (gline >=0))
            	action = "UNKNOWN";
            
            String FLabel = "";; 
            String GLabel = ""; 
            String FLabeler = "null"; 
            String GLabeler = "null"; 
            int FLineNum = 0;
            int GLineNum = 0;
            String twoout = "";
            if (FTree.findNode(f) != null)
            {
            	FLabel = FTree.findNode(f).getLabel();
            	if (FTree.findNode(f).getLabelerValue(labeler) != null)
            		FLabeler = FTree.findNode(f).getLabelerValue(labeler);
           		FLineNum = FTree.findNode(f).lineNum;
            }
            if (GTree.findNode(g) != null)
            {
            	GLabel = GTree.findNode(g).getLabel();
            	if (GTree.findNode(g).getLabelerValue(labeler) != null)
            		GLabeler = GTree.findNode(g).getLabelerValue(labeler);
           		GLineNum = GTree.findNode(g).lineNum;
            }
            twoout = "f=" + f + " FLabel=" + FLabel + " FLabeler=" + FLabeler + " FLineNum=" + FLineNum;
            System.out.println(twoout);
            twoout = "g=" + g + " GLabel=" + GLabel + " GLabeler=" + GLabeler + " GLineNum=" + GLineNum;
            System.out.println(twoout);
            
            // if the diagonal is less than the current cell (a change is detected) 
            // and we're not inserting or deleting...
            if ((diagdist < getFD(f, g, FinalDPTable2)) &&
                    (!InsertStarted) && (!DeleteStarted)) {
            	// if this is a reportable change
            	if ((ReportBlanks) || (!FTree.findNode(f).getLabelerValue(labeler).equals(""))
                        || (!GTree.findNode(g).getLabelerValue(labeler).equals(""))) {
            		if ((fline >= 0) && (gline >= 0))
            		{
//	                    if (modlist[modindex].indexOf("CHG") == -1)
	//                    	modlist[modindex] = modlist[modindex] + "CHG";
	  //                  if (modflist[modindex] <= 0)
	    //                	modflist[modindex] = (fline);
	      //              if (modglist[modindex] <= 0)
	        //            	modglist[modindex] = (gline);
            		}
            		if (debug)
            		{
	                    action = String.format("id=%s value=%s CHANGED to id=%s value=%s <%s:%s:%s>%n",
	                    		FTree.findNode(f).getLabel(),
	                            FTree.findNode(f).getLabelerValue(labeler),
	                            GTree.findNode(g).getLabel(),
	                            GTree.findNode(g).getLabelerValue(labeler),getFD(f, g, FinalDPTable2),f,g);
	                    thediffs[diffcounter++] = action;
            		}
                	else
                		if (ShowSingleChange)
                		{
                			if ((fline > 0) && (gline > 0))
                			{
			                    //action = String.format("value=%s CHANGED to value=%s on [%s] to [%s]%n",
			                    //        FTree.findNode(f).getLabelerValue(labeler),
			                    //        GTree.findNode(g).getLabelerValue(labeler),
			                    //        fileAlist[fline],
			                    //        fileBlist[gline]);
			                    //thediffs[diffcounter++] = action;
			                    if ((gline != lastg) || (fline != lastf))
			                    {
				                    if (modlist[modindex].indexOf("CHG") == -1)
				                    	modlist[modindex] = modlist[modindex] + "CHG";
				                    if (modflist[modindex] <= 0)
				                    	modflist[modindex] = (fline);
				                    if (modglist[modindex] <= 0)
				                    	modglist[modindex] = (gline);
				                    modindex++;
				                    lastf = fline;
				                    lastg = gline;
			                    }
			                    
                			}
                		}
                }
            }
            // if an insert was detected
            else if (InsertStarted) {
            	// if this is a reportable change
                if ((ReportBlanks) || (!GTree.findNode(g).getLabelerValue(labeler).equals(""))) {
  //                  if (modlist[modindex].indexOf("INS") == -1)
  //                  	modlist[modindex] = modlist[modindex] + "INS";
  //                  if (modglist[modindex] <= 0)
  //                  	modglist[modindex] = (gline);
                	if (debug)
                	{
                		{
            			if (gline > 0)
            			{
		                	action = String.format("id=%s value=%s INSERTED <%s:%s:%s>%n", 
		                			GTree.findNode(g).getLabel(),
		                            GTree.findNode(g).getLabelerValue(labeler),
		                            getFD(f, g, FinalDPTable2),f,g);
		                    thediffs[diffcounter++] = action;

            			}
                		}
                	}
                	else
                		if (ShowSingleChange)
                		{
                			if (gline > 0)
                			{
			                	//action = String.format("value=%s INSERTED on [%s]%n", 
			                    //        GTree.findNode(g).getLabelerValue(labeler),
			                    //        fileBlist[gline]);
			                    //thediffs[diffcounter++] = action;
			                    if (gline != lastg)
			                    {
				                    if (modlist[modindex].indexOf("INS") == -1)
				                    	modlist[modindex] = modlist[modindex] + "INS";
				                    if (modglist[modindex] <= 0)
				                    	modglist[modindex] = (gline);
				                    modindex++;
				                    lastg = gline;
			                    }
			                    
                			}
                		}
                }
            }
            // if a delete was detected
            else if (DeleteStarted) {
            	// if this is a reportable change
                if ((ReportBlanks) || (!FTree.findNode(f).getLabelerValue(labeler).equals(""))) {
//                    if (modlist[modindex].indexOf("REM") == -1)
  //                  	modlist[modindex] = modlist[modindex] + "REM";
    //                if (modflist[modindex] <= 0)
      //              	modflist[modindex] = (fline);
                	if (debug)
                	{
	                	action = String.format("id=%s value=%s REMOVED <%s:%s:%s>%n", 
	                			FTree.findNode(f).getLabel(),
	                            FTree.findNode(f).getLabelerValue(labeler),getFD(f, g, FinalDPTable2),f,g);
	                    thediffs[diffcounter++] = action;
                	}
                	else
                		if (ShowSingleChange)
                		{
                			if (fline > 0)
                			{
		                	//action = String.format("value=%s REMOVED on [%s]%n", 
		                    //        FTree.findNode(f).getLabelerValue(labeler),
		                    //        fileAlist[fline]);
		                	//thediffs[diffcounter++] = action;
		                    if (fline != lastf)
		                    {
			                    if (modlist[modindex].indexOf("REM") == -1)
			                    	modlist[modindex] = modlist[modindex] + "REM";
			                    if (modflist[modindex] <= 0)
			                    	modflist[modindex] = (fline);
			                    modindex++;
			                    lastf = fline;
		                    }
		                    
            			}
                		}
                }
            }
            if (GTree.findNode(g) != null)
	            if ((gline) > 0)
	            	gprev = (gline);
            if (FTree.findNode(g) != null)
	            if ((fline) > 0)
	            	fprev = (fline);
            // if we *must* go left, then an insert was detected
            if ((leftdist < diagdist) && (leftdist < updist)) {
                InsertStarted = true;
                DeleteStarted = false;
            }
            // if we *must* go up, then a delete was detected
            else if ((updist < diagdist) && (updist < leftdist)) {
                InsertStarted = false;
                DeleteStarted = true;
            }
            // if we *must* go diagonal, then cancel any insert or delete
            else if ((diagdist < leftdist) && (diagdist < updist)) {
                InsertStarted = false;
                DeleteStarted = false;
            }
            // now if we *can* go left, do so
            if ((leftdist <= getFD(f, g, FinalDPTable2)) && (InsertStarted))
                g = g - 1;
            // now if we *can* go up, do so
            else if ((updist <= getFD(f, g, FinalDPTable2)) && (DeleteStarted))
                f = f - 1;
            // else go diagonal
            else {
                f = f - 1;
                g = g - 1;
            }
            int fnow = -2;
            int gnow =-2;
            if (FTree.findNode(f) != null)
                fnow = (fline);
            if (GTree.findNode(g) != null)
                gnow = (gline);
//        	if ((gprev != gnow) ||
//            		(fprev != fnow))
            if ((fnow > 0) && (fprev < 0))
            	fprev = fnow;
            if ((gnow > 0) && (gprev < 0))
            	gprev = gnow;
//        	if (((gnow > 0) && (gprev != gnow)) ||
//        		((fnow > 0) && (fprev != fnow)))
//        		modindex++;
            if (GTree.findNode(g) != null)
	            if ((gline) > 0)
	            	gprev = (gline);
            if (FTree.findNode(g) != null)
	            if ((fline) > 0)
	            	fprev = (fline);

        }

        if (ShowLineChange)
        {
	        for (int i=0;i<1000;i++)
	        {
	        	String modline = "";
	        	if (modlist[i].indexOf("INS") != -1)
	        		modline = modline + "INSERTED";
	        	if (modlist[i].indexOf("REM") != -1)
	        		if (modline.isEmpty())
	        			modline = modline + "REMOVED";
	        		else
	        			modline = modline + "/REMOVED";
	        	if (modlist[i].indexOf("CHG") != -1)
	        		if (modline.isEmpty())
	        			modline = modline + "CHANGED";
	        		else
	        			modline = modline + "/CHANGED";
	        	if ((modflist[i] > 0) && (modglist[i] > 0))
	        		modline = modline + String.format(" [%s] to [%s]%n",fileAlist[modflist[i]],fileBlist[modglist[i]]);
	        	else if (modflist[i] > 0)
	        		modline = modline + String.format(" [%s]%n",fileAlist[modflist[i]]);
	        	else if (modglist[i] > 0)
	        		modline = modline + String.format(" [%s]%n",fileBlist[modglist[i]]);
	        	thediffs[diffcounter++] = modline;
	        }
	    	thediffs[diffcounter++] = String.format("%n");
        }
        // reverse the differences found (since we found in reverse order)
        diffs = "";
        for (int i=diffcounter;i>=0;i--) {
        	if (thediffs[i] != null)
        	    diffs = diffs + thediffs[i];
        }

        // record all the changes to the string
        if (!diffs.isEmpty()) {
            diffs = String.format("DIFFERENCES DETECTED:%n%s", diffs);
        }
        else {
            diffs = "DIFFERENCES DETECTED: No differences detected.";
        }
        // return the differences found
        return diffs;
    }

    /**
     * The initiating call should be to the root node of the tree.
     * It fills in an nxn (hash) table of the leftmost leaf for a
     * given node. It also compiles an array of key roots. The
     * integer values IDs must come from the post-ordering of the
     * nodes in the tree.
     */
    private void findHelperTables(TreeNode someTree, 
    		int[] leftmostLeaves2,
            ArrayList<Integer> keyroots, int aNodeID) {
        findHelperTablesRecurse(someTree, leftmostLeaves2, keyroots, aNodeID);

        // add root to keyroots
        keyroots.add(aNodeID);

        // add boundary nodes
//        leftmostLeaves.put(0, 0);
        leftmostLeaves2[0] = 0;
    }

    private void findHelperTablesRecurse(TreeNode someTree,
            int[] leftmostLeaves2, 
            ArrayList<Integer> keyroots, int aNodeID) {
        // System.out.println("findHelperTablesRecurse started");
        // System.out.println("aNodeID="+aNodeID);
        // someTree.PrintSuGTree();
        // System.out.println("someTree.ChildNodes.size()="+someTree.ChildNodes.size());
        // If this is a leaf, then it is the leftmost leaf
        if (someTree.GetChildCount() == 0) {
            //leftmostLeaves.put(aNodeID, aNodeID);
            leftmostLeaves2[aNodeID] = aNodeID;
        }
        else {
            // System.out.println("AAA");
            boolean seenLeftmost = false;
            for (int count = 0; count < someTree.GetChildCount(); count++)
            // for (Integer child : someTree.getChildrenIDs(aNodeID))
            {
                findHelperTablesRecurse(someTree.GetIthChild(count), leftmostLeaves2, keyroots,
                        someTree.GetIthChild(count).GetPostOrderID());
                if (!seenLeftmost) {
                    // System.out.println("someTree.ChildNodes.get(count).GetChildNumber()="+someTree.ChildNodes.get(count).GetChildNumber());
                    // System.out.println("leftmostLeaves.get(someTree.ChildNodes.get(count).GetChildNumber())="+leftmostLeaves.get(someTree.ChildNodes.get(count).GetChildNumber()));
                    // System.out.println("leftmostLeaves.get(someTree.ChildNodes.get(count).GetID())="+leftmostLeaves.get(someTree.ChildNodes.get(count).GetID()));
                    // someTree.PrintSuGTree();
//                	Integer x = someTree.GetIthChild(count).GetPostOrderID();
  //              	Integer y = leftmostLeaves.get(someTree.GetIthChild(count).GetPostOrderID());
        //        	System.out.println(x+" "+y);
    //                leftmostLeaves.put(aNodeID,
      //                      leftmostLeaves.get(someTree.GetIthChild(count).GetPostOrderID()));
                    leftmostLeaves2[aNodeID] = leftmostLeaves2[someTree.GetIthChild(count).GetPostOrderID()]; 
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
    private Integer getFD(int a, int b,
            //Hashtable<Integer, Hashtable<Integer, Integer>> forestDistance, 
            int[][] forestDistance2) {
//        Hashtable<Integer, Integer> rows = null;
//        Hashtable<Integer, Integer> z = new Hashtable<Integer, Integer>();
//        if (!forestDistance.containsKey(a)) {
//            // System.out.println("getFD: creating new aStr entry.");
//            forestDistance.put(a, z);
//        }

//        rows = forestDistance.get(a);
//        if (!rows.containsKey(b)) {
//            // System.out.println("creating new bStr entry.");
//            rows.put(b, 0);
//        }
//        Integer retval = rows.get(b);
//        z = null;
//        return retval;
        return forestDistance2[a][b];

    }

    /**
     * This sets the value in the cell of the ForestDistance table
     */
    private void setFD(int a, int b,
            Integer aValue,
//            int[] forestDistance) {
            //Hashtable<Integer, Hashtable<Integer, Integer>>
            //forestDistance, 
            int[][] forestDistance2 ) {

//        Hashtable<Integer, Integer> rows = null;
//        Hashtable<Integer, Integer> z = new Hashtable<Integer, Integer>();
//        if (!forestDistance.containsKey(a)) {
//            // System.out.println("setFD: creating new aStr entry.");
//            forestDistance.put(a, z);
//        }

//        rows = forestDistance.get(a);
//        rows.put(b, aValue);

//        z = null;
        forestDistance2[a][b] = aValue;
        // for (String key: forestDistance.keySet()) {
        // System.out.println("FD key: "+key);
        // for (String key2: forestDistance.get(key).keySet()) {
        // System.out.println("FD key2: "+key2);
        // }
        // }
    }

    /** Returns a String for trace writes */    
    private String seeFD(Hashtable<Integer, Hashtable<Integer,Integer>>
                       forestDistance) {
    	String rval = String.format("Forest Distance");  
    	//Return result  
    	for (Integer i : new TreeSet<Integer>(forestDistance.keySet())) {
    		rval = String.format("%s%s: ",rval,i);
    		for (Integer j : new TreeSet<Integer>(forestDistance.get(i).keySet())) {
        		rval = String.format("%s%s(%s)  ",rval,forestDistance.get(i).get(j),j);
    			}
    		rval = rval + String.format("%n");
   		}
    	return rval;
   	}

    private String seeFDfile(Hashtable<Integer, Hashtable<Integer,Integer>>
    						forestDistance) {

    	
    	String rval = String.format("Forest Distance%n");  
		//Return result  
		for (Integer i : new TreeSet<Integer>(forestDistance.keySet())) {
			rval = String.format("%s%s, ",rval,i);
			for (Integer j : new TreeSet<Integer>(forestDistance.get(i).keySet())) {
				rval = String.format("%s%s(%s), ",rval,forestDistance.get(i).get(j),j);
			}
			rval = rval + String.format("%n");
	        try 
	        {
	          BufferedWriter out = new BufferedWriter(new FileWriter("fd.txt", true));
	          out.write(rval);
	          out.close();
	        }
	        catch (IOException e) 
	        {
	        }
			rval = "";
		}

		return "";
}

}