package org.marcosoft.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

public class XmlFormatter {
	private static final int TAMANHO_LINHA = 100;
	private static final int QTD_ESPACOS_ATTRIBUTE = 3;
	private static final int QTD_ESPACOS_ELEMENT = 2;

	public static void main(String[] args) throws IOException, DocumentException {
		final XmlFormatter instance = new XmlFormatter();

		if (!instance.validarArgumentos(args)) {
			return;
		}

		final File file = instance.getFile(args[0]);

		final StringBuilder sb = instance.format(file);
		System.out.println(sb);

	}

	/**
	 * Formatar o arquivo XML
	 * @param xml arquivo xml
	 * @return o arquivo formatado
	 * @throws DocumentException caso ocorra alguma erro
	 */
	public StringBuilder format(final File xml) throws DocumentException {
            final Document document = new SAXReader(false).read(xml);
            final StringBuilder sb = new StringBuilder();
            final int identLevel = 0;
            walk(sb, document.getRootElement(), identLevel);
			return sb;
	}

	/**
	 * Percorre os elementos do arquivo.
	 * @param sb conteudo ja formatado
	 * @param element elemento
	 * @param identLevel nivel de identacao
	 */
	private void walk(final StringBuilder sb, final Element element, final int identLevel) {
	    sb.append(format(element, identLevel));
	    if (element.content().isEmpty()) {
	        sb.append(" />");
	    } else {
            sb.append(">");
            for (final Object object : element.content()) {
                if (object instanceof DefaultElement) {
                    walk(sb, (Element) object, identLevel + QTD_ESPACOS_ELEMENT);
                } else if (object instanceof DefaultCDATA) {
                	sb.append(tratarCdata((DefaultCDATA) object, identLevel+2));
                } else {
                    if (object instanceof DefaultText) {
                        sb.append(tratarTexto((DefaultText) object));
                    }
                }
            }
            sb.append(finalizarElemento(element, identLevel));
	    }
	}

    private String tratarCdata(DefaultCDATA cdata, int identLevel) {
    	final StringBuilder sb = new StringBuilder();
    	sb.append("<![CDATA[");
    	sb.append(cdata.getText());
    	sb.append("]]>");
		return sb.toString();
	}

	/**
     * Finalizar tag do elemento
     * @param element elemento
     * @param identLevel nivel de identacao
     * @return a finalizacao do elemento
     */
    private String finalizarElemento(final Element element, final int identLevel) {
        final StringBuilder sb = new StringBuilder();
        if (!element.isTextOnly() || contemCData(element)) {
            sb.append("\n" + spaces(identLevel));
        }
        sb.append("</" + element.getQualifiedName() + ">");
        return sb.toString();
    }

    private boolean contemCData(Element element) {
    	for (final Object object : element.content()) {
            if (object instanceof DefaultCDATA) {
            	return true;
            }
    	}
		return false;
	}

	/**
     * Tratar um conteudo do tipo texto.
     * @param defaultText texto
     * @return o texto formatado
     */
    private String tratarTexto(final DefaultText defaultText) {
        final StringBuilder sb = new StringBuilder();
        final String text = defaultText.getText();
        final int qtdNL = countChar('\n', text);
        for(int i=1; i<qtdNL; i++) {
            sb.append("\n");
        }
        sb.append(text.trim());
        return sb.toString();
    }

    /**
     * Conta a quantidade do caracter <code>c</code> no <code>text</code> passado.
     * @param c caracter
     * @param text texo
     * @return o numero de ocorrencias
     */
    private int countChar(final char c, final String text) {
	    int count = 0;
	    for (int i=0; i<text.length(); i++) {
	        if (text.charAt(i) == c) count++;
	    }
        return count;
    }

    /**
     * Formatar o elemento.
     * @param element elemento
     * @param identLevel nivel de identacao atual.
     * @return o elemento formatado
     */
    private String format(final Element element, final int identLevel) {
	    final StringBuilder sb = new StringBuilder();
	    final StringBuilder line = new StringBuilder();

	    final String inicioElemento = String.format("\n%s<%s", spaces(identLevel), element.getQualifiedName());
	    line.append(inicioElemento);
	    
	    List<String> elementAttributes = selectElementAttributes(element);
	    for (String str : elementAttributes) {
            if (line.length() + str.length() + 1 > TAMANHO_LINHA) {
                sb.append(line.toString());
                line.setLength(0);
                line.append(String.format("\n%s%s", spaces(identLevel + QTD_ESPACOS_ATTRIBUTE), str));
            } else {
                line.append(str);
            }
	    }
	    
        sb.append(line.toString());
        return sb.toString();

    }

	/**
	 * Selecionar os atributos do elemento. Considerar os {@link Namespace} como
	 * atributo.
	 * @param element elemento
	 * @return os atributos
	 */
	private List<String> selectElementAttributes(Element element) {
		List<String> attributes = selectNamespaces(element);
		attributes.addAll(selectAttributes(element));
		return attributes;
	}
	
	/**
	 * Selecionar os atributos do elemento.
	 * @param element elemento
	 * @return os atributos
	 */
	private List<String> selectAttributes(Element element) {
		List<String> attributes = new ArrayList<String>();
        for (final Object object : element.attributes()) {
            final DefaultAttribute attr = (DefaultAttribute) object;
            final String str = String.format(" %s=\"%s\"", attr.getName() , attr.getValue());
            attributes.add(str);
        }
		return attributes;
	}
	

	/**
	 * Selecionar os {@link Namespace} do elemento.
	 * @param element element
	 * @return os {@link Namespace}
	 */
	private List<String> selectNamespaces(Element element) {
		List<String> namespaces = new ArrayList<String>();
		
		for (final Object o : element.content()) {
	        if (o instanceof Namespace) {
	            final Namespace namespace = (Namespace) o;
	            final String str;
	            if (namespace.getPrefix().length() == 0) {
	                str = String.format(" xmlns=\"%s\"", namespace.getURI());
	            } else {
	                str = String.format(" xmlns:%s=\"%s\"", namespace.getPrefix() , namespace.getURI());
	            }
	            namespaces.add(str);
	        }
	    }
		return namespaces;
	}

    /**
     * Gera <code>qtd</code> espacos.
     * @param qtd quantidade
     * @return os espacos
     */
    private String spaces(final int qtd) {
        final StringBuilder sb = new StringBuilder();
        for (int i=0; i<qtd; i++) sb.append(" ");
        return sb.toString();
    }

    /**
     * Recupera o arquivo.
     * @param filename filename
     * @throws IOException em caso de erro
     * @return {@link File} para o arquivo 
     */
    private File getFile(final String filename) throws IOException {
	    final File externalFile = new File(filename);
	    if (externalFile.exists()) {
	        return externalFile;
	    }

		final String fullname = getClass().getResource(filename).getFile();
		final File file = new File(fullname);
		return file;
	}

	/**
	 * Verifica se os argumentos sao validos.
	 * @param args argumentos
	 * @return <code>true</code> se foi passado algum argumento
	 */
	public boolean validarArgumentos(final String[] args) {
		if (args.length == 0) {
			System.err.println("Sintaxe: " + getClass().getName() + " <ArquivoXml>");
			return false;
		}
		return true;
	}
}
