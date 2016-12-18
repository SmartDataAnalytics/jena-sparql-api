package org.aksw.jena_sparql_api.utils.enhanced;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.ext.com.google.common.base.Optional;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.ResourceUtils;

import com.google.common.collect.MutableClassToInstanceMap;

/**
 * Enhancable resource which enables painless association of Java objects to it.
 * Useful for passing resources through a workflow, where on output should be directly attached
 * to the resources based on processing data stored in Java objects.
 *
 * The ResourceEnh will only allocate an entry for its java objects, and will
 * remove its entry once all associated objects have been removed.
 *
 * <pre>
 * {@code
 * Stream<Resource> stream
 *   .map(r -> r.as(EnhResource.class))
 *   .peek(r ->r.addTrait(QueryFactory.parse(r.getProperty(LSQ.text, ...)))
 *   .peek(r -> benchmarkQuery(r, r.getTrait(Query.class), service)
 *   .foreach(r.getModel().write(System.out, "TURTLE")))
 * }
 * </pre>
 *
 * TODO: Trait in this context is meant as a class-based attribute; there might be a better name out there
 *
 * r.addTrait("hello")
 * r.addTrait(Base.class, new Derived())
 *
 * r.getTrait(String.class)
 * r.removeTrait(String.class)
 *
 * Note: r.addTrait(o) is equivalent to r.addTrait(o.getClass(), o);
 *
 * @author Claus Stadler
 *
 */
public class ResourceEnh
	extends ResourceImpl
{
	protected Map<Node, MutableClassToInstanceMap<Object>> meta;

	public ResourceEnh(Node n, EnhGraph m, Map<Node, MutableClassToInstanceMap<Object>> meta) {
		super(n, m);
		Objects.requireNonNull(meta);
		this.meta = meta;
	}

	public ResourceEnh addTrait(Object o) {
		Objects.requireNonNull(o);

		addTrait(o.getClass(), o);

		return this;
	}

	@SuppressWarnings("deprecation")
	public ResourceEnh addTrait(Class<?> clazz, Object o) {
		MutableClassToInstanceMap<Object> map = meta.get(node);
        if(map == null) {
        	map = MutableClassToInstanceMap.create();
        	meta.put(node, map);
        }

        map.put(clazz, o);

        return this;
	}

	public ResourceEnh removeTrait(Object clazz) {
		MutableClassToInstanceMap<Object> map = meta.get(node);
		if(map != null) {
			map.remove(clazz);

			if(map.isEmpty()) {
				meta.remove(node);
			}
		}

		return this;
	}

	public <T> Optional<T> getTrait(Class<T> clazz) {
		MutableClassToInstanceMap<Object> map = meta.get(node);
		T tmp = map == null ? null : map.getInstance(clazz);
		return Optional.fromNullable(tmp);
	}

	public ResourceEnh clearTraits() {
		MutableClassToInstanceMap<Object> map = meta.get(node);
		if(map != null) {
			map.clear();
			meta.remove(node);
		}

		return this;
	}

	public static Object resolve(Resource r, Object o) {
		Object result;
		if(o instanceof Property) {
			result = r.getProperty((Property)o).getObject().asLiteral().getValue();
		} else if(o instanceof Supplier) {
			Object tmp = ((Supplier<?>)o).get();
			result = resolve(r, tmp);
		} else {
			result = o;
		}
		return result;
	}

	public static String interpolate(Resource r, String template, Object ... objs) {

		List<Object> tmp = Arrays.asList(objs).stream()
			.map(o -> resolve(r, o))
			.collect(Collectors.toList());

		Object[] xs = new Object[tmp.size()];
		tmp.toArray(xs);

		String result = MessageFormat.format(template, xs);
		return result;
	}

	public ResourceEnh rename(String uriTemplate, Object ... objs) {
		String uri = objs.length == 0 ? uriTemplate : interpolate(this, uriTemplate, objs);


		ResourceEnh result = ResourceUtils.renameResource(this, uri).as(ResourceEnh.class);

		MutableClassToInstanceMap<Object> srcMap = meta.get(node);
		if(srcMap != null) {
			Node tgtNode = this.getModel().createResource(uri).asNode();
			MutableClassToInstanceMap<Object> tgtMap = meta.get(tgtNode);

			if(tgtMap != null) {
				tgtMap.putAll(srcMap);
			} else {
				meta.put(tgtNode, srcMap);
			}
		}

		return result;
	}
}
