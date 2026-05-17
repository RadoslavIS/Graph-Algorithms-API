package com.rado.graphs;

public class Application {
    public static void main(String[] args) {
        section("Empty graph edge cases");
        Graph<String, Integer> empty = new Graph<>(true);
        check("isEmpty()", empty.isEmpty());
        check("Vertex count", empty.getVertexCount());
        check("Edge count", empty.getEdgeCount());
        check("BFS from missing node", empty.bfs("A"));
        check("DFS recursive from missing node", empty.dfsRecursive("A"));
        check("DFS iterative from missing node", empty.dfsIterative("A"));
        check("hasPathDFS on missing nodes", empty.hasPathDFS("A", "B"));
        check("hasPathBFS on missing nodes", empty.hasPathBFS("A", "B"));
        check("Top sort on empty graph", empty.topSort());
        check("isDAG on empty graph", empty.isDAG());

        section("Self-loop and invalid input edge cases");
        Graph<String, Integer> invalid = new Graph<>(true);
        invalid.addNode("S", 1);
        invalid.addEdge("S", "S");
        check("Edge count after self-loop attempt", invalid.getEdgeCount());
        expectException("Null source", () -> invalid.addEdge(null, "T"));
        expectException("Null destination", () -> invalid.addEdge("T", null));
        expectException("Negative weight", () -> invalid.addEdge("T", "U", -1.0));

        section("Directed DAG");
        Graph<String, Integer> dag = new Graph<>(true);
        dag.addEdge("A", "B");
        dag.addEdge("A", "C");
        dag.addEdge("B", "D");
        dag.addEdge("C", "D");
        dag.addNode("E", 42);

        check("Vertex count", dag.getVertexCount());
        check("Edge count", dag.getEdgeCount());
        check("DFS recursive from A", dag.dfsRecursive("A"));
        check("DFS iterative from A", dag.dfsIterative("A"));
        check("BFS from A", dag.bfs("A"));
        check("hasPathDFS A->D", dag.hasPathDFS("A", "D"));
        check("hasPathBFS A->D", dag.hasPathBFS("A", "D"));
        check("hasPathDFS A->E", dag.hasPathDFS("A", "E"));
        check("Topological sort", dag.topSort());
        check("isDAG()", dag.isDAG());

        section("Directed cycle");
        Graph<String, Integer> cyclic = new Graph<>(true);
        cyclic.addEdge("X", "Y");
        cyclic.addEdge("Y", "Z");
        cyclic.addEdge("Z", "X");
        check("Topological sort size", cyclic.topSort().size());
        check("Vertex count", cyclic.getVertexCount());
        check("isDAG()", cyclic.isDAG());

        section("Dijkstra algorithm");
        Graph<Integer, String> weighted = new Graph<>(true);
        weighted.addEdge(1, 2, 3.0);
        weighted.addEdge(2, 3, 7.0);
        weighted.addEdge(1, 5, 7.0);
        weighted.addEdge(2, 5, 4.5);
        weighted.addEdge(3, 1, 3.0);

        check("Shortest path 1->3", weighted.shortestPathDijkstra(1, 3));
        check("Shortest path 1->5", weighted.shortestPathDijkstra(1, 5));
        expectException("Missing destination node", () -> weighted.shortestPathDijkstra(1, 4));

        section("Undirected graph, removeEdge, removeNode");
        Graph<Integer, String> und = new Graph<>(false);
        und.addEdge(1, 2);
        und.addEdge(2, 3);
        und.addEdge(3, 4);
        und.addNode(99, "isolated");

        check("Before removal - vertex count", und.getVertexCount());
        check("Before removal - edge count", und.getEdgeCount());
        check("BFS from 1", und.bfs(1));
        check("hasPathBFS 1->4", und.hasPathBFS(1, 4));
        check("removeEdge(1, 2)", und.removeEdge(1, 2));
        check("removeEdge(1, 2) again", und.removeEdge(1, 2));
        check("Edge count after removals", und.getEdgeCount());
        check("removeNode(99)", und.removeNode(99));
        check("removeNode(99) again", und.removeNode(99));
        check("Vertex count after removing isolated node", und.getVertexCount());
        check("Graph after removals", und);

        section("Prim's MST");
        Graph<String, Integer> mstGraph = new Graph<>(false);
        mstGraph.addEdge("A", "B", 1.0);
        mstGraph.addEdge("A", "C", 5.0);
        mstGraph.addEdge("B", "C", 2.0);
        mstGraph.addEdge("B", "D", 4.0);
        mstGraph.addEdge("C", "D", 1.0);
        mstGraph.addEdge("D", "E", 3.0);

        Graph<String, Integer> mst = mstGraph.getMinSpanningForestPrim();
        check("MST vertex count", mst.getVertexCount());
        check("MST edge count", mst.getEdgeCount());
        check("MST graph", mst);

        section("clone() and equals()");
        Graph<String, Integer> dagClone = dag.clone();
        check("dag.equals(dagClone)", dag.equals(dagClone));
        check("Clone vertex count", dagClone.getVertexCount());
        check("Clone edge count", dagClone.getEdgeCount());

        section("flipDirectedness()");
        Graph<String, Integer> flip = new Graph<>(true);
        flip.addEdge("P", "Q");
        flip.addEdge("Q", "R");
        check("Before flip", flip);
        flip.printDirectedness();
        flip.flipDirectedness();
        flip.printDirectedness();
        check("After flip", flip);
    }

    private static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    private static void check(String label, Object value) {
        System.out.println(label + ": " + value);
    }

    private static void expectException(String label, Runnable action) {
        try {
            action.run();
            System.out.println(label + ": [FAILED] expected exception, but none was thrown");
        } catch (Exception e) {
            System.out.println(label + ": [OK] " + e.getClass().getSimpleName() + " -> " + e.getMessage());
        }
    }
}