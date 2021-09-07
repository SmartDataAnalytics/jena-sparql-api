package org.aksw.jena_sparql_api.path.relgen;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.concepts.UnaryXExpr;
import org.aksw.jena_sparql_api.path.core.PathOpsPE;
import org.aksw.jena_sparql_api.path.core.PathPE;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

/** */
public abstract class RelationGeneratorBase
//    implements Trav1Provider<Node, RelationBuilder>
{

    /** Relations that have been traversed by the path -
     *  does not include the current relation */
    protected List<Relation> pastRelations = new ArrayList<>();

    /** The current relation */
    protected Relation relation;

    // A hash updated upon requesting a new relation; hash is based on the seen path segments
    // Note the hash is really per relation and not per column.
    // The column names are appended to the hash
    protected HashCode contextHash;

    /** Cached String version of the hash */
    protected String contextHashStr;

    /** Conditions imposed the current relation based on the seen path segments */
    protected List<Expr> conditions = new ArrayList<>();


    /** The absolute path at which the relation was requested
     * Once it covers all columns of the relation it is used to compute the
     * next context hash
     */
    protected PathPE relationStartAbsPath;


    /**
     * The relative path of segments seen for the current relation
     * Connects to relationStartPath
     */
    protected PathPE relPath;

    /** The path segments seen for the current relation. */
    // protected List<Node> segments = new ArrayList<>();

    int columnIdx = 0;




    /**
     * Yield the next relation to traverse
     *
     * @param path
     * @param index
     * @return
     */
    protected abstract Relation nextInstance();


    public RelationGeneratorBase() {
        super();
        reset();
    }

    public Relation process(PathPE path) {
        if (path.isAbsolute()) {
            reset();
        }

        ensureInit();


        Relation result = relation;

        for (UnaryXExpr segment : path.getSegments()) {
            result = process(segment);
        }

        return result;
    }

    protected void reset() {
        setHashCode(null);
        pastRelations.clear();
        relation = null;
        columnIdx = 0;
        relationStartAbsPath = PathOpsPE.newAbsolutePath();
        relPath = PathOpsPE.newRelativePath();
        updateHash();
    }

    public void ensureInit() {
        if (relation == null || columnIdx >= relation.getVars().size()) {

            String oldHash = contextHashStr;
            updateHash();

            Var pastLastVar = null;

            if (relation != null) {
                Relation pastItem = relation.filter(conditions);
                pastLastVar = pastItem.getVars().get(pastItem.getVars().size() - 1);
                pastRelations.add(pastItem);
            }

            relation = nextInstance();

            relationStartAbsPath = relationStartAbsPath.resolve(relPath);
            relPath = PathOpsPE.newRelativePath();

            List<Var> vars = relation.getVars();
            if (vars.size() <= 1) {
                throw new RuntimeException("Relations must have at least 2 variables");
            }

            conditions.clear();

            // Rename variables w.r.t the hashes:
            // The first var receives the prior hash (in order to join with the prior relation)
            // all other vars receive the new hash

            Var firstVar = vars.get(0);

            if (pastLastVar == null) {
                pastLastVar = Var.alloc(contextHashStr + "_" + firstVar.getName());
            }

            Var plv = pastLastVar;

            Map<Var, Node> remap = relation.getVarsMentioned().stream()
                    .collect(Collectors.toMap(
                            v -> v,
                            node -> {
                                Node r;
                                if (node.isVariable()) {
                                    if (node.equals(firstVar)) {
                                        r = plv;
                                    } else {
//	                                    String prefix = node.equals(firstVar)
//	                                            ? oldHash
//	                                            : contextHashStr;
                                        r = Var.alloc(contextHashStr + "_" + node.getName());
                                    }
                                } else {
                                    r = node;
                                }
                                return r;
                            }));

            relation = relation.applyNodeTransform(v -> remap.getOrDefault(v, v));

            // If we joined the last column of the previous relation with the first
            // column of the next one, then jump over that first column
            columnIdx = pastRelations.isEmpty() ? 0 : 1;
        }
    }


    public Relation process(UnaryXExpr segment) {

        ensureInit();

        relPath = relPath.resolve(segment);


        List<Var> vars = relation.getVars();
        Var v = vars.get(columnIdx);
        ++columnIdx;

        if (!segment.isAlwaysTrue()) {

            // Substitute the only variable in the expression with that of the relation instance
            Expr expr = segment.getExpr().applyNodeTransform(x -> x.isVariable() ? v : x);


            conditions.add(expr);
        }

        Relation r = relation.filter(conditions);

        ensureInit();

        return r;
    }


    public Var getCurrentVar() {
        return relation.getVars().get(columnIdx);
    }

    public List<Relation> getPastRelations() {
        return pastRelations;
    }


    public UnaryRelation getCurrentConcept() {
        return new Concept(assemble(), getCurrentVar());
    }

    /** Assemble the complete element */
    public Element assemble() {
        List<Element> elts = pastRelations.stream()
                .flatMap(r -> r.getElements().stream())
                .collect(Collectors.toList());

        if (relation != null) {
            elts.add(relation.filter(conditions).getElement());
        }

        Element elt = ElementUtils.groupIfNeeded(elts);
        return elt;
    }


    protected void updateHash() {
        HashCode nextHashCode = computeNextHash(contextHash, relationStartAbsPath, relPath);
        setHashCode(nextHashCode);
    }

    protected void setHashCode(HashCode hashCode) {
        contextHash = hashCode;
        contextHashStr = hashCode == null ? null : encodeHashCode(hashCode);
    }

    protected String encodeHashCode(HashCode hashCode) {
        return hashCode.toString(); // BaseEncoding.base64Url().encode(contextHash.asBytes());
    }

    protected HashCode computeNextHash(HashCode currentHash, PathPE relationStartAbsPath, PathPE relPath) {
        HashCode contrib = Hashing.murmur3_32().hashString(relPath.toString(), StandardCharsets.UTF_8);

        HashCode result = currentHash == null ? contrib : Hashing.combineOrdered(Arrays.asList(currentHash, contrib));
        return result;
    }

}
