package com.carlgrundstrom.chess;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyManager {
    private File configurationFile;
    private HashMap<String,String> properties = new HashMap<>();
    private boolean modified;
	private static Logger logger = Logger.getLogger(PropertyManager.class.getName());

	public PropertyManager(String name) {
		this(new File(System.getProperty("user.home"), name));
	}

	public PropertyManager(File configurationFile) {
        this.configurationFile = configurationFile;
        if (configurationFile.exists()) {
			try {
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
				Document document = domBuilder.parse(configurationFile);

				Node root = document.getDocumentElement();
				if (root.getNodeName().equals("settings")) {
					NodeList settingNodes = root.getChildNodes();
					int len0 = settingNodes.getLength();
					for (int i = 0 ; i < len0; i++) {
						Node settingNode = settingNodes.item(i);
						if (!(settingNode instanceof Element))
							continue;
						if (settingNode.getNodeName().equals("properties")) {
							NodeList propertyNodes = settingNode.getChildNodes();
							int len1 = propertyNodes.getLength();
							for (int j = 0 ; j < len1; j++) {
								Node propertyNode = propertyNodes.item(j);
								if (!(propertyNode instanceof Element))
									continue;
								if (!propertyNode.getNodeName().equals("property"))
									throw new Exception("Invalid XML tag in " + configurationFile + " :" + propertyNode.getNodeName());

								NamedNodeMap map = propertyNode.getAttributes();
								Node nameNode = map.getNamedItem("name");
								if (nameNode == null)
									throw new Exception("xml attribute 'name' not found in " + configurationFile);
								String name = nameNode.getNodeValue();

								Node valueNode = map.getNamedItem("value");
								if (valueNode == null)
									throw new Exception("xml tag 'value' not found for property '" + name + "' in " + configurationFile);
								String value = valueNode.getNodeValue();

								setProperty(name, value);
							}
						}
						else
							throw new Exception("Invalid Setting element in " + configurationFile + ": " + settingNode.getNodeName());
					}
				}
				else
					throw new Exception("Invalid XML root in " + configurationFile + ": " + root.getNodeName());
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "Error loading configuration file: " + configurationFile, e);
				configurationFile.delete();
			}
		}
	}

    public File getConfigurationFile() {
        return configurationFile;
    }

    public synchronized String getString(String name, String defaultValue) {
        String value = properties.get(name);
        if (value == null)
            value = defaultValue;
        return value;
    }

	public Integer getInteger(String name, Integer defaultValue) {
		String s = getString(name, null);
		if (s == null)
			return defaultValue;
		return Integer.parseInt(s);
	}

	public Long getLong(String name, Long defaultValue) {
		String s = getString(name, null);
		if (s == null)
			return defaultValue;
		return Long.parseLong(s);
	}

    public Boolean getBoolean(String name, Boolean defaultValue) {
        String s = getString(name, null);
        if (s == null)
            return defaultValue;
        return Boolean.parseBoolean(s);
    }

	public Double getDouble(String name, Double defaultValue) {
		String s = getString(name, null);
		if (s == null)
			return defaultValue;
		return Double.parseDouble(s);
	}

    public synchronized void setProperty(String name, Object value) {
        String s = properties.get(name);
        String v = String.valueOf(value);
        if (s == null || !s.equals(v)) {
            properties.put(name, v);
            modified = true;
        }
    }

	public synchronized void removeProperty(String name) {
		if (properties.remove(name) != null)
			modified = true;
	}

	public synchronized void write() {
        if (modified) {
            try {
                PrintWriter out = new PrintWriter(configurationFile);
                try {
                    out.println("<settings>");

                    out.println("  <properties>");
                    ArrayList<String> names = new ArrayList<String>();
                    for (String name : properties.keySet())
                        names.add(name);
                    Collections.sort(names);

                    for (String name : names) {
                        String value = properties.get(name);
                        out.println("    <property name=\"" + name + "\" value=\"" + xmlEncode(value) + "\"/>");
                    }
                    out.println("  </properties>");

                    out.println("</settings>");
                }
                finally {
                    out.close();
                }
            }
            catch (Exception e) {
				logger.log(Level.WARNING, "Error writing configuration file: " + configurationFile, e);
            }
            modified = false;
        }
    }

	public static String xmlEncode(String s) {
		if (s == null)
			return null;
		StringBuffer b = new StringBuffer();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '<':
					b.append("&lt;");
					break;
				case '>':
					b.append("&gt;");
					break;
				case '&':
					b.append("&amp;");
					break;
				case '"':
					b.append("&quot;");
					break;
				case '\'':
					b.append("&apos;");
					break;
				default:
					b.append(ch);
			}
		}
		return b.toString();
	}
}
