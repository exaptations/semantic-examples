package uk.co.exaptation.semantic.jrdf;

import static java.net.URI.create;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;

import org.jrdf.JRDFFactory;
import org.jrdf.SortedMemoryJRDFFactory;
import org.jrdf.graph.Graph;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Resource;
import org.jrdf.graph.Triple;
import org.jrdf.graph.TripleFactory;
import org.jrdf.parser.RdfReader;
import org.jrdf.query.answer.Answer;
import org.jrdf.sparql.SparqlConnection;
import org.jrdf.writer.BlankNodeRegistry;
import org.jrdf.writer.RdfNamespaceMap;
import org.jrdf.writer.RdfWriter;
import org.jrdf.writer.mem.MemBlankNodeRegistryImpl;
import org.jrdf.writer.ntriples.NTriplesWriter;
import org.jrdf.writer.ntriples.NTriplesWriterImpl;

import uk.co.exaptation.semantic.jrdf.api.IRdfUtils;
import uk.co.exaptation.semantic.jrdf.impl.MyRdfNamespaceMapImpl;
import uk.co.exaptation.semantic.jrdf.impl.MyRdfXmlWriter;

public class RdfUtils extends AbstractRdfUtils implements IRdfUtils {

	private RdfReader rdfReader;

	public RdfUtils() {
		rdfReader = new RdfReader();
	}

	public Graph parse(RdfType rdfType, File file) {
		if (rdfType.equals(RdfType.N3)) {
			return rdfReader.parseN3(file);
		} else if (rdfType.equals(RdfType.NTriples)) {
			return rdfReader.parseNTriples(file);
		} else if (rdfType.equals(RdfType.RdfXml)) {
			return rdfReader.parseRdfXml(file);
		}
		return null;
	}

	public Graph parse(RdfType rdfType, InputStream inputStream) {
		if (rdfType.equals(RdfType.N3)) {
			return rdfReader.parseN3(inputStream);
		} else if (rdfType.equals(RdfType.NTriples)) {
			return rdfReader.parseNTriples(inputStream);
		} else if (rdfType.equals(RdfType.RdfXml)) {
			return rdfReader.parseRdfXml(inputStream);
		}
		return null;
	}

	public Graph getEmptyGraph() {
		JRDFFactory jrdfFactory = SortedMemoryJRDFFactory.getFactory();
		return jrdfFactory.getNewGraph();
	}

	public SparqlConnection getSparqlConnection() {
		JRDFFactory jrdfFactory = SortedMemoryJRDFFactory.getFactory();
		return jrdfFactory.getNewSparqlConnection();
	}

	public Triple buildTriple(Graph graph, String subject, String predicate, String literal) {
		GraphElementFactory elementFactory = graph.getElementFactory();
		TripleFactory tripleFactory = graph.getTripleFactory();
		URI subjectURI = create(subject);
		URI predicateURI = create(predicate);
		Literal createLiteral = elementFactory.createLiteral(literal);
		Resource createResource = elementFactory.createResource(subjectURI);
		Resource createResource2 = elementFactory.createResource(predicateURI);
		return tripleFactory.createTriple(createResource, createResource2, createLiteral);
	}

	@Override
	public void addTriple(Graph graph, String subject, String predicate, String literal) {
		graph.add(buildTriple(graph, subject, predicate, literal));
	}

	@Override
	public void shardNTriples(File ntripleFile, File destDir) {
		if (ntripleFile.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(ntripleFile));
				String st = reader.readLine();
				while (st != null) {
					st = reader.readLine();
					if (st != null) {
						String fileName = getFileName(st, ".nt");
						File destFile = new File(destDir, fileName);
						processLine(st, destFile);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public Answer queryRdfFile(File file, String query) {
		String filename = file.getName();
		Graph graph = null;
		if (filename.endsWith(".nt")) {
			graph = parse(RdfType.NTriples, file);
		} else if (filename.endsWith(".n3")) {
			graph = parse(RdfType.NTriples, file);
		} else if (filename.endsWith(".rdf")) {
			graph = parse(RdfType.NTriples, file);
		}
		return getSparqlConnection().executeQuery(graph, query);
	}

	@Override
	public void writeGraph(RdfType rdfType, Graph graph, StringWriter stringWriter) {
		if (rdfType.equals(RdfType.N3)) {
		} else if (rdfType.equals(RdfType.NTriples)) {
			NTriplesWriter writer = new NTriplesWriterImpl();
			writer.write(graph, stringWriter);
		} else if (rdfType.equals(RdfType.RdfXml)) {
			BlankNodeRegistry nodeRegistry = new MemBlankNodeRegistryImpl();
			nodeRegistry.clear();
			RdfNamespaceMap map = new MyRdfNamespaceMapImpl();
			RdfWriter writer = new MyRdfXmlWriter(nodeRegistry, map);
			writer.write(graph, stringWriter);
		}
	}
}
