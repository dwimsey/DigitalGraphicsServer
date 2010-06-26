/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rtsz.dgs4j.dgs4cl;

import org.apache.batik.util.ApplicationSecurityEnforcer;

import java.io.*;
import com.rtsz.dgs4j.*;
import com.rtsz.dgs4j.ProcessingEngine.*;
import com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.*;

/**
 *
 * @author dwimsey
 */
public class Main {

    public static void usage()
    {
        System.out.println("dgs4cl v");
        System.out.println("\t dgs4cl.jar -d DGSPackage.xml input.svg output.png");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        // Apply script security option
        ApplicationSecurityEnforcer securityEnforcer =
        new ApplicationSecurityEnforcer(Main.class.getClass(), "dgspreviewer/resources/DGSPreviewer.policy");

        securityEnforcer.enforceSecurity(false);

        DGSWorkspaceParsedURLProtocolHandler ph = new DGSWorkspaceParsedURLProtocolHandler();
        org.apache.batik.util.ParsedURL.registerHandler(ph);

        com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine pEngine = new com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine();

        String dgsTemplateFilename = "";
        String previewPackageFilename = "";
        String outputMimeType = "image/png";
        String outputFilename = "";
        boolean continueOnError = false;
        String current_arg;


        for(int i = 0; i < args.length; i++) {
            current_arg = args[i];
            if(current_arg.equals("-h")) {
                // we don't do anything after the -h so just bail out
                usage();
                return;
//            } else if(current_arg.equals("-t")) {
//                i++;
//                if(args.length>i) {
//
//                } else {
//                    System.out.println("-t option missing argument");
//                    return;
//                }
            } else if(current_arg.equals("-d")) {
                i++;
                if(args.length>i) {
                    previewPackageFilename = args[i];
                } else {
                    System.out.println("-d option missing argument");
                    return;
                }
            } else {
                if(current_arg.equals("-")) {
                    dgsTemplateFilename = "=".intern();
                } else if(current_arg.startsWith("-")) {
                    System.out.println("Unexpected option: " + current_arg);
                    usage();
                    return;
                } else {
                    // we assume this is an input file name
                    dgsTemplateFilename = current_arg.intern();
                }

                i++;
                if(args.length>i) {
                    outputFilename = args[i];
                } else {
                    // no output file name, just make one up based on the input file name and type
                    if(dgsTemplateFilename.equals("-")) {
                        outputFilename = "-".intern();
                    } else {
                        if(dgsTemplateFilename.endsWith(".svg")) {
                            outputFilename = dgsTemplateFilename.substring(0,dgsTemplateFilename.length()-4) + ".png";
                        }
                    }
                }

                processDGSPackage(pEngine, dgsTemplateFilename, previewPackageFilename, outputMimeType, outputFilename, continueOnError);
            }
        }

    }


    private static void processDGSPackage(String dgsTemplateFilename,
            String previewPackageFilename, String outputMimeType,
            String outputFilename, boolean continueOnError)
    {
        com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine pEngine = new com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine();
        processDGSPackage(pEngine, dgsTemplateFilename, previewPackageFilename, outputMimeType, outputFilename, continueOnError);
    }

    private static void processDGSPackage(com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine pEngine, 
            String dgsTemplateFilename, String previewPackageFilename, String outputMimeType,
            String outputFilename, boolean continueOnError)
    {
        DGSRequestInfo dgsRequestInfo = new DGSRequestInfo();
        dgsRequestInfo.continueOnError = continueOnError;

        DGSFileInfo templateFileInfo;
        try {
            templateFileInfo = loadImageFileData(dgsTemplateFilename);
        } catch(Exception e) {
            System.out.println("Could not open input file: " + dgsTemplateFilename + ": " + e.getMessage());
            return;
        }

        if(templateFileInfo == null) {
            System.out.println("Could not open input file: " + dgsTemplateFilename);
            return;
        }

        DGSPackage dPkg = new DGSPackage();
        if((previewPackageFilename == null) || (previewPackageFilename.length() == 0)) {
            dgsRequestInfo.files = new DGSFileInfo[1];
            dgsRequestInfo.variables = null;
        } else {
            if(dPkg.loadFile(previewPackageFilename)) {

                if(dPkg.files!=null && (dPkg.files.length>0)) {
                    dgsRequestInfo.files = new DGSFileInfo[dPkg.files.length+1];
                    for(int i = 0; i<dPkg.files.length; i++) {
                        dgsRequestInfo.files[i+1] = dPkg.files[i];
                    }
                } else {
                    dgsRequestInfo.files = new DGSFileInfo[1];
                }
                dgsRequestInfo.variables = dPkg.variables;
            } else {
                dgsRequestInfo.files = new DGSFileInfo[1];
                dgsRequestInfo.variables = null;
            }
        }











        dgsRequestInfo.files[0] = templateFileInfo;
        if((dgsRequestInfo.files[0].name == null) || (dgsRequestInfo.files[0].name.length() == 0)) {
            dgsRequestInfo.files[0].name = "input.svg"; // we need this set to Something, so set it ourselves
        }
        //if((dgsRequestInfo.files[0].mimeType == null) || (dgsRequestInfo.files[0].mimeType.length() == 0)) {
            dgsRequestInfo.files[0].mimeType = "image/svg+xml"; // we only process svg for now
        //}

        // Form the instruction xml fragment
        dgsRequestInfo.instructionsXML = "<commands><load filename=\"" + dgsRequestInfo.files[0].name + "\" buffer=\"" + dPkg.templateBuffer + "\" mimeType=\"image/svg+xml\" />";
        if(dPkg.commandString != null && dPkg.commandString.length() > 0) {
            dgsRequestInfo.instructionsXML += dPkg.commandString;
        } else {
            dgsRequestInfo.instructionsXML += "<substituteVariables buffer=\"main\" />";
        }
        //dgsRequestInfo.instructionsXML += "<addWatermark buffer=\"" + dPkg.templateBuffer + "\" srcImage=\"watermark\" opacity=\"0.05\"/>";
        dgsRequestInfo.instructionsXML += "<save ";
        if((dPkg.animationDuration>0.0f) && (dPkg.animationFramerate>0.0f)) {
            //dgsRequestInfo.instructionsXML += "animationDuration=\"" + dPkg.animationDuration + "\" animationFramerate=\"" + dPkg.animationFramerate + "\" ";
        }

        dgsRequestInfo.instructionsXML += "filename=\"" + outputFilename + "\" buffer=\"" + dPkg.templateBuffer + "\" mimeType=\"" + outputMimeType + "\" /></commands>";








        ProcessingWorkspace workspace = new ProcessingWorkspace(dgsRequestInfo);
        DGSResponseInfo dgsResponseInfo = pEngine.processCommandString(workspace);




        for(int i = 0; i < dgsResponseInfo.processingLog.length; i++) {
            //this.notificationMethods.logEvent(200, "     " + dgsResponseInfo.processingLog[i]);
        }
        //this.notificationMethods.logEvent(200, "-- END DGS Request Log --");

        if(dgsResponseInfo.resultFiles.length == 0) {
            //this.notificationMethods.statusMessage(10, "No image files were returned by the processing engine, this generally indicates an error in the input file: " + this.imageFilename);
            System.out.println("No image files were returned by the processing engine, this generally indicates an error in the input file: " + dgsTemplateFilename);
            return;
        }


        // save image data to filename provided
        java.io.FileOutputStream fs = null;
        try {
            fs = new java.io.FileOutputStream(outputFilename);
            try {
                fs.write(((byte[])dgsResponseInfo.resultFiles[0].data));
            } catch (Throwable t) {
                //this.notificationMethods.statusMessage(0, "Could not save file: " + t.getMessage());
                System.out.println("Could not save output file: " + outputFilename + ": " + t.getMessage());
                return;
            } finally {
                fs.close();
            }
        } catch (Throwable t) {
            //this.notificationMethods.statusMessage(0, "Could not open file: " + t.getMessage());
            System.out.println("Could not open output file: " + outputFilename + ": " + t.getMessage());
            return;
        }
    }

    private static DGSFileInfo loadImageFileData(String fileName) throws FileNotFoundException, IOException, Exception
    {
        byte fDat[] = null;
        fDat = fileToBytes(fileName);
        if(fDat == null) {
            //this.notificationMethods.statusMessage(5, "An unknown error occurred reading file: " + fileName);
            return(null);
        }
        if(fDat.length == 0) {
           //this.notificationMethods.statusMessage(10, "The specified file is empty: " + fileName);
            return(null);
        }
        DGSFileInfo fInfo = new DGSFileInfo();
        fInfo.data = fDat;
        fInfo.name = fileName;
        fInfo.width = -1;
        fInfo.height = -1;
        return(fInfo);
    }

    private static byte[] fileToBytes(String fileName) throws FileNotFoundException, IOException
    {
        byte fDat[] = new byte[0];

        FileInputStream fs = new FileInputStream(fileName);
        int i = fs.available();
        fDat = new byte[i];
        i = fs.read(fDat);
        fs.close();
        return(fDat);
    }
}