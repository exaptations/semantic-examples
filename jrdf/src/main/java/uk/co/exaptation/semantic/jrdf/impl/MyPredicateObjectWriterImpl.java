package uk.co.exaptation.semantic.jrdf.impl;

import static org.jrdf.util.param.ParameterUtil.checkNotNull;

import java.net.URI;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.Resource;
import org.jrdf.graph.URIReference;
import org.jrdf.vocabulary.RDF;
import org.jrdf.writer.BlankNodeRegistry;
import org.jrdf.writer.RdfNamespaceMap;
import org.jrdf.writer.RdfWriter;
import org.jrdf.writer.WriteException;
import org.jrdf.writer.rdfxml.PredicateObjectWriter;
import org.jrdf.writer.rdfxml.XmlLiteralWriter;

public final class MyPredicateObjectWriterImpl implements PredicateObjectWriter {
	private final RdfNamespaceMap names;
	private final BlankNodeRegistry registry;
	private final XmlLiteralWriter xmlLiteralWriter;
	private XMLStreamWriter xmlStreamWriter;
	private Exception exception;

	public MyPredicateObjectWriterImpl(final RdfNamespaceMap newNames, final BlankNodeRegistry newBlankNodeRegistry, final XMLStreamWriter newXmlStreamWriter, final XmlLiteralWriter newXmlLiteralWriter) {
		checkNotNull(newNames, newBlankNodeRegistry, newXmlStreamWriter, newXmlLiteralWriter);
		this.xmlLiteralWriter = newXmlLiteralWriter;
		this.names = newNames;
		this.registry = newBlankNodeRegistry;
		this.xmlStreamWriter = newXmlStreamWriter;
		// names.reset();
	}

	public void writePredicateObject(final PredicateNode predicate, final ObjectNode object) throws WriteException {
		checkNotNull(predicate, object);
		try {
			writePredicate(predicate);
			writeObject(object);
			xmlStreamWriter.writeEndElement();
			xmlStreamWriter.writeCharacters(RdfWriter.NEW_LINE);
			xmlStreamWriter.flush();
		} catch (Exception e) {
			exception = null;
			throw new WriteException(e);
		}
	}

	public void visitBlankNode(BlankNode blankNode) {
		checkNotNull(blankNode);
		try {
			xmlStreamWriter.writeAttribute("rdf:nodeID", registry.getNodeId(blankNode));
		} catch (XMLStreamException e) {
			exception = e;
		}
	}

	public void visitURIReference(URIReference uriReference) {
		checkNotNull(uriReference);
		try {
			final URI uri = uriReference.getURI();
			xmlStreamWriter.writeAttribute("rdf:resource", uri.toString());
		} catch (XMLStreamException e) {
			exception = e;
		}
	}

	public void visitLiteral(Literal literal) {
		checkNotNull(literal);
		try {
			if (literal.isDatatypedLiteral() && literal.getDatatypeURI().equals(RDF.XML_LITERAL)) {
				xmlLiteralWriter.write(literal);
			} else {
				if (literal.isDatatypedLiteral()) {
					xmlStreamWriter.writeAttribute("rdf:datatype", literal.getDatatypeURI().toString());
				} else if (literal.isLanguageLiteral()) {
					xmlStreamWriter.writeAttribute("xml:lang", literal.getLanguage());
				}
				xmlStreamWriter.writeCharacters(literal.getLexicalForm());
			}
		} catch (XMLStreamException e) {
			exception = e;
		}
	}

	public void visitNode(Node node) {
		checkNotNull(node);
		exception = new WriteException("Unknown object node type: " + node.getClass().getName());
	}

	public void visitResource(Resource resource) {
		checkNotNull(resource);
		exception = new WriteException("Unknown object node type: " + resource.getClass().getName());
	}

	private void writePredicate(PredicateNode predicate) throws WriteException, XMLStreamException {
		if (!(predicate instanceof URIReference)) {
			throw new WriteException("Unknown predicate node type: " + predicate.getClass().getName());
		}
		String resourceName = names.replaceNamespace((URIReference) predicate);
		int lastIndexOf = predicate.toString().lastIndexOf('/');

		// xmlStreamWriter.writeStartElement(resourceName);
		String[] split = resourceName.split(":");
		xmlStreamWriter.writeStartElement(split[1]);
		xmlStreamWriter.writeNamespace("xmlns", predicate.toString().substring(0, lastIndexOf) + "/");
	}

	private void writeObject(ObjectNode object) throws Exception {
		object.accept(this);
		if (exception != null) {
			throw exception;
		}
	}
}