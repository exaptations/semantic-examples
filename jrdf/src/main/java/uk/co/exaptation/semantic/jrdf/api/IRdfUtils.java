package uk.co.exaptation.semantic.jrdf.api;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import org.jrdf.graph.Graph;
import org.jrdf.graph.Triple;
import org.jrdf.query.answer.Answer;
import org.jrdf.sparql.SparqlConnection;

import uk.co.exaptation.semantic.jrdf.RdfType;

public interface IRdfUtils {
	public Graph parse(RdfType rdfType, File file);

	public Graph parse(RdfType rdfType, InputStream inputStream);

	public void writeGraph(RdfType rdfType, Graph graph, StringWriter stringWriter);

	public Graph getEmptyGraph();

	public Answer queryRdfFile(File file, String query);

	public void addTriple(Graph graph, String subject, String predicate, String literal);

	public Triple buildTriple(Graph graph, String subject, String predicate, String literal);

	public SparqlConnection getSparqlConnection();

	public void shardNTriples(File ntripleFile, File destDir);
}
