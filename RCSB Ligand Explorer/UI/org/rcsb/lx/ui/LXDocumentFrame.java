package org.rcsb.lx.ui;


import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rcsb.lx.controllers.app.LXVersionInformation;
import org.rcsb.lx.controllers.app.LigandExplorer;
import org.rcsb.lx.controllers.scene.LXSceneController;
import org.rcsb.lx.controllers.update.LXUpdateController;
import org.rcsb.lx.glscene.jogl.LXGlGeometryViewer;
import org.rcsb.lx.model.LXModel;
import org.rcsb.lx.ui.dialogs.AngleDialog;
import org.rcsb.lx.ui.dialogs.DihedralDialog;
import org.rcsb.lx.ui.dialogs.DistanceDialog;
import org.rcsb.lx.ui.dialogs.IPickInfoReceiver;
import org.rcsb.mbt.glscene.jogl.Constants;
import org.rcsb.mbt.glscene.jogl.GlGeometryViewer;
import org.rcsb.mbt.model.StructureModel;
import org.rcsb.mbt.model.Residue;
import org.rcsb.mbt.model.Structure;
import org.rcsb.mbt.model.util.Status;
import org.rcsb.mbt.ui.views.StructureComponentInspector;
import org.rcsb.vf.controllers.app.BBBrowserLauncher;
import org.rcsb.vf.controllers.app.VFAppBase;
import org.rcsb.vf.ui.VFDocumentFrameBase;



public class LXDocumentFrame extends VFDocumentFrameBase
{
	public LXModel getModel() { return (LXModel)super.getModel(); }
	public LXGlGeometryViewer getGlGeometryViewer() { return (LXGlGeometryViewer)super.getGlGeometryViewer(); }
	public LXUpdateController getUpdateController() { return (LXUpdateController)super.getUpdateController(); }
	public LXSceneController getSceneController() { return (LXSceneController)super.getSceneController(); }
	
	class LigandExplorerUIBuilder extends VFDocumentFrameBase.UIBuilder
	{
		/**
		 * Receive notification if a display dialog is destroyed.
		 * 
		 * @author rickb
		 *
		 */
		
		class DisplayDialogListener extends WindowAdapter
		{

			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosed(WindowEvent e)
			{
				displayDialog = null;
				super.windowClosed(e);
			}
		}
		
		/***
		 * Race condition - if we check and dispose the current displayDialog
		 * in the UI thread, then create a new dialog, we'll receive the
		 * WindowClosed event *after* the new dialog has been created and
		 * assigned, which will reassign displayDialog to null (after
		 * we've assigned it to the newly created dialog.)
		 * 
		 * So, by creating a new thread, and calling 'invokeAndWait' on the
		 * dispose call, all of the UI events will be processed and our
		 * re-assignment won't get clobbered.
		 * 
		 * And we'll still know when the dialog gets closed, otherwise.
		 * (A lot of coding just to clear the event que, I might add.)
		 * 
		 * 25-Jun-08 - rickb
		 */
		class MeasurementDialogExclusiveCheck extends Thread
		{
			private JDialog _dlg;
			
			MeasurementDialogExclusiveCheck(JDialog dlg) { _dlg = dlg; }
			
			public void run()
			{
				if (displayDialog != null)
				{
					try
					{
					SwingUtilities.invokeAndWait(new Runnable()
					{
						public void run()
						{											
							JDialog dlg = (JDialog)displayDialog;
							dlg.dispose();										
						}
					});
					}
					catch (InvocationTargetException e) {}
					catch (InterruptedException e){}
				}

				_dlg.addWindowListener(new DisplayDialogListener());
				displayDialog = (IPickInfoReceiver)_dlg;
				_dlg.setVisible(true);
			}
		};
		
		public void run()
		{
			super.run();
			// define the base level UI items

			if (!LigandExplorer.backgroundScreenshotOnly)
			{	

				final JMenuItem fileSaveContactsItem = new JMenuItem("Save Interactions...");
				fileSaveContactsItem.setEnabled(true);
				final ActionListener fileSaveContactsListener = new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						LigandExplorer.getApp().saveInteractionsFlag = true;
						sidebar.applyButton.doClick();
					}
				};
				
				fileSaveContactsItem.addActionListener(fileSaveContactsListener);
				fileMenu.add(fileSaveContactsItem);

				menuBar.add(fileMenu);

				final JMenu analysisMenu = new JMenu("Analysis");
				final JMenuItem jMenuAnalysisDistance = new JMenuItem("Measure Distance...");
				final JMenuItem jMenuAnalysisAngle = new JMenuItem("Measure Angle...");
				final JMenuItem jMenuAnalysisDihedral = new JMenuItem("Dihedral Angle...");

				jMenuAnalysisDistance.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent ae)
					{
						DistanceDialog dlg = new DistanceDialog(LXDocumentFrame.this);
						dlg.setVisible(false);
						MeasurementDialogExclusiveCheck ex = new MeasurementDialogExclusiveCheck(dlg);
						ex.start();
					}
				});

				jMenuAnalysisAngle.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent ae)
					{
						AngleDialog dlg = new AngleDialog(LXDocumentFrame.this);
						dlg.setVisible(false);
						MeasurementDialogExclusiveCheck ex = new MeasurementDialogExclusiveCheck(dlg);
						ex.start();
					}
				});

				jMenuAnalysisDihedral.addActionListener(
					new ActionListener()
					{
						public void actionPerformed(final ActionEvent ae)
						{
							DihedralDialog dlg = new DihedralDialog(LXDocumentFrame.this);
							dlg.setVisible(false);
							MeasurementDialogExclusiveCheck ex = new MeasurementDialogExclusiveCheck(dlg);
							ex.start();
						}
					});

				analysisMenu.add(jMenuAnalysisDistance);
				analysisMenu.add(jMenuAnalysisAngle);
				analysisMenu.add(jMenuAnalysisDihedral);

				menuBar.add(analysisMenu);

				final JMenu helpMenu = new JMenu("Help");
				final JMenuItem helpItem = new JMenuItem("Help");
				final ActionListener helpListener =
					new ActionListener()
					{
						public void actionPerformed(ActionEvent actionEvent)
						{
							Thread runner = new Thread()
							{
								public void run()
								{
									String address = helpURL;
									try
									{
										BBBrowserLauncher.openURL(address);
									}
									
									catch (IOException e)
									{
										e.printStackTrace();
										displayErrorMessage("Unable to open help site.");
									}
								}
							};
							runner.start();
						}
					};
				
				helpItem.addActionListener(helpListener);
				helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
						Event.CTRL_MASK));
				helpMenu.add(helpItem);
				menuBar.add(helpMenu);	

				simpleViewerFrameContainer = LXDocumentFrame.this
						.getContentPane();

				// Create a splitPane for the sidebar and the vertical
				// split
				// pane
				horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
				horizontalSplitPane.setOneTouchExpandable(true);
				verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
				verticalSplitPane.setOneTouchExpandable(true);
				simpleViewerFrameContainer.add(verticalSplitPane);
				verticalSplitPane.setBottomComponent(horizontalSplitPane);

				Status.progress(-1, "Creating sequence viewer...");
				// create a sequence viewer
				sequenceViewer = new SequenceTabbedPane();
//				JoglViewer.this.sequenceViewer.setComponents();
				StructureModel model = getModel();
				if (model.hasStructures())
					sequenceViewer.newStructureAdded(model.getStructures().get(0), false);

				verticalSplitPane.setTopComponent(sequenceViewer);
			
				// create the id widget.
				pdbIdList.setEditable(true);
							
				final ActionListener rcsbClickListener =
					new ActionListener()
					{
						public void actionPerformed(ActionEvent actionEvent)
						{
							String cmd = actionEvent.getActionCommand();
							if (cmd.equals("comboBoxChanged"))
							{
								String pdbId = (String) pdbIdList
										.getSelectedItem();
								String url = Constants.pdbFileBase + pdbId
										+ Constants.pdbFileExtension;
								LigandExplorer.sgetDocController().loadStructure(url, pdbId);
	
								Status.progress(-1, "Creating side bar...");
								sidebar = new LigandSideBar(LXDocumentFrame.this);
								LXDocumentFrame.this
										.displaySideBar(sidebar);
							}
							// if ( cmd.equals( "comboBoxEdited" ) )
						}
					};
				
				pdbIdList.addActionListener(rcsbClickListener);

				// Only load the PDB ID code list when the list is first
				// popped up.
				final PopupMenuListener idCodePopupListener =
					new PopupMenuListener()
					{
						public void popupMenuWillBecomeVisible(PopupMenuEvent e)
						{
							// loadIdCodes();
						}
	
						public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
						{
						}
	
						public void popupMenuCanceled(PopupMenuEvent e)
						{
						}
					};
					
				pdbIdList.addPopupMenuListener(idCodePopupListener);

				// Create the sidebar
				sidebar = new LigandSideBar(LXDocumentFrame.this);
				horizontalSplitPane
						.setLeftComponent(sidebar);

				// Create a splitPane for the structure viewer and the
				// sequence viewer
				horizontalSplitPane
						.setRightComponent(getGlGeometryViewer());

				// Pack the frame
				// this.simpleViewerFrame.pack();

				validate();
				// this.simpleViewerFrame.setSize( 800, 600 );
				final Dimension screenSize = Toolkit
						.getDefaultToolkit().getScreenSize();
				if(screenSize.height < 768)
					setSize( 800, 550 );
				
				else
				{
					final int width = (int) (screenSize.getWidth() * 0.8);
					final int height = (int) (screenSize.getHeight() * 0.8);
					setBounds((int) (screenSize.getWidth() / 2 - width / 2),
								(int) (screenSize.getHeight() / 2 - height / 2),
								width, height);
				}
			}

			GlGeometryViewer viewer = getGlGeometryViewer();
			addWindowListener(viewer);
			// -----------------------------------------------------------------------------

			//
			// Enable picking
			//
			viewer.addPickEventListener(getSceneController());

			// Reset the view to look at the center of the data.
			getSceneController().resetView(false);

			if (!LigandExplorer.backgroundScreenshotOnly)
			{
				StructureModel model = getModel();
				String id = "";
				if (model.hasStructures())
					id = ": " + model.getStructures().get(0).getStructureMap().getPdbId();

				LXDocumentFrame.this
						.setTitle("RCSB PDB Ligand Explorer "
								+ LXVersionInformation.version()
								+ " (powered by the MBT)" + id);
			}

			if (_showFrame)
				setVisible(true);


			viewer.setDoubleBuffered(false);
			horizontalSplitPane
					.setDoubleBuffered(false);
			verticalSplitPane.setDividerLocation(150);					

			// Make sure we clean up if the user hits the close box
			final WindowAdapter closer = VFAppBase.getApp().new ViewerCloserListener(true);
			addWindowListener(closer);
		}
	}

	
	// the intended background color of the sidebar. All components internal to
	// the sidebar should be assigned this background color.
	public final static Color sidebarColor = new Color(198, 210, 223);

	private final JComboBox pdbIdList = new JComboBox();
	public JComboBox getPdbIdList() { return pdbIdList; }

	public Container simpleViewerFrameContainer = null;

	public SequenceTabbedPane sequenceViewer = null;

	private final String helpURL = "http://www.pdb.org/robohelp_f/#molecular_viewers/ligandviewer/user_guide.htm";

	/**
	 * The splitpane on the main frame's frame's contentPane.
	 */
	public JSplitPane horizontalSplitPane = null;
	public JSplitPane verticalSplitPane = null;

	/**
	 * Saves the number of pixels that the horizontal bar is from the right side
	 * of the screen. For resize operations.
	 */
	public int horizontalBarDistanceFromRight = 0; // the distance (in pixels)

	// of the horizontal
	// splitpane bar from the
	// right side of the window.

	public static boolean isStandalone = false;

	// Viewers
	protected LigandSideBar sidebar = null;
	public LigandSideBar getLigandSideBar () { return sidebar; }

	private IPickInfoReceiver displayDialog = null;
	
	public void initialize(boolean showFrame)
	{
		super.initialize(showFrame);
		
		LXModel model = getModel();

		for (Structure structure : model.getStructures())
		{
			// get the pdb id from the structure's url.
			String pdbId = "";
			if (structure != null)
			{
				// structureDocument.addStructure(structure);
				getGlGeometryViewer().structureAdded(structure);

				final String url = structure.getUrlString();
				String[] split = url.split("[/\\\\]");
				split = split[split.length - 1].split("\\.");
				pdbId = split[0];
				structure.getStructureMap().setPdbId(pdbId);
			}
			// ConvenienceUtils.setStructureDocument(structureDocument);

		}
		
		try
		{
			SwingUtilities.invokeAndWait(new LigandExplorerUIBuilder());
		}
		
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
		
		catch (final java.lang.reflect.InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	
	public void reset()
	{
		this.sidebar = new LigandSideBar(this);
		this.displaySideBar(this.sidebar);
	}

	public IPickInfoReceiver getDisplayDialog()
	{
		return this.displayDialog;
	}

	public LXDocumentFrame(final String title)
	{
		super(title);
	}
	
	
	public void displaySideBar(final LigandSideBar ligandSideBar)
	{
		this.horizontalSplitPane.setLeftComponent(ligandSideBar);
	}

	public void displayErrorMessage(final String text) {
		Status.output(Status.LEVEL_ERROR, text);
	}

	public void displayMessage(final String text) {

	}

	private JFrame inspectorFrame = null;

	private StructureComponentInspector inspector = null;

	/**
	 * Show or hide the inspector window.
	 */
	public void showInspector() {
		if (this.inspectorFrame == null)
		{
			this.inspectorFrame = new JFrame("MBT Inspector");
			final Container inspectorFrameContainer = this.inspectorFrame.getContentPane();
			this.inspectorFrame.setSize(300, 280);
			this.inspector = new StructureComponentInspector();
			inspectorFrameContainer.add(this.inspector);
		}

		this.inspectorFrame.setVisible(true);
	}
	
	public void displayInitialLigand()
	{
		final int ligSize = this.sidebar.ligandList.size();
		final String initialLigand = LigandExplorer.sgetModel().getInitialLigand();
		if(initialLigand != null) {
			for(int i = 0; i < ligSize; i++) {
				Residue r = (Residue)this.sidebar.ligandList.get(i);
				if(r.toString().toLowerCase().indexOf(initialLigand.toLowerCase()) >= 0) {
					sidebar.ligandJList.setSelectedIndex(i);
					sidebar.applyButton.doClick();
				}
			}
		}
	}
}
