package org.aksw.jena_sparql_api.rdf.collections;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Create a modifiable {@link List} view over an {@link RDFList}
 *
 *
 * @author Claus Stadler, Jan 18, 2019
 *
 */
public class ListFromRDFList
    extends AbstractList<RDFNode>
{
    protected Resource s;
    protected Property p;
    protected boolean isFwd;


    protected RDFList getList() {
        // Pick any resource and treat it as a list
        // Also, clear all any other value for consistency
        Resource o = ResourceUtils.getPropertyValue(s, p, isFwd, Resource.class);
        if(o == null) {
            o = RDF.nil.inModel(s.getModel());
        }
        ResourceUtils.setProperty(s, p, isFwd, o);

        return o.as(RDFList.class);
    }

    public ListFromRDFList(Resource subject, Property property) {
        this(subject, property, true);
    }

    public ListFromRDFList(Resource subject, Property property, boolean isFwd) {
        super();
        this.s = subject;
        this.p = property;
        this.isFwd = isFwd;
    }

    @Override
    public boolean contains(Object o) {
        boolean result = o instanceof RDFNode
                ? getList().contains((RDFNode)o)
                : false;
        return result;
    }

    @Override
    public boolean add(RDFNode e) {
        RDFList newList = getList().with(e);
        ResourceUtils.setProperty(s, p, newList);
        //System.out.println("list prop: " + s.getProperty(p));
        return true;
    }

    @Override
    public void add(int index, RDFNode element) {
        RDFList list = getList();
        RDFList pos = findParent(list, index);

        if(pos == null) {
            RDFList newPos = list.cons(element);
            ResourceUtils.setProperty(s, p, newPos);
        } else {
            RDFList remainder = pos.cons(element);
            remainder.setTail(pos.getTail());
            pos.setTail(remainder);

        }
    }


    public static RDFList getParent(RDFList child) {
        // TODO Somehow replace RDF.rest with the property referred to by the list implementation
        RDFNode _parent = ResourceUtils.getReversePropertyValue(child, RDF.rest);

        RDFList result = _parent == null ? null : _parent.as(RDFList.class);
        return result;
    }

    public static void setTail(RDFList parent, RDFList element, Resource s, Property p) {
        if(parent == null) {
            ResourceUtils.setProperty(s, p, element);
        } else {
            parent.setTail(element);
        }
    }

    public static void linkParentTo(RDFList element, Resource s, Property p) {
        RDFList parent = getParent(element);
        setTail(parent, element, s, p);
    }
//
//	public static shiftValue(RDFList parent) {
//		while(parent != null) {
//			RDFList child = parent.getTail();
//
//			if(parent.isEmpty()) {
//			} else {
//
//			}
//		}
//
//		if(list.isEmpty()) {
//			list.with(value)
//
//			Resource newElement = list.getModel().createResource();
//			newElement.addProperty(RDF.first, list.getHead());
//			newElement.addProperty(RDF.rest, list.getTail());
//
//			list.setTail(newElement);
//		} else {
//
//		}
//	}
//
    @Override
    public RDFNode get(int index) {
        RDFList list = getList();
        RDFNode result = list.get(index);
        return result;
    }

    @Override
    public RDFNode set(int index, RDFNode element) {
        RDFList list = getList();
        RDFList item = findElement(list, index);

        item.setHead(element);

        return element;
    }

    public static RDFList findParent(RDFList list, int index) {
        RDFList result;
        if(index == 0) {
            result = null;
        } else {
            result = list;
            int n = index - 1;
            for(int i = 0; i < n; ++i) {
                result = result.getTail();
            }
        }

        return result;
    }


    public static RDFList findElement(RDFList list, int index) {
        RDFList result = list;
        for(int i = 0; i < index; ++i) {
            result = result.getTail();
        }

        return result;
    }


    @Override
    public int size() {
        RDFList list = getList();
        int result = list.size();
        return result;
    }

    @Override
    public Iterator<RDFNode> iterator() {
        RDFList list = getList();
        Iterator<RDFNode> result = new RDFListIterator(s, p, list);

        return result;
    }
    //protected RDFList list;


    /**
     * <p>
     * Iterator that can step along chains of list pointers to the end of the
     * list.
     * </p>
     */
    public class RDFListIterator extends NiceIterator<RDFNode>
    {
        // Instance variables

        protected Resource s;
        protected Property p;

        /** The current list node */
        protected RDFList m_head;

        /** The most recently seen node */
        protected RDFList m_seen = null;


        // Constructor
        //////////////

        /**
         * Construct an iterator for walking the list starting at head
         */
        protected RDFListIterator(Resource s, Property p, RDFList head) {
            this.s = s;
            this.p = p;
            m_head = head;
        }


        // External contract methods
        ////////////////////////////

        /**
         * @see Iterator#hasNext
         */
        @Override public boolean hasNext() {
            return !m_head.isEmpty();
        }

        /**
         * @see Iterator#next
         */
        @Override public RDFNode next() {
            m_seen = m_head;
            m_head = m_head.getTail();

            return m_seen.getHead();
        }

        /**
         * @see Iterator#remove
         */
        @Override public void remove() {
            //RDFDataMgr.write(System.out, s.getModel(), RDFFormat.TURTLE_FLAT);


            if (m_seen == null) {
                throw new IllegalStateException( "Illegal remove from list operator" );
            }

            // If we modify the head, ensure the reference by (s, p) is updated.
//            RDFNode root = ResourceUtils.getPropertyValue(s, p);
//            if(Objects.equals(root, m_head)) {

            //RDFList element = m_seen.getTail();
            RDFList parent = getParent(m_seen);
            setTail(parent, m_head, s, p);

            //linkParentTo(element, s, p);
//        	RDFNode _parent = ResourceUtils.getReversePropertyValue(m_head, RDF.rest);
//        	m_head = m_head.getTail();
//        	if(_parent == null) {
//            	ResourceUtils.setProperty(s, p, m_head);
//        	} else {
//        		RDFList parent = _parent.as(RDFList.class);
//            	parent.setTail(m_head);
//        	}

            // will remove three statements in a well-formed list
            m_seen.removeProperties();
            //m_head = element;
            m_seen = null;
        }
    }

    // TODO hashCode and equals
}
