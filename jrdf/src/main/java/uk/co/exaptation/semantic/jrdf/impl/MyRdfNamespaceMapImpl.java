package uk.co.exaptation.semantic.jrdf.impl;

import static org.jrdf.query.relation.type.PredicateNodeType.PREDICATE_TYPE;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jrdf.graph.Graph;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.jrdf.util.ClosableIterable;
import org.jrdf.vocabulary.RDF;
import org.jrdf.writer.NamespaceException;
import org.jrdf.writer.RdfNamespaceMap;

/**
 * Contains mappings between namespaces and partial URIs.
 *
 * @author TurnerRX
 */
public class MyRdfNamespaceMapImpl implements RdfNamespaceMap {
	private static final String NS_PREFIX = "ns";
	private Map<String, String> names = new HashMap<String, String>();
	private Map<String, String> uris = new HashMap<String, String>();

	public MyRdfNamespaceMapImpl() {
		// add some well known namespaces
		initNamespaces();
	}

	public void load(Graph graph) throws GraphException {
		// check for blank nodes
		ClosableIterable<? extends Node> predicates = graph.findNodes(PREDICATE_TYPE);
		for (Node node : predicates) {
			URIReference uriReference = (URIReference) node;
			String partial = getPartialUri(uriReference.getURI().toString());
			if (!uris.containsKey(partial)) {
				String ns = NS_PREFIX + names.size();
				add(ns, partial);
			}
		}

		// add("ns50", "jig://file/");
	}

	public String replaceNamespace(URIReference resource) throws NamespaceException {
		URI uri = resource.getURI();
		String full = uri.toString();
		String partial = getPartialUri(full);
		String ns = uris.get(partial);
		if (ns == null) {
			// throw new NamespaceException("Partial uri: " + partial +
			// " is not mapped to a namespace.");
		}
		return full.replaceFirst(partial, ns + ":");
	}

	public String getPrefix(URIReference resource) {
		URI uri = resource.getURI();
		String partial = getPartialUri(uri.toString());
		return uris.get(partial);
	}

	public String getFullUri(String partial) {
		return names.get(partial);
	}

	public Set<Map.Entry<String, String>> getNameEntries() {
		return names.entrySet();
	}

	public void reset() {
		names.clear();
		uris.clear();
		initNamespaces();
	}

	private void initNamespaces() {
		add("rdf", getPartialUri(RDF.BASE_URI.toString()));
		// add("rdfs", getPartialUri(RDFS.BASE_URI.toString()));
		// add("owl", "http://www.w3.org/2002/07/owl#");
		// add("dc", "http://purl.org/dc/elements/1.1/");
		// add("dcterms", "http://purl.org/dc/terms/");
	}

	public String toString() {
		return names.toString();
	}

	/**
	 * Extracts the uri to the last '#' or '/' character.
	 *
	 * @param uri
	 *            String URI
	 * @return String partial URI
	 */
	private String getPartialUri(String uri) {
		int hashIndex = uri.lastIndexOf('#');
		int slashIndex = uri.lastIndexOf('/');
		int index = Math.max(hashIndex, slashIndex);
		// if there is no '#' or '/', return entire uri
		return (index > 0) && (index < uri.length()) ? uri.substring(0, ++index) : uri;
	}

	public void add(String name, String uri) {
		// map bi-directionally
		uris.put(uri, name);
		names.put(name, uri);
	}
}