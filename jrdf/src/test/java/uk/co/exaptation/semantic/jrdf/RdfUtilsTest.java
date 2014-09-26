package uk.co.exaptation.semantic.jrdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.StringWriter;

import org.jrdf.graph.Graph;
import org.jrdf.query.answer.Answer;
import org.jrdf.sparql.SparqlConnection;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import uk.co.exaptation.semantic.jrdf.api.IRdfUtils;

public class RdfUtilsTest {

	public static String NEWLINE = System.getProperty("line.separator");

	@Test
	public void testReadFileAndQuery() {
		IRdfUtils rdfUtils = new RdfUtils();
		File file = new File("./src/test/resources/data/00D896C1-3B78-1E6D-4943-C8E832AE791F.nt");
		Graph graph = rdfUtils.parse(RdfType.NTriples, file);
		String query = "SELECT ?subject ?sha1 WHERE{ ?subject <jig://hash/sha1> ?sha1}";
		SparqlConnection sparqlConnection = rdfUtils.getSparqlConnection();
		Answer answer = sparqlConnection.executeQuery(graph, query);
		assertThat(answer.getVariableNames(), is(arrayContaining("subject", "sha1")));
	}

	@Test
	public void testEmptyResultsQuery() {
		IRdfUtils rdfUtils = new RdfUtils();
		File file = new File("./src/test/resources/data/00D896C1-3B78-1E6D-4943-C8E832AE791F.nt");
		Graph graph = rdfUtils.parse(RdfType.NTriples, file);
		String query = "SELECT ?subject ?ref WHERE{ ?subject <jig://book/amazon/ref> ?ref}";
		SparqlConnection sparqlConnection = rdfUtils.getSparqlConnection();
		Answer answer = sparqlConnection.executeQuery(graph, query);
		Assert.assertFalse(answer.columnValuesIterator().hasNext());
	}

	@Test
	public void testBuildGraphAndQuery() {
		IRdfUtils rdfUtils = new RdfUtils();
		Graph graph = rdfUtils.getEmptyGraph();
		rdfUtils.addTriple(graph, "jig://node/0C3E659E-F3E3-645A-A635-49D3690455B5", "jig://hash/sha1", "c4672868c8ce04f239dbd459c4bcb37eb1543fc8");
		String query = "SELECT ?subject ?sha1 WHERE{ ?subject <jig://hash/sha1> ?sha1}";
		SparqlConnection sparqlConnection = rdfUtils.getSparqlConnection();
		Answer answer = sparqlConnection.executeQuery(graph, query);
		assertThat(answer.getVariableNames(), is(arrayContaining("subject", "sha1")));
	}

	@Test
	public void testBuildGraphAndWriterNT() {
		IRdfUtils rdfUtils = new RdfUtils();
		Graph graph = rdfUtils.getEmptyGraph();
		StringWriter stringWriter = new StringWriter();
		rdfUtils.addTriple(graph, "jig://node/0C3E659E-F3E3-645A-A635-49D3690455B5", "jig://hash/sha1", "c4672868c8ce04f239dbd459c4bcb37eb1543fc8");
		rdfUtils.writeGraph(RdfType.NTriples, graph, stringWriter);
		Assert.assertEquals("<jig://node/0C3E659E-F3E3-645A-A635-49D3690455B5> <jig://hash/sha1> \"c4672868c8ce04f239dbd459c4bcb37eb1543fc8\" .".trim(), stringWriter.toString().trim());
	}

	@Test
	public void testBuildGraphAndWriterRdf() {
		IRdfUtils rdfUtils = new RdfUtils();
		Graph graph = rdfUtils.getEmptyGraph();
		StringWriter stringWriter = new StringWriter();
		rdfUtils.addTriple(graph, "jig://node/0C3E659E-F3E3-645A-A635-49D3690455B5", "jig://hash/sha1", "c4672868c8ce04f239dbd459c4bcb37eb1543fc8");
		rdfUtils.writeGraph(RdfType.RdfXml, graph, stringWriter);
		Assert.assertEquals(380, stringWriter.toString().length());
	}

	@Test
	public void testQueryRdfFile() {
		IRdfUtils rdfUtils = new RdfUtils();
		File file = new File("./src/test/resources/data/00D896C1-3B78-1E6D-4943-C8E832AE791F.nt");
		String query = "SELECT ?subject ?sha1 WHERE{ ?subject <jig://hash/sha1> ?sha1}";
		Answer answer = rdfUtils.queryRdfFile(file, query);
		assertThat(answer.getVariableNames(), is(arrayContaining("subject", "sha1")));
	}

	@Ignore
	public void testShard() {
		IRdfUtils rdfUtils = new RdfUtils();
		rdfUtils.shardNTriples(new File("X:/data/tools/triple/rdf2rdf/1.0.1/books-all-test.nt"), new File("c:/temp/shard/"));
	}
}
