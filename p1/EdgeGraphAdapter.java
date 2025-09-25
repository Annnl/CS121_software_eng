import java.util.*;

public class EdgeGraphAdapter implements EdgeGraph {

    private Graph g;

    public EdgeGraphAdapter(Graph g) { this.g = g; }

    public boolean addEdge(Edge e) {
      g.addNode(e.getSrc());
      g.addNode(e.getDst());
      return g.addEdge(e.getSrc(), e.getDst());
    }

    public boolean hasNode(String n) {
	    return g.hasNode(n);
    }

    public boolean hasEdge(Edge e) {
	    return g.hasEdge(e.getSrc(), e.getDst());
    }

    public boolean removeEdge(Edge e) {
      boolean flag = g.removeEdge(e.getSrc(), e.getDst());
      if (flag){
        if (g.succ(e.getSrc()).isEmpty() && g.pred(e.getSrc()).isEmpty()){
        g.removeNode(e.getSrc());
        }
        if (g.succ(e.getDst()).isEmpty() && g.pred(e.getDst()).isEmpty()){
          g.removeNode(e.getDst());
        }
      }
      
	    return flag;
    }

    public List<Edge> outEdges(String n) {
      LinkedList<Edge> result = new LinkedList<>();
      for (String node : g.succ(n)){
        Edge edge = new Edge(n, node);
        result.add(edge);
      }
      return result;
    }

    public List<Edge> inEdges(String n) {
      LinkedList<Edge> result = new LinkedList<>();
      for (String node : g.pred(n)){
        Edge edge = new Edge(node, n);
        result.add(edge);
      }
      return result;
    }

    public List<Edge> edges() {
      LinkedList<Edge> result = new LinkedList<>();
      for (String start_node : g.nodes()){
        for (String end_node : g.succ(start_node)){
          result.add(new Edge(start_node, end_node));
        }
      }
      return result;
    }

    public EdgeGraph union(EdgeGraph new_g) {
      Graph graph = new ListGraph();
      for (Edge edge : new_g.edges()){
        graph.addNode(edge.getDst());
        graph.addNode(edge.getSrc());
        graph.addEdge(edge.getSrc(), edge.getDst());
      }
      for (String node : g.nodes()){
        graph.addNode(node);
        for (String ending : g.succ(node)){
          graph.addEdge(node, ending);
        }
      }
      return new EdgeGraphAdapter(graph);
    }

    public boolean hasPath(List<Edge> e) {
	    for (Edge edge : e){
        if (!g.hasEdge(edge.getSrc(), edge.getDst())){
          return false;
        }
      }
      for (int i=0; i < e.size()-1;i++){
        if (e.get(i).getDst() != e.get(i+1).getSrc()){
          throw new BadPath();
        }
      }
      return true;
    }

}
