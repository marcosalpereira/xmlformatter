package org.marcosoft.xml;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.dom4j.DocumentException;
import org.junit.Test;

public class TestesFuncionais {

	@Test
	public void all() throws DocumentException, IOException {
		assertContentsEquals("/visualizarParecerListar.xml");
	}
	@Test
	public void cdata() throws DocumentException, IOException {
		assertContentsEquals("/cdata.xml");
	}

	/**
	 * @param inputFile
	 *            input file
	 * @throws DocumentException
	 *             em caso de erro
	 * @throws IOException
	 *             em caso de erro
	 */
	private void assertContentsEquals(final String inputFile)
			throws DocumentException, IOException {
		XmlFormatter fmt = new XmlFormatter();
		StringBuilder real = fmt.format(getFile(inputFile + ".cenario"));
		real.append("\n");
		assertEquals(getContents(inputFile + ".expected"), real.toString());
	}

	public String getContents(final String resource) throws IOException {
		final InputStream is = this.getClass().getResourceAsStream(resource);

		final StringBuilder sb = new StringBuilder();
		if (is != null) {
			String line;

			try {
				// BufferedReader reader = new BufferedReader(new
				// InputStreamReader(is, "UTF-8"));
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	private File getFile(final String filename) throws IOException {
		final String fullname = this.getClass().getResource(filename).getFile();
		final File file = new File(fullname);
		return file;
	}

}
