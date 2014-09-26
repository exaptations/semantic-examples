package uk.co.exaptation.semantic.jrdf.impl;

import static org.jrdf.util.param.ParameterUtil.checkNotNull;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.Resource;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.jrdf.util.IteratorStack;
import org.jrdf.writer.BlankNodeRegistry;
import org.jrdf.writer.RdfNamespaceMap;
import org.jrdf.writer.RdfWriter;
import org.jrdf.writer.WriteException;
import org.jrdf.writer.rdfxml.PredicateObjectWriter;
import org.jrdf.writer.rdfxml.ResourceWriter;
import org.jrdf.writer.rdfxml.XmlLiteralWriter;

public class MyResourceWriterImpl implements ResourceWriter {
	private final BlankNodeRegistry registry;
	private final XMLStreamWriter xmlStreamWriter;
	private final PredicateObjectWriter statement;
	private Triple currentTriple;
	private Exception exception;

	public MyResourceWriterImpl(final RdfNamespaceMap names, final BlankNodeRegistry newRegistry, final XMLStreamWriter newXmlStreamWriter, XmlLiteralWriter xmlLiteralWriter) {
		checkNotNull(names, newRegistry, newXmlStreamWriter);
		this.registry = newRegistry;
		this.xmlStreamWriter = newXmlStreamWriter;
		this.statement = new MyPredicateObjectWriterImpl(names, registry, xmlStreamWriter, xmlLiteralWriter);
	}

	public void setTriple(final Triple triple) {
		this.currentTriple = triple;
	}

	public void writeStart() throws WriteException {
		try {
			xmlStreamWriter.writeStartElement("rdf:Description");
			currentTriple.getSubject().accept(this);
			xmlStreamWriter.writeCharacters(RdfWriter.NEW_LINE + "    ");
			xmlStreamWriter.flush();
			if (exception != null) {
				throw exception;
			}
		} catch (Exception e) {
			exception = null;
			throw new WriteException(e);
		}
	}

	public void writeNestedStatements(final IteratorStack<Triple> stack) throws WriteException, XMLStreamException {
		statement.writePredicateObject(currentTriple.getPredicate(), currentTriple.getObject());
		while (stack.hasNext()) {
			SubjectNode currentSubject = currentTriple.getSubject();
			currentTriple = stack.pop();
			// Have we run out of the same subject - if so push it back on an
			// stop iterating.
			if (!currentSubject.equals(currentTriple.getSubject())) {
				stack.push(currentTriple);
				break;
			}
			xmlStreamWriter.writeCharacters("    ");
			statement.writePredicateObject(currentTriple.getPredicate(), currentTriple.getObject());
		}
	}

	public void writeEnd() throws WriteException {
		try {
			xmlStreamWriter.writeEndElement();
			xmlStreamWriter.writeCharacters(RdfWriter.NEW_LINE);
			xmlStreamWriter.flush();
		} catch (XMLStreamException e) {
			throw new WriteException(e);
		}
	}

	public void visitBlankNode(final BlankNode blankNode) {
		try {
			xmlStreamWriter.writeAttribute("rdf:nodeID", registry.getNodeId(blankNode));
		} catch (XMLStreamException e) {
			exception = e;
		}
	}

	public void visitURIReference(final URIReference uriReference) {
		try {
			xmlStreamWriter.writeAttribute("rdf:about", uriReference.getURI().toString());
		} catch (XMLStreamException e) {
			exception = e;
		}
	}

	public void visitLiteral(final Literal literal) {
		unknownType(literal);
	}

	public void visitNode(final Node node) {
		unknownType(node);
	}

	public void visitResource(Resource resource) {
		unknownType(resource);
	}

	private void unknownType(final Node node) {
		System.out.println("**here**");
		exception = new WriteException("Unknown SubjectNode type: " + node.getClass().getName());
	}
}
