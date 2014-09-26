package uk.co.exaptation.semantic.jrdf.impl;

import static org.jrdf.graph.AnyObjectNode.ANY_OBJECT_NODE;
import static org.jrdf.graph.AnyPredicateNode.ANY_PREDICATE_NODE;
import static org.jrdf.graph.AnySubjectNode.ANY_SUBJECT_NODE;
import static org.jrdf.util.param.ParameterUtil.checkNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jrdf.graph.Graph;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.Triple;
import org.jrdf.util.ClosableIterator;
import org.jrdf.util.IteratorStack;
import org.jrdf.writer.BlankNodeRegistry;
import org.jrdf.writer.RdfNamespaceMap;
import org.jrdf.writer.RdfWriter;
import org.jrdf.writer.WriteException;
import org.jrdf.writer.rdfxml.RdfXmlDocument;
import org.jrdf.writer.rdfxml.RdfXmlDocumentImpl;
import org.jrdf.writer.rdfxml.ResourceWriter;
import org.jrdf.writer.rdfxml.XmlLiteralWriterImpl;

public class MyRdfXmlWriter implements RdfWriter {
    private static final String ENCODING_DEFAULT = "UTF-8";
    private static final XMLOutputFactory FACTORY = XMLOutputFactory.newInstance();

    /**
     * PrintWriter output. Caller is responsible for closing stream.
     */
    private PrintWriter printWriter;

    /**
     * Used to track blank nodes.
     */
    private BlankNodeRegistry blankNodeRegistry;

    /**
     * Containing mappings between partial URIs and namespaces.
     */
    private RdfNamespaceMap names;

    /**
     * Writer for the XML document.
     */
    private XMLStreamWriter xmlStreamWriter;

    public MyRdfXmlWriter(BlankNodeRegistry newBlankNodeRegistry, RdfNamespaceMap newNames) {
        checkNotNull(newBlankNodeRegistry, newNames);
        this.blankNodeRegistry = newBlankNodeRegistry;
        this.names = newNames;
    }

    public void write(Graph graph, OutputStream stream) throws WriteException, GraphException {
        final OutputStreamWriter writer = new OutputStreamWriter(stream);
        try {
            write(graph, writer);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new WriteException(e);
            }
        }
    }

    public void write(Graph graph, Writer writer) throws WriteException, GraphException {
        printWriter = new PrintWriter(writer);
        try {
            write(graph, (String) null);
        } catch (XMLStreamException e) {
            //throw new WriteException(e);
        } finally {
            printWriter.close();
        }
    }

    /**
     * Writes the graph contents to the writer, including the specified encoding
     * in the XML header.
     *
     * @param graph    Graph to be written.
     * @param encoding String XML encoding attribute.
     * @throws GraphException If the graph cannot be read.
     * @throws WriteException If the contents could not be written
     */
    private void write(Graph graph, String encoding) throws GraphException, WriteException, XMLStreamException {
        try {
            // Initialize values.
            blankNodeRegistry.clear();
            names.reset();
            //names.load(graph);

            try {
                xmlStreamWriter = FACTORY.createXMLStreamWriter(printWriter);
            } catch (XMLStreamException e) {
                throw new WriteException(e);
            }

            // header
            encoding = (encoding == null) ? ENCODING_DEFAULT : encoding;
            final RdfXmlDocument header = new RdfXmlDocumentImpl(encoding, names, xmlStreamWriter);
            header.writeHeader();

            // body
            writeStatements(graph);

            // footer
            header.writeFooter();
        } finally {
            if (printWriter != null) {
                printWriter.flush();
            }
        }
    }

    /**
     * Writes all statements in the Graph to the writer.
     *
     * @param graph  Graph containing statements.
     * @throws GraphException If the graph cannot be read.
     * @throws WriteException If the statements could not be written.
     */
    private void writeStatements(final Graph graph) throws GraphException, WriteException, XMLStreamException {
        final ClosableIterator<Triple> iter = graph.find(ANY_SUBJECT_NODE, ANY_PREDICATE_NODE, ANY_OBJECT_NODE).iterator();
        try {
            final IteratorStack<Triple> stack = new IteratorStack<Triple>(iter);
            final ResourceWriter writer = new MyResourceWriterImpl(names, blankNodeRegistry, xmlStreamWriter, new XmlLiteralWriterImpl(xmlStreamWriter));
            while (stack.hasNext()) {
                final Triple currentTriple = stack.pop();
                writer.setTriple(currentTriple);
                writer.writeStart();
                writer.writeNestedStatements(stack);
                writer.writeEnd();
            }
        } finally {
            iter.close();
        }
    }
}
