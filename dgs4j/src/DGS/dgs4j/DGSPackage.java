/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j;


import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.apache.xml.serialize.*;
//import org.xml.sax.*;
//import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 *
 * @author dwimsey
 */
public class DGSPackage {

	public String fileName = "";
	public String templateBuffer = "main";
	public float animationDuration = 0.0f;
	public float animationFramerate = 0.0f;
	public String commandString = "";
	public DGSFileInfo[] files = null;
	public DGSVariable[] variables = null;
	public String stylesheet = null;

	public DGSPackage() {
		super();
		reset();
	}

	private void reset() {
		fileName = "";
		templateBuffer = "main";
		animationDuration = 0.0f;
		animationFramerate = 0.0f;
		commandString = "";
		files = null;
		variables = null;
		stylesheet = null;
	}

	public boolean loadFileStream(InputStream inStream, String filename) {
		reset();
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
		Document doc = null;
		try {
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
		} catch (Exception ex) {
//			setStatusMessage(10, "An unexpected error occurred creating the xml document for the DGS Package specified: " + filename + "  Error: " + ex.getLocalizedMessage());
		//	ex.printStackTrace();
			return (false);
		}
		try {
			doc = db.parse(inStream);
			doc.getDocumentElement().normalize();
		} catch (Exception ex) {
			ex.printStackTrace();
			return (false);
		}
		try {
			NodeList tLst = doc.getElementsByTagName("DGSCommands");
			int nLen = tLst.getLength();
			if (tLst.getLength() > 0) {
				NodeList nodeLst = tLst.item(0).getChildNodes();
				Node dgsCommands = tLst.item(0);
				nLen = nodeLst.getLength();
				this.commandString = dgsCommands.getTextContent();
				NamedNodeMap aMap = dgsCommands.getAttributes();
				Node templateBufferNode = aMap.getNamedItem("templateBuffer");
				Node animationDurationNode = aMap.getNamedItem("animationDuration");
				Node animationFramerateNode = aMap.getNamedItem("animationFramerate");

				if (templateBufferNode != null) {
					this.templateBuffer = templateBufferNode.getNodeValue();
					if ((this.templateBuffer == null) || (this.templateBuffer.length() == 0)) {
						this.templateBuffer = "main"; // assume the fallback
					}
				}

				String tStr;
				if (animationDurationNode != null) {
					tStr = animationDurationNode.getNodeValue();
					if ((tStr != null) && (tStr.length() > 0)) {
						try {
							this.animationDuration = Float.parseFloat(tStr);
							if (this.animationDuration < Float.MIN_VALUE) {
								this.animationDuration = 0.0f;
							}
						} catch (NumberFormatException ex) {
							ex.printStackTrace();
							return (false);
						}
					}
				}
				if (animationFramerateNode != null) {
					tStr = animationFramerateNode.getNodeValue();
					if ((tStr != null) && (tStr.length() > 0)) {
						try {
							this.animationFramerate = Float.parseFloat(tStr);
							if (this.animationFramerate < Float.MIN_VALUE) {
								this.animationFramerate = 0.0f;
							}
						} catch (NumberFormatException ex) {
							ex.printStackTrace();
							return (false);
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return (false);
		}

		// this try wraps the variables reading
		Node nameAttr;
		Node valueAttr;
		String vStr;
		NamedNodeMap aMap;
		NodeList nodeLst;
		Node fstNode;

		try {
			nodeLst = doc.getElementsByTagName("DGSVariable");

			int nLen = nodeLst.getLength();
			DGSVariable vars[] = new DGSVariable[nLen];
			for (int s = 0; s < nLen; s++) {
				fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					aMap = fstNode.getAttributes();
					nameAttr = aMap.getNamedItem("name");
					if (nameAttr == null) {
						// this is an invalid variable, skip it
						continue;
					}
					vStr = fstNode.getTextContent();
					if (vStr == null) {
						vStr = "";
					}
					if (vStr.isEmpty()) {
						valueAttr = aMap.getNamedItem("value");
						if (valueAttr != null) {
							vStr = valueAttr.getTextContent();
							if (vStr == null) {
								vStr = "";
							}
						} else {
							vStr = "";
						}
					}
					vars[s] = new DGSVariable(nameAttr.getNodeValue(), vStr);
					vars[s] = vars[s];
				}
			}
			this.variables = vars;
		} catch (Exception ex) {
			ex.printStackTrace();
			return (false);
		}

		try {
			nodeLst = doc.getElementsByTagName("DGSFile");

			int nLen = nodeLst.getLength();
			DGSFileInfo vars[] = new DGSFileInfo[nLen];
			for (int s = 0; s < nLen; s++) {
				fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					aMap = fstNode.getAttributes();
					vars[s] = new DGSFileInfo();
					vars[s].name = aMap.getNamedItem("name").getNodeValue();
					String nv = aMap.getNamedItem("data").getNodeValue();
					if(nv.startsWith("text:")) {
						vars[s].data = nv.substring(5).getBytes();
					} else if(nv.startsWith("base64:")) {
						vars[s].data = nv.substring(7).getBytes();
					} else {
						vars[s].data = DGS.dgs4j.ProcessingEngine.Base64.decode(nv);
					}
					vars[s].mimeType = "" + aMap.getNamedItem("mimeType").getNodeValue();
					vars[s].width = Integer.valueOf(aMap.getNamedItem("width").getNodeValue());
					vars[s].height = Integer.valueOf(aMap.getNamedItem("height").getNodeValue());
				}
			}
			this.files = vars;
		} catch (Exception ex) {
//			setStatusMessage(10, "loadImageFiles: An error occurred parsing the variable data in" + varFileName + "\": " + ex.getLocalizedMessage());
			ex.printStackTrace();
			return (false);
		}

		try {
			nodeLst = doc.getElementsByTagName("DGSStyleSheet");

			if (nodeLst != null) {
				int nLen = nodeLst.getLength();
				if (nLen > 0) {
					this.stylesheet = nodeLst.item(0).getTextContent();
					if (nLen > 1) {
						throw new Exception("Only one DGSStyleSheet is allowed.");
					}
				}
			}
		} catch (Exception ex) {
//			setStatusMessage(10, "loadImageFiles: An error occurred parsing the variable data in" + varFileName + "\": " + ex.getLocalizedMessage());
			ex.printStackTrace();
			return (false);
		}

		this.fileName = filename;
		return (true);
	}


	public boolean loadFile(String filename) {
		reset();
		File file = null;
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
		Document doc = null;
		try {
			file = new File(filename);
		} catch (Exception ex) {
//			setStatusMessage(10, "An unexpected error occurred creating the xml document for the DGS Package specified: " + filename + "  Error: " + ex.getLocalizedMessage());
			ex.printStackTrace();
			return (false);
		}

		boolean result = false;
		try {
			java.io.InputStream inStream = new java.io.FileInputStream(file);
			result = loadFileStream(inStream, filename);
			inStream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return (false);
		}
		return(result);
	}
}