/*
 * Mazes.java
 * 
 * Last updated: 08/17/2023
 * 
 * @author: Owen Mathay
 * 
 */

import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.concurrent.TimeUnit;


/*
 * The interface IGameConstants provides values for use in defining 
 * the maze and display window. This structure also allows users to
 * safely manipulate these variables.
 */
interface IGameConstants {
	int WINDOW_W = 1000;
	int WINDOW_H = 750;
}

/*
 * The interface IGamePiece defines methods for any piece which is an
 * option for connections within the maze game.
 */
interface IGamePiece {

	/* Is this IGamePiece in the same set as given other? */
	boolean sameCode(IGamePiece other);

	/* Helper for determining the set compatibility */
	boolean sameCodeHelper(Node node);

	/* Effect: to update the set of this IGamePiece to match other */
	void updateSet(IGamePiece other);

	/* Effect: helper for updating the set codes */
	void updateSetHelper(Node n);

	/* Effect: updates the set codes of all nodes in the set */
	void updateNeighborCode(int to, int from);

	/* Should this IGamePiece be drawn with high priority? */
	boolean overridesDraw();

	/* Helper method for getting the unvisited neighbors of a given Node n. */
	ArrayList<Node> getNeighborsHelper(Node n, ArrayList<Edge> edge);

	/* Helper method for getting all neighbors of a given Node n. */
	ArrayList<Node> getNeighborsHelperPlayer(Node node, ArrayList<Edge> edges);
}

/*
 * The Barrier class, an instance of which serves as a placeholder
 * where a Node object would have no neighbor. There are no 
 * fields, so the constructor is implicit.
 */
class Barrier implements IGamePiece {

	/* Is this Barrier object in the same set as given other? */
	public boolean sameCode(IGamePiece other) {
		return false; // ALWAYS FALSE: Barrier has no setCode
	}

	/* Helper for determining the set compatibility */
	public boolean sameCodeHelper(Node node) {
		return false; // ALWAYS FALSE: Barrier has no setCode
	}

	/* Effect: to update the set of this Barrier to match given other */
	public void updateSet(IGamePiece other) {
		// no effect	
	}

	/* Effect: helper for updating the set codes */
	public void updateSetHelper(Node n) {
		// no effect
	}

	/* Effect: updates the set codes of all nodes in the set */
	public void updateNeighborCode(int to, int from) {
		// no effect
	}

	/* Should this Barrier be drawn with high priority? */
	public boolean overridesDraw() {
		return true; // ALWAYS TRUE: Necessary for drawing Maze border
	}

	/* Helper method for getting the unvisited neighbors of a given Node n. */
	public ArrayList<Node> getNeighborsHelper(Node n, ArrayList<Edge> edge) {
		return new ArrayList<Node>();
	}

	/* Helper method for getting all neighbors of a given Node n. */
	public ArrayList<Node> getNeighborsHelperPlayer(Node node, ArrayList<Edge> edges) {
		return new ArrayList<Node>();
	}
}

/*
 * The Node class, an instance of which represents a single cell
 * of the Maze game. An instance can either be customized, or the 
 * fields will be determined within the convenience constructor.
 */
class Node implements IGamePiece {

	int x;
	int y;
	int code;

	int g_cost;
	int h_cost;

	boolean visited;
	IGamePiece left;
	IGamePiece right;
	IGamePiece top;
	IGamePiece bottom;
	Node repNode;
	boolean playerIn;

	/*
	 * Full constructor for Node class.
	 * 
	 * @param x: x-coordinate of this node in the maze
	 * @param y: y-coordinate of this node in the maze
	 * (In logical coordinates, with origin at top left corner)
	 * 
	 * @param code: first set-identifier code of this node
	 * @param visited: has this node been seen?
	 * @param left: left neighbor of this node
	 * @param right: right neighbor of this node
	 * @param top: top neighbor of this node
	 * @param bottom: neighbor of this node
	 * 
	 */
	Node(int x, int y, int code, boolean visited, 
			IGamePiece left, IGamePiece right, IGamePiece top, IGamePiece bottom) {
		this.x = x;
		this.y = y;
		this.code = code;
		this.visited = visited;
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	/*
	 * Convenience constructor, for general use.
	 */
	Node(int x, int y, int code) {
		this(x, y, code, false, new Barrier(), new Barrier(), new Barrier(), new Barrier());
	}

	/*
	 * Constructor for the player's node.
	 */
	Node(int x, int y, Board b) {
		this.x = x;
		this.y = y;
		this.repNode = b.get(0, 0);
		this.playerIn = true;
	}

	/*
	 * Checks if this node is from the same set as given other.
	 * 
	 * @param other: other IGamePiece to compare codes with
	 * @return: calls sameCodeHelper on other
	 */
	public boolean sameCode(IGamePiece other) {
		return other.sameCodeHelper(this);
	}

	/*
	 * Helper for sameCode():
	 * Compares own code with given other's code.
	 * 
	 * @param other: node to compare codes with
	 * @return: whether the codes match or not
	 */
	public boolean sameCodeHelper(Node other) {
		return this.code == other.code;
	}

	/*
	 * Updates code of given other to match this node.
	 * 
	 * @param other: other IGamePiece to update code for
	 */
	public void updateSet(IGamePiece other) {
		other.updateSetHelper(this);
	}

	/*
	 * Helper for updateSet():
	 * Updates the code of given node to match this.
	 * 
	 * @param node: node to be updated
	 */
	public void updateSetHelper(Node node) {
		node.updateNeighborCode(node.code, this.code);
	}

	/*
	 * Updates the set code of all nodes in the same set.
	 * 
	 * @param from: code to compare node sets for valid code updates
	 * @param to: code to update all nodes of this set to
	 */
	public void updateNeighborCode(int from, int to) {
		if (this.code != from) {
			return;
		}
		this.code = to;
		this.left.updateNeighborCode(from, to);
		this.right.updateNeighborCode(from, to);
		this.top.updateNeighborCode(from, to);
		this.bottom.updateNeighborCode(from, to);
	}

	/*
	 * Checks if this node should be drawn with priority.
	 * 
	 * @return: Always false.
	 */
	public boolean overridesDraw() {
		return false;
	}

	/*
	 * Collects valid neighbors of this node.
	 * 
	 * @param edges: all valid walls within the maze board
	 * @return: any valid neighbors of this node
	 */
	public ArrayList<Node> getAllNeighbors(ArrayList<Edge> edges) {

		ArrayList<Node> neighbors = new ArrayList<Node>();
		neighbors.addAll(this.left.getNeighborsHelper(this, edges));
		neighbors.addAll(this.right.getNeighborsHelper(this, edges));
		neighbors.addAll(this.top.getNeighborsHelper(this, edges));
		neighbors.addAll(this.bottom.getNeighborsHelper(this, edges));

		return neighbors;
	}

	/*
	 * Helper for getAllNeighbors():
	 * Confirms that the neighbor nodes are not separated by a valid wall.
	 * 
	 * @param n: node to confirm connections with this node
	 * @param edges: all valid walls within the maze board
	 * @return: any valid neighbors of this node
	 */
	public ArrayList<Node> getNeighborsHelper(Node n, ArrayList<Edge> edges) {
		ArrayList<Node> neighbors = new ArrayList<Node>();
		for (Edge e: edges) {
			if (e.connectsNodes(this, n)) {
				return neighbors;
			}
		}
		if (!this.visited) {
			neighbors.add(this);
		}
		return neighbors;
	}

	/*
	 * FOR USE IN USER CONTROL ONLY.
	 * 
	 * @param edges: all valid walls within the maze board
	 * @return: any valid neighbors of this node
	 */
	public ArrayList<Node> getAllNeighborsPlayer(ArrayList<Edge> edges) {

		ArrayList<Node> neighbors = new ArrayList<Node>();
		neighbors.addAll(this.left.getNeighborsHelperPlayer(this, edges));
		neighbors.addAll(this.right.getNeighborsHelperPlayer(this, edges));
		neighbors.addAll(this.top.getNeighborsHelperPlayer(this, edges));
		neighbors.addAll(this.bottom.getNeighborsHelperPlayer(this, edges));

		return neighbors;
	}

	/*
	 * FOR USE IN USER CONTROL ONLY - Disregards visited.
	 * 
	 * @param n: node to confirm connections with this node
	 * @param edges: all valid walls within the maze board
	 * @return: any valid neighbors of this node
	 */
	public ArrayList<Node> getNeighborsHelperPlayer(Node n, ArrayList<Edge> edges) {
		ArrayList<Node> neighbors = new ArrayList<Node>();
		for (Edge e: edges) {
			if (e.connectsNodes(this, n)) {
				return neighbors;
			}
		}
		neighbors.add(this);
		return neighbors;
	}

	/*
	 * Checks if this node should have a right wall.
	 * 
	 * @param edges: all valid walls of the maze 
	 * @return: whether  wall should be drawn or not
	 */
	public boolean doesDrawRight(ArrayList<Edge> edges) {
		for (Edge e: edges) {
			if (e.connectsNodes(this, this.right)) {
				return true;
			}
			if (this.right instanceof Barrier) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Checks if this node should have a wall below.
	 * 
	 * @param edges: all valid walls of the maze 
	 * @return: whether wall should be drawn or not
	 */	
	public boolean doesDrawBottom(ArrayList<Edge> edges) {
		for (Edge e: edges) {
			if (e.connectsNodes(this, this.bottom)) {
				return true;
			}
			if (this.bottom instanceof Barrier) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Checks if this node should have a left wall.
	 * 
	 * @param edges: all valid walls of the maze 
	 * @return: calls overridesDraw method of left neighbor
	 */
	public boolean doesDrawLeft(ArrayList<Edge> edges) {
		return this.left.overridesDraw();
	}

	/*
	 * Checks if this node should have a wall above.
	 * 
	 * @param edges: all valid walls of the maze 
	 * @return: calls overridesDraw method of bottom neighbor
	 */
	public boolean doesDrawTop(ArrayList<Edge> edges) {
		return this.top.overridesDraw();
	}

	/*
	 * Connects this node with a right neighbor.
	 * 
	 * @param node: node to connect with on right
	 */
	public void updateRight(Node node) {
		this.right = node;
		// Also connects this node to given node as left neighbor
		node.left = this; 
	}

	/*
	 * Connects this node with a lower neighbor.
	 * 
	 * @param node: node to connect with on bottom
	 */
	public void updateBottom(Node node) {
		this.bottom = node;
		// Also connects this node to given node as upper neighbor
		node.top = this;
	}

	/* Calculate the "F Cost" for A Star algorithm */
	public int fCost() {
		return this.g_cost + this.h_cost;
	}

	/*
	 * Verifies the validity of a user move.
	 * 
	 * @param target: Node to indicate desired move direction
	 * @param b: board for the edges
	 * @return: whether the move can be made or not
	 */
	public boolean isValidMove(IGamePiece target, Board b) {
		ArrayList<Node> validNeighbors = this.repNode.getAllNeighborsPlayer(b.edges);
		return validNeighbors.contains(target);
	}

	/*
	 * Moves the player icon to the target neighbor node.
	 * 
	 * @param xChange: amount to adjust node.x
	 * @param yChange: amount to adjust node.y
	 * @param b: maze to access other nodes
	 */
	public void adjustPos(int xChange, int yChange, Board b) {
		this.x += xChange;
		this.y += yChange;
		this.repNode = b.get(this.x, this.y);
		b.explored.add(b.get(this.x, this.y));
		b.get(this.x, this.y).visited = true;
	}


	/* Restores this node's unvisited status. */
	void reset() {
		this.visited = false;
	}
}

/*
 * The Edge class, an instance of which represents the connection
 * between two Node objects, or as a wall separating them. Edges 
 * as walls are removed from the Maze's board object according to 
 * Kruskal's method, but remain as connections between Nodes.
 */
class Edge {
	IGamePiece from;
	IGamePiece to;
	int weight;

	/*
	 * Constructor for Edge objects.
	 * 
	 * @param from: node to build edge from
	 * @param to: node to build edge towards
	 * @param weight: randomly generated for generation
	 */
	Edge(Node from, Node to, int weight) {
		this.from = from;
		this.to = to;
		this.weight = weight;
	}

	/*
	 * Checks if this Edge connects the given IGamePiece objects.
	 * 
	 * @param to: first node for comparison
	 * @param from: second node for comparison
	 * @return: whether this edge connects the given nodes
	 */
	public boolean connectsNodes(IGamePiece to, IGamePiece from) {
		return (this.from.equals(to) && this.to.equals(from)) 
				|| (this.from.equals(from) && this.to.equals(to));
	}

	/*
	 * Checks if nodes connected by this Edge are in the same set.
	 * 
	 * @return: whether they share the same set
	 */
	public boolean sameCodeBothSides() {
		return this.from.sameCode(this.to);
	}

	/* Joins the sets of the nodes connected by this edge into one. */
	public void mergeSets() {
		this.from.updateSet(this.to);
	}
}


/*
 * The Board class, an instance of which represents the maze in a two-
 * dimensional capacity. Board is called by MazeWorld relating to the 
 * construction and storage of IGamePiece and Edge objects.
 */
class Board implements IGameConstants {
	int width;
	int height;
	ArrayList<ArrayList<Node>> board;
	ArrayList<Edge> edges;
	Random rand;
	boolean forTests;
	Node target;
	ArrayList<Node> explored;

	/*
	 * Constructor for the Board class.
	 * 
	 * @param w: width, in number of nodes
	 * @param h: height, in number of nodes
	 * @param board: 2D ArrayList of all nodes in the maze
	 * @param edges: all edges in the maze
	 * @param rand: Random object for edge weight generation
	 */
	Board(int w, int h, ArrayList<ArrayList<Node>> board, ArrayList<Edge> edges, Random rand) {
		this.width = w;
		this.height = h;
		this.board = board;
		this.edges = edges;
		this.rand = rand;
	}

	/*
	 * Convenience constructor, for playing the maze.
	 */
	Board(int width, int height) {
		this(width, height, new ArrayList<ArrayList<Node>>(), new ArrayList<Edge>(), new Random());
		for (int j = 0; j < width; j ++) {
			this.board.add(new ArrayList<Node>());
			for (int k = 0; k < height; k ++) {
				this.board.get(j).add(new Node(j, k, k * this.width + j));
			}
		}
		this.target = this.get(width - 1, height - 1);
		this.fixBoard();
	}

	/*
	 * Convenience constructor, for testing.
	 * 
	 * @param testMaze: signals need to seed random
	 */
	Board(int width, int height, boolean testMaze) {
		this(width, height, new ArrayList<ArrayList<Node>>(), new ArrayList<Edge>(), new Random(25));
		for (int j = 0; j < width; j ++) {
			this.board.add(new ArrayList<Node>());
			for (int k = 0; k < height; k ++) {
				this.board.get(j).add(new Node(j, k, k * this.width + j));
			}
		}
		this.target = this.get(width - 1, height - 1);
		this.fixBoard();
	}

	/* Creates connections between nodes in the form of Edge objects. */
	void fixBoard() {
		for (int j = 0; j < this.board.size(); j ++) {
			for (int k = 0; k < this.board.get(j).size(); k ++) {
				Node n = this.get(j, k);
				n.h_cost = getDistance(n, this.target);
				n.g_cost = getDistance(n, this.get(0, 0));
				if (j < this.board.size() - 1) {
					n.updateRight(this.get(j + 1, k));
					this.edges.add(new Edge(n, this.get(j + 1, k), 
							rand.nextInt((int) Math.pow(10, 6))));
				}
				if (k < this.board.get(j).size() - 1) {
					n.updateBottom(this.get(j, k + 1));
					this.edges.add(new Edge(n, this.get(j, k + 1), 
							rand.nextInt((int) Math.pow(10, 6))));
				}
			}
		}
		/* Comparator sorts all edges in the grid by their random weight. */
		this.edges.sort((edge1, edge2) -> edge1.weight - edge2.weight);
	}

	/*
	 * Assigns the distance value from one given node to another.
	 * 
	 * @param current: first node for calculation
	 * @param target: second node for calculation
	 */
	public int getDistance(Node current, Node target) {
		int xDist = Math.abs(target.x - current.x);
		int yDist = Math.abs(target.y - current.y);

		if (xDist > yDist) {
			return (14 * yDist) + (10 * (xDist - yDist));
		} else {
			return (14 * xDist) + (10 * (yDist - xDist));

		}
	}

	/*
	 * Identifies the node at given logical x-y coordinates.
	 * 
	 * @param x: x-coordinate of desired node
	 * @param y: y-coordinate of desired node
	 * @return: node object at selected coordinates
	 */
	public Node get(int x, int y) {
		return this.board.get(x).get(y);
	}

	/*
	 * Determines size for Node for drawing based on window size.
	 * 
	 * @return: integer value of node size limit
	 */
	public int cellSize() {
		int minScreenDim = Math.min(WINDOW_W, WINDOW_H);
		int size = minScreenDim / Math.max(this.width, this.height);
		return size;
	}

	/*
	 * Determines size of Edge for drawing based on node size
	 * 
	 * @return: integer value of edge size limit
	 */
	public int edgeThickness() {
		return Math.max(3, this.cellSize() / 12);
	}

	/* Retains maze design while restoring all nodes to unvisited status */
	void reset() {
		for (int j = 0; j < this.width; j ++) {
			for (int k = 0; k < this.height; k++) {
				this.get(j, k).reset();
			}
		}
		this.explored = new ArrayList<Node>();
	}
}


/*
 * The SearchType enumeration, which allows the MazeWorld class and bigBang 
 * to distinguish between different search algorithms.
 */
enum SearchType {
	DEPTH_FIRST,
	BREADTH_FIRST,
	A_STAR,
	INACTIVE,
	USER
}


/*
 * The MazeWorld class, the main class for the Maze generation game, 
 * extends the World class imported in "Impworld." A MazeWorld instance
 * is responsible for the visual component of the program, as well as the 
 * search and solution of the generated maze. If not designed for tests,
 * the program will randomly generate mazes.
 */
class MazeWorld extends World implements IGameConstants {
	int width;
	int height;
	int nodeSize;
	int edgeThickness;
	int updatesPerTick;
	int steps;
	long startTime;

	/* Variables which handle differentiating algorithms */
	SearchType search;
	boolean isInitializing;
	boolean isSearching;
	boolean isDrawingPath;

	Board board;
	ArrayList<Edge> worklist;
	ArrayList<Node> stack;
	ArrayList<Node> open;
	ArrayList<Node> closed;
	HashMap<Node, Node> parentNodeOf;
	boolean userInControl = false;
	Node playerNode;

	/*
	 * Constructor for MazeWorld objects.
	 * 
	 * @param width: how many columns of nodes in the maze?
	 * @param height: how many rows of nodes in the maze?
	 */
	MazeWorld(int width, int height) {
		this.board = new Board(width, height);
		this.width = width;
		this.height = height;
		this.isInitializing = true;
		this.isSearching = false;
		this.isDrawingPath = false;
		this.search = SearchType.INACTIVE;
		this.worklist = new ArrayList<Edge>(this.board.edges);
		this.stack = new ArrayList<Node>();
		this.open = new ArrayList<Node>();
		this.closed = new ArrayList<Node>();
		this.parentNodeOf = new HashMap<Node, Node>();
		this.nodeSize = this.board.cellSize();
		this.edgeThickness = this.board.edgeThickness();
		this.updatesPerTick = Math.max(1, width * height / 120);
		this.playerNode = new Node(0, 0, this.board);
	}

	/*
	 * Convenience constructor for testing.
	 * 
	 * @param testMaze: extra parameter to pass to Board
	 */
	MazeWorld(int width, int height, boolean testMaze) {
		this.board = new Board(width, height, testMaze);
		this.width = width;
		this.height = height;
		this.isInitializing = true;
		this.isSearching = false;
		this.isDrawingPath = false;
		this.search = SearchType.INACTIVE;
		this.worklist = new ArrayList<Edge>(this.board.edges);
		this.stack = new ArrayList<Node>();
		this.open = new ArrayList<Node>();
		this.closed = new ArrayList<Node>();
		this.parentNodeOf = new HashMap<Node, Node>();
		this.nodeSize = this.board.cellSize();
		this.edgeThickness = this.board.edgeThickness();
		this.updatesPerTick = Math.max(1, width * height / 120);
		this.playerNode = new Node(0, 0, this.board);
	}

	/*
	 * Displays and updates the window containing the maze.
	 * 
	 * @return: WorldScene to be shown on each tick
	 */
	@Override
	public WorldScene makeScene() {
		WorldScene screen = new WorldScene(WINDOW_W, WINDOW_H);
		for (int j = 0; j < this.width; j ++) {
			for (int k = 0; k < this.height; k ++) {
				Node v = this.board.get(j, k);

				// Handles drawing visited nodes, distinction for the correct path.
				if (this.stack.contains(v)) {
					screen.placeImageXY(
							new RectangleImage(this.nodeSize, this.nodeSize, OutlineMode.SOLID, new Color(51, 255, 255)),
							j * this.nodeSize + WINDOW_W / 2 - (this.width * this.nodeSize / 2) + this.nodeSize / 2, 
							k * this.nodeSize + WINDOW_H / 2 - (this.height * this.nodeSize / 2) + this.nodeSize / 2);
				} else if (v.visited || this.closed.contains(v)) {
					screen.placeImageXY(
							new RectangleImage(this.nodeSize, this.nodeSize, 
									OutlineMode.SOLID, new Color(255, 185, 104)),
							j * this.nodeSize + WINDOW_W / 2 - (this.width * this.nodeSize / 2) + this.nodeSize / 2, 
							k * this.nodeSize + WINDOW_H / 2 - (this.height * this.nodeSize / 2) + this.nodeSize / 2);
				}

				// Handles drawing the origin and destination nodes.
				if (j == 0 && k == 0) {
					screen.placeImageXY(
							new RectangleImage(this.nodeSize, this.nodeSize, OutlineMode.SOLID, Color.GREEN),
							j * this.nodeSize + WINDOW_W / 2 - (this.width * this.nodeSize / 2) + this.nodeSize / 2, 
							k * this.nodeSize + WINDOW_H / 2 - (this.height * this.nodeSize / 2) + this.nodeSize / 2);
				}
				else if (j == this.width - 1 && k == this.height - 1) {
					screen.placeImageXY(
							new RectangleImage(this.nodeSize, this.nodeSize, OutlineMode.SOLID, Color.RED),
							j * this.nodeSize + WINDOW_W / 2 - (this.width * this.nodeSize / 2) + this.nodeSize / 2, 
							k * this.nodeSize + WINDOW_H / 2 - (this.height * this.nodeSize / 2) + this.nodeSize / 2);
				}

				// Handles drawing each Edge of this node.
				if (v.doesDrawRight(this.board.edges) || j == this.width - 1) {
					screen.placeImageXY(
							new RectangleImage(this.edgeThickness, this.nodeSize, OutlineMode.SOLID, Color.BLACK),
							j * this.nodeSize + WINDOW_W / 2 - (this.width * this.nodeSize / 2) + this.nodeSize, 
							k * this.nodeSize + WINDOW_H / 2 - (this.height * this.nodeSize / 2) + this.nodeSize / 2);
				}
				if (v.doesDrawBottom(this.board.edges) || k == this.height - 1 ) {
					screen.placeImageXY(
							new RectangleImage(this.nodeSize, this.edgeThickness, OutlineMode.SOLID, Color.BLACK),
							j * this.nodeSize + WINDOW_W / 2 - (this.width * this.nodeSize / 2) + this.nodeSize / 2, 
							k * this.nodeSize + WINDOW_H / 2 - (this.height * this.nodeSize / 2) + this.nodeSize);
				}
				if (v.doesDrawLeft(this.board.edges)) {
					screen.placeImageXY(
							new RectangleImage(this.edgeThickness, this.nodeSize, OutlineMode.SOLID, Color.BLACK),
							j * this.nodeSize + WINDOW_W / 2 - (this.width * this.nodeSize / 2), 
							k * this.nodeSize + WINDOW_H / 2 - (this.height * this.nodeSize / 2) + this.nodeSize / 2);	
				}
				if (v.doesDrawTop(this.board.edges)) {
					screen.placeImageXY(
							new RectangleImage(this.nodeSize, this.edgeThickness, OutlineMode.SOLID, Color.BLACK),
							j * this.nodeSize + WINDOW_W / 2 - (this.width * this.nodeSize / 2) + this.nodeSize / 2, 
							k * this.nodeSize + WINDOW_H / 2 - (this.height * this.nodeSize / 2));
				}
				if (this.userInControl) {
					screen.placeImageXY(
							new CircleImage(this.nodeSize / 3, OutlineMode.SOLID, new Color(119, 0, 200)),
							this.playerNode.x * this.nodeSize + WINDOW_W / 2 - (this.width * this.nodeSize / 2) + this.nodeSize / 2, 
							this.playerNode.y * this.nodeSize + WINDOW_H / 2 - (this.height * this.nodeSize / 2) + this.nodeSize / 2);
				}
			}
		}
		return screen;
	}

	/* Handles maze updates on each tick and distinguishes between run–tasks. */
	@Override
	public void onTick() {

		// Organizing and combining the node sets using Kruskal's method.
		if(this.isInitializing) {
			for (int q = 0; q < this.updatesPerTick; q ++) {
				for (int r = 0; r < 1; r ++) {
					if(this.worklist.size() == 0) {
						this.isInitializing = false;
						return;
					}
					Edge e = this.worklist.get(0);
					while(e.sameCodeBothSides()) {
						if(this.worklist.size() == 0) {
							this.isInitializing = false;
							return;
						}
						e = this.worklist.remove(0);
					}
					this.board.edges.remove(e);
					e.mergeSets();
				}
			}
		} else if(this.isSearching) {

			// Traversing the maze from origin ––> destination with the selected algorithm.
			for (int s = 0; s < this.updatesPerTick; s ++) {

				// Distinguishing between different search algorithms.
				switch(this.search) {
				case DEPTH_FIRST: 

					// DFS: Depth-First Search (LI-FO)
					if (this.stack.size() == 0) {
						this.stack.add(this.board.get(0, 0));
					}

					if (this.stack.get(this.stack.size() - 1).equals(
							this.board.get(this.width - 1, this.height - 1))) {
						this.isSearching = false;
						long endTime = System.nanoTime();
						this.singleRunSummary(endTime - this.startTime);
						this.search = SearchType.INACTIVE;
						return;
					}

					ArrayList<Node> neighbors = this.stack.get(
							this.stack.size() - 1).getAllNeighbors(this.board.edges);
					if (neighbors.size() == 0) {
						stack.remove(this.stack.size() - 1);
					} else {
						Node temp = neighbors.get(0);
						temp.visited = true;
						this.closed.add(temp);
						stack.add(temp);
					}
					break;

				case BREADTH_FIRST: 
					// BFS: Breadth-First Search (FI-FO)
					// -- Kept "stack" variable for makeScene().

					if (this.isDrawingPath) {
						// End-case: backtracking completed, path will be shown.
						if (this.stack.get(this.stack.size() - 1) == this.board.get(0, 0)) {
							this.isSearching = false;
							this.isDrawingPath = false;
							this.search = SearchType.INACTIVE;
							return;
						}
						// Backtracking-in-progress using HashMap.
						Node old = this.stack.get(this.stack.size() -1);
						Node next = this.parentNodeOf.get(old);
						this.stack.add(next);
						return;

					} else {
						// Searching: seeking the destination.
						Node n = this.stack.remove(0);
						if (n.equals(this.board.get(this.width - 1, this.height - 1))) {
							long endTime = System.nanoTime();
							this.singleRunSummary(endTime - this.startTime);
							this.isDrawingPath = true;
							this.stack = new ArrayList<Node>();
							this.stack.add(n);
							return;
						}

						// Adds neighbors to the Queue.
						ArrayList<Node> neighbors_bfs = n.getAllNeighbors(this.board.edges);
						for (Node neighbor : neighbors_bfs) {
							this.parentNodeOf.put(neighbor, n);
							this.stack.add(neighbor);
							neighbor.visited = true;
							this.closed.add(neighbor);
						}
					}
					break;

				case A_STAR: 

					// A*: A Star Algorithm
					// Implements a heuristic
					if (this.isDrawingPath) {

						// End-case: drawing path complete.
						if (this.stack.get(this.stack.size() - 1) == this.board.get(0, 0)) {
							this.isSearching = false;
							this.isDrawingPath = false;
							this.search = SearchType.INACTIVE;
							return;
						}

						// Search completed! Begin drawing.
						Node old = this.stack.get(this.stack.size() - 1);
						Node parent = this.parentNodeOf.get(old);
						this.stack.add(parent);
						return;

					} else {

						// Searching: determine next node by lowest "F Cost".
						Node current = this.open.get(0);
						for (int i = 1; i < open.size(); i ++) {
							if (open.get(i).fCost() < current.fCost() 
									|| (open.get(i).fCost() == current.fCost() && open.get(i).h_cost < current.h_cost)) {
								current = open.get(i);
							}
						}
						this.open.remove(current);
						this.closed.add(current);

						// Check for target
						if (current.equals(this.board.target)) {
							this.isDrawingPath = true;
							this.stack.add(current);
							long endTime = System.nanoTime();
							this.singleRunSummary(endTime - this.startTime);
							return;
						}

						// Add valid neighbors to open list 
						for (Node node : current.getAllNeighbors(this.board.edges)) {
							if (closed.contains(node)) {
								continue;
							}
							if (!open.contains(node)) {
								this.parentNodeOf.put(node, current);
								this.open.add(node);
							}
						}
					}
				case USER:
					if (this.playerNode.repNode.equals(this.board.target)) {
						this.userInControl = false;
						this.isSearching = false;
						long endTime = System.nanoTime();
						System.out.println("Congratulations, player!");
						this.closed = new ArrayList<Node>(this.board.explored);
						this.singleRunSummary(endTime - this.startTime);
						this.search = SearchType.INACTIVE;
						return;
					}
					break;

				case INACTIVE: break;
				}
			}
		}
	}

	/*
	 * Handles key inputs from user to modify run.
	 * 
	 * @param ke: the key that was pressed
	 */
	public void onKeyEvent(String ke) {
		// New Maze
		if (ke.equals("n")) {
			this.board = new Board(this.width, this.height);
			System.out.println("Initializing new maze...");
			System.out.println("Press C for controls");
			this.isInitializing = true;
			this.isSearching = false;
			this.search = SearchType.INACTIVE;
			this.closed = new ArrayList<Node>();
			this.open = new ArrayList<Node>();
			this.userInControl = false;
			this.worklist = new ArrayList<Edge>(this.board.edges);
		}

		// All other keyEvents defer to task completion
		if ((this.isInitializing || this.isSearching) && !this.userInControl) {
			return;
		}

		// DFS Setup
		if (ke.equals("d")) {
			this.isSearching = true;
			this.search = SearchType.DEPTH_FIRST;
			this.parentNodeOf = new HashMap<Node, Node>();
			this.board.reset();
			this.stack = new ArrayList<Node>();
			this.closed = new ArrayList<Node>();
			this.open = new ArrayList<Node>();
			this.stack.add(this.board.get(0, 0));
			this.board.get(0, 0).visited = true;
			this.startTime = System.nanoTime();
		}

		// BFS Setup
		else if (ke.equals("b")) {
			this.isSearching = true;
			this.search = SearchType.BREADTH_FIRST;
			this.parentNodeOf = new HashMap<Node, Node>();
			this.board.reset();
			this.closed = new ArrayList<Node>();
			this.open = new ArrayList<Node>();
			this.stack = new ArrayList<Node>();
			this.stack.add(this.board.get(0, 0));
			this.board.get(0, 0).visited = true;
			this.startTime = System.nanoTime();
		}

		// A* Setup
		else if (ke.equals("a")) {
			this.isSearching = true;
			this.search = SearchType.A_STAR;
			this.parentNodeOf = new HashMap<Node, Node>();
			this.board.reset();
			this.stack = new ArrayList<Node>();
			this.closed = new ArrayList<Node>();
			this.open = new ArrayList<Node>();
			this.open.add(this.board.get(0, 0));
			this.board.get(0, 0).visited = true;
			this.startTime = System.nanoTime();
		}

		// Press C for Controls
		else if (ke.equals("c")) {
			System.out.println("\n------CONTROLS------");
			System.out.println("[n]: Generate new maze");
			System.out.println("[d]: Select DFS");
			System.out.println("[b]: Select BFS");
			System.out.println("[a]: Select A* (A Star)\n");
			System.out.println("---PLAYER CONTROL---");
			System.out.println("[u]: Toggle user control on/off");
			System.out.println("[<][^][>][v]: Move player icon");
			System.out.println("--------------------");
		}

		// User Setup
		else if (ke.equals("u")) {
			if (this.userInControl) {
				System.out.println("User forfeit. Thanks for playing!");
				long endTime = System.nanoTime();
				this.singleRunSummary(endTime - this.startTime);
				this.search = SearchType.INACTIVE;
				this.isSearching = false;
				this.userInControl = false;
			} else {
				System.out.println("User in Control. Try your best!");
				this.playerNode = new Node(0, 0, this.board);
				this.isSearching = true;
				this.userInControl = true;
				this.board.reset();
				this.stack = new ArrayList<Node>();
				this.closed = new ArrayList<Node>();
				this.open = new ArrayList<Node>();
				this.search = SearchType.USER;
				this.startTime = System.nanoTime();
			}
		}
		// User Controls
		else if ((ke.equals("up") || ke.equals("down") 
				|| ke.equals("left") || ke.equals("right")) 
				&& this.userInControl) {
			Node rep = this.playerNode.repNode;
			switch(ke) {
			case "right": 
				if (this.playerNode.isValidMove(rep.right, this.board)) {
					this.playerNode.adjustPos(1, 0, this.board);
				}
				break;
			case "down": 
				if (this.playerNode.isValidMove(rep.bottom, this.board)) {
					this.playerNode.adjustPos(0, 1, this.board);
				}
				break;
			case "left":
				if (this.playerNode.isValidMove(rep.left, this.board)) {
					this.playerNode.adjustPos(-1, 0, this.board);
				}
				break;
			case "up": 
				if (this.playerNode.isValidMove(rep.top, this.board)) {
					this.playerNode.adjustPos(0, -1, this.board);
				}
			}
		}
	}

	/*
	 * Print out a summary of results from chosen search.
	 * 
	 * @param elapsed: time in nanoseconds from start to end of process
	 */
	public void singleRunSummary(long elapsed) {
		System.out.println("------------");
		System.out.println("MAZE SOLVED.");
		System.out.println("------------");
		System.out.println("Pathfinder:  	" + this.search);
		System.out.println("Nodes explored: " + this.closed.size());
		System.out.println("Time elapsed: 	" + 
				(double) TimeUnit.SECONDS.convert(100 * elapsed, TimeUnit.NANOSECONDS) / 100);
		System.out.println("------------");
	}
}

/*
 * The ExamplesMazes class contains examples and tests for all
 * methods mentioned in the MazeWorld class, as well as those
 * for the Edge, Barrier, Node, and Board classes.
 * 
 * All methods but initData have a parameter of Tester t 
 * to call  test methods from NEU's imported tester.jar.
 */
class ExamplesMazes implements IGameConstants {

	MazeWorld maze;
	MazeWorld testMaze;
	MazeWorld testMaze1x2;

	Barrier bar;
	Node n1;
	Node n1_1;
	Node n2;
	Node n3;
	Node n4;

	Node nUpd1;
	Node nUpd2;

	Edge e1;
	Edge e2;
	Edge e3;
	Edge e4;

	ArrayList<Edge> e_list;

	Board board;

	/* Initializes example data so each test can occur on uniform data. */
	void initData() {
		this.maze = new MazeWorld(50, 50);
		this.testMaze = new MazeWorld(5, 5, true);
		this.testMaze1x2 = new MazeWorld(1, 2, true);

		this.bar = new Barrier();

		this.n1 = new Node(0, 0, 0, false, bar, bar, bar, bar);
		this.n1_1 = new Node(1, 0, 0, false, bar, bar, bar, bar);
		this.n2 = new Node(1, 0, 1, false, bar, bar, bar, bar);
		this.n3 = new Node(0, 1, 2, false, bar, bar, bar, bar);
		this.n4 = new Node(1, 1, 3, false, bar, bar, bar, bar);

		this.nUpd1 = new Node(2, 1, 5, false, bar, bar, bar, bar);
		this.nUpd2 = new Node(2, 2, 8, false, bar, bar, this.nUpd1, bar);
		this.nUpd1.bottom = this.nUpd2;

		this.e1 = new Edge(n1, n2, 10);
		this.e2 = new Edge(n1, n3, 20);
		this.e3 = new Edge(n2, n4, 40);
		this.e4 = new Edge(n3, n4, 30);

		this.e_list = new ArrayList<Edge>();
		this.e_list.add(e1);
		this.e_list.add(e2);
		this.e_list.add(e3);
		this.e_list.add(e4);

		this.board = new Board(2, 2, true);

	}


	/* Tests on IGamePiece objects and methods occupy lines 1216 through 1716 */


	/* Tests the SameCode method for IGamePiece objects */
	void testSameCode(Tester t) {

		this.initData();
		t.checkExpect(this.n1.sameCode(bar), false);
		t.checkExpect(this.n2.sameCode(bar), false);
		t.checkExpect(this.n3.sameCode(bar), false);
		t.checkExpect(this.n4.sameCode(bar), false);

		t.checkExpect(this.bar.sameCode(this.n1), false);
		t.checkExpect(this.bar.sameCode(this.bar), false);

		t.checkExpect(this.n1.sameCode(this.n1_1), true);
		t.checkExpect(this.n2.sameCode(this.n1_1), false);
		t.checkExpect(this.n3.sameCode(this.n1_1), false);
		t.checkExpect(this.n4.sameCode(this.n1_1), false);

		t.checkExpect(this.n1_1.sameCode(this.n1), true);
		t.checkExpect(this.n1_1.sameCode(this.n2), false);
		t.checkExpect(this.n1_1.sameCode(this.n3), false);
		t.checkExpect(this.n1_1.sameCode(this.n4), false);

	}

	/* Tests the SameCodeHelper method */
	void testSameCodeHelper(Tester t) {
		this.initData();
		t.checkExpect(this.bar.sameCodeHelper(this.n1), false);
		t.checkExpect(this.bar.sameCodeHelper(this.n2), false);
		t.checkExpect(this.bar.sameCodeHelper(this.n3), false);
		t.checkExpect(this.bar.sameCodeHelper(this.n4), false);

		t.checkExpect(this.n1.sameCodeHelper(this.n1_1), true);
		t.checkExpect(this.n1_1.sameCodeHelper(this.n1), true);
		t.checkExpect(this.n1_1.sameCodeHelper(this.n2), false);
		t.checkExpect(this.n1_1.sameCodeHelper(this.n3), false);
		t.checkExpect(this.n1_1.sameCodeHelper(this.n4), false);

	}

	/* Tests the updateSet method */
	void testUpdateSet(Tester t) {
		this.initData();

		t.checkExpect(this.n1.code, 0);
		t.checkExpect(this.n2.code, 1);
		this.n1.updateSet(this.n2);
		t.checkExpect(this.n1.code, this.n2.code);

		t.checkExpect(this.n3.code, 2);
		t.checkExpect(this.n4.code, 3);
		this.n3.updateSet(this.n4);

		this.initData();
		t.checkExpect(this.n1.code, 0);
		t.checkExpect(this.n3.code, 2);
		this.n3.updateSet(this.n1);
		t.checkExpect(this.n1.code, this.n3.code);

		t.checkExpect(this.n2.code, 1);
		t.checkExpect(this.n4.code, 3);
		t.checkExpect(this.n2.sameCode(this.n4), false);
		this.n2.updateSet(this.n4);
		t.checkExpect(this.n2.code, this.n4.code);
	}

	/* Tests the updateSetHelper method */
	void testUpdateSetHelper(Tester t) {
		this.initData();
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 8);
		this.nUpd1.updateSetHelper(this.nUpd2);
		t.checkExpect(this.nUpd1.sameCode(this.nUpd2), true);
		t.checkExpect(this.nUpd2.sameCode(this.nUpd1), true);
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 5);

		this.initData();
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 8);
		this.nUpd2.updateSetHelper(this.nUpd1);
		t.checkExpect(this.nUpd1.sameCode(this.nUpd2), true);
		t.checkExpect(this.nUpd2.sameCode(this.nUpd1), true);
		t.checkExpect(this.nUpd1.code, 8);
		t.checkExpect(this.nUpd2.code, 8);
	}

	/* Tests the updateNeighborCode method */
	void testUpdateNeighborCode(Tester t) {
		this.initData();
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 8);
		this.nUpd1.updateNeighborCode(this.nUpd1.code, this.nUpd2.code);
		t.checkExpect(this.nUpd1.sameCode(this.nUpd2), true);
		t.checkExpect(this.nUpd2.sameCode(this.nUpd1), true);
		t.checkExpect(this.nUpd1.code, 8);
		t.checkExpect(this.nUpd2.code, 8);

		this.initData();
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 8);
		this.nUpd1.updateNeighborCode(this.nUpd2.code, this.nUpd1.code);
		t.checkExpect(this.nUpd1.sameCode(this.nUpd2), false);
		t.checkExpect(this.nUpd2.sameCode(this.nUpd1), false);
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 8);

		this.initData();
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 8);
		this.nUpd2.updateNeighborCode(this.nUpd2.code, this.nUpd1.code);
		t.checkExpect(this.nUpd1.sameCode(this.nUpd2), true);
		t.checkExpect(this.nUpd2.sameCode(this.nUpd1), true);
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 5);

		this.initData();
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 8);
		this.nUpd2.updateNeighborCode(this.nUpd1.code, this.nUpd2.code);
		t.checkExpect(this.nUpd1.sameCode(this.nUpd2), false);
		t.checkExpect(this.nUpd2.sameCode(this.nUpd1), false);
		t.checkExpect(this.nUpd1.code, 5);
		t.checkExpect(this.nUpd2.code, 8);

	}

	/* Tests the overridesDraw method */
	void testOverridesDraw(Tester t) {

		this.initData();
		t.checkExpect(this.n1.overridesDraw(), false);
		t.checkExpect(this.n2.overridesDraw(), false);
		t.checkExpect(this.n3.overridesDraw(), false);
		t.checkExpect(this.n4.overridesDraw(), false);
		t.checkExpect(this.bar.overridesDraw(), true);

	}

	/* Tests the updateRight method */
	void testUpdateRight(Tester t) {

		this.initData();
		t.checkExpect(this.n1.right, this.bar);
		t.checkExpect(this.n2.left, this.bar);
		this.n1.updateRight(this.n2);
		t.checkExpect(this.n1.right, this.n2);
		t.checkExpect(this.n2.left, this.n1);

	}

	/* Tests the updateBottom method */
	void testUpdateBottom(Tester t) {

		this.initData();
		t.checkExpect(this.n1.bottom, this.bar);
		t.checkExpect(this.n3.top, this.bar);
		this.n1.updateBottom(this.n3);
		t.checkExpect(this.n1.bottom, this.n3);
		t.checkExpect(this.n3.top, this.n1);

	}

	/* Tests the getAllNeighbors method. */
	void testGetAllNeighbors(Tester t) {

		this.initData();
		t.checkExpect(this.n1.getAllNeighbors(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getAllNeighbors(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getAllNeighbors(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getAllNeighbors(this.e_list), new ArrayList<Node>());

		this.n1.updateBottom(this.n3);
		this.n1.updateRight(this.n2);
		this.n2.updateBottom(this.n4);
		this.n3.updateRight(this.n4);

		t.checkExpect(this.n1.getAllNeighbors(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getAllNeighbors(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getAllNeighbors(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getAllNeighbors(this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));

		this.e_list.remove(0);
		t.checkExpect(this.n1.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n2, this.n3)));
		t.checkExpect(this.n3.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));

		this.e_list.remove(0);
		t.checkExpect(this.n1.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n2, this.n3)));
		t.checkExpect(this.n2.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n1, this.n4)));
		t.checkExpect(this.n3.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n4.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));

		this.e_list.remove(0);
		t.checkExpect(this.n3.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n4, this.n1)));
		t.checkExpect(this.n4.getAllNeighbors(this.e_list), new ArrayList<Node>(Arrays.asList(this.n3, this.n2)));

	}

	/* Tests the getAllNeighborsPlayer method, which doesn't discriminate against visited nodes. */
	void testGetAllNeighborsPlayer(Tester t) {

		this.initData();
		t.checkExpect(this.n1.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>());

		this.n1.updateBottom(this.n3);
		this.n1.updateRight(this.n2);
		this.n2.updateBottom(this.n4);
		this.n3.updateRight(this.n4);
		this.n1.visited = true;
		this.n2.visited = true;
		this.n3.visited = true;
		this.n4.visited = true;


		t.checkExpect(this.n1.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));

		this.e_list.remove(0);
		t.checkExpect(this.n1.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n2, this.n3)));
		t.checkExpect(this.n3.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));

		this.e_list.remove(0);
		t.checkExpect(this.n1.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n2, this.n3)));
		t.checkExpect(this.n2.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n1, this.n4)));
		t.checkExpect(this.n3.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n4.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));

		this.e_list.remove(0);
		t.checkExpect(this.n3.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n4, this.n1)));
		t.checkExpect(this.n4.getAllNeighborsPlayer(this.e_list), new ArrayList<Node>(Arrays.asList(this.n3, this.n2)));
	}

	/* Tests the getNeighborsHelper method. */
	void testGetNeighborsHelper(Tester t) {

		this.initData();
		this.n1.updateBottom(this.n3);
		this.n1.updateRight(this.n2);
		this.n2.updateBottom(this.n4);
		this.n3.updateRight(this.n4);


		t.checkExpect(this.n1.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n1.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n1.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n1.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n2.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n3)));
		t.checkExpect(this.n3.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n1.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n2.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n3.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n3)));
		t.checkExpect(this.n3.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n4)));
		t.checkExpect(this.n4.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n1.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n2.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n3.getNeighborsHelper(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n3)));
		t.checkExpect(this.n3.getNeighborsHelper(this.n4, this.e_list), new ArrayList<Node>(Arrays.asList(this.n3)));
		t.checkExpect(this.n4.getNeighborsHelper(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n4)));
		t.checkExpect(this.n4.getNeighborsHelper(this.n3, this.e_list), new ArrayList<Node>(Arrays.asList(this.n4)));

	}

	/* Tests the getNeighborsHelper method, which doesn't discriminate against visited nodes. */
	void testGetNeighborsHelperPlayer(Tester t) {

		this.initData();
		this.n1.updateBottom(this.n3);
		this.n1.updateRight(this.n2);
		this.n2.updateBottom(this.n4);
		this.n3.updateRight(this.n4);
		this.n1.visited = true;
		this.n2.visited = true;
		this.n3.visited = true;
		this.n4.visited = true;

		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n3)));
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n3)));
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>());
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n4)));
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>());

		this.e_list.remove(0);
		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n1.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>(Arrays.asList(this.n1)));
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n2.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>(Arrays.asList(this.n2)));
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n1, this.e_list), new ArrayList<Node>(Arrays.asList(this.n3)));
		t.checkExpect(this.n3.getNeighborsHelperPlayer(this.n4, this.e_list), new ArrayList<Node>(Arrays.asList(this.n3)));
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n2, this.e_list), new ArrayList<Node>(Arrays.asList(this.n4)));
		t.checkExpect(this.n4.getNeighborsHelperPlayer(this.n3, this.e_list), new ArrayList<Node>(Arrays.asList(this.n4)));

	}

	/* Tests the doesDrawRight method */
	void testDoesDrawRight(Tester t) {

		this.initData();
		t.checkExpect(this.n1.doesDrawRight(this.e_list), true);
		this.n1.updateRight(this.n2);
		t.checkExpect(this.n1.doesDrawRight(this.e_list), true);
		this.e_list.remove(0);
		t.checkExpect(this.n1.doesDrawRight(this.e_list), false);

		this.initData();
		t.checkExpect(this.n2.doesDrawRight(this.e_list), true);
		this.n1.updateRight(this.n2);
		t.checkExpect(this.n2.doesDrawRight(this.e_list), true);
		this.e_list.remove(0);
		t.checkExpect(this.n2.doesDrawRight(this.e_list), true);

		this.initData();
		t.checkExpect(this.n3.doesDrawRight(this.e_list), true);
		this.n3.updateRight(this.n4);
		t.checkExpect(this.n3.doesDrawRight(this.e_list), true);
		this.e_list.remove(3);
		t.checkExpect(this.n3.doesDrawRight(this.e_list), false);

		this.initData();
		t.checkExpect(this.n4.doesDrawRight(this.e_list), true);
		this.n3.updateRight(this.n4);
		t.checkExpect(this.n4.doesDrawRight(this.e_list), true);
		this.e_list.remove(3);
		t.checkExpect(this.n4.doesDrawRight(this.e_list), true);
	}

	/* Tests the doesDrawBottom method */
	void testDoesDrawBottom(Tester t) {

		this.initData();
		t.checkExpect(this.n1.doesDrawBottom(this.e_list), true);
		this.n1.updateBottom(this.n3);
		t.checkExpect(this.n1.doesDrawBottom(this.e_list), true);
		this.e_list.remove(1);
		t.checkExpect(this.n1.doesDrawBottom(this.e_list), false);

		this.initData();
		t.checkExpect(this.n2.doesDrawBottom(this.e_list), true);
		this.n2.updateBottom(this.n4);
		t.checkExpect(this.n2.doesDrawBottom(this.e_list), true);
		this.e_list.remove(2);
		t.checkExpect(this.n2.doesDrawBottom(this.e_list), false);

		this.initData();
		t.checkExpect(this.n3.doesDrawBottom(this.e_list), true);
		this.n1.updateBottom(this.n3);
		t.checkExpect(this.n3.doesDrawBottom(this.e_list), true);
		this.e_list.remove(1);
		t.checkExpect(this.n3.doesDrawBottom(this.e_list), true);

		this.initData();
		t.checkExpect(this.n4.doesDrawBottom(this.e_list), true);
		this.n2.updateBottom(this.n4);
		t.checkExpect(this.n4.doesDrawBottom(this.e_list), true);
		this.e_list.remove(2);
		t.checkExpect(this.n4.doesDrawBottom(this.e_list), true);
	}

	/* Tests the doesDrawLeft method */
	void testDoesDrawLeft(Tester t) {

		this.initData();
		t.checkExpect(this.n1.doesDrawLeft(this.e_list), true);
		this.n1.updateRight(this.n2); // updates Left at the same time
		t.checkExpect(this.n1.doesDrawLeft(this.e_list), true);

		this.initData();
		t.checkExpect(this.n2.doesDrawLeft(this.e_list), true);
		this.n1.updateRight(this.n2); // updates Left at the same time
		t.checkExpect(this.n2.doesDrawLeft(this.e_list), false);

		this.initData();
		t.checkExpect(this.n3.doesDrawLeft(this.e_list), true);
		this.n3.updateRight(this.n4);
		t.checkExpect(this.n3.doesDrawLeft(this.e_list), true);

		this.initData();
		t.checkExpect(this.n4.doesDrawLeft(this.e_list), true);
		this.n3.updateRight(this.n4);
		t.checkExpect(this.n4.doesDrawLeft(this.e_list), false);

	}

	/* Tests the doesDrawTop method */
	void testDoesDrawTop(Tester t) {

		this.initData();
		t.checkExpect(this.n1.doesDrawTop(this.e_list), true);
		this.n1.updateBottom(this.n3);
		t.checkExpect(this.n1.doesDrawTop(this.e_list), true);

		this.initData();
		t.checkExpect(this.n2.doesDrawTop(this.e_list), true);
		this.n2.updateBottom(this.n4);
		t.checkExpect(this.n2.doesDrawTop(this.e_list), true);

		this.initData();
		t.checkExpect(this.n3.doesDrawTop(this.e_list), true);
		this.n1.updateBottom(this.n3);
		t.checkExpect(this.n3.doesDrawTop(this.e_list), false);

		this.initData();
		t.checkExpect(this.n4.doesDrawTop(this.e_list), true);
		this.n2.updateBottom(this.n4);
		t.checkExpect(this.n4.doesDrawTop(this.e_list), false);		

	}

	/* Tests the fCost method. */
	void testFCost(Tester t) {
		this.initData();
		t.checkExpect(this.testMaze.board.get(0, 1).h_cost, 52);
		t.checkExpect(this.testMaze.board.get(0, 1).g_cost, 10);
		t.checkExpect(this.testMaze.board.get(0, 1).fCost(), 62);

		t.checkExpect(this.testMaze.board.get(1, 0).h_cost, 52);
		t.checkExpect(this.testMaze.board.get(1, 0).g_cost, 10);
		t.checkExpect(this.testMaze.board.get(1, 0).fCost(), 62);

		t.checkExpect(this.testMaze.board.get(1, 1).h_cost, 42);
		t.checkExpect(this.testMaze.board.get(1, 1).g_cost, 14);
		t.checkExpect(this.testMaze.board.get(1, 1).fCost(), 56);
	}

	/* Tests the reset method for the Node class. */
	void testResetNode(Tester t) {
		this.initData();
		t.checkExpect(this.n1.visited, false);
		this.n1.visited = true;
		t.checkExpect(this.n1.visited, true);
		this.n1.reset();
		t.checkExpect(this.n1.visited, false);
	}

	/* Tests for Edge object methods occupy lines 1722 through 1779 */


	/* Tests the connectsNodes methods */
	void testConnectsNodes(Tester t) {

		this.initData();
		t.checkExpect(this.e1.connectsNodes(n1, n2), true);
		t.checkExpect(this.e1.connectsNodes(n2, n1), true);
		t.checkExpect(this.e1.connectsNodes(n1, n4), false);
		t.checkExpect(this.e1.connectsNodes(n4, n1), false);
		t.checkExpect(this.e1.connectsNodes(n3, n4), false);

		t.checkExpect(this.e2.connectsNodes(n1, n3), true);
		t.checkExpect(this.e2.connectsNodes(n3, n1), true);
		t.checkExpect(this.e2.connectsNodes(n2, n3), false);
		t.checkExpect(this.e2.connectsNodes(n3, n2), false);
		t.checkExpect(this.e2.connectsNodes(n2, n4), false);

		t.checkExpect(this.e3.connectsNodes(n2, n4), true);
		t.checkExpect(this.e3.connectsNodes(n4, n2), true);
		t.checkExpect(this.e3.connectsNodes(n2, n3), false);
		t.checkExpect(this.e3.connectsNodes(n3, n2), false);
		t.checkExpect(this.e3.connectsNodes(n1, n3), false);

		t.checkExpect(this.e4.connectsNodes(n3, n4), true);
		t.checkExpect(this.e4.connectsNodes(n4, n3), true);
		t.checkExpect(this.e4.connectsNodes(n3, n2), false);
		t.checkExpect(this.e4.connectsNodes(n2, n3), false);
		t.checkExpect(this.e4.connectsNodes(n1, n2), false);

	}

	/* Tests the mergeSets method */
	void testMergeSets(Tester t) {

		this.initData();
		t.checkExpect(this.e1.from.sameCode(this.e1.to), false);
		this.e1.mergeSets();
		t.checkExpect(this.e1.from.sameCode(this.e1.to), true);

		this.initData();
		t.checkExpect(this.e2.from.sameCode(this.e2.to), false);
		this.e2.mergeSets();
		t.checkExpect(this.e2.from.sameCode(this.e2.to), true);

	}

	/* Tests the sameCodeBothSides method */
	void testSameCodeBothSides(Tester t) {

		this.initData();
		t.checkExpect(this.e1.sameCodeBothSides(), false);
		this.e1.mergeSets();
		t.checkExpect(this.e1.sameCodeBothSides(), true);

		this.initData();
		t.checkExpect(this.e2.sameCodeBothSides(), false);
		this.e2.mergeSets();
		t.checkExpect(this.e2.sameCodeBothSides(), true);

	}


	/* Tests on Board objects and methods occupy lines 1396 through 1459 */


	/* Tests the shortcut Get method */
	void testGet(Tester t) {

		this.initData();
		t.checkExpect(this.board.get(0, 0), this.board.board.get(0).get(0));
		t.checkExpect(this.board.get(0, 1), this.board.board.get(0).get(1));
		t.checkExpect(this.board.get(1, 0), this.board.board.get(1).get(0));
		t.checkExpect(this.board.get(1, 0), this.board.board.get(1).get(0));

	}

	/* Tests the fixBoard method */
	void testFixBoard(Tester t) {

		this.initData();
		t.checkExpect(this.board.get(0, 0).right, this.board.get(1, 0));
		t.checkExpect(this.board.get(0, 0).bottom, this.board.get(0, 1));
		t.checkExpect(this.board.get(0, 0).top, new Barrier());
		t.checkExpect(this.board.get(0, 0).left, new Barrier());

		this.board.get(0, 0).bottom = this.board.get(1, 1);
		t.checkExpect(this.board.get(0, 0).bottom, this.board.get(1, 1));
		this.board.fixBoard();
		t.checkExpect(this.board.get(0, 0).bottom, this.board.get(0, 1));

	}

	/* Tests the cellSize method */
	void testCellSize(Tester t) {

		this.initData();
		t.checkExpect(this.board.cellSize(), 375);
		this.board.width = 5;
		this.board.height = 5;
		t.checkExpect(this.board.cellSize(), 150);

	}

	/* Tests the edgeThickness method */
	void testEdgeThickness(Tester t) {

		this.initData();
		t.checkExpect(this.board.edgeThickness(), 375 / 12);
		this.board.width = 5;
		this.board.height = 5;
		t.checkExpect(this.board.edgeThickness(), 150 / 12);

	}

	/* Tests the reset method (for Board only) */
	void testResetBoard(Tester t) {

		this.initData();
		this.board.fixBoard();
		t.checkExpect(this.board.get(0, 0).visited, false);
		t.checkExpect(this.board.get(0, 1).visited, false);
		this.board.get(0, 0).visited = true;
		this.board.get(0, 1).visited = true;
		t.checkExpect(this.board.get(0, 0).visited, true);
		t.checkExpect(this.board.get(0, 1).visited, true);
		this.board.reset();
		t.checkExpect(this.board.get(0, 0).visited, false);
		t.checkExpect(this.board.get(0, 1).visited, false);

	}

	/* Tests the getDistance method. */
	void testGetDistance(Tester t) {
		this.initData();
		t.checkExpect(this.testMaze.board.get(0, 0).x, 0);
		t.checkExpect(this.testMaze.board.get(0, 0).y, 0);
		t.checkExpect(this.testMaze.board.get(1, 2).x, 1);
		t.checkExpect(this.testMaze.board.get(1, 2).y, 2);
		t.checkExpect(this.testMaze.board.target.x, 4);
		t.checkExpect(this.testMaze.board.target.y, 4);

		t.checkExpect(this.testMaze.board.getDistance(this.testMaze.board.get(0, 0), this.testMaze.board.target), 56);
		t.checkExpect(this.testMaze.board.getDistance(this.testMaze.board.target, this.testMaze.board.get(0, 0)), 56);
		t.checkExpect(this.testMaze.board.getDistance(this.testMaze.board.get(1, 2), this.testMaze.board.target), 38);
		t.checkExpect(this.testMaze.board.getDistance(this.testMaze.board.target, this.testMaze.board.get(1, 2)), 38);
	}


	/* --- Tests of the mazeWorld objects and methods occupy lines 1872 to the end of the file.  --- */


	/* Tests the overridden makeScene method */
	void testMakeScene(Tester t) {

		this.initData();
		t.checkExpect(this.testMaze1x2.edgeThickness, 31);
		t.checkExpect(this.testMaze1x2.nodeSize, 375);
		WorldScene testScene = new WorldScene(IGameConstants.WINDOW_W, IGameConstants.WINDOW_H);
		testScene.placeImageXY(new RectangleImage(375, 375, OutlineMode.SOLID, Color.GREEN), 500, 187);
		testScene.placeImageXY(new RectangleImage(31, 375, OutlineMode.SOLID, Color.BLACK), 688, 187);
		testScene.placeImageXY(new RectangleImage(375, 31, OutlineMode.SOLID, Color.BLACK), 500, 375);	
		testScene.placeImageXY(new RectangleImage(31, 375, OutlineMode.SOLID, Color.BLACK), 313, 187);
		testScene.placeImageXY(new RectangleImage(375, 31, OutlineMode.SOLID, Color.BLACK), 500, 0);
		testScene.placeImageXY(new RectangleImage(375, 375, OutlineMode.SOLID, Color.RED), 500, 562);
		testScene.placeImageXY(new RectangleImage(31, 375, OutlineMode.SOLID, Color.BLACK), 688, 562);
		testScene.placeImageXY(new RectangleImage(375, 31, OutlineMode.SOLID, Color.BLACK), 500, 750);
		testScene.placeImageXY(new RectangleImage(31, 375, OutlineMode.SOLID, Color.BLACK), 313, 562);
		t.checkExpect(this.testMaze1x2.makeScene(), testScene);

	}

	/* Tests the overridden onKeyEvent method */
	void testOnKeyEvent(Tester t) {

		this.initData();
		t.checkExpect(this.testMaze1x2.isInitializing, true);
		t.checkExpect(this.testMaze1x2.isSearching, false);
		t.checkExpect(this.testMaze1x2.search, SearchType.INACTIVE);
		t.checkExpect(this.testMaze1x2.isDrawingPath, false);

		// interrupt cancel
		this.testMaze1x2.onKeyEvent("d");
		t.checkExpect(this.testMaze1x2.isInitializing, true);
		this.testMaze1x2.isInitializing = false;

		// depth first
		this.testMaze1x2.onKeyEvent("d");
		t.checkExpect(this.testMaze1x2.isInitializing, false);
		t.checkExpect(this.testMaze1x2.isSearching, true);
		t.checkExpect(this.testMaze1x2.search, SearchType.DEPTH_FIRST);
		this.testMaze1x2.isSearching = false;

		// breadth first
		this.testMaze1x2.onKeyEvent("b");
		t.checkExpect(this.testMaze1x2.isInitializing, false);
		t.checkExpect(this.testMaze1x2.isSearching, true);
		t.checkExpect(this.testMaze1x2.search, SearchType.BREADTH_FIRST);
		this.testMaze1x2.isSearching = false;

		// a*
		this.testMaze1x2.onKeyEvent("a");
		t.checkExpect(this.testMaze1x2.isInitializing, false);
		t.checkExpect(this.testMaze1x2.isSearching, true);
		t.checkExpect(this.testMaze1x2.search, SearchType.A_STAR);
		this.testMaze1x2.isSearching = false;

		// user control toggle
		this.testMaze1x2.onKeyEvent("u");
		t.checkExpect(this.testMaze1x2.isInitializing, false);
		t.checkExpect(this.testMaze1x2.isSearching, true);
		t.checkExpect(this.testMaze1x2.userInControl, true);
		t.checkExpect(this.testMaze1x2.search, SearchType.USER);
		this.testMaze1x2.onKeyEvent("u");
		t.checkExpect(this.testMaze1x2.isInitializing, false);
		t.checkExpect(this.testMaze1x2.isSearching, false);
		t.checkExpect(this.testMaze1x2.userInControl, false);
		t.checkExpect(this.testMaze1x2.search, SearchType.USER);

		// User inputs
		this.initData();
		t.checkExpect(this.testMaze.playerNode.x, 0);
		t.checkExpect(this.testMaze.playerNode.y, 0);
		this.testMaze.onKeyEvent("up");
		t.checkExpect(this.testMaze.playerNode.x, 0);
		t.checkExpect(this.testMaze.playerNode.y, 0);
		this.testMaze.onKeyEvent("down");
		t.checkExpect(this.testMaze.playerNode.x, 0);
		t.checkExpect(this.testMaze.playerNode.y, 0);
		this.testMaze.onKeyEvent("left");
		t.checkExpect(this.testMaze.playerNode.x, 0);
		t.checkExpect(this.testMaze.playerNode.y, 0);
		this.testMaze.onKeyEvent("right");
		t.checkExpect(this.testMaze.playerNode.x, 0);
		t.checkExpect(this.testMaze.playerNode.y, 0);

		// new board
		this.testMaze1x2.onKeyEvent("n");
		t.checkExpect(this.testMaze1x2.isInitializing, true);
		t.checkExpect(this.testMaze1x2.isSearching, false);
		t.checkExpect(this.testMaze1x2.search, SearchType.INACTIVE);

	}

	/* Tests the overridden onTick method */
	void testOnTick(Tester t) {

		this.initData();
		t.checkExpect(this.testMaze1x2.isInitializing, true);
		t.checkExpect(this.testMaze1x2.isSearching, false);
		t.checkExpect(this.testMaze1x2.search, SearchType.INACTIVE);
		t.checkExpect(this.testMaze1x2.board.edges, new ArrayList<Edge>(Arrays.asList(
				new Edge(
						this.testMaze1x2.board.get(0, 0), 
						this.testMaze1x2.board.get(0, 1), 
						87981)))); // seeded random edge weight
		this.testMaze1x2.onTick();
		t.checkExpect(this.testMaze1x2.board.edges, new ArrayList<Edge>());
		t.checkExpect(this.testMaze1x2.isInitializing, true);
		this.testMaze1x2.onTick();
		t.checkExpect(this.testMaze1x2.isInitializing, false);
		t.checkExpect(this.testMaze1x2.isSearching, false);
		this.testMaze1x2.onKeyEvent("d");
		t.checkExpect(this.testMaze1x2.search, SearchType.DEPTH_FIRST);
		t.checkExpect(this.testMaze1x2.isInitializing, false);
		t.checkExpect(this.testMaze1x2.isSearching, true);
		t.checkExpect(this.testMaze1x2.stack.size(), 1);
		this.testMaze1x2.onTick();
		t.checkExpect(this.testMaze1x2.stack.size(), 2);
		this.testMaze1x2.onTick();
		t.checkExpect(this.testMaze1x2.stack.size(), 2);
		t.checkExpect(this.testMaze1x2.isSearching, false);
		this.testMaze1x2.onKeyEvent("b");
		t.checkExpect(this.testMaze1x2.search, SearchType.BREADTH_FIRST);
		t.checkExpect(this.testMaze1x2.isInitializing, false);
		t.checkExpect(this.testMaze1x2.isSearching, true);
		t.checkExpect(this.testMaze1x2.isDrawingPath, false);
		t.checkExpect(this.testMaze1x2.stack.size(), 1);
		this.testMaze1x2.onTick();
		t.checkExpect(this.testMaze1x2.stack.size(), 1);
		this.testMaze1x2.onTick();
		t.checkExpect(this.testMaze1x2.stack.size(), 1);
		this.testMaze1x2.onTick();
		t.checkExpect(this.testMaze1x2.isDrawingPath, true);
		t.checkExpect(this.testMaze1x2.stack.size(), 2);
		this.testMaze1x2.onTick();
		t.checkExpect(this.testMaze1x2.isSearching, false);
	}


	/* Tests the bigBang function and the running of the maze. */
	void testMaze(Tester t) {
		this.initData();
		this.maze.bigBang(WINDOW_W, WINDOW_H, 0.0001);
	}
}
