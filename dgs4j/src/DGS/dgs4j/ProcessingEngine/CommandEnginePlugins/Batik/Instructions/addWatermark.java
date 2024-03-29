/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.Instructions;

import DGS.dgs4j.ProcessingEngine.Instructions.IInstruction;
import DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.CommandEngine;
import DGS.dgs4j.ProcessingEngine.ProcessingEngineImageBuffer;
import DGS.dgs4j.ProcessingEngine.ProcessingWorkspace;
import DGS.dgs4j.ProcessingEngine.Base64;
import DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.CommandEngine.*;

import org.w3c.dom.*;

import java.io.*;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 * @author dwimsey
 */
public class addWatermark implements IInstruction {

	String instructionName = "addWatermark";
	String xlinkNS = "http://www.w3.org/1999/xlink";

	public boolean process(ProcessingWorkspace workspace, Node instructionNode) {
		NamedNodeMap nm = instructionNode.getAttributes();
		Node bufferNode = nm.getNamedItem("buffer");
		Node srcImageNode = nm.getNamedItem("srcImage");
		if (bufferNode == null) {
			workspace.logFatal(instructionName + " command failed: no buffer attribute specified.");
			return (false);
		}
		if (srcImageNode == null) {
			workspace.logFatal(instructionName + " command failed: no srcImageNode attribute specified.");
			return (false);
		}

		String bufferName = bufferNode.getNodeValue();
		String srcImageName = srcImageNode.getNodeValue();
		if (bufferName == null || bufferName.length() == 0) {
			workspace.logFatal(instructionName + " command failed: buffer attribute has no data.");
			return (false);
		}
		if (srcImageName == null || srcImageName.length() == 0) {
			workspace.logFatal(instructionName + " command failed: srcImageNode attribute has no data.");
			return (false);
		}

		ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
		if (iBuffer == null) {
			workspace.logFatal(instructionName + " command failed: There is no buffer named '" + bufferName + "'.");
			return (false);
		}
		if ((!iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) && (!iBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE))) {
			workspace.logFatal(instructionName + " command failed: Buffer is not of type '" + CommandEngine.MIME_BUFFERTYPE + "' or '" + CommandEngine.INTERNAL_BUFFERTYPE + "' and no conversion is available: " + bufferName);
			return (false);
		}

		String opacityStr = "0.1";
		Node opacityNode = nm.getNamedItem("opacity");
		if (opacityNode != null) {
			String tStr = opacityNode.getNodeValue();
			if (tStr != null) {
				if (tStr.length() > 0) {
					opacityStr = tStr;
				}
			}
		}

		org.apache.batik.dom.svg.SVGOMDocument doc = null;

		if (iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
			try {
				doc = (org.apache.batik.dom.svg.SVGOMDocument)CommandEngine.svgBytes2Doc((byte[])iBuffer.data);
			} catch (IOException ex) {
				workspace.logFatal("An error occurred parsing the SVG file data in the addWatermark command: " + ex.getMessage());
				return (false);
			}
		} else {
			doc = (org.apache.batik.dom.svg.SVGOMDocument) iBuffer.data;
		}

		ProcessingEngineImageBuffer imgBuffer = workspace.getImageBuffer(srcImageName);
		if (imgBuffer == null) {
			workspace.logFatal(instructionName + " command failed: There is no buffer named '" + srcImageName + "' to get the watermark image from.");
			return (false);
		}
		if (!imgBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE) && !imgBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE) && !imgBuffer.mimeType.equals("image/png") && !imgBuffer.mimeType.equals("image/jpeg") && !imgBuffer.mimeType.equals("image/tiff")) {
			return (false);
		}

		String dataUri = "";
		if (imgBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE)) {
			Document imgDoc = (Document) imgBuffer.data;
			dataUri = "data://" + CommandEngine.MIME_BUFFERTYPE + ";base64,";
			TransformerFactory tf = TransformerFactory.newInstance();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			Transformer t = null;
			try {
				dataUri += Base64.encodeBytes(CommandEngine.svgDoc2Bytes((Document)imgBuffer.data));
			} catch (Exception ex) {
				workspace.logFatal("An error occurred while reconstructing the XML file after replaceImage call: " + ex.getMessage());
				return (false);
			}
		} else {
			dataUri = "data://" + imgBuffer.mimeType.trim() + ";base64,";
			dataUri += Base64.encodeBytes((byte[]) imgBuffer.data);
		}

		org.w3c.dom.svg.SVGSVGElement rootNode = doc.getRootElement();

		org.w3c.dom.Element wmNode = doc.createElement("image");
		wmNode.setAttribute("x", rootNode.getAttribute("x"));
		wmNode.setAttribute("y", rootNode.getAttribute("y"));
		wmNode.setAttribute("height", rootNode.getAttribute("height"));
		wmNode.setAttribute("width", rootNode.getAttribute("width"));
		wmNode.setAttribute("opacity", opacityStr);
		wmNode.setAttribute("xlink:href", dataUri);
		rootNode.appendChild(wmNode);

		// TODO: this extra conversion should be fixed somehow to speed things up
		try {
			iBuffer.data = CommandEngine.svgDoc2Bytes(doc);
		} catch (Exception ex) {
			workspace.logFatal("An error occurred while reconstructing the XML file after replaceImage call: " + ex.getMessage());
			return (false);
		}

		if (iBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE)) {
			try {
				iBuffer.data = CommandEngine.svgBytes2Doc((byte[])iBuffer.data);
			} catch (Exception ex) {
				workspace.logFatal("An error occurred re-parsing the SVG file data in the addWatermark command: " + ex.getMessage());
				return (false);
			}
		}
		return (true);
	}
}
