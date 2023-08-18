# README
Pathfinder is an application capable of constructing perfect mazes from randomly generated minimum-spanning trees and demonstrating their solutions with some prominent search algorithms. It is written in Java and uses a university-provided library to display the maze. While it started as an assignment, this version introduces the ability for users to manually traverse the maze and implements additional algorithms.

The program launches with the _bigBang_ call at the end of the ExamplesMazes class. It initially generates a 2D array of Nodes (the Board) and creates connections between adjacent Nodes. For each connection, an Edge is initialized with a random "weight" value. Following Kruskal's Algorithm, the edges are sorted and removed from least to greatest weight. Each edge is removed until no more edges can be removed without creating a cyclical graph. When all nodes in the maze are connected by a single path, the maze is traversable.

![Maze Generation with Kruskal's Algorithm](https://github.com/omathay/pathfinder/assets/87339590/41205004-7fe6-4be4-9935-813da8b89646)


The goal of this project was to practice working with graphs and improve my knowledge of graph algorithms by designing mazes using Kruskal’s algorithm and solving them using either breadth- or depth-first searches. I intended to expand upon this by adding an additional algorithm, which was originally supposed to be Dijkstra's Algorithm. Due to the non-cyclical nature of the graph, it lost nearly all advantage over BFS. It was replaced by A*, or A Star, which performs similar to DFS but typically moves much faster due to the inclusion of a heuristic.

Additionally, this version tracks the performance in time and distance, and allows the user to solve the maze manually.

![Manual Traversal](https://github.com/omathay/pathfinder/blob/88e7be386e4c41ea2a403ae24c7d1249dd3783e1/manual%20traversal.gif)


# How to Use
