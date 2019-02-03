import java.io.*;
import java.util.*;

public class Solution {

    public static void main(String [] args) throws IOException{
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			TwoThreeTree tree = new TwoThreeTree();
			try {
              //read in the database size
				int size = Integer.parseInt(in.readLine());
              //read in each line and insert into tree
			    for (int i = 0; i < size; i++) {
			    	String[] arr = in.readLine().split(" ");
			    	int num = Integer.parseInt(arr[0]);
                    //insert into tree
			    	if(num == 1) {
			    		insert(arr[1],Integer.parseInt(arr[2]), tree);
			    	}
			    	//use range
			    	if(num == 2) {
			    		String x = (arr[1].compareTo(arr[2]) <= 0) ? arr[1] : arr[2];
			    		String y = (arr[1].compareTo(x) == 0) ? arr[2] : arr[1];
			    		range(x,y, "",tree.root.guide,Integer.parseInt(arr[3]),tree.root,tree.height);
			    	}
			    	//search for the planet
		    		if(num == 3) {
		    		    search(arr[1], tree.root, tree.height,0);
		    		}
		    		
			    }
			} catch (IOException e) {
				System.err.println("IO Exception");
			}
			//close BufferReader
			in.close();
			
		} catch (IOException e) {
			System.err.println("IO Exception");
		}

		
	}

    public static void range(String start, String end, String min, String max, int num, Node node, int height){
        if(height == 0){
            if(node.guide.compareTo(start) >= 0 && node.guide.compareTo(end) <= 0)
                node.value += num;
        } else {
            //node is an internal node
            InternalNode n= (InternalNode)node;
            if(min.compareTo(start) >= 0 && max.compareTo(end) <= 0){
                //increase the value, node is within range
                node.value += num;
            } else if(min.compareTo(end) < 0 && max.compareTo(start) >= 0){
                range(start, end, min, n.child0.guide, num, n.child0, height-1);
                range(start, end, n.child0.guide, n.child1.guide, num, n.child1, height-1);
                //if there is a 3rd child, recursively range as well
                if(n.child2!=null){
                    range(start, end, n.child1.guide, n.child2.guide, num, n.child2, height-1);
                }
            }
        }
    }



    public static void search(String key, Node node, int height,int num){
        if(height == 0){
            if(key.compareTo(node.guide) == 0){ //found the node we are looking for
                System.out.println(node.value + num);
            }
            else{ //otherwise node does not exist in the tree
                System.out.println("-1");
            }
        } else { //is an internal node
            InternalNode n= (InternalNode) node;
            //search through first child
            if(key.compareTo(n.child0.guide)<=0){
                search(key, n.child0, height-1, num+n.value);
            } else if(key.compareTo(n.child1.guide)<=0 ){ //search through second child
                search(key, n.child1, height-1, num+n.value);
            } else if(n.child2!=null &&key.compareTo(n.child1.guide) > 0 && key.compareTo(n.child2.guide) <= 0) {
                //search through 3rd child
                search(key, n.child2, height-1, num+n.value);
            } else { //otherwise, does not exist in the tree
                System.out.println("-1");
            }
        }
    }
    
    public static void insert(String key, int value, TwoThreeTree tree) {
        // insert a key value pair into tree (overwrite existsing value
        // if key is already present)

        int h = tree.height;

        if (h == -1) {
            LeafNode newLeaf = new LeafNode();
            newLeaf.guide = key;
            newLeaf.value = value;
            tree.root = newLeaf; 
            tree.height = 0;
        }
        else {
            WorkSpace ws = doInsert(key, value, tree.root, h);

            if (ws != null && ws.newNode != null) {
                // create a new root

                InternalNode newRoot = new InternalNode();
                if (ws.offset == 0) {
                    newRoot.child0 = ws.newNode; 
                    newRoot.child1 = tree.root;
                }
                else {
                    newRoot.child0 = tree.root; 
                    newRoot.child1 = ws.newNode;
                }
                resetGuide(newRoot);
                tree.root = newRoot;
                tree.height = h+1;
            }
        }
    }

    public static WorkSpace doInsert(String key, int value, Node p, int h) {
        // auxiliary recursive routine for insert

        if (h == 0) {
            // we're at the leaf level, so compare and 
            // either update value or insert new leaf

            LeafNode leaf = (LeafNode) p; //downcast
            int cmp = key.compareTo(leaf.guide);

            if (cmp == 0) {
                leaf.value = value; 
                return null;
            }

            // create new leaf node and insert into tree
            LeafNode newLeaf = new LeafNode();
            newLeaf.guide = key; 
            newLeaf.value = value;

            int offset = (cmp < 0) ? 0 : 1;
            // offset == 0 => newLeaf inserted as left sibling
            // offset == 1 => newLeaf inserted as right sibling

            WorkSpace ws = new WorkSpace();
            ws.newNode = newLeaf;
            ws.offset = offset;
            ws.scratch = new Node[4];

            return ws;
        }
        else {
            InternalNode q = (InternalNode) p; // downcast
            int pos;
            WorkSpace ws;
            
            q.child0.value+=p.value;
            q.child1.value+=p.value;
            if(q.child2!=null){
                q.child2.value += p.value;
            }
            
            p.value=0;
            
            if (key.compareTo(q.child0.guide) <= 0) {
                pos = 0; 
                ws = doInsert(key, value, q.child0, h-1);
                q.child0.value += p.value;
            }
            else if (key.compareTo(q.child1.guide) <= 0 || q.child2 == null) {
                pos = 1;
                ws = doInsert(key, value, q.child1, h-1);
                q.child1.value += p.value;
            }
            else {
                pos = 2; 
                ws = doInsert(key, value, q.child2, h-1);
                q.child2.value += p.value;
            }

            if (ws != null) {
                if (ws.newNode != null) {
                    // make ws.newNode child # pos + ws.offset of q

                    int sz = copyOutChildren(q, ws.scratch);
                    insertNode(ws.scratch, ws.newNode, sz, pos + ws.offset);
                    if (sz == 2) {
                        ws.newNode = null;
                        ws.guideChanged = resetChildren(q, ws.scratch, 0, 3);
                    }
                    else {
                        ws.newNode = new InternalNode();
                        ws.offset = 1;
                        resetChildren(q, ws.scratch, 0, 2);
                        resetChildren((InternalNode) ws.newNode, ws.scratch, 2, 2);
                    }
                }
                else if (ws.guideChanged) {
                    ws.guideChanged = resetGuide(q);
                }
            }

            return ws;
        }
    }

    public static int copyOutChildren(InternalNode q, Node[] x) {
        // copy children of q into x, and return # of children

        int sz = 2;
        x[0] = q.child0; x[1] = q.child1;
        if (q.child2 != null) {
            x[2] = q.child2; 
            sz = 3;
        }
        return sz;
    }

    public static void insertNode(Node[] x, Node p, int sz, int pos) {
        // insert p in x[0..sz) at position pos,
        // moving existing extries to the right

        for (int i = sz; i > pos; i--)
            x[i] = x[i-1];

        x[pos] = p;
    }

    public static boolean resetGuide(InternalNode q) {
        // reset q.guide, and return true if it changes.

        String oldGuide = q.guide;
        if (q.child2 != null)
            q.guide = q.child2.guide;
        else
            q.guide = q.child1.guide;

        return q.guide != oldGuide;
    }


    public static boolean resetChildren(InternalNode q, Node[] x, int pos, int sz) {
        // reset q's children to x[pos..pos+sz), where sz is 2 or 3.
        // also resets guide, and returns the result of that

        q.child0 = x[pos]; 
        q.child1 = x[pos+1];

        if (sz == 3) 
            q.child2 = x[pos+2];
        else
            q.child2 = null;

        return resetGuide(q);
    }
}


class Node {
	int value=0;
   String guide;
   // guide points to max key in subtree rooted at node
}

class InternalNode extends Node {
   Node child0, child1, child2;
   // child0 and child1 are always non-null
   // child2 is null iff node has only 2 children
}

class LeafNode extends Node {
   // guide points to the key

 // int value;
}

class TwoThreeTree {
   Node root;
   int height;

   TwoThreeTree() {
      root = null;
      height = -1;
   }
}

class WorkSpace {
// this class is used to hold return values for the recursive doInsert
// routine

   Node newNode;
   int offset;
   boolean guideChanged;
   Node[] scratch;
}
