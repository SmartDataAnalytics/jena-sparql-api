package org.aksw.jena_sparql_api.sparql_path.core;



// TODO: Maybe define a path as a sequence of edges?
//
//class Step<T> {
//    private T from;
//    private T to;
//    //private T node;
//    private boolean direction;
//    
//    public Step(T node, boolean direction) {
//        this.node = node;
//        this.direction = direction;
//    }
//
//    public T getNode() {
//        return node;
//    }
//
//    public boolean isDirection() {
//        return direction;
//    }
//}
//
//
///**
// * A path is a (nested) sequence of steps.
// * 
// * 
// * @author raven
// *
// * @param <T>
// */
//class Path<T> {
//    private Path<T> parent;
//    private T step;
//    
//    public Path(Path<T> parent, T step) {
//        this.parent = parent;
//        this.step = step;
//    }
//    
//    public Path<T> getParent() {
//        return parent;
//    }
//
//    public T getStep() {
//        return step;
//    }
//}
//
//// TODO Change to a predicate whether the path could still be part of a solution
//class PathToDirChanges<T>
//    implements Function<Iterable<Edge<T>>, Integer>
//{
//    @Override
//    public Integer apply(Iterable<Edge<T>> path) {
//        
//        int result = 0;
//        Integer priorDirection = null; 
//        for(Edge<T> edge : path) {
//            int direction = current.getStep().getDirection();
//            if(priorDirection != null) {
//                if(priorDirection != direction) {
//                    ++result;
//                }
//            }
//            
//            priorDirection = direction;            
//        }
//        
//        
//        return result;
//    }   
//}
//
//
//
///**
// * A frontier is a set of *to-be* traversed paths of a wave
// * I.e. before advancing a frontier, it is checked for whether it
// * contains (non-)solution paths
// * 
// * @author raven
// *
// * @param <T>
// */

//
//
///**
// * 
// * 
// * 
// * @author raven
// *
// * @param <T>
// */
//class BfsTask<T> {
//
//    //private Map<> pairToPaths;
//    
//    private PathFinderConfig config;
//    
//    
//    private NeighborProvider<T> neigborProvider;
//    
//    // A map from the nodes by which frontiers it has been visited
//    private Map<T, Map<Integer, Frontier<T>>> nodeToWaves;
//    
//    
//    // Each wave has a set of frontiers (e.g. one frontier going backwards, and the other forwards)
//    private List<Wave<T>> waves;
//    
//    public BfsTask(PathFinderConfig config) {
//        this.config = config;
//    }
//    
//    
//    public int getDirChanges(Path<Step<T>> path) {
//        int result = 0;
//        if(path.)
//    }
//    
//    public Frontier<Step<T>> advanceFrontier(Frontier<Step<T>> frontier, boolean forward) {
//        List<Path<Step<T>>> paths = frontier.getPaths();
//        
//        
//        List<Path<Step<T>>> newPaths = new ArrayList<Path<Step<T>>>();
//        for(Path<Step<T>> path : paths) {
//            Step<T> step = path.getStep();
//            
//            T node = step.getNode();
//                        
//            Set<T> successors = neigborProvider.getSuccessors(node);
//            
//            for(T succ : successors) {
//            
//                Path<T> newPath = new Path<T>(path, succ);
//                newPaths.add(newPath);
//            }
//            
//            
//        }
//        Frontier<T> result = new Frontier<T>(newPaths);
//        
//        return result;
//    }
//    
//    public void run() {
//        // Pick the wave with the smallest frontier
//        List<Wave<T>> sortedWaves = new ArrayList<Wave<T>>(waves);
//        
//        Collections.sort(sortedWaves, new Comparator<Wave<T>>() {
//            @Override
//            public int compare(Wave<T> a, Wave<T> b) {
//                int x = a.getFrontierSize();
//                int y = b.getFrontierSize();
//                int result = y - x;
//                return result;
//            }
//        });
//        
//        Wave<T> wave = sortedWaves.iterator().next();
//        
//
//        // Check the frontier of the wave whether it reaches the target        
//        List<Frontier<T>> frontiers = wave.getFrontiers();
//        
//        for(Frontier<T> frontier : frontiers) {
//           List<Path<T>> paths = frontier.getPaths();
//           
//           T head = path.getHead();
//           
//           if(target.equals(head)) {
//               
//           }
//           
//           // Check if the frontier reached a node from which a path to the target exists
//           nodeToWaves.get(head);           
//        }
//        
//
//        // Create the next frontier for the wave
//        
//        
//        
//            List<Frontier<T>> nextFrontiers = new ArrayList<Frontier<T>>();
//            
//            for(Frontier<T> frontier : frontiers) {
//                Frontier<T> outF = advanceFrontier(frontier, true);
//                
//                
//                // Map the nodes to the paths
//                for(Path<T> path : outF.getPaths()) {
//                    Step<T> step = path.getStep();
//                    T node = step.getNode();
//                    
//                    nodeToPaths.put(node, path);
//                }
//                
//                
//                
//                
//                // Check if any of the nodes of the frontier overlap with nodes of another wave
//                for(T node : frontier.getNodes()) {
//                    Map<Integer, Frontier<T>> waves = nodeToWaves.get(node);
//                    
//                    
//                }
//            }
//            
//        }
//    }
//    
//    
//    
//
//    public static <T> void create(NeighborProvider<T> np, T start, T dest) {
//        //Frontier<T> startFrontier = new Frontier<T>(null, start);
//        //Frontier<T> destFrontier = new Frontier<T>(null, start);
//        
//        Wave<T> startWave = Wave.create(start);
//        Wave<T> destWave = Wave.create(dest);
//        
//        List<Wave<T>> waves = Arrays.asList(startWave, destWave);
//        
//        
//    }
//}
//
//
//
///**
// * There is a callback for getting notified about found paths.
// * 
// * @author raven
// *
// */
//public class BreathFirstTask {
//	private Model model;
//	
//	private Node a;
//	private Node b;
//	
//	private Set<Resource> sourceFront;
//	private Set<Resource> targetFront;
//	
//	private Function<Void, Void> callback;
//	
//	public BreathFirstTask()
//	{
//	}
//
//	public static ExtendedIterator<Resource> createForwardIterator(Model model, Resource start) {
//		// For the current resource, get all possible outgoing paths
//		ExtendedIterator<Statement> itTmp = model.listStatements(start, VocabPath.joinsWith, (RDFNode)null);
//		ExtendedIterator<Resource> result = itTmp.mapWith(new Map1StatementToObject());
//		
//		return result;
//	}
//
//
//	public static ExtendedIterator<Resource> createBackwardIterator(Model model, Resource start) {
//		// For the current resource, get all possible outgoing paths
//		ExtendedIterator<Statement> itTmp = model.listStatements(null, VocabPath.joinsWith, start);
//		ExtendedIterator<Resource> result = itTmp.mapWith(new Map1StatementToObject());
//		
//		return result;
//	}
//	
//	public static int maxPaths = 100;
//	public static int maxDepth = 7;
//	
//	public static void run(NeighborProvider<Resource> np, Resource start, Resource dest, List<Step> steps, PathCallback callback) {
//		
//	    //System.out.println(steps);
//		if(start.equals(dest)) {
//			// emit empty path
//			callback.handle(new Path(steps));
//			return;
//		}
//
//		if(steps.size() > maxDepth) {
//			return;
//		}
//
//		// Note: There is 2x2 possibilities per step:
//		// .) we move forward from the source / backward from the dest
//		// .) we move backward from the source / forward to the dest
//		
//		
//		// The decision on whether to start from the front or the back can depend on which node leads to
//		// fewer options
//		Set<Resource> succs = np.getSuccessors(start).toSet();
//		for(Resource succ : succs) {
//			List<Step> tmp = new ArrayList<Step>(steps);
//			
//			Step s = new Step(succ.getURI(), false);
//			tmp.add(s);
//			
//			if(tmp.size() >= maxPaths) {
//			    break;
//			}
//			
//			run(np, succ, dest, tmp, callback);
//		}
//
//	}
//
//
//	public static void runFoo(NeighborProvider<Resource> np, Resource start, Resource dest, List<Step> startSteps, List<Step> destSteps, PathCallback callback) {
//		
//		List<Step> steps = null;
//		
//		if(start.equals(dest)) {
//			// emit empty path
//			callback.handle(new Path(steps));
//		}
//
//		if(startSteps.size() + destSteps.size() > 10) {
//			return;
//		}
//
//		// Note: There is 2x2 possibilities per step:
//		// .) we move forward from the source / backward from the dest
//		// .) we move backward from the source / forward to the dest
//		
//		
//		// The decision on whether to start from the front or the back can depend on which node leads to
//		// fewer options
//		Set<Resource> succs = np.getSuccessors(start).toSet();
//		Set<Resource> preds = np.getPredecessors(dest).toSet();
//
//		
//		if(preds.size() < succs.size()) {
//			
//			
//			
//		}
//		
//		// NOTE: We could now take the smaller set to make another step
//
//		for(Resource succ : succs) {
//			List<Step> tmp = new ArrayList<Step>(steps);
//			
//			Step s = new Step(succ.getURI(), false);
//			tmp.add(s);
//
//			/*
//			if(succ.equals(dest)) {
//				callback.handle(new Path(new ArrayList<Step>(tmp)));
//			}*/
//			
//			//run(np, succ, dest, tmp, callback);
//		}
//		
//		
//		for(Resource pred : preds) {
//			
//		}
//		
//	}
//	
//	
//	
//
//	/*
//	public static isSolution() {
//		
//	}
//	*/
//	
//	public static DataSource createDb() throws IOException, SQLException {
//		DataSource ds = null;// SparqlifyUtils.createDefaultDatabase("paths");
//
//		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//		org.springframework.core.io.Resource r = resolver.getResource("paths.sql");
//		
//		InputStream in = r.getInputStream();
//		String str = StreamUtils.toStringSafe(in);
//		
//		Connection conn = ds.getConnection();
//		try {
//			conn.createStatement().executeUpdate(str);
//		}
//		finally {
//			conn.close();
//		}
//		
//		return ds;
//	}
//	
//	public static void doSomething(Model model, Resource start, Resource end) {
//
//		Set<Resource> visited = new HashSet<Resource>();
//		
//		Resource current = null;		
//		visited.add(current);
//
//		}
//		
//		
////		// Go forward and backward from the current concept
////		// The take step function checks 
////		takeStep(a, false);
////		takeStep(a, true);
////
////		takeStep(b, false);
////		takeStep(b, true);
////
////		
////		
////		
////		while(it.hasNext()) {
////			Resource node = it.next();
////		}
//		
////	}
//}
//
