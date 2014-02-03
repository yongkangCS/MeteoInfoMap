/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteoinfo.forms;

import org.meteoinfo.data.mapdata.MapDataManage;
import org.meteoinfo.global.event.ActiveMapFrameChangedEvent;
import org.meteoinfo.global.event.ElementSelectedEvent;
import org.meteoinfo.global.event.GraphicSelectedEvent;
import org.meteoinfo.global.event.IActiveMapFrameChangedListener;
import org.meteoinfo.global.event.IElementSelectedListener;
import org.meteoinfo.global.event.IGraphicSelectedListener;
import org.meteoinfo.global.event.IZoomChangedListener;
import org.meteoinfo.global.event.ZoomChangedEvent;
import org.meteoinfo.global.FrmProperty;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointF;
import org.meteoinfo.layer.FrmLabelSet;
import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.layout.ElementType;
import org.meteoinfo.layout.FrmPageSet;
import org.meteoinfo.layout.LayoutGraphic;
import org.meteoinfo.layout.MouseMode;
import org.meteoinfo.legend.LayerNode;
import org.meteoinfo.legend.LayersLegend;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.legend.NodeTypes;
import org.meteoinfo.legend.VectorBreak;
import org.meteoinfo.map.MouseTools;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.Reproject;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.WindArraw;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.xml.parsers.ParserConfigurationException;
import meteoinfo.classes.GenericFileFilter;
import meteoinfo.classes.Options;
import meteoinfo.classes.Plugin;
import meteoinfo.classes.ProjectFile;
import org.meteoinfo.global.GlobalUtil;
import org.meteoinfo.map.MapView;
import org.meteoinfo.plugin.IApplication;
import org.meteoinfo.plugin.IPlugin;
import org.xml.sax.SAXException;

/**
 *
 * @author Yaqiang Wang
 */
public class FrmMainOld extends javax.swing.JFrame implements IApplication {

    // <editor-fold desc="Variables">
    private String _startupPath;
    private Options _options = new Options();
    private JButton _currentTool = null;
    ResourceBundle bundle;
    ProjectFile _projectFile;
    private boolean _isEditingVertices = false;
    private boolean _isLoading = false;
    private FrmMeteoData _frmMeteoData;
    //private String _currentDataFolder = "";
    private ImageIcon _loadedPluginIcon;
    private ImageIcon _unloadedPluginIcon;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Creates new form frmMain
     */
    public FrmMainOld() {
        //Locale.setDefault(Locale.ENGLISH);
        initComponents();

        _mapDocument.addActiveMapFrameChangedListener(new IActiveMapFrameChangedListener() {
            @Override
            public void activeMapFrameChangedEvent(ActiveMapFrameChangedEvent event) {
                _mapView = _mapDocument.getActiveMapFrame().getMapView();
                setMapView();
                if (jTabbedPane_Main.getSelectedIndex() == 0) {
                    _mapView.paintLayers();
                }
            }
        });
        _mapLayout.addElementSelectedListener(new IElementSelectedListener() {
            @Override
            public void elementSelectedEvent(ElementSelectedEvent event) {
                if (_mapLayout.getSelectedElements().size() > 0) {
                    if (_mapLayout.getSelectedElements().get(0).getElementType() == ElementType.LayoutGraphic) {
                        switch (((LayoutGraphic) _mapLayout.getSelectedElements().get(0)).getGraphic().getShape().getShapeType()) {
                            case Polyline:
                            case CurveLine:
                            case Polygon:
                            case CurvePolygon:
                                jButton_EditVertices.setEnabled(true);
                                break;
                            default:
                                jButton_EditVertices.setEnabled(false);
                                break;
                        }
                    }
                } else {
                    jButton_EditVertices.setEnabled(false);
                }
            }
        });
        _mapLayout.addZoomChangedListener(new IZoomChangedListener() {
            @Override
            public void zoomChangedEvent(ZoomChangedEvent event) {
                jComboBox_PageZoom.setSelectedItem(String.valueOf((int) (_mapDocument.getMapLayout().getZoom() * 100)) + "%");
            }
        });
        _mapLayout.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                layout_MouseMoved(e);
            }
        });

        this.jPanel_MapTab.setLayout(new BorderLayout());
        _mapLayout.setFocusable(true);
        _mapLayout.requestFocusInWindow();

        _projectFile = new ProjectFile(this);
        this._mapDocument.getActiveMapFrame().setMapView(_mapView);
        this._mapDocument.setMapLayout(_mapLayout);
        this._mapDocument.setIsLayoutView(false);
        this._options.setLegendFont(_mapDocument.getFont());

        BufferedImage image = null;
        try {
            image = ImageIO.read(this.getClass().getResource("/meteoinfo/resources/MeteoInfo_1_16x16x8.png"));
        } catch (Exception e) {
        }
        this.setIconImage(image);
        this.setTitle("MeteoInfo");
        this.setSize(1000, 650);
        this.jMenuItem_Layers.setSelected(true);
        this.jButton_SelectElement.doClick();

        //this._startupPath = GlobalUtil.getAppPath(FrmMainOld.class);
        this._startupPath = System.getProperty("user.dir");
        String pluginPath = this._startupPath + File.separator + "plugins";
        this._options.setPluginPath(pluginPath);

        loadForm();
    }

    private void loadForm() {
        _isLoading = true;

        //Set layout zoom combobox
        this.jComboBox_PageZoom.removeAllItems();
        String[] zooms = new String[]{"20%", "50%", "75%", "100%", "150%", "200%", "300%"};
        for (String zoom : zooms) {
            this.jComboBox_PageZoom.addItem(zoom);
        }
        this.jComboBox_PageZoom.setSelectedItem(String.valueOf((int) (_mapDocument.getMapLayout().getZoom() * 100)) + "%");
        try {
            this._loadedPluginIcon = new ImageIcon(ImageIO.read(this.getClass().getResource("/meteoinfo/resources/plugin_green.png")));
            this._unloadedPluginIcon = new ImageIcon(ImageIO.read(this.getClass().getResource("/meteoinfo/resources/plugin_unsel.png")));
        } catch (IOException ex) {
            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.loadDefaultPojectFile();
        this.loadConfigureFile();
        try {
            this.loadPlugins(this._options.getPlugins());
        } catch (MalformedURLException ex) {
            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
        }
        _mapView = _mapDocument.getActiveMapFrame().getMapView();
        setMapView();
        //_mapView.setIsLayoutMap(false);
        //_mapView.zoomToExtent(_mapView.getViewExtent());

        this.jToolBar_Layout.setEnabled(false);
        for (Component c : this.jToolBar_Layout.getComponents()) {
            c.setEnabled(false);
        }
        this.jMenuItem_LayoutProperty.setEnabled(false);
        this.jMenuItem_InsertLegend.setEnabled(false);
        this.jMenuItem_InsertTitle.setEnabled(false);
        this.jMenuItem_InsertText.setEnabled(false);
        this.jMenuItem_InsertNorthArrow.setEnabled(false);
        this.jMenuItem_InsertScaleBar.setEnabled(false);
        this.jMenuItem_InsertWindArrow.setEnabled(false);

        _isLoading = false;
    }

    private void setMapView() {
        //Add map view 
        _mapView.setLockViewUpdate(true);
        this.jPanel_MapTab.removeAll();
        this.jPanel_MapTab.add(_mapView, BorderLayout.CENTER);
        _mapView.setLockViewUpdate(false);
        if (_currentTool != null) {
            _currentTool.doClick();
        }

        _mapView.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mapView_MouseMoved(e);
            }
        });
        _mapView.addGraphicSelectedListener(new IGraphicSelectedListener() {
            @Override
            public void graphicSelectedEvent(GraphicSelectedEvent event) {
                if (_mapView.getSelectedGraphics().size() > 0) {
                    switch (_mapView.getSelectedGraphics().get(0).getShape().getShapeType()) {
                        case Polyline:
                        case CurveLine:
                        case Polygon:
                        case CurvePolygon:
                            jButton_EditVertices.setEnabled(true);
                            break;
                        default:
                            jButton_EditVertices.setEnabled(false);
                            break;
                    }
                } else {
                    jButton_EditVertices.setEnabled(false);
                }
            }
        });

        _mapView.setFocusable(true);
        _mapView.requestFocusInWindow();

//            tabControl1.TabPages[0].Controls.Clear();
//            tabControl1.TabPages[0].Controls.Add(_mapView);
//            _mapView.Dock = DockStyle.Fill;
//            _mapView.MouseMove += new MouseEventHandler(this.MapView_MouseMove);
//            _mapView.MouseDown += new MouseEventHandler(this.MapView_MouseDown);
//            _mapView.GraphicSeleted += new EventHandler(this.MapView_GraphicSelected);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator13 = new javax.swing.JSeparator();
        jSeparator14 = new javax.swing.JSeparator();
        jPanel_MainToolBar = new javax.swing.JPanel();
        jToolBar_Base = new javax.swing.JToolBar();
        jButton_AddLayer = new javax.swing.JButton();
        jButton_OpenData = new javax.swing.JButton();
        jButton_RemoveDataLayers = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton_SelectElement = new javax.swing.JButton();
        jButton_ZoomIn = new javax.swing.JButton();
        jButton_ZoomOut = new javax.swing.JButton();
        jButton_Pan = new javax.swing.JButton();
        jButton_FullExtent = new javax.swing.JButton();
        jButton_ZoomToLayer = new javax.swing.JButton();
        jButton_ZoomToExtent = new javax.swing.JButton();
        jButton_Identifer = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton_SelectFeatures = new javax.swing.JButton();
        jButton_Measurement = new javax.swing.JButton();
        jButton_LabelSet = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton_SavePicture = new javax.swing.JButton();
        jToolBar_Graphic = new javax.swing.JToolBar();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jButton_NewLabel = new javax.swing.JButton();
        jButton_NewPoint = new javax.swing.JButton();
        jButton_NewPolyline = new javax.swing.JButton();
        jButton_NewFreehand = new javax.swing.JButton();
        jButton_NewCurve = new javax.swing.JButton();
        jButton_NewPolygon = new javax.swing.JButton();
        jButton_NewCurvePolygon = new javax.swing.JButton();
        jButton_NewRectangle = new javax.swing.JButton();
        jButton_NewCircle = new javax.swing.JButton();
        jButton_NewEllipse = new javax.swing.JButton();
        jButton_EditVertices = new javax.swing.JButton();
        jToolBar_Layout = new javax.swing.JToolBar();
        jSeparator15 = new javax.swing.JToolBar.Separator();
        jButton_PageSet = new javax.swing.JButton();
        jButton_PageZoomIn = new javax.swing.JButton();
        jButton_PageZoomOut = new javax.swing.JButton();
        jButton_FitToScreen = new javax.swing.JButton();
        jComboBox_PageZoom = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane_Main = new javax.swing.JTabbedPane();
        jPanel_MapTab = new javax.swing.JPanel();
        _mapView = new org.meteoinfo.map.MapView();
        jPanel_LayoutTab = new javax.swing.JPanel();
        _mapLayout = new org.meteoinfo.layout.MapLayout();
        _mapDocument = new org.meteoinfo.legend.LayersLegend();
        jPanel5 = new javax.swing.JPanel();
        jLabel_Status = new javax.swing.JLabel();
        jLabel_Coordinate = new javax.swing.JLabel();
        jMenuBar_Main = new javax.swing.JMenuBar();
        jMenu_Project = new javax.swing.JMenu();
        jMenuItem_Open = new javax.swing.JMenuItem();
        jMenuItem_Save = new javax.swing.JMenuItem();
        jMenuItem_SaveAs = new javax.swing.JMenuItem();
        jMenu_View = new javax.swing.JMenu();
        jMenuItem_Layers = new javax.swing.JMenuItem();
        jMenuItem_AttributeData = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_LayoutProperty = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_MapProperty = new javax.swing.JMenuItem();
        jMenuItem_MaskOut = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_Projection = new javax.swing.JMenuItem();
        jMenu_Insert = new javax.swing.JMenu();
        jMenuItem_InsertMapFrame = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_InsertTitle = new javax.swing.JMenuItem();
        jMenuItem_InsertText = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_InsertLegend = new javax.swing.JMenuItem();
        jMenuItem_InsertScaleBar = new javax.swing.JMenuItem();
        jMenuItem_InsertNorthArrow = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_InsertWindArrow = new javax.swing.JMenuItem();
        jMenu_Selection = new javax.swing.JMenu();
        jMenuItem_SelByAttr = new javax.swing.JMenuItem();
        jMenuItem_SelByLocation = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_ClearSelection = new javax.swing.JMenuItem();
        jMenu_Tools = new javax.swing.JMenu();
        jMenuItem_Script = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_Options = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_OutputMapData = new javax.swing.JMenuItem();
        jMenuItem_Clipping = new javax.swing.JMenuItem();
        jMenu_Plugin = new javax.swing.JMenu();
        jMenuItem_PluginManager = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        jMenu_Help = new javax.swing.JMenu();
        jMenuItem_About = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_Help = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jToolBar_Base.setFloatable(false);
        jToolBar_Base.setRollover(true);
        jToolBar_Base.setName(""); // NOI18N

        jButton_AddLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/Add_1_16x16x8.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("meteoinfo/bundle/Bundle_FrmMain"); // NOI18N
        jButton_AddLayer.setToolTipText(bundle.getString("FrmMainOld.jButton_AddLayer.toolTipText")); // NOI18N
        jButton_AddLayer.setFocusable(false);
        jButton_AddLayer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_AddLayer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_AddLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_AddLayerActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_AddLayer);

        jButton_OpenData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/Folder_1_16x16x8.png"))); // NOI18N
        jButton_OpenData.setToolTipText(bundle.getString("FrmMainOld.jButton_OpenData.toolTipText")); // NOI18N
        jButton_OpenData.setFocusable(false);
        jButton_OpenData.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_OpenData.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_OpenData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OpenDataActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_OpenData);

        jButton_RemoveDataLayers.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_RemoveDataLayes.Image.png"))); // NOI18N
        jButton_RemoveDataLayers.setToolTipText(bundle.getString("FrmMainOld.jButton_RemoveDataLayers.toolTipText")); // NOI18N
        jButton_RemoveDataLayers.setFocusable(false);
        jButton_RemoveDataLayers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_RemoveDataLayers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_RemoveDataLayers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RemoveDataLayersActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_RemoveDataLayers);
        jToolBar_Base.add(jSeparator1);

        jButton_SelectElement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/Arrow_1_16x16x8.png"))); // NOI18N
        jButton_SelectElement.setToolTipText(bundle.getString("FrmMainOld.jButton_SelectElement.toolTipText")); // NOI18N
        jButton_SelectElement.setFocusable(false);
        jButton_SelectElement.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_SelectElement.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_SelectElement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SelectElementActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_SelectElement);

        jButton_ZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_ZoomIn.Image.png"))); // NOI18N
        jButton_ZoomIn.setToolTipText(bundle.getString("FrmMainOld.jButton_ZoomIn.toolTipText")); // NOI18N
        jButton_ZoomIn.setFocusable(false);
        jButton_ZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomInActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_ZoomIn);

        jButton_ZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_ZoomOut.Image.png"))); // NOI18N
        jButton_ZoomOut.setToolTipText(bundle.getString("FrmMainOld.jButton_ZoomOut.toolTipText")); // NOI18N
        jButton_ZoomOut.setFocusable(false);
        jButton_ZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomOutActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_ZoomOut);

        jButton_Pan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_Pan.Image.png"))); // NOI18N
        jButton_Pan.setToolTipText(bundle.getString("FrmMainOld.jButton_Pan.toolTipText")); // NOI18N
        jButton_Pan.setFocusable(false);
        jButton_Pan.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Pan.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Pan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PanActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_Pan);

        jButton_FullExtent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_FullExent.Image.png"))); // NOI18N
        jButton_FullExtent.setToolTipText(bundle.getString("FrmMainOld.jButton_FullExtent.toolTipText")); // NOI18N
        jButton_FullExtent.setFocusable(false);
        jButton_FullExtent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_FullExtent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_FullExtent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_FullExtentActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_FullExtent);

        jButton_ZoomToLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_ZoomToLayer.Image.png"))); // NOI18N
        jButton_ZoomToLayer.setToolTipText(bundle.getString("FrmMainOld.jButton_ZoomToLayer.toolTipText")); // NOI18N
        jButton_ZoomToLayer.setFocusable(false);
        jButton_ZoomToLayer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomToLayer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomToLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomToLayerActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_ZoomToLayer);

        jButton_ZoomToExtent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_ZoomToExtent.Image.png"))); // NOI18N
        jButton_ZoomToExtent.setToolTipText(bundle.getString("FrmMainOld.jButton_ZoomToExtent.toolTipText")); // NOI18N
        jButton_ZoomToExtent.setFocusable(false);
        jButton_ZoomToExtent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomToExtent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomToExtent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomToExtentActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_ZoomToExtent);

        jButton_Identifer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/information.png"))); // NOI18N
        jButton_Identifer.setToolTipText(bundle.getString("FrmMainOld.jButton_Identifer.toolTipText")); // NOI18N
        jButton_Identifer.setFocusable(false);
        jButton_Identifer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Identifer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Identifer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_IdentiferActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_Identifer);
        jToolBar_Base.add(jSeparator2);

        jButton_SelectFeatures.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_SelectFeatures.Image.png"))); // NOI18N
        jButton_SelectFeatures.setToolTipText(bundle.getString("FrmMainOld.jButton_SelectFeatures.toolTipText")); // NOI18N
        jButton_SelectFeatures.setFocusable(false);
        jButton_SelectFeatures.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_SelectFeatures.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_SelectFeatures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SelectFeaturesActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_SelectFeatures);

        jButton_Measurement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_Measurement.Image.png"))); // NOI18N
        jButton_Measurement.setToolTipText(bundle.getString("FrmMainOld.jButton_Measurement.toolTipText")); // NOI18N
        jButton_Measurement.setFocusable(false);
        jButton_Measurement.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Measurement.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Measurement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_MeasurementActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_Measurement);

        jButton_LabelSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_LabelSet.Image.png"))); // NOI18N
        jButton_LabelSet.setToolTipText(bundle.getString("FrmMainOld.jButton_LabelSet.toolTipText")); // NOI18N
        jButton_LabelSet.setFocusable(false);
        jButton_LabelSet.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_LabelSet.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_LabelSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_LabelSetActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_LabelSet);
        jToolBar_Base.add(jSeparator3);

        jButton_SavePicture.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/image_1.png"))); // NOI18N
        jButton_SavePicture.setToolTipText(bundle.getString("FrmMainOld.jButton_SavePicture.toolTipText")); // NOI18N
        jButton_SavePicture.setFocusable(false);
        jButton_SavePicture.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_SavePicture.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_SavePicture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SavePictureActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_SavePicture);

        jToolBar_Graphic.setFloatable(false);
        jToolBar_Graphic.setRollover(true);
        jToolBar_Graphic.add(jSeparator4);

        jButton_NewLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewLabel.Image.png"))); // NOI18N
        jButton_NewLabel.setToolTipText(bundle.getString("FrmMainOld.jButton_NewLabel.toolTipText")); // NOI18N
        jButton_NewLabel.setFocusable(false);
        jButton_NewLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewLabelActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewLabel);

        jButton_NewPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewPoint.Image.png"))); // NOI18N
        jButton_NewPoint.setToolTipText(bundle.getString("FrmMainOld.jButton_NewPoint.toolTipText")); // NOI18N
        jButton_NewPoint.setFocusable(false);
        jButton_NewPoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewPoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewPointActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewPoint);

        jButton_NewPolyline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewPolyline.Image.png"))); // NOI18N
        jButton_NewPolyline.setToolTipText(bundle.getString("FrmMainOld.jButton_NewPolyline.toolTipText")); // NOI18N
        jButton_NewPolyline.setFocusable(false);
        jButton_NewPolyline.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewPolyline.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewPolyline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewPolylineActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewPolyline);

        jButton_NewFreehand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewFreehand.Image.png"))); // NOI18N
        jButton_NewFreehand.setToolTipText(bundle.getString("FrmMainOld.jButton_NewFreehand.toolTipText")); // NOI18N
        jButton_NewFreehand.setFocusable(false);
        jButton_NewFreehand.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewFreehand.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewFreehand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewFreehandActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewFreehand);

        jButton_NewCurve.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewCurve.Image.png"))); // NOI18N
        jButton_NewCurve.setToolTipText(bundle.getString("FrmMainOld.jButton_NewCurve.toolTipText")); // NOI18N
        jButton_NewCurve.setFocusable(false);
        jButton_NewCurve.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewCurve.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewCurve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewCurveActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewCurve);

        jButton_NewPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewPolygon.Image.png"))); // NOI18N
        jButton_NewPolygon.setToolTipText(bundle.getString("FrmMainOld.jButton_NewPolygon.toolTipText")); // NOI18N
        jButton_NewPolygon.setFocusable(false);
        jButton_NewPolygon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewPolygon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewPolygon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewPolygonActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewPolygon);

        jButton_NewCurvePolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewCurvePolygon.Image.png"))); // NOI18N
        jButton_NewCurvePolygon.setToolTipText(bundle.getString("FrmMainOld.jButton_NewCurvePolygon.toolTipText")); // NOI18N
        jButton_NewCurvePolygon.setFocusable(false);
        jButton_NewCurvePolygon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewCurvePolygon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewCurvePolygon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewCurvePolygonActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewCurvePolygon);

        jButton_NewRectangle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewRectangle.Image.png"))); // NOI18N
        jButton_NewRectangle.setToolTipText(bundle.getString("FrmMainOld.jButton_NewRectangle.toolTipText")); // NOI18N
        jButton_NewRectangle.setFocusable(false);
        jButton_NewRectangle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewRectangle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewRectangle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewRectangleActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewRectangle);

        jButton_NewCircle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewCircle.Image.png"))); // NOI18N
        jButton_NewCircle.setToolTipText(bundle.getString("FrmMainOld.jButton_NewCircle.toolTipText")); // NOI18N
        jButton_NewCircle.setFocusable(false);
        jButton_NewCircle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewCircle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewCircle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewCircleActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewCircle);

        jButton_NewEllipse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_NewEllipse.Image.png"))); // NOI18N
        jButton_NewEllipse.setToolTipText(bundle.getString("FrmMainOld.jButton_NewEllipse.toolTipText")); // NOI18N
        jButton_NewEllipse.setFocusable(false);
        jButton_NewEllipse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewEllipse.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewEllipse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewEllipseActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewEllipse);

        jButton_EditVertices.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_EditVertices.Image.png"))); // NOI18N
        jButton_EditVertices.setToolTipText(bundle.getString("FrmMainOld.jButton_EditVertices.toolTipText")); // NOI18N
        jButton_EditVertices.setFocusable(false);
        jButton_EditVertices.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_EditVertices.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_EditVertices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EditVerticesActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_EditVertices);

        jToolBar_Layout.setFloatable(false);
        jToolBar_Layout.setRollover(true);
        jToolBar_Layout.add(jSeparator15);

        jButton_PageSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/page_portrait.png"))); // NOI18N
        jButton_PageSet.setToolTipText(bundle.getString("FrmMainOld.jButton_PageSet.toolTipText")); // NOI18N
        jButton_PageSet.setFocusable(false);
        jButton_PageSet.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_PageSet.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_PageSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PageSetActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jButton_PageSet);

        jButton_PageZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_PageZoomIn.Image.png"))); // NOI18N
        jButton_PageZoomIn.setToolTipText(bundle.getString("FrmMainOld.jButton_PageZoomIn.toolTipText")); // NOI18N
        jButton_PageZoomIn.setFocusable(false);
        jButton_PageZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_PageZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_PageZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PageZoomInActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jButton_PageZoomIn);

        jButton_PageZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSB_PageZoomOut.Image.png"))); // NOI18N
        jButton_PageZoomOut.setToolTipText(bundle.getString("FrmMainOld.jButton_PageZoomOut.toolTipText")); // NOI18N
        jButton_PageZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PageZoomOutActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jButton_PageZoomOut);

        jButton_FitToScreen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/ZoomFullMap.png"))); // NOI18N
        jButton_FitToScreen.setToolTipText(bundle.getString("FrmMainOld.jButton_FitToScreen.toolTipText")); // NOI18N
        jButton_FitToScreen.setFocusable(false);
        jButton_FitToScreen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_FitToScreen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_FitToScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_FitToScreenActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jButton_FitToScreen);

        jComboBox_PageZoom.setEditable(true);
        jComboBox_PageZoom.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_PageZoom.setMinimumSize(new java.awt.Dimension(60, 24));
        jComboBox_PageZoom.setPreferredSize(new java.awt.Dimension(80, 24));
        jComboBox_PageZoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_PageZoomActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jComboBox_PageZoom);

        javax.swing.GroupLayout jPanel_MainToolBarLayout = new javax.swing.GroupLayout(jPanel_MainToolBar);
        jPanel_MainToolBar.setLayout(jPanel_MainToolBarLayout);
        jPanel_MainToolBarLayout.setHorizontalGroup(
            jPanel_MainToolBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_MainToolBarLayout.createSequentialGroup()
                .addComponent(jToolBar_Base, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar_Graphic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar_Layout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_MainToolBarLayout.setVerticalGroup(
            jPanel_MainToolBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jToolBar_Base, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jToolBar_Graphic, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jToolBar_Layout, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jSplitPane1.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane1.setDividerLocation(180);

        jTabbedPane_Main.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane_MainStateChanged(evt);
            }
        });

        _mapView.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                _mapViewComponentResized(evt);
            }
        });

        javax.swing.GroupLayout _mapViewLayout = new javax.swing.GroupLayout(_mapView);
        _mapView.setLayout(_mapViewLayout);
        _mapViewLayout.setHorizontalGroup(
            _mapViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 637, Short.MAX_VALUE)
        );
        _mapViewLayout.setVerticalGroup(
            _mapViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 458, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel_MapTabLayout = new javax.swing.GroupLayout(jPanel_MapTab);
        jPanel_MapTab.setLayout(jPanel_MapTabLayout);
        jPanel_MapTabLayout.setHorizontalGroup(
            jPanel_MapTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(_mapView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel_MapTabLayout.setVerticalGroup(
            jPanel_MapTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(_mapView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane_Main.addTab(bundle.getString("FrmMainOld.jPanel_MapTab.TabConstraints.tabTitle"), jPanel_MapTab); // NOI18N

        javax.swing.GroupLayout _mapLayoutLayout = new javax.swing.GroupLayout(_mapLayout);
        _mapLayout.setLayout(_mapLayoutLayout);
        _mapLayoutLayout.setHorizontalGroup(
            _mapLayoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 637, Short.MAX_VALUE)
        );
        _mapLayoutLayout.setVerticalGroup(
            _mapLayoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 458, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel_LayoutTabLayout = new javax.swing.GroupLayout(jPanel_LayoutTab);
        jPanel_LayoutTab.setLayout(jPanel_LayoutTabLayout);
        jPanel_LayoutTabLayout.setHorizontalGroup(
            jPanel_LayoutTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(_mapLayout, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel_LayoutTabLayout.setVerticalGroup(
            jPanel_LayoutTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(_mapLayout, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane_Main.addTab(bundle.getString("FrmMainOld.jPanel_LayoutTab.TabConstraints.tabTitle"), jPanel_LayoutTab); // NOI18N

        jSplitPane1.setRightComponent(jTabbedPane_Main);
        jSplitPane1.setLeftComponent(_mapDocument);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        jLabel_Status.setText(bundle.getString("FrmMainOld.jLabel_Status.text")); // NOI18N

        jLabel_Coordinate.setText(bundle.getString("FrmMainOld.jLabel_Coordinate.text")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_Status, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_Coordinate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel_Status, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel_Coordinate, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jMenuBar_Main.setFont(new java.awt.Font("微软雅黑", 0, 14)); // NOI18N

        jMenu_Project.setText(bundle.getString("FrmMainOld.jMenu_Project.text")); // NOI18N

        jMenuItem_Open.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_Open.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/Folder_1_16x16x8.png"))); // NOI18N
        jMenuItem_Open.setText(bundle.getString("FrmMainOld.jMenuItem_Open.text")); // NOI18N
        jMenuItem_Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_OpenActionPerformed(evt);
            }
        });
        jMenu_Project.add(jMenuItem_Open);

        jMenuItem_Save.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK));
        jMenuItem_Save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/Disk_1_16x16x8.png"))); // NOI18N
        jMenuItem_Save.setText(bundle.getString("FrmMainOld.jMenuItem_Save.text")); // NOI18N
        jMenuItem_Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SaveActionPerformed(evt);
            }
        });
        jMenu_Project.add(jMenuItem_Save);

        jMenuItem_SaveAs.setText(bundle.getString("FrmMainOld.jMenuItem_SaveAs.text")); // NOI18N
        jMenuItem_SaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SaveAsActionPerformed(evt);
            }
        });
        jMenu_Project.add(jMenuItem_SaveAs);

        jMenuBar_Main.add(jMenu_Project);

        jMenu_View.setText(bundle.getString("FrmMainOld.jMenu_View.text")); // NOI18N

        jMenuItem_Layers.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/Layers.png"))); // NOI18N
        jMenuItem_Layers.setText(bundle.getString("FrmMainOld.jMenuItem_Layers.text")); // NOI18N
        jMenuItem_Layers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_LayersActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_Layers);

        jMenuItem_AttributeData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSMI_AttriData.Image.png"))); // NOI18N
        jMenuItem_AttributeData.setText(bundle.getString("FrmMainOld.jMenuItem_AttributeData.text")); // NOI18N
        jMenuItem_AttributeData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AttributeDataActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_AttributeData);
        jMenu_View.add(jSeparator5);

        jMenuItem_LayoutProperty.setText(bundle.getString("FrmMainOld.jMenuItem_LayoutProperty.text")); // NOI18N
        jMenuItem_LayoutProperty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_LayoutPropertyActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_LayoutProperty);
        jMenu_View.add(jSeparator6);

        jMenuItem_MapProperty.setText(bundle.getString("FrmMainOld.jMenuItem_MapProperty.text")); // NOI18N
        jMenuItem_MapProperty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_MapPropertyActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_MapProperty);

        jMenuItem_MaskOut.setText(bundle.getString("FrmMainOld.jMenuItem_MaskOut.text")); // NOI18N
        jMenuItem_MaskOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_MaskOutActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_MaskOut);
        jMenu_View.add(jSeparator7);

        jMenuItem_Projection.setText(bundle.getString("FrmMainOld.jMenuItem_Projection.text")); // NOI18N
        jMenuItem_Projection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ProjectionActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_Projection);

        jMenuBar_Main.add(jMenu_View);

        jMenu_Insert.setText(bundle.getString("FrmMainOld.jMenu_Insert.text")); // NOI18N

        jMenuItem_InsertMapFrame.setText(bundle.getString("FrmMainOld.jMenuItem_InsertMapFrame.text")); // NOI18N
        jMenuItem_InsertMapFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertMapFrameActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertMapFrame);
        jMenu_Insert.add(jSeparator10);

        jMenuItem_InsertTitle.setText(bundle.getString("FrmMainOld.jMenuItem_InsertTitle.text")); // NOI18N
        jMenuItem_InsertTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertTitleActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertTitle);

        jMenuItem_InsertText.setText(bundle.getString("FrmMainOld.jMenuItem_InsertText.text")); // NOI18N
        jMenuItem_InsertText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertTextActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertText);
        jMenu_Insert.add(jSeparator8);

        jMenuItem_InsertLegend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/TSMI_InsertLegend.Image.png"))); // NOI18N
        jMenuItem_InsertLegend.setText(bundle.getString("FrmMainOld.jMenuItem_InsertLegend.text")); // NOI18N
        jMenuItem_InsertLegend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertLegendActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertLegend);

        jMenuItem_InsertScaleBar.setText(bundle.getString("FrmMainOld.jMenuItem_InsertScaleBar.text")); // NOI18N
        jMenuItem_InsertScaleBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertScaleBarActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertScaleBar);

        jMenuItem_InsertNorthArrow.setText(bundle.getString("FrmMainOld.jMenuItem_InsertNorthArrow.text")); // NOI18N
        jMenuItem_InsertNorthArrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertNorthArrowActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertNorthArrow);
        jMenu_Insert.add(jSeparator9);

        jMenuItem_InsertWindArrow.setText(bundle.getString("FrmMainOld.jMenuItem_InsertWindArrow.text")); // NOI18N
        jMenuItem_InsertWindArrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertWindArrowActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertWindArrow);

        jMenuBar_Main.add(jMenu_Insert);

        jMenu_Selection.setText(bundle.getString("FrmMainOld.jMenu_Selection.text")); // NOI18N

        jMenuItem_SelByAttr.setText(bundle.getString("FrmMainOld.jMenuItem_SelByAttr.text")); // NOI18N
        jMenuItem_SelByAttr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SelByAttrActionPerformed(evt);
            }
        });
        jMenu_Selection.add(jMenuItem_SelByAttr);

        jMenuItem_SelByLocation.setText(bundle.getString("FrmMainOld.jMenuItem_SelByLocation.text")); // NOI18N
        jMenuItem_SelByLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SelByLocationActionPerformed(evt);
            }
        });
        jMenu_Selection.add(jMenuItem_SelByLocation);
        jMenu_Selection.add(jSeparator11);

        jMenuItem_ClearSelection.setText(bundle.getString("FrmMainOld.jMenuItem_ClearSelection.text")); // NOI18N
        jMenuItem_ClearSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ClearSelectionActionPerformed(evt);
            }
        });
        jMenu_Selection.add(jMenuItem_ClearSelection);

        jMenuBar_Main.add(jMenu_Selection);

        jMenu_Tools.setText(bundle.getString("FrmMainOld.jMenu_Tools.text")); // NOI18N

        jMenuItem_Script.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/snake.png"))); // NOI18N
        jMenuItem_Script.setText(bundle.getString("FrmMainOld.jMenuItem_Script.text")); // NOI18N
        jMenuItem_Script.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ScriptActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_Script);
        jMenu_Tools.add(jSeparator16);

        jMenuItem_Options.setText(bundle.getString("FrmMainOld.jMenuItem_Options.text")); // NOI18N
        jMenuItem_Options.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_OptionsActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_Options);
        jMenu_Tools.add(jSeparator17);

        jMenuItem_OutputMapData.setText(bundle.getString("FrmMainOld.jMenuItem_OutputMapData.text")); // NOI18N
        jMenuItem_OutputMapData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_OutputMapDataActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_OutputMapData);

        jMenuItem_Clipping.setText(bundle.getString("FrmMainOld.jMenuItem_Clipping.text")); // NOI18N
        jMenuItem_Clipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ClippingActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_Clipping);

        jMenuBar_Main.add(jMenu_Tools);

        jMenu_Plugin.setText(bundle.getString("FrmMainOld.jMenu_Plugin.text")); // NOI18N

        jMenuItem_PluginManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/meteoinfo/resources/plugin_edit_green.png"))); // NOI18N
        jMenuItem_PluginManager.setText(bundle.getString("FrmMainOld.jMenuItem_PluginManager.text")); // NOI18N
        jMenuItem_PluginManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_PluginManagerActionPerformed(evt);
            }
        });
        jMenu_Plugin.add(jMenuItem_PluginManager);
        jMenu_Plugin.add(jSeparator18);

        jMenuBar_Main.add(jMenu_Plugin);

        jMenu_Help.setText(bundle.getString("FrmMainOld.jMenu_Help.text")); // NOI18N

        jMenuItem_About.setText(bundle.getString("FrmMainOld.jMenuItem_About.text")); // NOI18N
        jMenuItem_About.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AboutActionPerformed(evt);
            }
        });
        jMenu_Help.add(jMenuItem_About);
        jMenu_Help.add(jSeparator12);

        jMenuItem_Help.setText(bundle.getString("FrmMainOld.jMenuItem_Help.text")); // NOI18N
        jMenuItem_Help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_HelpActionPerformed(evt);
            }
        });
        jMenu_Help.add(jMenuItem_Help);

        jMenuBar_Main.add(jMenu_Help);

        setJMenuBar(jMenuBar_Main);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel_MainToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel_MainToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // </editor-fold>
    // <editor-fold desc="Events">
    private void mapView_MouseMoved(MouseEvent e) {
        double pXY[] = _mapDocument.getActiveMapFrame().getMapView().screenToProj((double) e.getX(), (double) e.getY());
        double projX = pXY[0];
        double projY = pXY[1];
        if (_mapDocument.getActiveMapFrame().getMapView().getProjection().isLonLatMap()) {
            this.jLabel_Coordinate.setText("Lon: " + String.format("%1$.2f", projX) + "; Lat: " + String.format("%1$.2f", projY));
        } else {
            this.jLabel_Coordinate.setText("X: " + String.format("%1$.1f", projX) + "; Y: " + String.format("%1$.1f", projY));
            String theText = this.jLabel_Coordinate.getText();
            if (_mapDocument.getActiveMapFrame().getMapView().getProjection().getProjInfo().getProjectionName() == ProjectionNames.Robinson) {
                return;
            }

            ProjectionInfo toProj = KnownCoordinateSystems.geographic.world.WGS1984;
            ProjectionInfo fromProj = _mapDocument.getActiveMapFrame().getMapView().getProjection().getProjInfo();
            double[][] points = new double[1][];
            points[0] = new double[]{projX, projY};
            //double[] Z = new double[1];
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                this.jLabel_Coordinate.setText(theText + " (Lon: " + String.format("%1$.2f", points[0][0]) + "; Lat: "
                        + String.format("%1$.2f", points[0][1]) + ")");
            } catch (Exception ex) {
                //this.TSSL_Coord.Text = "X: " + ProjX.ToString("0.0") + "; Y: " + ProjY.ToString("0.0"); 
            }
        }
    }

    private void layout_MouseMoved(MouseEvent e) {
        Point pageP = _mapDocument.getMapLayout().screenToPage(e.getX(), e.getY());
        for (MapFrame mf : _mapDocument.getMapFrames()) {
            Rectangle rect = mf.getLayoutBounds();
            if (MIMath.pointInRectangle(pageP, rect)) {
                double pXY[] = mf.getMapView().screenToProj((double) (pageP.x - rect.x), (double) (pageP.y - rect.y), _mapDocument.getMapLayout().getZoom());
                double projX = pXY[0];
                double projY = pXY[1];
                if (mf.getMapView().getProjection().isLonLatMap()) {
                    this.jLabel_Coordinate.setText("Lon: " + String.format("%1$.2f", projX) + "; Lat: " + String.format("%1$.2f", projY));
                } else {
                    this.jLabel_Coordinate.setText("X: " + String.format("%1$.1f", projX) + "; Y: " + String.format("%1$.1f", projY));
                    String theText = this.jLabel_Coordinate.getText();
                    if (mf.getMapView().getProjection().getProjInfo().getProjectionName() == ProjectionNames.Robinson) {
                        return;
                    }

                    ProjectionInfo toProj = KnownCoordinateSystems.geographic.world.WGS1984;
                    ProjectionInfo fromProj = mf.getMapView().getProjection().getProjInfo();
                    double[][] points = new double[1][];
                    points[0] = new double[]{projX, projY};
                    try {
                        Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                        this.jLabel_Coordinate.setText(theText + " (Lon: " + String.format("%1$.2f", points[0][0]) + "; Lat: "
                                + String.format("%1$.2f", points[0][1]) + ")");
                    } catch (Exception ex) {
                        //this.TSSL_Coord.Text = "X: " + ProjX.ToString("0.0") + "; Y: " + ProjY.ToString("0.0"); 
                    }
                }

                break;
            }
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get and set Methods">
    /**
     * Get application startup path
     *
     * @return Applicatin startup path
     */
    public String getStartupPath() {
        return this._startupPath;
    }

    /**
     * Get MapView object in the active map frame
     *
     * @return MapView object
     */
    @Override
    public MapView getMapView() {
        return this._mapDocument.getActiveMapFrame().getMapView();
    }

    /**
     * Get map document (LayersLegend)
     *
     * @return The map document
     */
    @Override
    public LayersLegend getMapDocument() {
        return this._mapDocument;
    }

    /**
     * Get main menu bar
     *
     * @return Main menu bar
     */
    @Override
    public JMenuBar getMainMenuBar() {
        return this.jMenuBar_Main;
    }

    /**
     * Get tool bar panel
     *
     * @return Tool bar panel
     */
    @Override
    public JPanel getToolBarPanel() {
        return this.jPanel_MainToolBar;
    }

    /**
     * Get jTabbedPane_Main
     *
     * @return jTabbedPane_Main
     */
    public JTabbedPane getMainTab() {
        return this.jTabbedPane_Main;
    }

    /**
     * Get options
     *
     * @return Options
     */
    public Options getOptions() {
        return this._options;
    }

    /**
     * Get legend font
     *
     * @return Legend font
     */
    public Font getLegendFont() {
        return _mapDocument.getFont();
    }

    /**
     * Set legend font
     *
     * @param font Legend font
     */
    public void setLegendFont(Font font) {
        _mapDocument.setFont(font);
        _options.setLegendFont(font);
        _mapDocument.paintGraphics();
    }

//    /**
//     * Get current data folder
//     * 
//     * @return Current data folder
//     */
//    public String getCurrentDataFolder(){
//        return _currentDataFolder;
//    }
//    
//    /**
//     * Set current data folder
//     * 
//     * @param folder Current data folder
//     */
//    public void setCurrentDataFolder(String folder){
//        _currentDataFolder = folder;
//    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="GUI">
    /**
     * Add a tool bar
     *
     * @param toolBar The tool bar
     */
    public void addToolBar(JToolBar toolBar) {
        javax.swing.GroupLayout jPanel_MainToolBarLayout = new javax.swing.GroupLayout(jPanel_MainToolBar);
        jPanel_MainToolBar.setLayout(jPanel_MainToolBarLayout);
        jPanel_MainToolBarLayout.setHorizontalGroup(
                jPanel_MainToolBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel_MainToolBarLayout.createSequentialGroup()
                .addComponent(jToolBar_Base, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar_Graphic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar_Layout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel_MainToolBarLayout.setVerticalGroup(
                jPanel_MainToolBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jToolBar_Base, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jToolBar_Graphic, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jToolBar_Layout, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE));
    }

    public void removeToolBar(JToolBar toolBar) {
        this.jPanel_MainToolBar.remove(toolBar);
        Component[] comps = this.jPanel_MainToolBar.getComponents();
        javax.swing.GroupLayout jPanel_MainToolBarLayout = new javax.swing.GroupLayout(jPanel_MainToolBar);
        jPanel_MainToolBar.setLayout(jPanel_MainToolBarLayout);
        List<JToolBar> newcomps = new ArrayList<JToolBar>();
        for (Component com : comps) {
            if (com instanceof JToolBar) {
                if (((JToolBar) com).equals(jToolBar_Base)) {
                    continue;
                }
                if (((JToolBar) com).equals(jToolBar_Graphic)) {
                    continue;
                }
                if (((JToolBar) com).equals(jToolBar_Layout)) {
                    continue;
                }                
                newcomps.add((JToolBar) com);
            }
        }

        GroupLayout.SequentialGroup group = jPanel_MainToolBarLayout.createSequentialGroup();
        group.addComponent(jToolBar_Base, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
        group.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        group.addComponent(jToolBar_Graphic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
        group.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        group.addComponent(jToolBar_Layout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
        for (JToolBar tb : newcomps) {
            group.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
            group.addComponent(tb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
        }
        group.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        jPanel_MainToolBarLayout.setHorizontalGroup(
                jPanel_MainToolBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(group));

        GroupLayout.ParallelGroup pgroup = jPanel_MainToolBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING);
        pgroup.addComponent(jToolBar_Base, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE);
        pgroup.addComponent(jToolBar_Graphic, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE);
        pgroup.addComponent(jToolBar_Layout, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE);
        for (JToolBar tb : newcomps) {
            pgroup.addComponent(tb, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE);
        }
        jPanel_MainToolBarLayout.setVerticalGroup(pgroup);
    }
    // </editor-fold>
    // <editor-fold desc="Project">

    public final void loadDefaultPojectFile() {
        //Open default project file            
//        File directory = new File(".");        
//        String fn = null;
//        try {
//            fn = directory.getCanonicalPath();
//        } catch (IOException ex) {
//            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        fn = fn + File.separator + "default.mip";
        String fn = this._startupPath + File.separator + "default.mip";
        loadProjectFile(fn);
    }

    public final void loadConfigureFile() {
//        File directory = new File(".");
//        String fn = null;
//        try {
//            fn = directory.getCanonicalPath();
//        } catch (IOException ex) {
//            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
//        }
        String fn = this._startupPath + File.separator + "config.xml";
        if (new File(fn).exists()) {
            try {
                this._options.loadConfigFile(fn);
                this._mapDocument.setFont(this._options.getLegendFont());
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public final void saveConfigureFile() {
        String fn = this._options.getFileName();
        try {
            this._options.saveConfigFile(fn);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadProjectFile(String pFile) {
        if (new File(pFile).exists()) {
            try {
                _projectFile.loadProjFile(pFile);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.setTitle("MeteoInfo - " + new File(pFile).getName());
        }
    }

    public Plugin readPlugin(String jarFileName) {
        Plugin plugin = new Plugin();
        plugin.setName("Sample");
        plugin.setJarFileName(jarFileName);
        String className = GlobalUtil.getPluginClassName(jarFileName);
        plugin.setClassName(className);
        return plugin;
    }

    public List<Plugin> readPlugins() throws MalformedURLException {
        List<Plugin> plugins = new ArrayList<Plugin>();
        String pluginPath = this._startupPath + File.separator + "plugins";
        if (new File(pluginPath).isDirectory()) {
            List<String> fileNames = GlobalUtil.getFiles(pluginPath, ".jar");
            for (String fn : fileNames) {
                Plugin plugin = readPlugin(fn);
                plugins.add(plugin);
            }
        }

        return plugins;
    }

    public void loadPlugins() throws MalformedURLException, IOException {
        List<Plugin> plugins = new ArrayList<Plugin>();
        String pluginPath = this._startupPath + File.separator + "plugins";
        if (new File(pluginPath).isDirectory()) {
            List<String> fileNames = GlobalUtil.getFiles(pluginPath, ".jar");
            for (String fn : fileNames) {
                final Plugin plugin = this.readPlugin(fn);
                plugins.add(plugin);
                URL url = new URL("file:" + plugin.getJarFileName());
                final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
                final JMenuItem pluginMI = new JMenuItem();
                pluginMI.setText(plugin.getName());
                pluginMI.setIcon(this._unloadedPluginIcon);
                pluginMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        FrmMainOld.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        try {
                            if (!plugin.isLoad()) {
                                Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
                                IPlugin instance = (IPlugin) clazz.newInstance();
                                instance.setApplication(FrmMainOld.this);
                                instance.setName(plugin.getName());
                                plugin.setPluginObject(instance);
                                plugin.setLoad(true);
                                instance.load();
                                pluginMI.setSelected(true);
                                pluginMI.setIcon(FrmMainOld.this._loadedPluginIcon);
                            } else {
                                plugin.getPluginObject().unload();
                                plugin.setPluginObject(null);
                                plugin.setLoad(false);
                                pluginMI.setSelected(false);
                                pluginMI.setIcon(FrmMainOld.this._unloadedPluginIcon);
                            }
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InstantiationException ex) {
                            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        FrmMainOld.this.setCursor(Cursor.getDefaultCursor());
                    }
                });
                this.jMenu_Plugin.add(pluginMI);
            }
        }
        this._options.setPlugins(plugins);
    }

    public void loadPlugins(List<Plugin> plugins) throws MalformedURLException, IOException {
        if (plugins.size() > 0) {
            for (final Plugin plugin : plugins) {
                this.addPlugin(plugin);
            }
        }
    }

    private JMenuItem findPluginMenuItem(String name) {
        for (int i = 0; i < this.jMenu_Plugin.getItemCount(); i++) {
            JMenuItem mi = this.jMenu_Plugin.getItem(i);
            if (mi != null) {
                if (mi.getText().equals(name)) {
                    return mi;
                }
            }
        }

        return null;
    }

    public void addPlugin(final Plugin plugin) throws IOException {
        //this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));        
        URL url = new URL("file:" + plugin.getJarFileName());
        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
        final JMenuItem pluginMI = new JMenuItem();
        pluginMI.setText(plugin.getName());
        if (plugin.isLoad()) {
            try {
                Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
                IPlugin instance = (IPlugin) clazz.newInstance();
                instance.setApplication(FrmMainOld.this);
                instance.setName(plugin.getName());
                plugin.setPluginObject(instance);
                plugin.setLoad(true);
                instance.load();
                pluginMI.setSelected(true);
                pluginMI.setIcon(this._loadedPluginIcon);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            pluginMI.setIcon(this._unloadedPluginIcon);
        }
        pluginMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FrmMainOld.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    if (!plugin.isLoad()) {
                        Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
                        IPlugin instance = (IPlugin) clazz.newInstance();
                        instance.setApplication(FrmMainOld.this);
                        instance.setName(plugin.getName());
                        plugin.setPluginObject(instance);
                        plugin.setLoad(true);
                        instance.load();
                        pluginMI.setSelected(true);
                        pluginMI.setIcon(FrmMainOld.this._loadedPluginIcon);
                    } else {
                        plugin.getPluginObject().unload();
                        //plugin.setPluginObject(null);
                        plugin.setLoad(false);
                        pluginMI.setSelected(false);
                        pluginMI.setIcon(FrmMainOld.this._unloadedPluginIcon);
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                }
                FrmMainOld.this.setCursor(Cursor.getDefaultCursor());
            }
        });
        this.jMenu_Plugin.add(pluginMI);
        //this.setCursor(Cursor.getDefaultCursor());
    }

    public void loadPlugin(Plugin plugin) {
        if (plugin.isLoad()) {
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        JMenuItem pluginMI = this.findPluginMenuItem(plugin.getName());
        URL url = null;
        try {
            url = new URL("file:" + plugin.getJarFileName());
        } catch (MalformedURLException ex) {
            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
        }
        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
        try {
            Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
            IPlugin instance = (IPlugin) clazz.newInstance();
            instance.setApplication(FrmMainOld.this);
            instance.setName(plugin.getName());
            plugin.setPluginObject(instance);
            plugin.setLoad(true);
            instance.load();
            pluginMI.setSelected(true);
            pluginMI.setIcon(this._loadedPluginIcon);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void unloadPlugin(Plugin plugin) {
        if (!plugin.isLoad()) {
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        JMenuItem pluginMI = this.findPluginMenuItem(plugin.getName());
        plugin.getPluginObject().unload();
        plugin.setPluginObject(null);
        plugin.setLoad(false);
        pluginMI.setSelected(false);
        pluginMI.setIcon(this._unloadedPluginIcon);
        this.setCursor(Cursor.getDefaultCursor());
    }
    // </editor-fold>

    // <editor-fold desc="Menu">
    private void setCurrentTool(JButton currentTool) {
        if (!(_currentTool == null)) {
            _currentTool.setSelected(false);
        }
        _currentTool = currentTool;
        _currentTool.setSelected(true);
        jLabel_Status.setText(_currentTool.getToolTipText());

        if (!"jButton_EditVertices".equals(_currentTool.getName())) {
            //this.jButton_EditVertices.setEnabled(false);
            if (_isEditingVertices) {
                if (this.jTabbedPane_Main.getSelectedIndex() == 0) {
                    _mapView.paintLayers();
                } else {
                    _mapDocument.getMapLayout().paintGraphics();
                }

                _isEditingVertices = false;
            }
        }
    }

    private void _mapViewComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event__mapViewComponentResized
        // TODO add your handling code here:
        //this.mapView1.zoomToExtent(this.mapView1.getViewExtent());
    }//GEN-LAST:event__mapViewComponentResized

    private void jTabbedPane_MainStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane_MainStateChanged
        // TODO add your handling code here:
        int selIndex = this.jTabbedPane_Main.getSelectedIndex();
        switch (selIndex) {
            case 0:    //MapView
                _mapDocument.setIsLayoutView(false);

                this.jToolBar_Layout.setEnabled(false);
                for (Component c : this.jToolBar_Layout.getComponents()) {
                    c.setEnabled(false);
                }
                this.jMenuItem_LayoutProperty.setEnabled(false);
                this.jMenuItem_InsertLegend.setEnabled(false);
                this.jMenuItem_InsertTitle.setEnabled(false);
                this.jMenuItem_InsertText.setEnabled(false);
                this.jMenuItem_InsertNorthArrow.setEnabled(false);
                this.jMenuItem_InsertScaleBar.setEnabled(false);
                this.jMenuItem_InsertWindArrow.setEnabled(false);

                _mapView.setIsLayoutMap(false);
                _mapView.zoomToExtent(_mapView.getViewExtent());
                break;
            case 1:    //MapLayout
                _mapDocument.setIsLayoutView(true);

                this.jToolBar_Layout.setEnabled(true);
                for (Component c : this.jToolBar_Layout.getComponents()) {
                    c.setEnabled(true);
                }
                this.jMenuItem_LayoutProperty.setEnabled(true);
                this.jMenuItem_InsertLegend.setEnabled(true);
                this.jMenuItem_InsertTitle.setEnabled(true);
                this.jMenuItem_InsertText.setEnabled(true);
                this.jMenuItem_InsertNorthArrow.setEnabled(true);
                this.jMenuItem_InsertScaleBar.setEnabled(true);
                this.jMenuItem_InsertWindArrow.setEnabled(true);

                _mapDocument.getMapLayout().paintGraphics();
                break;
        }
    }//GEN-LAST:event_jTabbedPane_MainStateChanged

    private void jMenuItem_OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_OpenActionPerformed
        // TODO add your handling code here:
        JFileChooser aDlg = new JFileChooser();
        String curDir = System.getProperty("user.dir");
        aDlg.setCurrentDirectory(new File(curDir));
        String[] fileExts = {"mip"};
        GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "MeteoInfo Project File (*.mip)");
        aDlg.setFileFilter(pFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            System.setProperty("user.dir", aFile.getParent());
            openProjectFile(aFile.getAbsolutePath());
        }
    }//GEN-LAST:event_jMenuItem_OpenActionPerformed

    private void jMenuItem_SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SaveActionPerformed
        // TODO add your handling code here:
        String aFile = _projectFile.getFileName();
        try {
            _projectFile.saveProjFile(aFile);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItem_SaveActionPerformed

    private void jMenuItem_SaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SaveAsActionPerformed
        // TODO add your handling code here:
        JFileChooser aDlg = new JFileChooser();
        String[] fileExts = {"mip"};
        GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "MeteoInfo Project File (*.mip)");
        aDlg.setFileFilter(pFileFilter);
        aDlg.setSelectedFile(new File(_projectFile.getFileName()));
        if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(this)) {
            File file = aDlg.getSelectedFile();
            System.setProperty("user.dir", file.getParent());
            String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
            String fileName = file.getAbsolutePath();
            if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                fileName = fileName + "." + extent;
            }
            file = new File(fileName);
            try {
                _projectFile.saveProjFile(file.getAbsolutePath());
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.setTitle("MeteoInfo - " + file.getName());
        }
    }//GEN-LAST:event_jMenuItem_SaveAsActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        //_mapView.setLockViewUpdate(true);
        _mapDocument.setIsLayoutView(false);
        _mapView.setIsLayoutMap(false);
        _mapView.zoomToExtent(_mapView.getViewExtent());
        //_mapView.setLockViewUpdate(false);

        //Open MeteoData form
        _frmMeteoData = new FrmMeteoData(this, false);
        _frmMeteoData.setLocation(this.getX() + 10, this.getY() + this.getHeight() - _frmMeteoData.getHeight() - 40);
        _frmMeteoData.setVisible(true);
    }//GEN-LAST:event_formWindowOpened

    private void jMenuItem_LayersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_LayersActionPerformed
        // TODO add your handling code here:
        if (this.jMenuItem_Layers.isSelected()) {
            this.jSplitPane1.setDividerLocation(0);
            this.jSplitPane1.setDividerSize(0);
            this.jMenuItem_Layers.setSelected(false);
        } else {
            this.jSplitPane1.setDividerLocation(180);
            this.jSplitPane1.setDividerSize(5);
            this.jMenuItem_Layers.setSelected(true);
        }
    }//GEN-LAST:event_jMenuItem_LayersActionPerformed

    private void jMenuItem_AttributeDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_AttributeDataActionPerformed
        // TODO add your handling code here:
        //JOptionPane.showMessageDialog(null, "Under developing!");
        if (_mapDocument.getSelectedNode() == null) {
            return;
        }

        if (_mapDocument.getSelectedNode().getNodeType() == NodeTypes.LayerNode) {
            LayerNode aLN = (LayerNode) _mapDocument.getSelectedNode();
            MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerFromHandle(aLN.getLayerHandle());
            if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
                if (((VectorLayer) aLayer).getShapeNum() > 0) {
                    FrmAttriData aFrmData = new FrmAttriData(this, false);
                    aFrmData.setLayer((VectorLayer) aLayer);
                    aFrmData.setLocationRelativeTo(this);
                    aFrmData.setVisible(true);
                }
            }
        }
    }//GEN-LAST:event_jMenuItem_AttributeDataActionPerformed

    private void jMenuItem_LayoutPropertyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_LayoutPropertyActionPerformed
        // TODO add your handling code here:
        FrmProperty pFrm = new FrmProperty(this, true, false);
        pFrm.setObject(this._mapLayout);
        pFrm.setLocationRelativeTo(this);
        pFrm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_LayoutPropertyActionPerformed

    private void jMenuItem_MapPropertyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_MapPropertyActionPerformed
        // TODO add your handling code here:
        FrmProperty pFrm = new FrmProperty(this, true, false);
        pFrm.setObject(this._mapView);
        pFrm.setLocationRelativeTo(this);
        pFrm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_MapPropertyActionPerformed

    private void jMenuItem_MaskOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_MaskOutActionPerformed
        // TODO add your handling code here:
        FrmProperty pFrm = new FrmProperty(this, true, false);
        pFrm.setObject(this._mapView.getMaskOut().new MaskOutBean());
        pFrm.setLocationRelativeTo(this);
        pFrm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_MaskOutActionPerformed

    private void jMenuItem_ProjectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ProjectionActionPerformed
        // TODO add your handling code here:
        FrmProjection frmProj = new FrmProjection(this, false);
        frmProj.setLocationRelativeTo(this);
        frmProj.setVisible(true);
    }//GEN-LAST:event_jMenuItem_ProjectionActionPerformed

    private void jMenuItem_InsertMapFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_InsertMapFrameActionPerformed
        // TODO add your handling code here:
        MapFrame aMF = new MapFrame();
        aMF.setText(_mapDocument.getNewMapFrameName());
        _mapDocument.addMapFrame(aMF);
        _mapDocument.paintGraphics();
    }//GEN-LAST:event_jMenuItem_InsertMapFrameActionPerformed

    private void jMenuItem_InsertTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_InsertTitleActionPerformed
        // TODO add your handling code here:
        _mapDocument.getMapLayout().addText("Map Title", _mapDocument.getMapLayout().getWidth() / 2, 20, 12);
        _mapDocument.getMapLayout().paintGraphics();
    }//GEN-LAST:event_jMenuItem_InsertTitleActionPerformed

    private void jMenuItem_InsertTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_InsertTextActionPerformed
        // TODO add your handling code here:
        _mapDocument.getMapLayout().addText("Text", _mapDocument.getMapLayout().getWidth() / 2, 200);
        _mapDocument.getMapLayout().paintGraphics();
    }//GEN-LAST:event_jMenuItem_InsertTextActionPerformed

    private void jMenuItem_InsertLegendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_InsertLegendActionPerformed
        // TODO add your handling code here:
        _mapDocument.getMapLayout().addLegend(100, 100);
        _mapDocument.getMapLayout().paintGraphics();
    }//GEN-LAST:event_jMenuItem_InsertLegendActionPerformed

    private void jMenuItem_InsertScaleBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_InsertScaleBarActionPerformed
        // TODO add your handling code here:
        _mapDocument.getMapLayout().addScaleBar(100, 100);
        _mapDocument.getMapLayout().paintGraphics();
    }//GEN-LAST:event_jMenuItem_InsertScaleBarActionPerformed

    private void jMenuItem_InsertNorthArrowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_InsertNorthArrowActionPerformed
        // TODO add your handling code here:
        _mapDocument.getMapLayout().addNorthArrow(200, 100);
        _mapDocument.getMapLayout().paintGraphics();
    }//GEN-LAST:event_jMenuItem_InsertNorthArrowActionPerformed

    private void jMenuItem_InsertWindArrowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_InsertWindArrowActionPerformed
        // TODO add your handling code here:
        WindArraw aWindArraw = new WindArraw();
        aWindArraw.angle = 270;
        VectorBreak aVB = new VectorBreak();
        aVB.setColor(Color.black);
        LayoutGraphic wag = new LayoutGraphic(new Graphic(aWindArraw, aVB), _mapDocument.getMapLayout(),
                _mapDocument.getMapLayout().getActiveLayoutMap());
        wag.setLeft(100);
        wag.setTop(100);
        _mapDocument.getMapLayout().addElement(wag);
        _mapDocument.getMapLayout().paintGraphics();
    }//GEN-LAST:event_jMenuItem_InsertWindArrowActionPerformed

    private void jMenuItem_ScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ScriptActionPerformed
        // TODO add your handling code here:
        //JOptionPane.showMessageDialog(null, "Under developing!");
//        // Create an instance of the PythonInterpreter
//        PythonInterpreter interp = new PythonInterpreter();
//
//        // The exec() method executes strings of code
//        interp.exec("import sys");
//        interp.exec("print sys");
//
//        // Set variable values within the PythonInterpreter instance
//        //interp.set("a", new PyInteger(42));
//        interp.exec("a = 42");
//        interp.exec("print a");
//        interp.exec("x = 2+2");
//        interp.exec("b = 25" + "\n" + "print a + b");
//
//        // Obtain the value of an object from the PythonInterpreter and store it
//        // into a PyObject.
//        PyObject x = interp.get("x");
//        System.out.println("x: " + x);

        FrmTextEditor frmTE = new FrmTextEditor(this);
        frmTE.setTextFont(this._options.getTextFont());
        frmTE.setLocationRelativeTo(this);
        frmTE.setVisible(true);
    }//GEN-LAST:event_jMenuItem_ScriptActionPerformed

    private void jMenuItem_SelByAttrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SelByAttrActionPerformed
        // TODO add your handling code here:
        FrmSelectByAttributes frmSel = new FrmSelectByAttributes(this, false);
        frmSel.setLocationRelativeTo(this);
        frmSel.setVisible(true);
    }//GEN-LAST:event_jMenuItem_SelByAttrActionPerformed

    private void jMenuItem_SelByLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SelByLocationActionPerformed
        // TODO add your handling code here:
        FrmSelectByLocation frmSel = new FrmSelectByLocation(this, false);
        frmSel.setLocationRelativeTo(this);
        frmSel.setVisible(true);
    }//GEN-LAST:event_jMenuItem_SelByLocationActionPerformed

    private void jMenuItem_ClearSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ClearSelectionActionPerformed
        // TODO add your handling code here:
        MapLayer aLayer = _mapDocument.getActiveMapFrame().getMapView().getLayerFromHandle(
                _mapDocument.getActiveMapFrame().getMapView().getSelectedLayer());
        if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
            ((VectorLayer) aLayer).clearSelectedShapes();
        }

        _mapDocument.getActiveMapFrame().getMapView().paintLayers();
    }//GEN-LAST:event_jMenuItem_ClearSelectionActionPerformed

    private void jMenuItem_HelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_HelpActionPerformed
        // TODO add your handling code here:
        //JOptionPane.showMessageDialog(null, "Under developing!");
        FrmMain frm = new FrmMain();
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_HelpActionPerformed

    private void jMenuItem_AboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_AboutActionPerformed
        // TODO add your handling code here:
        FrmAbout frmAbout = new FrmAbout(this, false);
        frmAbout.setLocationRelativeTo(this);
        frmAbout.setVisible(true);
    }//GEN-LAST:event_jMenuItem_AboutActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        int result = JOptionPane.showConfirmDialog(null, "If save the project?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            String aFile = _projectFile.getFileName();
            try {
                _projectFile.saveProjFile(aFile);
                this.saveConfigureFile();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            }
            //this.dispose();
            System.exit(0);
        } else if (result == JOptionPane.NO_OPTION) {
            //this.dispose();
            try {
                this.saveConfigureFile();
            } catch (Exception e) {
            }
            System.exit(0);
        } else if (result == JOptionPane.CANCEL_OPTION) {
        }
    }//GEN-LAST:event_formWindowClosing

    private void jComboBox_PageZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_PageZoomActionPerformed
        // TODO add your handling code here:
        if (_isLoading) {
            return;
        }
        String zoomStr = this.jComboBox_PageZoom.getSelectedItem().toString().trim();
        if (zoomStr.endsWith("%")) {
            zoomStr = zoomStr.substring(0, zoomStr.length() - 1);
        }
        try {
            float zoom = Float.parseFloat(zoomStr);
            _mapDocument.getMapLayout().setZoom(zoom / 100);
            _mapDocument.getMapLayout().paintGraphics();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jComboBox_PageZoomActionPerformed

    private void jButton_FitToScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_FitToScreenActionPerformed
        // TODO add your handling code here:
        float zoomX = (float) _mapDocument.getMapLayout().getWidth() / _mapDocument.getMapLayout().getPageBounds().width;
        float zoomY = (float) _mapDocument.getMapLayout().getHeight() / _mapDocument.getMapLayout().getPageBounds().height;
        float zoom = Math.min(zoomX, zoomY);
        PointF aP = new PointF(0, 0);
        _mapDocument.getMapLayout().setPageLocation(aP);
        _mapDocument.getMapLayout().setZoom(zoom);
        _mapDocument.getMapLayout().paintGraphics();
    }//GEN-LAST:event_jButton_FitToScreenActionPerformed

    private void jButton_PageZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_PageZoomOutActionPerformed
        // TODO add your handling code here:
        _mapLayout.setZoom(_mapLayout.getZoom() * 0.8F);
        _mapLayout.paintGraphics();
    }//GEN-LAST:event_jButton_PageZoomOutActionPerformed

    private void jButton_PageZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_PageZoomInActionPerformed
        // TODO add your handling code here:
        _mapLayout.setZoom(_mapLayout.getZoom() * 1.2F);
        _mapLayout.paintGraphics();
    }//GEN-LAST:event_jButton_PageZoomInActionPerformed

    private void jButton_PageSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_PageSetActionPerformed
        // TODO add your handling code here:
        FrmPageSet aFrmPageSet = new FrmPageSet(this, true);
        aFrmPageSet.setMapLayout(_mapLayout);
        aFrmPageSet.setPaperSize(_mapDocument.getMapLayout().getPaperSize());
        aFrmPageSet.setLandscape(_mapDocument.getMapLayout().isLandscape());
        aFrmPageSet.setLocationRelativeTo(this);
        aFrmPageSet.setVisible(true);
    }//GEN-LAST:event_jButton_PageSetActionPerformed

    private void jButton_EditVerticesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_EditVerticesActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.EditVertices);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.EditVertices);

        setCurrentTool((JButton) evt.getSource());

        _isEditingVertices = true;
        if (this.jTabbedPane_Main.getSelectedIndex() == 0) {
            _mapView.paintLayers();
        } else {
            _mapDocument.getMapLayout().paintGraphics();
        }
    }//GEN-LAST:event_jButton_EditVerticesActionPerformed

    private void jButton_NewEllipseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewEllipseActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Ellipse);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Ellipse);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewEllipseActionPerformed

    private void jButton_NewCircleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewCircleActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Circle);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Circle);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewCircleActionPerformed

    private void jButton_NewRectangleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewRectangleActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Rectangle);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Rectangle);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewRectangleActionPerformed

    private void jButton_NewCurvePolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewCurvePolygonActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_CurvePolygon);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_CurvePolygon);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewCurvePolygonActionPerformed

    private void jButton_NewPolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewPolygonActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Polygon);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Polygon);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewPolygonActionPerformed

    private void jButton_NewCurveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewCurveActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Curve);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Curve);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewCurveActionPerformed

    private void jButton_NewFreehandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewFreehandActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Freehand);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Freehand);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewFreehandActionPerformed

    private void jButton_NewPolylineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewPolylineActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Polyline);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Polyline);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewPolylineActionPerformed

    private void jButton_NewPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewPointActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Point);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Point);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewPointActionPerformed

    private void jButton_NewLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewLabelActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Label);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Label);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_NewLabelActionPerformed

    private void jButton_SavePictureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SavePictureActionPerformed
        // TODO add your handling code here:
        String path = System.getProperty("user.dir");
        File pathDir = new File(path);
        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        String[] fileExts = new String[]{"png"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Png Image (*.png)");
        aDlg.setFileFilter(mapFileFilter);
        fileExts = new String[]{"gif"};
        mapFileFilter = new GenericFileFilter(fileExts, "Gif Image (*.gif)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"jpg"};
        mapFileFilter = new GenericFileFilter(fileExts, "Jpeg Image (*.jpg)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"bmp"};
        mapFileFilter = new GenericFileFilter(fileExts, "Bitmap Image (*.bmp)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"tif"};
        mapFileFilter = new GenericFileFilter(fileExts, "Tiff Image (*.tif)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"ps"};
        mapFileFilter = new GenericFileFilter(fileExts, "Postscript Image (*.ps)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        aDlg.setAcceptAllFileFilterUsed(false);
        if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            System.setProperty("user.dir", aFile.getParent());
            String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
            String fileName = aFile.getAbsolutePath();
            if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                fileName = fileName + "." + extent;
            }

            if (this.jTabbedPane_Main.getSelectedIndex() == 0) {
                try {
                    _mapDocument.getActiveMapFrame().getMapView().exportToPicture(fileName);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                } catch (PrintException ex) {
                    Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (this.jTabbedPane_Main.getSelectedIndex() == 1) {
                try {
                    _mapDocument.getMapLayout().exportToPicture(fileName);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                } catch (PrintException ex) {
                    Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_jButton_SavePictureActionPerformed

    private void jButton_LabelSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_LabelSetActionPerformed
        // TODO add your handling code here:
        if (_mapDocument.getSelectedNode() == null) {
            return;
        }

        if (_mapDocument.getSelectedNode().getNodeType() == NodeTypes.LayerNode) {
            LayerNode aLN = (LayerNode) _mapDocument.getSelectedNode();
            MapLayer aMLayer = aLN.getMapFrame().getMapView().getLayerFromHandle(aLN.getLayerHandle());
            if (aMLayer.getLayerType() == LayerTypes.VectorLayer) {
                VectorLayer aLayer = (VectorLayer) aMLayer;
                if (aLayer.getShapeNum() > 0) {
                    FrmLabelSet aFrmLabel = new FrmLabelSet(this, false, _mapDocument.getActiveMapFrame().getMapView());
                    aFrmLabel.setLayer(aLayer);
                    aFrmLabel.setLocationRelativeTo(this);
                    aFrmLabel.setVisible(true);
                }
            }
        }
    }//GEN-LAST:event_jButton_LabelSetActionPerformed

    private void jButton_MeasurementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_MeasurementActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Measurement);
        //_mapDocument.getMapLayout().setMeasurementForm(_mapView.getMeasurementForm());
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Measurement);
        if (this.jTabbedPane_Main.getSelectedIndex() == 0) {
            _mapView.showMeasurementForm();
        } else if (this.jTabbedPane_Main.getSelectedIndex() == 1) {
            _mapDocument.getMapLayout().showMeasurementForm();
        }

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_MeasurementActionPerformed

    private void jButton_SelectFeaturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SelectFeaturesActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.SelectFeatures);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_SelectFeatures);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_SelectFeaturesActionPerformed

    private void jButton_IdentiferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_IdentiferActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Identifer);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Identifer);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_IdentiferActionPerformed

    private void jButton_ZoomToExtentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ZoomToExtentActionPerformed
        // TODO add your handling code here:
        FrmZoomToExtent frmZoom = new FrmZoomToExtent(this, true);
        frmZoom.setVisible(true);
    }//GEN-LAST:event_jButton_ZoomToExtentActionPerformed

    private void jButton_ZoomToLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ZoomToLayerActionPerformed
        // TODO add your handling code here:
        if (_mapDocument.getSelectedNode() == null) {
            return;
        }

        if (_mapDocument.getSelectedNode().getNodeType() == NodeTypes.LayerNode) {
            MapFrame aMF = _mapDocument.getCurrentMapFrame();
            MapLayer aLayer = ((LayerNode) _mapDocument.getSelectedNode()).getMapLayer();
            if (aLayer != null) {
                aMF.getMapView().zoomToExtent(aLayer.getExtent());
            }
        }
    }//GEN-LAST:event_jButton_ZoomToLayerActionPerformed

    private void jButton_FullExtentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_FullExtentActionPerformed
        // TODO add your handling code here:
        _mapView.zoomToExtent(_mapView.getExtent());
    }//GEN-LAST:event_jButton_FullExtentActionPerformed

    private void jButton_PanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_PanActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Pan);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Pan);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_PanActionPerformed

    private void jButton_ZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ZoomOutActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Zoom_Out);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_ZoomOut);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_ZoomOutActionPerformed

    private void jButton_ZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ZoomInActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Zoom_In);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_ZoomIn);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_ZoomInActionPerformed

    private void jButton_SelectElementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SelectElementActionPerformed
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.SelectElements);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Select);

        setCurrentTool((JButton) evt.getSource());
    }//GEN-LAST:event_jButton_SelectElementActionPerformed

    private void jButton_RemoveDataLayersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RemoveDataLayersActionPerformed
        // TODO add your handling code here:
        _mapDocument.getActiveMapFrame().removeMeteoLayers();
    }//GEN-LAST:event_jButton_RemoveDataLayersActionPerformed

    private void jButton_OpenDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OpenDataActionPerformed
        // TODO add your handling code here:
        if (_frmMeteoData == null) {
            _frmMeteoData = new FrmMeteoData(this, false);
        }
        _frmMeteoData.setVisible(true);
    }//GEN-LAST:event_jButton_OpenDataActionPerformed

    private void jButton_AddLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_AddLayerActionPerformed
        // TODO add your handling code here:
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String path = System.getProperty("user.dir");
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setAcceptAllFileFilterUsed(false);
        aDlg.setCurrentDirectory(pathDir);
        String[] fileExts = new String[]{"shp", "bil", "wmp", "bln", "bmp", "gif", "jpg", "tif", "png"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Supported Formats");
        aDlg.setFileFilter(mapFileFilter);
        fileExts = new String[]{"shp"};
        mapFileFilter = new GenericFileFilter(fileExts, "Shape File (*.shp)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            System.setProperty("user.dir", aFile.getParent());
            MapLayer aLayer = null;
            try {
                //aLayer = ShapeFileManage.loadShapeFile(aFile.getAbsolutePath());
                aLayer = MapDataManage.loadLayer(aFile.getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(FrmMainOld.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (aLayer != null) {
                this._mapDocument.getActiveMapFrame().addLayer(aLayer);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_jButton_AddLayerActionPerformed

    private void jMenuItem_OptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_OptionsActionPerformed
        // TODO add your handling code here:
        FrmOptions frmOption = new FrmOptions(this, true);
        frmOption.setLocationRelativeTo(this);
        frmOption.setVisible(true);
    }//GEN-LAST:event_jMenuItem_OptionsActionPerformed

    private void jMenuItem_OutputMapDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_OutputMapDataActionPerformed
        // TODO add your handling code here:
        FrmOutputMapData frm = new FrmOutputMapData(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_OutputMapDataActionPerformed

    private void jMenuItem_ClippingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ClippingActionPerformed
        // TODO add your handling code here:
        FrmClipping frm = new FrmClipping(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_ClippingActionPerformed

    private void jMenuItem_PluginManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_PluginManagerActionPerformed
        // TODO add your handling code here:
        FrmPluginManager frm = new FrmPluginManager(this, true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_PluginManagerActionPerformed

    /**
     * Open project file
     *
     * @param projFile project file path
     */
    public void openProjectFile(String projFile) {
        for (MapFrame mf : _mapDocument.getMapFrames()) {
            if (mf.getMapView().getLayerNum() > 0) {
                mf.removeAllLayers();
            }
        }
        //Application.DoEvents();
        loadProjectFile(projFile);
        _mapView = _mapDocument.getActiveMapFrame().getMapView();
        setMapView();
        //setMapLayout();

        //this.Text = "MeteoInfo - " + Path.GetFileNameWithoutExtension(projFile);
        _mapDocument.paintGraphics();
        _mapView.zoomToExtent(_mapView.getViewExtent());
    }
    // </editor-fold>
    // </editor-fold>

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel("javax.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmMainOld.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmMainOld.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmMainOld.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmMainOld.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //new frmMain().setVisible(true);
                FrmMainOld frame = new FrmMainOld();
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.meteoinfo.legend.LayersLegend _mapDocument;
    private org.meteoinfo.layout.MapLayout _mapLayout;
    private org.meteoinfo.map.MapView _mapView;
    private javax.swing.JButton jButton_AddLayer;
    private javax.swing.JButton jButton_EditVertices;
    private javax.swing.JButton jButton_FitToScreen;
    private javax.swing.JButton jButton_FullExtent;
    private javax.swing.JButton jButton_Identifer;
    private javax.swing.JButton jButton_LabelSet;
    private javax.swing.JButton jButton_Measurement;
    private javax.swing.JButton jButton_NewCircle;
    private javax.swing.JButton jButton_NewCurve;
    private javax.swing.JButton jButton_NewCurvePolygon;
    private javax.swing.JButton jButton_NewEllipse;
    private javax.swing.JButton jButton_NewFreehand;
    private javax.swing.JButton jButton_NewLabel;
    private javax.swing.JButton jButton_NewPoint;
    private javax.swing.JButton jButton_NewPolygon;
    private javax.swing.JButton jButton_NewPolyline;
    private javax.swing.JButton jButton_NewRectangle;
    private javax.swing.JButton jButton_OpenData;
    private javax.swing.JButton jButton_PageSet;
    private javax.swing.JButton jButton_PageZoomIn;
    private javax.swing.JButton jButton_PageZoomOut;
    private javax.swing.JButton jButton_Pan;
    private javax.swing.JButton jButton_RemoveDataLayers;
    private javax.swing.JButton jButton_SavePicture;
    private javax.swing.JButton jButton_SelectElement;
    private javax.swing.JButton jButton_SelectFeatures;
    private javax.swing.JButton jButton_ZoomIn;
    private javax.swing.JButton jButton_ZoomOut;
    private javax.swing.JButton jButton_ZoomToExtent;
    private javax.swing.JButton jButton_ZoomToLayer;
    private javax.swing.JComboBox jComboBox_PageZoom;
    private javax.swing.JLabel jLabel_Coordinate;
    private javax.swing.JLabel jLabel_Status;
    private javax.swing.JMenuBar jMenuBar_Main;
    private javax.swing.JMenuItem jMenuItem_About;
    private javax.swing.JMenuItem jMenuItem_AttributeData;
    private javax.swing.JMenuItem jMenuItem_ClearSelection;
    private javax.swing.JMenuItem jMenuItem_Clipping;
    private javax.swing.JMenuItem jMenuItem_Help;
    private javax.swing.JMenuItem jMenuItem_InsertLegend;
    private javax.swing.JMenuItem jMenuItem_InsertMapFrame;
    private javax.swing.JMenuItem jMenuItem_InsertNorthArrow;
    private javax.swing.JMenuItem jMenuItem_InsertScaleBar;
    private javax.swing.JMenuItem jMenuItem_InsertText;
    private javax.swing.JMenuItem jMenuItem_InsertTitle;
    private javax.swing.JMenuItem jMenuItem_InsertWindArrow;
    private javax.swing.JMenuItem jMenuItem_Layers;
    private javax.swing.JMenuItem jMenuItem_LayoutProperty;
    private javax.swing.JMenuItem jMenuItem_MapProperty;
    private javax.swing.JMenuItem jMenuItem_MaskOut;
    private javax.swing.JMenuItem jMenuItem_Open;
    private javax.swing.JMenuItem jMenuItem_Options;
    private javax.swing.JMenuItem jMenuItem_OutputMapData;
    private javax.swing.JMenuItem jMenuItem_PluginManager;
    private javax.swing.JMenuItem jMenuItem_Projection;
    private javax.swing.JMenuItem jMenuItem_Save;
    private javax.swing.JMenuItem jMenuItem_SaveAs;
    private javax.swing.JMenuItem jMenuItem_Script;
    private javax.swing.JMenuItem jMenuItem_SelByAttr;
    private javax.swing.JMenuItem jMenuItem_SelByLocation;
    private javax.swing.JMenu jMenu_Help;
    private javax.swing.JMenu jMenu_Insert;
    private javax.swing.JMenu jMenu_Plugin;
    private javax.swing.JMenu jMenu_Project;
    private javax.swing.JMenu jMenu_Selection;
    private javax.swing.JMenu jMenu_Tools;
    private javax.swing.JMenu jMenu_View;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel_LayoutTab;
    private javax.swing.JPanel jPanel_MainToolBar;
    private javax.swing.JPanel jPanel_MapTab;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JToolBar.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane_Main;
    private javax.swing.JToolBar jToolBar_Base;
    private javax.swing.JToolBar jToolBar_Graphic;
    private javax.swing.JToolBar jToolBar_Layout;
    // End of variables declaration//GEN-END:variables
}
