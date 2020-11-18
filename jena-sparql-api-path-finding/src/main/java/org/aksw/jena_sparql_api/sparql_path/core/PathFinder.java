package org.aksw.jena_sparql_api.sparql_path.core;


//
//interface Nested<T> {
//    Nested<T> getParent();
//    T getValue();
//}
//
//class NestedInteratorImpl<T>
//    extends AbstractIterator<T>
//{
//    private Nested<T> current;
//
//    public NestedInteratorImpl(Nested<T> current) {
//        this.current = current;
//    }
//    
//    @Override
//    protected T computeNext() {
//        T result;
//        if(current == null) {
//            result = null;
//        }
//        else {
//            result = current.getValue();        
//            current = current.getParent();
//        }
//        
//        return result;
//    }
//}
//
//class Path<V, E>
//    implements Iterable<E>, Nested<E>
//{
//    private Path<V, E> parent;
//    private V targetVertex;
//    private E step;
//
//    public Path(Path<E> parent, E step) {
//        this.parent = parent;
//        this.step = step;
//    }
//
//    public Path<E> getParent() {
//        return parent;
//    }
//
//    public E getValue() {
//        return step;
//    }
//
//    @Override
//    public Iterator<E> iterator() {
//        return new NestedInteratorImpl<E>(this);
//    }
//}
//
//class Frontier<V, E> {
//    private List<GraphPath<V, E>> paths;
//    
//    public Frontier(List<GraphPath<V, E>> paths) {
//        this.paths = paths;
//    }
//    
//    public List<GraphPath<V, E>> getPaths() {
//        return paths;
//    }
//}
//
//
///**
// * A wave is essentially a breath first search starting from a specific vertex
// * 
// * @author raven
// *
// * @param <V>
// * @param <E>
// */
//class Wave<V, E> {
//    private V startVertex;
//    
//    private List<Frontier<V, E>> frontiers;
//    
//    public Wave(List<Frontier<V, E>> frontiers) {
//        this.frontiers = frontiers;
//    }
//
//    public V getStartVertex() {
//        return startVertex;
//    }
//   
//    public List<Frontier<V, E>> getFrontiers() {
//        return frontiers;
//    }
//    
//    public int getFrontierSize() {
//        int result = 0;
//
//        for(Frontier<V, E> frontier : frontiers) {
//            result += frontiers.size();
//        }
//        
//        return result;
//    }
//    
//    public static <V, E> Wave<V, E> create(V vertex) {
//        GraphPath<V, E> path = new GraphPathImpl<V, E>();
//        
//        Frontier<V, E> frontier = new Frontier<V, E>(Collections.singletonList(path));
//        Wave<V, E> result = new Wave<V, E>(Collections.singletonList(frontier));
//        
//        return result;
//    }
//}
//
//
//public class PathFinder<V, E> {
//    private V start;
//    private V end;
//    
//    private Graph<V, E> graph;
//    
//    // Maps a vertex to all paths by which it has been visited
//    private Map<V, Multimap<Integer, GraphPath<V, E>>> vertexToWaveToPaths = new HashMap<V, Multimap<Integer, GraphPath<V, E>>>();
// 
//    
//    //private List<Wave<>> waves;
//    
//    public PathFinder(Graph<V, E> graph) {
//       this.graph = graph;
//    }
//    
//    
//    public void doStep() {
//      // Pick the wave with the smallest frontier
//      List<Wave<V, E>> sortedWaves = new ArrayList<Wave<V, E>>(waves);
//      
//      Collections.sort(sortedWaves, new Comparator<Wave<V, E>>() {
//          @Override
//          public int compare(Wave<V, E> a, Wave<V, E> b) {
//              int x = a.getFrontierSize();
//              int y = b.getFrontierSize();
//              int result = y - x;
//              return result;
//          }
//      });
//      
//      Wave<V, E> wave = sortedWaves.iterator().next();
//      
//      int targetWave;
//      V targetVertex;
//
//      // Check the frontier of the wave whether it reaches the target        
//      List<Frontier<T>> frontiers = wave.getFrontiers();
//      
//      for(Frontier<V, E> frontier : frontiers) {
//         List<GraphPath<V, E>> paths = frontier.getPaths();
//         
//         for(GraphPath<V, E> path : paths) {
//             V headVertex = path.getEndVertex();
//             
//         
//             if(targetVertex.equals(headVertex)) {
//                 
//             }
//         
//             // Check if the frontier reached a node from which a path to the target exists
//             Multimap<Integer, GraphPath<V, E>> waveToPaths = vertexToWaveToPaths.get(headVertex);
//             
//             Collection<GraphPath<V, E>> paths = waveToPaths.get(targetWave);
//             
//             
//      }
//      
//
//      // Create the next frontier for the wave
//      
//      
//      
//          List<Frontier<T>> nextFrontiers = new ArrayList<Frontier<T>>();
//          
//          for(Frontier<T> frontier : frontiers) {
//              Frontier<T> outF = advanceFrontier(frontier, true);
//
//        
//        
//    }
//    
//    
//}
