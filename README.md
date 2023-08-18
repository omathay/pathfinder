# README
Pathfinder is an application capable of constructing perfect mazes from randomly generated minimum-spanning trees and animating their solutions using prominent search algorithms. It is written in Java and uses a university-provided library to display the maze. While it started as an assignment, this version introduces the ability for users to manually traverse the maze and implements additional algorithms.

The program launches with the _bigBang_ call at the end of the ExamplesMazes class. It initially generates a 2D array of Nodes (the Board) and creates connections between adjacent Nodes. For each connection, an Edge is initialized with a random "weight" value. Following Kruskal's Algorithm, the edges are sorted and removed from least to greatest weight. Each edge is removed until no more edges can be removed without creating a cyclical graph. When all nodes in the maze are connected by a single path, the maze is traversable.

![Maze Generation with Kruskal's Algorithm](https://github.com/omathay/pathfinder/blob/8b94a435cc961a146514fb673643eb638f1f92f3/maze%20generation%20(kruskals).gif)


The goal of this project was to practice working with graphs and improve my knowledge of graph algorithms by designing mazes using Kruskal’s algorithm and solving them using either breadth- or depth-first searches. 

![Sample Solutions, Depth- and Breadth-First searches](https://github.com/omathay/pathfinder/blob/6bc0f3546d0af9569d012f1e78434e52888fc3c8/DFS%3ABFS%20demonstration.gif)

I intended to expand upon this by adding an additional algorithm, which was originally supposed to be Dijkstra's Algorithm. Due to the non-cyclical nature of the graph, it lost nearly all advantage over BFS. It was replaced by A*, or A Star, which performs similar to DFS but typically moves much faster due to the inclusion of a heuristic. Additionally, this project version tracks the performance in time and distance, and allows the user to solve the maze manually.

![Manual Traversal](https://github.com/omathay/pathfinder/blob/88e7be386e4c41ea2a403ae24c7d1249dd3783e1/manual%20traversal.gif)
# How to Use
Due to its origin as an assignment, Pathfinder was written using Image and Tester Libraries specific to my university and must have those .jar files in the project classpath in order to run successfully. All libraries used to render and test the code were provided by the course in order to create familiarity working with external libraries and through uncertainty. (In an effort to maintain the privacy of university property, these will not be posted.) In order to run the file, create a new run configuration with the project file and the Main class as "_tester.Main_", and in the arguments field, write "_ExamplesMazes_". The program should run as expected.

If you are interested in reviewing the full project file, please contact me at the links on my [homepage.](https://github.com/omathay)
