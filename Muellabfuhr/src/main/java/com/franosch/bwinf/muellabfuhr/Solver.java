package com.franosch.bwinf.muellabfuhr;

import com.franosch.bwinf.muellabfuhr.io.FileReader;
import com.franosch.bwinf.muellabfuhr.model.*;

import java.util.*;

public class Solver {
    private final Graph graph;

    public Solver() {
        this.graph = new Graph();
    }

    public void initGraph(FileReader fileReader) {
        graph.initGraph(fileReader);
    }

    public void makeEven() {
        System.out.println(graph);
        Set<Node> odds = findOddDegree();
        System.out.println("odds " + odds);
        if (odds.size() != 0) {
            Graph completeGraph = completeGraph(odds);
            System.out.println("graph completed");
            Set<Edge> min = findMinimalPerfectMatching(completeGraph);
            System.out.println("min" + min);
            insert(min);
            System.out.println(graph);
        }
        List<Circle> cpp = eulerPath(graph);
        System.out.println(cpp);
        System.out.println(sum(cpp) / 5);

        allocate(5, cpp);
    }


    private List<Circle> eulerPath(Graph graph) {
        List<Edge> open = new ArrayList<>(graph.getEdges());
        Node root = graph.getNodes().get(0);
        Node start;
        List<Circle> circles = new ArrayList<>();
        while (!open.isEmpty()) {
            start = findStart(circles, open);
            if (start == null) start = root;
            Node currentNode = start;
            List<Circle> circle = findCircle(currentNode, start, open);
            circles.addAll(circle);
        }
        int i = 0;
        for (Circle circle : circles) {
            System.out.println("Circle " + i + ": " + circle);
            i++;
        }
        return circles;
    }

    private List<Circle> findCircle(Node current, Node start, List<Edge> open) {
        List<Circle> out = new ArrayList<>();
        Edge edge;
        List<Edge> currentPath = new ArrayList<>();
        do {
            System.out.println(current.getId() + " " + start.getId());
            if (!current.equals(start) && isSubCircle(current, currentPath)) {
                Circle subCircle = getSubCircle(current, currentPath);
                System.out.println("sub circle " + subCircle);
                currentPath.removeAll(subCircle.edges());
                out.add(subCircle);
            }
            edge = getEdgeFromNodeAndOpen(current, open);
            current = edge.getEnd(current);
            currentPath.add(edge);
            open.remove(edge);
        }
        while ((!isCircle(start, currentPath) && !open.isEmpty()));
        Circle circle = new Circle(currentPath, currentPath.stream().mapToDouble(value -> value.getPath().getWeight()).sum());
        System.out.println("normal circle " + circle);
        out.add(circle);
        return out;
    }

    private boolean isSubCircle(Node current, List<Edge> path) {
        if (path.size() < 2) return false;
        List<Edge> list = new ArrayList<>(path);
        Collections.reverse(list);
        Node copy = current;
        for (Edge edge : list) {
            copy = edge.getEnd(copy);
            if (copy.equals(current)) return true;
        }
        return false;
    }

    private Circle getSubCircle(Node current, List<Edge> path) {
        List<Edge> list = new ArrayList<>(path);
        Collections.reverse(list);
        Node node = current;
        int i = 0;
        for (Edge e : list) {
            node = e.getEnd(node);
            if (node.equals(current)) {
                break;
            }
            i++;
        }
        List<Edge> copy = new ArrayList<>(path);
        List<Edge> out = copy.subList(copy.size() - i - 1, copy.size());
        Circle circle = new Circle(out, out.stream().mapToDouble(value -> value.getPath().getWeight()).sum());
        System.out.println(circle);
        return circle;
    }


    private void allocate(int k, List<Circle> circles) {
        Map<Integer, List<Circle>> runner = new HashMap<>();
        for (int i = 0; i < k; i++) {
            runner.put(i, new ArrayList<>());
        }
        for (Circle circle : circles) {
            List<Circle> lowest = getLowest(runner);
            lowest.add(circle);
        }
        for (Integer integer : runner.keySet()) {
            List<Circle> current = runner.get(integer);
            System.out.println("Runner " + integer + ": weight " + sum(current) + " circles " + current);
        }
    }

    private List<Circle> getLowest(Map<Integer, List<Circle>> runner) {
        List<Circle> low = null;
        double weight = Double.MAX_VALUE;
        for (List<Circle> value : runner.values()) {
            double currentWeight = sum(value);
            if (currentWeight < weight) {
                low = value;
                weight = currentWeight;
            }
        }
        return low;
    }

    private double sum(List<Circle> circles) {
        double weight = 0;
        for (Circle circle : circles) {
            weight += circle.weight();
        }
        return weight;
    }

    private Node findStart(List<Circle> circles, List<Edge> open) {
        for (Circle circle : circles) {
            for (Edge edge : circle.edges()) {
                Edge e = getEdgeFromNodeAndOpen(edge.getPath().getFrom(), open);
                if (e != null) return edge.getPath().getFrom();
                e = getEdgeFromNodeAndOpen(edge.getPath().getTo(), open);
                if (e != null) return edge.getPath().getTo();
            }
        }
        return null;
    }

    private boolean isCircle(Node start, List<Edge> edges) {
        Node current = start;
        for (Edge edge : edges) {
            current = edge.getEnd(current);
        }
        return current.equals(start);
    }

    private Edge getEdgeFromNodeAndOpen(Node current, List<Edge> open) {
        for (Edge edge : current.getEdges()) {
            if (open.contains(edge)) return edge;
        }
        return null;
    }

    private void insert(Set<Edge> min) {
        for (Edge edge : min) {
            Node a = edge.getPath().getFrom();
            Node b = edge.getPath().getTo();
            Node actualA = graph.getNodes().get(a.getId());
            Node actualB = graph.getNodes().get(b.getId());
            graph.connect(actualA, actualB, edge, false);
        }
    }

    private Graph completeGraph(Set<Node> odds) {
        Node root = odds.stream().findAny().orElse(null);
        Graph complete = new Graph(root);
        for (Node odd : odds) {
            Node node = new Node(odd.getId());
            complete.insert(node);
        }
        Set<Node> inners = new HashSet<>(odds);
        for (Node outer : odds) {
            inners.remove(outer);
            this.graph.generateShortestPaths(outer);
            for (Node inner : inners) {
                Path path = this.graph.getShortestPath(inner);
                Edge edge = Edge.create(path, path.getWeight());
                // System.out.println("connecting " + edge);
                complete.connect(outer.getId(), inner.getId(), edge);
            }
        }
        return complete;
    }


    private Set<Edge> findMinimalPerfectMatching(Graph graph) {
        Node root = graph.getNodes().get(graph.getRoot().getId());
        Node current = root;
        Set<Integer> closed = new HashSet<>();
        List<Edge> path = new ArrayList<>();
        for (int i = 0; i < graph.getNodes().keySet().size() - 1; i++) {
            closed.add(current.getId());
            Set<Edge> open = getEdgesExcept(current.getId(), closed, graph.getNodes());
            Edge smallest = getSmallestNeighbour(open);
            path.add(smallest);
            current = smallest.getEnd(current);
        }
        Node finalCurrent = current;
        Edge endToRoot = root.getEdges().stream().filter(edge -> {
            Node end = edge.getEnd(root);
            Node node = graph.getNodes().get(end.getId());
            if (node == null) return false;
            return node.getId() == finalCurrent.getId();
        }).findAny().get();
        path.add(endToRoot);

        Set<Edge> out = pathToMinimalMatching(path);
        return out;
    }

    /**
     * Trivialer Algorithmus, abwechselnd in A und B einteilen, kleineres Matching wählen
     *
     * @param path
     * @return matching
     */
    private Set<Edge> pathToMinimalMatching(List<Edge> path) {
        Set<Edge> matchingA = new HashSet<>();
        Set<Edge> matchingB = new HashSet<>();
        int i = 0;
        for (Edge edge : path) {
            if (i == 0) {
                matchingA.add(edge);
                i++;
                continue;
            }
            matchingB.add(edge);
            i--;
        }
        double sumA = matchingA.stream().mapToDouble(value -> value.getPath().getWeight()).sum();
        double sumB = matchingB.stream().mapToDouble(value -> value.getPath().getWeight()).sum();
        if (sumA < sumB) return matchingA;
        return matchingB;
    }

    private Set<Edge> getEdgesExcept(int current, Set<Integer> closed, Map<Integer, Node> map) {
        Set<Edge> edges = new HashSet<>();
        Node node = map.get(current);

        for (Edge edge : node.getEdges()) {
            Node target = edge.getEnd(node);
            target = map.get(target.getId());
            if (target == null) continue;
            if (closed.contains(target.getId())) continue;
            edges.add(edge);
        }

        return edges;
    }

    private Edge getSmallestNeighbour(Set<Edge> edges) {
        double weight = Double.MAX_VALUE;
        Edge current = null;

        for (Edge neighbor : edges) {
            if (neighbor.getPath().getWeight() < weight) {
                current = neighbor;
                weight = neighbor.getPath().getWeight();
            }
        }
        return current;
    }


    public Set<Node> findOddDegree() {
        Set<Node> output = new HashSet<>();
        for (Node value : graph.getNodes().values()) {
            if (value.getDegree() % 2 == 0) continue;
            output.add(value);
        }
        return output;
    }
}
