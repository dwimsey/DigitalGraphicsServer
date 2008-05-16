/*
 * DGSPreviewerView.java
 */

package dgspreviewer;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

import ImageProcessor.*;
import ImageProcessor.ProcessingEngine.*;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * The application's main frame.
 */
public class DGSPreviewerView extends FrameView {

    private String lastLoadedFileName;
    private static ImageProcessor.ProcessingEngine.ProcessingEngine pEngine;

    public DGSPreviewerView(SingleFrameApplication app) {
        super(app);
        pEngine = new ImageProcessor.ProcessingEngine.ProcessingEngine();
        lastLoadedFileName = null;

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = DGSPreviewerApp.getApplication().getMainFrame();
            aboutBox = new DGSPreviewerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        DGSPreviewerApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new DGSPreviewerPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        refreshMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 249, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(dgspreviewer.DGSPreviewerApp.class).getContext().getResourceMap(DGSPreviewerView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(dgspreviewer.DGSPreviewerApp.class).getContext().getActionMap(DGSPreviewerView.class, this);
        openMenuItem.setAction(actionMap.get("loadFile")); // NOI18N
        openMenuItem.setName("openMenuItem"); // NOI18N
        fileMenu.add(openMenuItem);

        refreshMenuItem.setAction(actionMap.get("refreshImage")); // NOI18N
        refreshMenuItem.setName("jMenuItemRefresh"); // NOI18N
        fileMenu.add(refreshMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void loadFile() {
        JFileChooser fc = new JFileChooser("../examples");
        int choice = fc.showOpenDialog(mainPanel);
        if (choice == JFileChooser.APPROVE_OPTION) {
            java.io.File f = fc.getSelectedFile();
            lastLoadedFileName = f.getPath();
            loadImageFile(lastLoadedFileName);
        }
    }

    @Action
    public void refreshImage() {
        if(lastLoadedFileName != null) {
            loadImageFile(lastLoadedFileName);
        }
    }

    private void loadImageFile(String fileName)
    {
        
        mainPanel.image = null;
        mainPanel.invalidate();
        mainPanel.repaint();

        setStatusMessage("Reading image file: " + fileName);
        byte fDat[] = null;
        try {
            fDat = fileToBytes(fileName);
        } catch (FileNotFoundException fex) {
            setStatusMessage("Could not find the specified file: " + fileName);
        } catch (IOException iex) {
            setStatusMessage("Could not read the specified file: " + fileName + " Error: " + iex.getMessage());
        }
        if(fDat == null || fDat.length == 0) {
            return;
        }

        String outputMimeType = "image/png";

        setStatusMessage("Forming DGS Request ...");
        DGSRequestInfo dgsRequestInfo = new DGSRequestInfo();
        dgsRequestInfo.continueOnError = true;
        dgsRequestInfo.files = new DGSFileInfo[1];
        dgsRequestInfo.files[0] = new DGSFileInfo();
        dgsRequestInfo.files[0].name = "input.svg";
        dgsRequestInfo.files[0].mimeType = "image/svg+xml";
        dgsRequestInfo.files[0].data = fDat;
        dgsRequestInfo.files[0].width = -1;
        dgsRequestInfo.files[0].height = -1;
        dgsRequestInfo.variables = loadVariables("../examples/userVars.xml");
        dgsRequestInfo.instructionsXML = "<commands><load filename=\"input.svg\" buffer=\"main\" mimeType=\"image/svg+xml\" /><substituteVariables buffer=\"main\"/><save snapshotTime=\"1.0\" filename=\"output.png\" buffer=\"main\" mimeType=\"" + outputMimeType + "\" /></commands>";
        
        ProcessingWorkspace workspace = new ProcessingWorkspace(dgsRequestInfo);
        setStatusMessage("Performing DGS Request ...");
        DGSResponseInfo dgsResponseInfo = pEngine.processCommandString(workspace);
        setStatusMessage("DGS Request completed.");
        if(dgsResponseInfo.resultFiles.length == 0) {
            String plog[] = new String[dgsResponseInfo.processingLog.length];
            for(int i = 0; i<dgsResponseInfo.processingLog.length; i++) {
                plog[i] = dgsResponseInfo.processingLog[i];
            }
            javax.swing.JOptionPane.showMessageDialog(mainPanel, plog);
            setStatusMessage("No image files were returned by the processing engine, this generally indicates an error in the input file: " + fileName);
            return;
        }

        setStatusMessage("Updating display with new image ...");
        BufferedImage image = null;
        try {
            image = ImageIO.read(new java.io.ByteArrayInputStream((byte[])dgsResponseInfo.resultFiles[0].data));
        } catch (IOException ie) {
            setStatusMessage("Error processing output image: " + ie.getMessage());
        }
        mainPanel.image = image;
        this.mainPanel.repaint();
    }
    
    private byte[] fileToBytes(String fileName) throws FileNotFoundException, IOException
    {
        byte fDat[] = new byte[0];

        FileInputStream fs = new FileInputStream(fileName);
        int i = fs.available();
        fDat = new byte[i];
        i = fs.read(fDat);
        fs.close();
        return(fDat);
    }

    private DGSVariable[] loadVariables(String varFileName)
    {
        DGSVariable vars[] = null;
        File file = null;
        DocumentBuilderFactory dbf = null;
        DocumentBuilder db = null;
        Document doc = null;
        try {
            file = new File(varFileName);
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (Exception ex) {
            ex.printStackTrace();
            return(null);
        }
        try {
            doc = db.parse(file);
            doc.getDocumentElement().normalize();
        } catch (Exception ex) {
            ex.printStackTrace();
            return(null);
        }
        try {
//            System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList nodeLst = doc.getElementsByTagName("DGSVariable");

            int nLen = nodeLst.getLength();
            vars = new DGSVariable[nLen];
            for (int s = 0; s < nLen; s++) {
                Node fstNode = nodeLst.item(s);
                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap aMap = fstNode.getAttributes();
                    vars[s] = new DGSVariable(aMap.getNamedItem("name").getNodeValue(), aMap.getNamedItem("value").getNodeValue());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return(null);
        }

        return(vars);
    }

    private void setStatusMessage(String message)
    {
        statusMessageLabel.setText(message);
        statusMessageLabel.repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private DGSPreviewerPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem refreshMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}