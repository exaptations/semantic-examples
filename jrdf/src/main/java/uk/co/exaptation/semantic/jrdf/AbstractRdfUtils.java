package uk.co.exaptation.semantic.jrdf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AbstractRdfUtils {

	public static String NEWLINE = System.getProperty("line.separator");

	protected void processLine(String ntriple, File destFile) {
		try {
			FileWriter fileWriter = new FileWriter(destFile, true);
			if (destFile.length() > 0) {
				fileWriter.write(NEWLINE);
			}
			fileWriter.write(ntriple);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected String getFileName(String ntLine, String ext) {
		String[] ntTokens = ntLine.split("\\ ");
		String subject = ntTokens[0];
		String[] subjectTokens = subject.split("/");
		String rawNode = subjectTokens[subjectTokens.length - 1];
		String subjectNode = rawNode.replace(">", "");
		return subjectNode + ext;
	}
}
