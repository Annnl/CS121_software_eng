import java.util.*;

public class ListGraph implements Graph {
    private HashMap<String, LinkedList<String>> nodes = new HashMap<>();

    public boolean addNode(String n) {
        if (nodes.containsKey(n)){
            return false;
        }else{
            nodes.put(n, new LinkedList<>());
            return true;
        }
    }

    public boolean addEdge(String n1, String n2) {
        if (!nodes.containsKey(n1) || !nodes.containsKey(n2)) {
            throw new NoSuchElementException();
        }
	    LinkedList<String> n1_edge = nodes.get(n1);
        if (n1_edge.contains(n2)){
            return false;
        }else{
            n1_edge.add(n2);
            return true;
        }
    }

    public boolean hasNode(String n) {
	    if (nodes.containsKey(n)){
            return true;
        }else{
            return false;
        }
    }

    public boolean hasEdge(String n1, String n2) {
        if (!nodes.containsKey(n1) || !nodes.containsKey(n2)) {
            return false;
        }
	    LinkedList<String> n1_edge = nodes.get(n1);
        if (n1_edge.contains(n2)){
            return true;
        }else{
            return false;
        }
    }

    public boolean removeNode(String n) {
	    if (nodes.containsKey(n)){
            nodes.remove(n);
            for (String node : nodes.keySet()) { // iterate over key of HashMap
                LinkedList<String> s = nodes.get(node); // get corresponding successor lists
                s.remove(n);
            }
            return true;
        }else{
            return false;
        }
    }

    public boolean removeEdge(String n1, String n2) {
	    if (!nodes.containsKey(n1) || !nodes.containsKey(n2)) {
            throw new NoSuchElementException();
        }
        LinkedList<String> n1_edge = nodes.get(n1);
        return n1_edge.remove(n2);
    }

    public List<String> nodes() {
	    LinkedList<String> result = new LinkedList<>();
        for (String node : nodes.keySet()) { // iterate over key of HashMap
            result.add(node);
        }
        return result;
    }

    public List<String> succ(String n) {
	    if (!nodes.containsKey(n)) {
            throw new NoSuchElementException();
        }
        return nodes.get(n);
    }

    public List<String> pred(String n) {
	    if (!nodes.containsKey(n)) {
            throw new NoSuchElementException();
        }
        LinkedList<String> result = new LinkedList<>();
        for (String node : nodes.keySet()) { // iterate over key of HashMap
            if (nodes.get(node).contains(n)){
                result.add(node);
            }
        }
        return result;
    }

    public Graph union(Graph g) {
	    Graph result = new ListGraph();
        for (String node : g.nodes()) { // iterate over key of HashMap
            result.addNode(node);
            for (String n : g.succ(node)) { // iterate through elements of nodes.get("a")
                result.addNode(n);
                result.addEdge(node, n);
            }
        }
        for (String node : nodes.keySet()) { // iterate over key of HashMap
            result.addNode(node);
            for (String n : nodes.get(node)) { // iterate through elements of nodes.get("a")
                result.addNode(n);
                result.addEdge(node, n);
            }
        }
        return result;
    }

    public Graph subGraph(Set<String> candidates) {
	    Graph result = new ListGraph();
        for (String node : nodes.keySet()) { // iterate over key of HashMap
            if (candidates.contains(node)){
                result.addNode(node);
                for (String succ : nodes.get(node)){
                    if (candidates.contains(succ)){
                        result.addNode(succ);
                        result.addEdge(node, succ);
                    }
                }
            }
        }
        return result;
    }

    public boolean connected(String n1, String n2) {
	    if (!nodes.containsKey(n1) || !nodes.containsKey(n2)) {
            throw new NoSuchElementException();
        }
        if (n1.equals(n2)){
            return true;
        }
        LinkedList<String> visited = new LinkedList<>();
        LinkedList<String> next = new LinkedList<>();
        next.add(n1);
        while(!next.isEmpty()){
            LinkedList<String> list = new LinkedList<>(next);
            for (String node : list){
                if (node == n2){
                    return true;
                }
                visited.add(node);
                next.remove(node);
                for (String mid : nodes.get(node)){
                    if (!visited.contains(mid)){
                        next.add(mid);
                    }
                }
            }
        }
        return false;
    }
}
