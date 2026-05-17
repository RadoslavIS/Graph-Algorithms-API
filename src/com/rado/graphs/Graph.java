package com.rado.graphs;

import com.rado.pq.PriorityQueue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class Graph<K, V> {
    private final Map<K, Map<K, Edge>> adjList = new HashMap<>();
    private final Map<K, Node<K, V>> nodes = new HashMap<>();
    private final Map<K, Integer> inDegree = new HashMap<>();

    private boolean directed;
    private int edgeCount = 0;

    public Graph() { this(true); }

    public Graph(boolean directed) { this.directed = directed; }

    /** Graph from edge list **/
    public Graph(K[][] edges) {
        this(edges, true);
    }

    public Graph(K[][] edges, boolean directed) {

        this.directed = directed;
        for (K[] edge : edges) {
            if (edge.length != 2) continue;
            addEdge(edge[0], edge[1]);
        }
    }

    public int getVertexCount() { return nodes.size(); }

    /** @return edgeCount - logical edge count
     * (edges in undirected graph count as 1) **/
    public int getEdgeCount() { return edgeCount; }

    public boolean isEmpty() { return nodes.isEmpty(); }

    /** Adds a new node to the graph, if the key is already present, only updates the value **/
    public void addNode(K key, V data) { addNodeIfAbsent(key, data); }

    /** Adds a new node if the key is absent, otherwise it overrides its value
        Setting data to null ignores the parameter **/
    private void addNodeIfAbsent(K key, V data) {
        Node<K, V> n = nodes.get(key);
        if (n == null) {
            nodes.put(key, new Node<>(key, data));
            adjList.put(key, new HashMap<>());
            inDegree.put(key, 0);
        }
        else if (data != null) n.setData(data);
    }

    /** Adds an edge to the graph with weight 1 <br>
        addEdge(2, 4) - 2 points to 4 **/
    public void addEdge(K from, K to) {
        addEdge(from, to, 1.0);
    }

    public void addEdge(K from, K to, Double weight) {
        if (from == null || to == null) throw new NullPointerException("One of the keys are invalid");

        if (from.equals(to)) {
            System.out.println("Nodes can't point to themselves");
            return;
        }

        if (weight < 0) throw new IllegalArgumentException("Graph does not support negative weights");

        addNodeIfAbsent(from, null);
        addNodeIfAbsent(to, null);
        if (adjList.get(from).put(to, new Edge(weight)) == null) {
            inDegree.replace(to, inDegree.get(to) + 1);
            edgeCount++;
        }
        nodes.get(from).addNeighbor(nodes.get(to));

        if (!directed) {
            adjList.get(to).put(from, new Edge(weight));
            nodes.get(to).addNeighbor(nodes.get(from));
            inDegree.replace(from, inDegree.get(from) + 1);
        }
    }

    /**
        @param removed node removed
        @return deleted value or null if removed is not contained
     **/
    public V removeNode(K removed) {
        if (!nodes.containsKey(removed)) return null;
        for (K from : adjList.keySet()) {
            removeEdge(from, removed);
        }

        Set<K> removedNbrs = new HashSet<>(adjList.get(removed).keySet());
        for (K to : removedNbrs) {
            removeEdge(removed, to);
        }
        adjList.remove(removed);
        inDegree.remove(removed);
        // Remove node references
        V old = nodes.get(removed).getData();
        nodes.remove(removed);
        for (Node<K, V> n : nodes.values()) {
            n.removeNeighborByKey(removed);
        }
        return old;
    }

    /** Removes the edge between two existing nodes
     * @return if the edge is removed**/
    public boolean removeEdge(K from, K to) {
        if (from == null || to == null || from.equals(to) ||
                !nodes.containsKey(from) || !nodes.containsKey(to) || adjList.get(from).remove(to) == null) {
            return false;
        }
        inDegree.replace(to, Math.max(0, inDegree.get(to) - 1));
        edgeCount--;
        nodes.get(from).removeNeighborByKey(to);
        if (!directed) {
            if (adjList.get(to).remove(from) != null) {
                inDegree.replace(from, Math.max(0, inDegree.get(from) - 1));
            }
            nodes.get(to).removeNeighborByKey(from);
        }
        return true;
    }

    /** Recursive Depth-first-search graph traversal
        @param start starting key
        @return traversal order **/
    public List<K> dfsRecursive(K start) {
        List<K> order = new ArrayList<>();
        // Return empty list if key is not present
        if (!nodes.containsKey(start)) return order;
        dfsRecursiveHelper(start, new HashSet<>(), order);
        return order;
    }

    private void dfsRecursiveHelper(K node, Set<K> visited, List<K> order) {
        visited.add(node);
        order.add(node);
        for (K child : adjList.getOrDefault(node, Collections.emptyMap()).keySet()) {
            if (!visited.contains(child)) dfsRecursiveHelper(child, visited, order);
        }
    }

    /** Iterative Depth-first-search graph traversal
    * @param start starting key
    * @return traversal order **/
    public List<K> dfsIterative(K start) {
        if (!nodes.containsKey(start)) return Collections.emptyList();
        Set<K> visited = new HashSet<>();
        List<K> order = new ArrayList<>();
        Deque<K> stack = new ArrayDeque<>();
        visited.add(start);
        stack.push(start);

        K current;
        while (!stack.isEmpty()) {
            current = stack.pop();
            order.add(current);
            for (K nbr : adjList.getOrDefault(current, Collections.emptyMap()).keySet()) {
                if (visited.add(nbr)) stack.push(nbr);
            }
        }
        return order;
    }

    public boolean hasPathDFS(K from, K to) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return false;
        if (from.equals(to)) return true;
        Set<K> visited = new HashSet<>();
        Deque<K> stack = new ArrayDeque<>();
        visited.add(from);
        stack.push(from);

        K current;
        while (!stack.isEmpty()) {
            current = stack.pop();
            if (current.equals(to)) return true;
            for (K nbr : adjList.getOrDefault(current, Collections.emptyMap()).keySet()) {
                if (visited.add(nbr)) stack.push(nbr);
            }
        }
        return false;
    }

    /** Breadth-First Search (Level traversal) of the graph
        @param start starting key
        @return traversal order **/
    public List<K> bfs(K start) {
        if (!nodes.containsKey(start)) return Collections.emptyList();
        Set<K> visited = new HashSet<>();
        List<K> order = new ArrayList<>();
        Deque<K> q = new ArrayDeque<>();
        visited.add(start);
        q.addLast(start);

        K current;
        while (!q.isEmpty()) {
            current = q.removeFirst();
            order.add(current);
            for (K nbr : adjList.getOrDefault(current, Collections.emptyMap()).keySet()) {
                if (visited.add(nbr)) q.addLast(nbr);
            }
        }
        return order;
    }

    public boolean hasPathBFS(K from, K to) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return false;
        if (from.equals(to)) return true;
        Set<K> visited = new HashSet<>();
        Deque<K> q = new ArrayDeque<>();
        visited.add(from);
        q.addLast(from);

        K current;
        while (!q.isEmpty()) {
            current = q.removeFirst();
            if (current.equals(to)) return true;
            for (K nbr : adjList.getOrDefault(current, Collections.emptyMap()).keySet()) {
                if (visited.add(nbr)) q.addLast(nbr);
            }
        }
        return false;
    }

    /** Creates a topological ordering of the graph
      * if it is a DAG using Kahn's algorithm
      * @return order of traversal **/
    public List<K> topSort() {
        if (!directed) throw new IllegalArgumentException("Kahn's algorithm only supports directed graphs");

        Map<K, Integer> indeg = new HashMap<>(inDegree);
        Deque<K> q = new ArrayDeque<>();
        List<K> order = new ArrayList<>();

        // initial sources
        for (K k : indeg.keySet()) {
            if (indeg.get(k) == 0) q.addLast(k);
        }

        K current;
        while (!q.isEmpty()) {
            current = q.removeFirst();
            order.add(current);
            for (K k : adjList.getOrDefault(current, Collections.emptyMap()).keySet()) {
                indeg.put(k, Math.max(0, indeg.get(k) - 1));
                if (indeg.get(k) == 0) q.addLast(k);
            }
        }
        return order;
    }

    /** Finds the shortest path between 2 existing nodes **/
    public PathResult<K> shortestPathDijkstra(K src, K dest) {
        if (!adjList.containsKey(src) || !adjList.containsKey(dest)) {
            throw new IllegalArgumentException("One or more nodes are missing");
        }
        Map<K, Double> dist = new HashMap<>();
        Map<K, K> prev = new HashMap<>();
        PriorityQueue<PQNode<K>> pq = new PriorityQueue<>();
        final double INF = Double.POSITIVE_INFINITY;
        // every node has infinite distance from src initially
        for (K key : nodes.keySet()) dist.put(key, INF);

        dist.replace(src, 0.0d);
        pq.offer(new PQNode<>(src, 0.0d));

        while (!pq.isEmpty()) {
            PQNode<K> uNode = pq.poll();
            K u = uNode.key;
            Double d = uNode.distance;

            if (d > dist.getOrDefault(u, INF)) continue;

            for (Map.Entry<K, Edge> e : adjList.getOrDefault(u, Collections.emptyMap()).entrySet()) {
                K v = e.getKey();
                double dV = d + e.getValue().getWeight();

                if (dV < dist.getOrDefault(v, INF)) {
                    dist.replace(v, dV);
                    prev.put(v, u);
                    pq.offer(new PQNode<>(e.getKey(), dV));
                }
            }
        }
        double finalDist = dist.getOrDefault(dest, INF);
        List<K> path = new ArrayList<>();
        if (finalDist == INF) return new PathResult<>(INF, path);

        for (K at = dest; at != null; at = prev.get(at)) {
            path.addFirst(at);
        }
        return new PathResult<>(finalDist, path);
    }

    /** Wrapper node containing a key and its distance from a source for Dijkstra's algorithm**/
    private static class PQNode<K> implements Comparable<PQNode<K>> {
        private final K key;
        private final Double distance;

        PQNode(K key, Double data) { this.key = key; this.distance = data; }

        @Override
        public int compareTo(PQNode pqn) {
            return Double.compare(distance, pqn.distance);
        }

        public Double getDistance() { return this.distance; }
    }

    public record PathResult<K>(double distance, List<K> path) {

        public String toString() {
                return String.format("Distance from source: %s\nPath taken: %s", distance, path);
            }
        }

    public Graph<K, V> getMinSpanningForestPrim() {
        if (directed) throw new IllegalArgumentException("Prim's algorithm only supports undirected graphs");
        Graph<K, V> msf = new Graph<>(false);
        if (nodes.isEmpty()) return msf;

        for (Map.Entry<K, Node<K, V>> e : nodes.entrySet()) {
            msf.addNode(e.getKey(), e.getValue().getData());
        }

        Set<K> visited = new HashSet<>();
        PriorityQueue<MSFEdge<K>> pq = new PriorityQueue<>();

        for (K u : nodes.keySet()) {
            if (!visited.add(u)) continue;
            pushIncidentEdges(u, visited, pq);

            while (!pq.isEmpty()) {
                MSFEdge<K> curEdge = pq.poll();
                if (visited.contains(curEdge.to)) continue;
                visited.add(curEdge.to);
                msf.addEdge(curEdge.from, curEdge.to, curEdge.weight);
                pushIncidentEdges(curEdge.to, visited, pq);
            }
        }
        return msf;
    }

    private void pushIncidentEdges(K from, Set<K> visited, PriorityQueue<MSFEdge<K>> pq) {
        for (Map.Entry<K, Edge> e : adjList.getOrDefault(from, Collections.emptyMap()).entrySet()) {
            K to = e.getKey();
            if (!visited.contains(to)) {
                pq.offer(new MSFEdge<>(from, to, e.getValue().getWeight()));
            }
        }
    }

    private static final class MSFEdge<K> implements Comparable<MSFEdge<K>> {
        private final K from;
        private final K to;
        private final double weight;

        private MSFEdge(K from, K to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        @Override
        public int compareTo(MSFEdge<K> edge) {
            return Double.compare(this.weight, edge.weight);
        }
    }


    /** Checks if the graph is directed acyclic (DAG) **/
    public boolean isDAG() {
        return directed && topSort().size() == nodes.size();
    }

    public void flipDirectedness() {
        if (directed) {
            List<Map.Entry<K, K>> edges = new ArrayList<>();

            for (K u : adjList.keySet()) {
                for (K v : adjList.get(u).keySet()) {
                    edges.add(Map.entry(u, v));
                }
            }

            for (Map.Entry<K, K> e : edges) {
                addEdge(e.getValue(), e.getKey(), adjList.get(e.getKey()).get(e.getValue()).getWeight());
            }
            edgeCount /= 2;
            directed = false;
        }
        else {
            edgeCount *= 2;
            directed = true;
        }

    }

    public void printDirectedness() {
        System.out.println("This graph is " + (directed ? "directed" : "undirected"));
    }

    public Graph<K, V> clone() {
        Graph<K, V> g = new Graph<>();
        g.directed = directed;
        for (Map.Entry<K, Node<K, V>> n : nodes.entrySet()) {
            g.addNode(n.getKey(), n.getValue().getData());
        }

        for (Map.Entry<K, Map<K, Edge>> e : adjList.entrySet()) {
            for (K v : e.getValue().keySet()) {
                g.addEdge(e.getKey(), v, e.getValue().get(v).getWeight());
            }
        }

        // inDegree gets copied from previous operations
        return g;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Graph<?, ?> g)) return false;

        return directed == g.directed && adjList.equals(g.adjList) && nodes.equals(g.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adjList, nodes);
    }

    @Override
    public String toString() {
        return(String.format("Nodes: %s\nAdjacency List: %s\nIncoming degrees: %s", adjList.keySet(), adjList, inDegree));
    }

    static class Node<K, V> {
        private final Set<Node<K, V>> neighbors = new HashSet<>();
        private V data;
        private final K key;

        public Node(K key) {
            this(key,null);
        }

        public Node(K key, V data) {
            this.key = key;
            this.data = data;
        }

        public void addNeighbor(Node<K, V> newNode) {
            neighbors.add(newNode);
        }

        public boolean removeNeighbor(Node<K, V> removed) {
            return neighbors.remove(removed);
        }

        public boolean removeNeighborByKey(K key) {
            return neighbors.removeIf(n -> n.key.equals(key));
        }

        public K getKey() { return key; }

        public V getData() { return data; }

        public void setData(V data) { this.data = data; }

        // Doesn't compare neighbors because too slow and is already done by com.rado.graphs.Graph.equals()
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node<?, ?> n)) return false;
            return Objects.equals(key, n.key) && Objects.equals(data, n.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, data);
        }

        public String toString() {
            return String.format("Key: %s, Value: %s", key, data);
        }
    }

    static class Edge implements Comparable<Edge> {
        private final double weight;
        public Edge() { this(1.0f); }

        public Edge(double weight) { this.weight = weight; }

        public double getWeight() { return weight; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Edge e)) return false;
            return weight == e.weight;
        }

        @Override
        public int compareTo(Edge e) {
            return Double.compare(weight, e.weight);
        }

        @Override
        public int hashCode() {
            return Objects.hash(weight);
        }

        public String toString() {
            return String.format("Weight: %s", weight);
        }
    }
}

