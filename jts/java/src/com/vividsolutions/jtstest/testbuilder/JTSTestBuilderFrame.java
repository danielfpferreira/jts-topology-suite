/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.testbuilder;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jtstest.clean.CleanDuplicatePoints;
import com.vividsolutions.jtstest.clean.LineStringExtracter;
import com.vividsolutions.jtstest.testbuilder.controller.JTSTestBuilderController;
import com.vividsolutions.jtstest.testbuilder.controller.ResultController;
import com.vividsolutions.jtstest.testbuilder.model.GeometryEvent;
import com.vividsolutions.jtstest.testbuilder.model.HtmlWriter;
import com.vividsolutions.jtstest.testbuilder.model.JavaTestWriter;
import com.vividsolutions.jtstest.testbuilder.model.TestBuilderModel;
import com.vividsolutions.jtstest.testbuilder.model.TestCaseEdit;
import com.vividsolutions.jtstest.testbuilder.model.XMLTestWriter;
import com.vividsolutions.jtstest.testbuilder.ui.ImageUtil;
import com.vividsolutions.jtstest.testbuilder.ui.SwingUtil;
import com.vividsolutions.jtstest.testbuilder.ui.dnd.FileDrop;
import com.vividsolutions.jtstest.testbuilder.ui.tools.EditVertexTool;
import com.vividsolutions.jtstest.testbuilder.ui.tools.InfoTool;
import com.vividsolutions.jtstest.testbuilder.ui.tools.LineStringTool;
import com.vividsolutions.jtstest.testbuilder.ui.tools.PanTool;
import com.vividsolutions.jtstest.testbuilder.ui.tools.PointTool;
import com.vividsolutions.jtstest.testbuilder.ui.tools.RectangleTool;
import com.vividsolutions.jtstest.testbuilder.ui.tools.StreamPolygonTool;
import com.vividsolutions.jtstest.testbuilder.ui.tools.ZoomToClickTool;
import com.vividsolutions.jtstest.testrunner.GuiUtil;
import com.vividsolutions.jtstest.util.FileUtil;
import com.vividsolutions.jtstest.util.StringUtil;

/**
 * The main frame for the JTS Test Builder.
 * 
 * @version 1.7
 */
public class JTSTestBuilderFrame extends JFrame 
{
    
  private static JTSTestBuilderFrame singleton = null;
  private ResultController resultController = new ResultController(this);
  private JTSTestBuilderMenuBar tbMenuBar = new JTSTestBuilderMenuBar(this);
  private JTSTestBuilderToolBar tbToolBar = new JTSTestBuilderToolBar(this);
  //---------------------------------------------
  JPanel contentPane;
  BorderLayout borderLayout1 = new BorderLayout();
  Border border4;
  JSplitPane jSplitPane1 = new JSplitPane();
  JPanel jPanel1 = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  TestCasePanel testCasePanel = new TestCasePanel();
  JPanel jPanel2 = new JPanel();
  JTabbedPane inputTabbedPane = new JTabbedPane();
  BorderLayout borderLayout3 = new BorderLayout();
  JPanel testPanel = new JPanel();
  WKTPanel wktPanel = new WKTPanel();
  TestListPanel testListPanel = new TestListPanel(this);
  //LayerListPanel layerListPanel = new LayerListPanel();
  LayerListPanel layerListPanel = new LayerListPanel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridLayout gridLayout1 = new GridLayout();
  ResultWKTPanel resultWKTPanel = new ResultWKTPanel();
  ResultValuePanel resultValuePanel = new ResultValuePanel();
  StatsPanel statsPanel = new StatsPanel();
  InfoPanel logPanel = new InfoPanel();
  private ZoomToClickTool zoomInTool;
  private final ImageIcon appIcon = new ImageIcon(this.getClass().getResource("app-icon.gif"));

  private JFileChooser fileChooser = new JFileChooser();
  private JFileChooser pngFileChooser;
  private JFileChooser fileAndDirectoryChooser = new JFileChooser();
  private JFileChooser directoryChooser = new JFileChooser();
  
  TestBuilderModel tbModel;
  
  private TextViewDialog textViewDlg = new TextViewDialog(this, "", true);
  private TestCaseTextDialog testCaseTextDlg = new TestCaseTextDialog(this,
      "", true);
  private GeometryInspectorDialog geomInspectorDlg = new GeometryInspectorDialog(this);
  /*
  private LoadTestCasesDialog loadTestCasesDlg = new LoadTestCasesDialog(this,
      "Load Test Cases", true);
*/
  
  
  /**
   *  Construct the frame
   */
  public JTSTestBuilderFrame() {
    try {
      Assert.isTrue(singleton == null);
      singleton = this;
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      setIconImage(appIcon.getImage());
      jbInit();
      //#setRollover was introduced in Java 1.4 and is not present in 1.3.1. [Jon Aquino]
      //jToolBar1.setRollover(true);
 //     initList(tcList);
      //loadEditList(testpp);
//      testCasePanel.setModel(tbModel);
      testCasePanel.spatialFunctionPanel.addSpatialFunctionPanelListener(
          new SpatialFunctionPanelListener() {
            public void functionExecuted(SpatialFunctionPanelEvent e) {
            	resultController.spatialFunctionPanel_functionExecuted(e);
            }
          });
      testCasePanel.scalarFunctionPanel.addSpatialFunctionPanelListener(
          new SpatialFunctionPanelListener() {
            public void functionExecuted(SpatialFunctionPanelEvent e) {
            	resultController.scalarFunctionPanel_functionExecuted(e);
            }
          });
      testCasePanel.editCtlPanel.btnSetPrecisionModel.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              precisionModelMenuItem_actionPerformed(e);
            }
          });
      testCasePanel.editCtlPanel.cbMagnifyTopo.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              revealTopo_actionPerformed();
            }
          });
      testCasePanel.editCtlPanel.stretchDist.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent e) {
          revealTopo_actionPerformed();
        }
      });

      Cursor zoomInCursor = Toolkit.getDefaultToolkit().createCustomCursor(
      		new ImageIcon(this.getClass().getResource("MagnifyCursor.gif")).getImage(),
          new java.awt.Point(16, 16), "Zoom In");
      zoomInTool = new ZoomToClickTool(2, zoomInCursor);
      showGeomsTab();
      initFileDrop(testCasePanel);
      testCasePanel.getGeometryEditPanel().setCurrentTool(RectangleTool.getInstance());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initFileDrop(Component comp) {
    new FileDrop(comp, new FileDrop.Listener() {
      public void filesDropped(java.io.File[] files) {
        try {
          openXmlFilesAndDirectories(files);
        } catch (Exception ex) {
          SwingUtil.reportException(null, ex);
        }
      }
    });
  }
  private void initFileChoosers() {
    if (pngFileChooser == null) {
      pngFileChooser = new JFileChooser();
      pngFileChooser.addChoosableFileFilter(SwingUtil.PNG_FILE_FILTER);
      pngFileChooser.setDialogTitle("Save PNG");
      pngFileChooser.setSelectedFile(new File("geoms.png"));
    }
  }
  
  public static JTSTestBuilderFrame instance() {
    if (singleton == null) {
      new JTSTestBuilderFrame();
    }
    return singleton;
  }

  public static GeometryEditPanel getGeometryEditPanel()
  {
    return instance().getTestCasePanel().getGeometryEditPanel();
  }
  
  public TestBuilderModel getModel()
  {
    return tbModel;
  }
  
  public void setModel(TestBuilderModel model)
  {
  	tbModel = model;
    testCasePanel.setModel(tbModel);
    wktPanel.setModel(model);
    resultWKTPanel.setModel(model);
    resultValuePanel.setModel(model);
    statsPanel.setModel(model);
    
    model.getGeometryEditModel().addGeometryListener(
        new com.vividsolutions.jtstest.testbuilder.model.GeometryListener() {
          public void geometryChanged(GeometryEvent e) {
            model_geometryChanged(e);
          }
        });
    
    testListPanel.populateList();
    //layerListPanel.init(getModel().getLayers());
    layerListPanel.populateList();
    updateTestCaseView();
    updatePrecisionModelDescription();
  }
  
  public static void reportException(Exception e) {
  	SwingUtil.reportException(instance(), e);
  }

  public void setCurrentTestCase(TestCaseEdit testCase) {
    tbModel.setCurrentTestCase(testCase);
    updateTestCaseView();
  }

  public TestCasePanel getTestCasePanel() {
    return testCasePanel;
  }

  public ResultWKTPanel getResultWKTPanel() {
    return resultWKTPanel;
  }

  public ResultValuePanel getResultValuePanel() {
    return resultValuePanel;
  }
  
  /**
   *  File | Exit action performed
   */
  public void jMenuFileExit_actionPerformed(ActionEvent e) {
    System.exit(0);
  }

  /**
   *  Help | About action performed
   */
  public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
    JTSTestBuilder_AboutBox dlg = new JTSTestBuilder_AboutBox(this);
    java.awt.Dimension dlgSize = dlg.getPreferredSize();
    java.awt.Dimension frmSize = getSize();
    java.awt.Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height
         - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.setVisible(true);
  }

  public void showGeomsTab()
  {
    inputTabbedPane.setSelectedIndex(inputTabbedPane.indexOfTab("Input"));
  }
  
  public void showResultWKTTab()
  {
    inputTabbedPane.setSelectedIndex(inputTabbedPane.indexOfTab("Result"));
  }
  public void showResultValueTab()
  {
    inputTabbedPane.setSelectedIndex(inputTabbedPane.indexOfTab("Value"));
  }
  
  public void showInfoTab()
  {
    inputTabbedPane.setSelectedIndex(inputTabbedPane.indexOfTab(AppStrings.LOG_TAB_LABEL));
  }
  
  public void openXmlFilesAndDirectories(File[] files) throws Exception {
    if (files.length == 1) {
      fileChooser.setSelectedFile(files[0]);
    }
    tbModel.openXmlFilesAndDirectories(files);
    reportProblemsParsingXmlTestFile(tbModel.getParsingProblems());
    updateTestCaseView();
    testListPanel.populateList();
    updatePrecisionModelDescription();
  }

  /**
   *  Overridden so we can exit when window is closed
   */
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      jMenuFileExit_actionPerformed(null);
    }
  }

  void model_geometryChanged(GeometryEvent e) {
    //testCasePanel.relatePanel.clearResults();
    JTSTestBuilderController.geometryViewChanged();
    updateWktPanel();
//    updateTestableGeometries();
  }

  void btnNewCase_actionPerformed(ActionEvent e) {
    tbModel.createNew();
    showGeomsTab();
    updateTestCaseView();
    testListPanel.populateList();
  }

  void btnPrevCase_actionPerformed(ActionEvent e) {
    tbModel.prevCase();
    updateTestCaseView();
  }

  void btnNextCase_actionPerformed(ActionEvent e) {
    tbModel.nextCase();
     updateTestCaseView();
  }

  void btnCopyCase_actionPerformed(ActionEvent e) {
    tbModel.copyCase();
    updateTestCaseView();
    testListPanel.populateList();
  }

  public void copyResultToTest() 
  {
    Object currResult = tbModel.getResult();
    if (! (currResult instanceof Geometry))
      return;
    tbModel.addCase(new Geometry[] { (Geometry) currResult, null }, 
    		"Result of " + tbModel.getOpName());
    updateTestCaseView();
    testListPanel.populateList();  
  }
  
  void btnExchangeGeoms_actionPerformed(ActionEvent e) {
    tbModel.getCurrentTestCaseEdit().exchange();
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
  }

  void btnDeleteCase_actionPerformed(ActionEvent e) {
    tbModel.deleteCase();
    updateTestCaseView();
    testListPanel.populateList();
  }

  /*
  void menuLoadTestCases_actionPerformed(ActionEvent e) {
    try {
      loadTestCasesDlg.show();
      TestCaseList tcl = loadTestCasesDlg.getList();
      loadTestCaseList(tcl, new PrecisionModel());
      refreshNavBar();
    }
    catch (Exception x) {
      reportException(this, x);
    }
  }

  void loadTestCaseList(TestCaseList tcl, PrecisionModel precisionModel) throws Exception {
    tbModel.setPrecisionModel(precisionModel);
    if (tcl != null) {
      loadEditList(tcl);
    }
    testListPanel.populateList();
  }
*/
  
  void menuExchangeGeom_actionPerformed(ActionEvent e) {
    tbModel.getCurrentTestCaseEdit().exchange();
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
  }

  void menuViewText_actionPerformed(ActionEvent e) {
    testCaseTextDlg.setTestCase(tbModel.getCurrentTestCaseEdit());
    testCaseTextDlg.setVisible(true);
  }

  void actionInspectGeometry() {
    int geomIndex = tbModel.getGeometryEditModel().getGeomIndex();
    geomInspectorDlg.setGeometry(
        geomIndex == 0 ? AppStrings.GEOM_LABEL_A : AppStrings.GEOM_LABEL_B,
        tbModel.getCurrentTestCaseEdit().getGeometry(geomIndex));
    geomInspectorDlg.setVisible(true);
  }

  void menuLoadXmlTestFile_actionPerformed(ActionEvent e) {
    try {
      fileChooser.removeChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
      fileChooser.addChoosableFileFilter(SwingUtil.XML_FILE_FILTER);
      fileChooser.setDialogTitle("Open XML Test File(s)");
      fileChooser.setMultiSelectionEnabled(true);
      if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
        File[] files = fileChooser.getSelectedFiles();
        if (files.length == 0) {
          files = new File[]{fileChooser.getSelectedFile()};
        }
        openXmlFilesAndDirectories(files);
      }
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  void menuSaveAsXml_actionPerformed(ActionEvent e) {
    try {
      fileChooser.removeChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
      fileChooser.addChoosableFileFilter(SwingUtil.XML_FILE_FILTER);
      fileChooser.setDialogTitle("Save XML Test File");
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
        File file = fileChooser.getSelectedFile();
        if (! SwingUtil.confirmOverwrite(this, file)) return;
        FileUtil.setContents(fileChooser.getSelectedFile().getPath(), 
        		XMLTestWriter.getRunXml(tbModel.getTestCaseList(), tbModel.getPrecisionModel()) );
      }
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  public String getRunXml() 
  {
  	return XMLTestWriter.getRunXml(tbModel.getTestCaseList(), tbModel.getPrecisionModel());
  }
  
  void menuSaveAsJava_actionPerformed(ActionEvent e) {
    try {
      fileChooser.removeChoosableFileFilter(SwingUtil.XML_FILE_FILTER);
      fileChooser.addChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
      fileChooser.setDialogTitle("Save Java File");
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
        File file = fileChooser.getSelectedFile();
        if (! SwingUtil.confirmOverwrite(this, file)) return;
        String className = fileChooser.getSelectedFile().getName();
        int extensionIndex = className.lastIndexOf(".");
        if (extensionIndex > 0) {
          className = className.substring(0, extensionIndex);
        }
        ;
        FileUtil.setContents(fileChooser.getSelectedFile().getPath(), JavaTestWriter.getRunJava(className, tbModel));
      }
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  void menuSaveAsHtml_actionPerformed(ActionEvent e) {
    try {
      directoryChooser.setDialogTitle("Select Folder In Which To Save HTML and GIF Files");
      if (JFileChooser.APPROVE_OPTION == directoryChooser.showSaveDialog(this)) {
        int choice = JOptionPane.showConfirmDialog(this,
            "Would you like the spatial function images "
             + "to show the A and B geometries?", "Confirmation",
            JOptionPane.YES_NO_CANCEL_OPTION);
        final HtmlWriter writer = new HtmlWriter();
        switch (choice) {
          case JOptionPane.CANCEL_OPTION:
            return;
          case JOptionPane.YES_OPTION:
            writer.setShowingABwithSpatialFunction(true);
            break;
          case JOptionPane.NO_OPTION:
            writer.setShowingABwithSpatialFunction(false);
            break;
        }
        final File directory = directoryChooser.getSelectedFile();
        Assert.isTrue(directory.exists());
        //        BusyDialog.setOwner(this);
        //        BusyDialog busyDialog = new BusyDialog();
        //        writer.setBusyDialog(busyDialog);
        //        try {
        //          busyDialog.execute("Saving .html and .gif files", new BusyDialog.Executable() {
        //            public void execute() throws Exception {
        writer.write(directory, tbModel.getTestCaseList(), tbModel.getPrecisionModel());
        //            }
        //          });
        //        }
        //        catch (Exception e2) {
        //          System.out.println(busyDialog.getStackTrace());
        //          throw e2;
        //        }
      }
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  void menuSaveAsPNG_actionPerformed(ActionEvent e) {
    initFileChoosers();
    try {
      String fullFileName = SwingUtil.chooseFilenameWithConfirm(this, pngFileChooser);  
      if (fullFileName == null) return;
        ImageUtil.writeImage(testCasePanel.getGeometryEditPanel(), 
            fullFileName,
            ImageUtil.IMAGE_FORMAT_NAME_PNG);
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  void menuSaveScreenToClipboard_actionPerformed(ActionEvent e) {
    try {
        ImageUtil.saveImageToClipboard(testCasePanel.getGeometryEditPanel(), 
        		ImageUtil.IMAGE_FORMAT_NAME_PNG);
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  void drawRectangleButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(RectangleTool.getInstance());
  }

  void drawPolygonButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(StreamPolygonTool.getInstance());
  }

  void drawLineStringButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(LineStringTool.getInstance());
  }

  void drawPointButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(PointTool.getInstance());
  }

  void infoButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(InfoTool.getInstance());
  }

  void zoomInButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(zoomInTool);
  }

  void oneToOneButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().getViewport().zoomToInitialExtent();
  }

  void zoomToFullExtentButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToFullExtent();
  }

  void zoomToResult_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToResult();
  }

  void zoomToInputButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToInput();
  }

  void zoomToInputA_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToGeometry(0);
  }

  void zoomToInputB_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToGeometry(1);
  }

  void panButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(PanTool.getInstance());
  }

  void deleteAllTestCasesMenuItem_actionPerformed(ActionEvent e) {
    tbModel.initTestCaseList();
    updateTestCaseView();
    testListPanel.populateList();
  }

  public void setShowingGrid(boolean showGrid) {
    testCasePanel.editPanel.setGridEnabled(showGrid);
    JTSTestBuilderController.geometryViewChanged();
  }

  public void setShowingStructure(boolean showStructure) {
    TestBuilderModel.setShowingStructure(showStructure);
    JTSTestBuilderController.geometryViewChanged();
  }

  public void setShowingOrientations(boolean showingOrientations) {
    TestBuilderModel.setShowingOrientation(showingOrientations);
    JTSTestBuilderController.geometryViewChanged();
  }

  public void setShowVertexIndices(boolean showVertexIndices) {
    TestBuilderModel.setShowingOrientation(showVertexIndices);
    JTSTestBuilderController.geometryViewChanged();
  }

  public void setShowingVertices(boolean showingVertices) {
    TestBuilderModel.setShowingVertices(showingVertices);
    JTSTestBuilderController.geometryViewChanged();
  }

  void showVertexIndicesMenuItem_actionPerformed(ActionEvent e) {
//    testCasePanel.editPanel.setShowVertexIndices(showVertexIndicesMenuItem.isSelected());
  }

  void menuLoadXmlTestFolder_actionPerformed(ActionEvent e) {
    try {
      directoryChooser.removeChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
      directoryChooser.setDialogTitle("Open Folder(s) Containing XML Test Files");
      directoryChooser.setMultiSelectionEnabled(true);
      if (JFileChooser.APPROVE_OPTION == directoryChooser.showOpenDialog(this)) {
        File[] files = directoryChooser.getSelectedFiles();
        if (files.length == 0) {
          files = new File[]{fileChooser.getSelectedFile()};
        }
        openXmlFilesAndDirectories(files);
      }
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  void precisionModelMenuItem_actionPerformed(ActionEvent e) {
    try {
      PrecisionModelDialog precisionModelDialog = new PrecisionModelDialog(
          this, "Edit Precision Model", true);
      GuiUtil.center(precisionModelDialog, this);
      precisionModelDialog.setPrecisionModel(tbModel.getPrecisionModel());
      precisionModelDialog.setVisible(true);
      tbModel.changePrecisionModel(precisionModelDialog.getPrecisionModel());
      updatePrecisionModelDescription();
      updateGeometry();
    }
    catch (ParseException pe) {
      SwingUtil.reportException(this, pe);
    }
  }
  void revealTopo_actionPerformed() {
  	tbModel.setMagnifyingTopology(testCasePanel.editCtlPanel.cbMagnifyTopo.isSelected());
    tbModel.setTopologyStretchSize(testCasePanel.editCtlPanel.getStretchSize());
    JTSTestBuilderController.geometryViewChanged();
  }


  /**
   *  Component initialization
   */
  private void jbInit() throws Exception {
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    fileAndDirectoryChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fileAndDirectoryChooser.setMultiSelectionEnabled(true);
    directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    directoryChooser.setMultiSelectionEnabled(false);
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = textViewDlg.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    textViewDlg.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height
         - frameSize.height) / 2);
    /*
    loadTestCasesDlg.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height
         - frameSize.height) / 2);
         */
    
    //---------------------------------------------------
    contentPane = (JPanel) this.getContentPane();
    border4 = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white,
        Color.white, new Color(93, 93, 93), new Color(134, 134, 134));
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(800, 800));
    this.setTitle("JTS TestBuilder");
    
    /*
    testCasePanel.editPanel.addGeometryListener(
      new com.vividsolutions.jtstest.testbuilder.model.GeometryListener() {

        public void geometryChanged(GeometryEvent e) {
          editPanel_geometryChanged(e);
        }
      });
*/    
    
    jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jSplitPane1.setPreferredSize(new Dimension(601, 690));
    jPanel1.setLayout(borderLayout2);
    jPanel1.setMinimumSize(new Dimension(431, 0));
    contentPane.setPreferredSize(new Dimension(601, 690));
    inputTabbedPane.setTabPlacement(JTabbedPane.LEFT);
    jPanel2.setLayout(borderLayout3);
    wktPanel.setMinimumSize(new Dimension(111, 0));
    wktPanel.setPreferredSize(new Dimension(600, 100));
    wktPanel.setToolTipText(AppStrings.TEXT_ENTRY_TIP);
    testPanel.setLayout(gridBagLayout2);
    gridLayout1.setRows(4);
    gridLayout1.setColumns(1);
    
    contentPane.add(jSplitPane1, BorderLayout.CENTER);
    jSplitPane1.add(jPanel1, JSplitPane.TOP);
    jPanel1.add(testCasePanel, BorderLayout.CENTER);
    jSplitPane1.add(jPanel2, JSplitPane.BOTTOM);
    jPanel2.add(inputTabbedPane, BorderLayout.CENTER);
    jSplitPane1.setBorder(new EmptyBorder(2,2,2,2));
    jSplitPane1.setResizeWeight(0.5);
    inputTabbedPane.add(testListPanel, "Cases");
    inputTabbedPane.add(wktPanel,  "Input");
    inputTabbedPane.add(resultWKTPanel, "Result");
    inputTabbedPane.add(resultValuePanel, "Value");
    inputTabbedPane.add(statsPanel, "Stats");
    inputTabbedPane.add(logPanel, AppStrings.LOG_TAB_LABEL);
    inputTabbedPane.add(layerListPanel, "Layers");
    inputTabbedPane.setSelectedIndex(1);
    inputTabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e)
      {
        updateStatsPanelIfVisible();
        }
    });
    
    jSplitPane1.setDividerLocation(500);
    this.setJMenuBar(tbMenuBar.getMenuBar());
    contentPane.add(tbToolBar.getToolBar(), BorderLayout.NORTH);
  }

  private void updateStatsPanelIfVisible()
  {
    int index = inputTabbedPane.getSelectedIndex();
    if (index < 0) return;
    if (inputTabbedPane.getComponent(index) == statsPanel) {
      statsPanel.refresh();         
    }   
  }
  
  private void updateGeometry() {
    testCasePanel.relatePanel.clearResults();
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
    updateWktPanel();
  }

  private void updateWktPanel() {
    Geometry g0 = tbModel.getGeometryEditModel().getGeometry(0);
    wktPanel.setText(g0, 0);
    Geometry g1 = tbModel.getGeometryEditModel().getGeometry(1);
    wktPanel.setText(g1, 1);
  }

  private void updatePrecisionModelDescription() {
    testCasePanel.setPrecisionModelDescription(tbModel.getPrecisionModel().toString());
  }

  void updateTestCaseView() {
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
    getTestCasePanel().setCurrentTestCaseIndex(tbModel.getCurrentTestIndex() + 1);
    getTestCasePanel().setMaxTestCaseIndex(tbModel.getTestListSize());
    updateWktPanel();
    updateStatsPanelIfVisible();
  }

  public void displayInfo(Coordinate modelPt)
  {
    displayInfo(
        testCasePanel.getGeometryEditPanel().getInfo(modelPt)
        );
  }
  
  public void displayInfo(String s)
  {
    displayInfo(s, true);
  }
  
  public void displayInfo(String s, boolean showTab)
  {
    logPanel.addInfo(s);
    if (showTab) showInfoTab();
  }
  
  private void reportProblemsParsingXmlTestFile(List parsingProblems) {
    if (parsingProblems.isEmpty()) {
      return;
    }
    for (Iterator i = parsingProblems.iterator(); i.hasNext(); ) {
      String problem = (String) i.next();
      System.out.println(problem);
    }
    JOptionPane.showMessageDialog(this, StringUtil.wrap(parsingProblems.size()
         + " problems occurred parsing the XML test file."
         + " The first problem was: " + parsingProblems.get(0), 80),
        "Error", JOptionPane.ERROR_MESSAGE);
  }


  void menuRemoveDuplicatePoints_actionPerformed(ActionEvent e) {
    CleanDuplicatePoints clean = new CleanDuplicatePoints();
    Geometry cleanGeom = clean.clean(tbModel.getCurrentTestCaseEdit().getGeometry(0));
    tbModel.getCurrentTestCaseEdit().setGeometry(0, cleanGeom);
    updateGeometry();
  }

  void menuChangeToLines_actionPerformed(ActionEvent e) {
    LineStringExtracter lse = new LineStringExtracter();
    Geometry cleanGeom = lse.extract(tbModel.getCurrentTestCaseEdit().getGeometry(0));
    tbModel.getCurrentTestCaseEdit().setGeometry(0, cleanGeom);
    updateGeometry();
  }

  void btnEditVertex_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(EditVertexTool.getInstance());
  }

  private Coordinate pickOffset(Geometry a, Geometry b) {
    if (a != null && ! a.isEmpty()) {
      return a.getCoordinates()[0];
    }
    if (b != null && ! b.isEmpty()) {
      return b.getCoordinates()[0];
    }
    return null;
  }

}

