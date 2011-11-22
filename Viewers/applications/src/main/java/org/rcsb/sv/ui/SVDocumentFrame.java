/*
 * BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence. This should
 * be distributed with the code. If you do not have a copy,
 * see:
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors. These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 * http://www.biojava.org/
 *
 * This code was contributed from the Molecular Biology Toolkit
 * (MBT) project at the University of California San Diego.
 *
 * Please reference J.L. Moreland, A.Gramada, O.V. Buzko, Qing
 * Zhang and P.E. Bourne 2005 The Molecular Biology Toolkit (MBT):
 * A Modular Platform for Developing Molecular Visualization
 * Applications. BMC Bioinformatics, 6:21.
 *
 * The MBT project was funded as part of the National Institutes
 * of Health PPG grant number 1-P01-GM63208 and its National
 * Institute of General Medical Sciences (NIGMS) division. Ongoing
 * development for the MBT project is managed by the RCSB
 * Protein Data Bank(http://www.pdb.org) and supported by funds
 * from the National Science Foundation (NSF), the National
 * Institute of General Medical Sciences (NIGMS), the Office of
 * Science, Department of Energy (DOE), the National Library of
 * Medicine (NLM), the National Cancer Institute (NCI), the
 * National Center for Research Resources (NCRR), the National
 * Institute of Biomedical Imaging and Bioengineering (NIBIB),
 * the National Institute of Neurological Disorders and Stroke
 * (NINDS), and the National Institute of Diabetes and Digestive
 * and Kidney Diseases (NIDDK).
 *
 * Created on 2008/12/22
 *
 */ 
package org.rcsb.sv.ui;



import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.rcsb.mbt.model.Structure;
import org.rcsb.mbt.model.StructureModel;
import org.rcsb.mbt.model.util.Status;
import org.rcsb.sv.controllers.app.SVVersionInformation;
import org.rcsb.sv.controllers.app.SimpleViewer;
import org.rcsb.uiApp.controllers.app.AppBase;
import org.rcsb.vf.controllers.app.BBBrowserLauncher;
import org.rcsb.vf.controllers.app.VFAppBase;
import org.rcsb.vf.glscene.jogl.GlGeometryViewer;
import org.rcsb.vf.ui.VFDocumentFrameBase;
import org.rcsb.vf.ui.VFUIBuilder;



public class SVDocumentFrame extends VFDocumentFrameBase
{
	public class SimpleViewerUIBuilder extends VFUIBuilder
	{
		private final String helpURL = "http://www.pdb.org/pdb/staticHelp.do?p=help/viewers/simpleViewer_viewer.html";
		@Override
		public void run()
		{
			super.run();
							// create base level ui pieces
			
			GlGeometryViewer glViewer = getGlGeometryViewer();

			if (!AppBase.backgroundScreenshotOnly)
			{
				Container viewerFrameContainer = getContentPane();
				
				// Create a pane for the structure viewer and the
				// sequence viewer
				viewerFrameContainer.add(glViewer, BorderLayout.CENTER);

				validate();
				final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				
				if (screenSize.height < 768)
					setSize(800, 550);
				
				else
				{
					final int width = (int) (screenSize.getWidth() * 0.8);
					final int height = (int) (screenSize.getHeight() * 0.8);
					setBounds((int) (screenSize.getWidth() / 2 - width / 2),
							  (int) (screenSize.getHeight() / 2 - height / 2),
									width, height);
				}

				// Make sure we clean up if the user hits the close box
				final WindowAdapter closer = VFAppBase.getApp().new ViewerCloserListener(true);
				addWindowListener(closer);
			}

			// add help menu
			final String helpURL = "http://www.pdb.org/pdb/staticHelp.do?p=help/viewers/simpleViewer_viewer.html";
			final JMenu helpMenu = new JMenu("Help");
			final JMenuItem helpItem = new JMenuItem("Help");
			final ActionListener helpListener =
				new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						Thread runner = new Thread()
						{
							@Override
							public void run()
							{
								String address = helpURL;
								try
								{
									BBBrowserLauncher.openURL(address);
								}
								
								catch (IOException e)
								{
//									displayErrorMessage("Unable to open help site.");
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
			
			//
			// progress update 0.85, "transferring structure"
			//

			// View the Structure
			StructureModel model = getModel();

			for (Structure structure : model.getStructures())
			{
				// get the pdb id from the structure's url.
				String pdbId = "";
				if (structure != null) {
					glViewer.structureAdded(structure);

					final String url = structure.getUrlString();
					String[] split = url.split("[/\\\\]");
					split = split[split.length - 1].split("\\.");
					pdbId = split[0];
					structure.getStructureMap().setPdbId(pdbId);
				}
			}

			addWindowListener(glViewer);
			// -----------------------------------------------------------------------------

			//
			// Enable picking
			//
			
			glViewer.addPickEventListener(getSceneController());

			// Reset the view to look at the center of the data.
			getSceneController().resetView(false);

			// Show the Structure's pdb id and the version in the title
			// bar.
			if (!AppBase.backgroundScreenshotOnly)
			{
				if (model.hasStructures())
					setTitle(model.getStructures().get(0).getStructureMap().getPdbId());
			}
			// progress.setProgress(0.90f);
			// progress.setNote("Displaying Main Frame...");

			if (_showFrame) 
				setVisible(true);

			// curSize = getSize();

			glViewer.setDoubleBuffered(false);
		}
	}
	
	public void displayErrorMessage(final String text) {
		Status.output(Status.LEVEL_ERROR, text);
	}
	
	final static long serialVersionUID = 0x43518477;
	
	public SVDocumentFrame(String title, URL iconUrl)
	{
		super(title, iconUrl);
						// let the super create the glviewer
				
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				// Check and put up performance warning dialog for large molecules,
				// if it hasn't been presented, already.
				//
				public void run()
				{					
					Preferences prefs = Preferences.userNodeForPackage(SimpleViewer.getApp().getClass());
					boolean performanceWarningShown =
						prefs.getBoolean(VFAppBase.PERFORMANCE_WARNING_KEY, false);
					
					if (!performanceWarningShown)
					{
						int option = JOptionPane.showOptionDialog(
										/*  progress, */ null,
										"Please be aware that the ability to manipulate large molecules is dependent on hardware configuration.",
										"Title", JOptionPane.YES_NO_OPTION,
										JOptionPane.WARNING_MESSAGE, null,
										new String[] { "Continue", "Exit" },
										"Continue");
						if (option != JOptionPane.OK_OPTION)
							System.exit(0);
						
						prefs.putBoolean(VFAppBase.PERFORMANCE_WARNING_KEY, true);
					}
				}
			});
		}
			
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
		
		catch (final InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void initialize(boolean showFrame)
	{
		super.initialize(showFrame);
		
		try
		{
			SwingUtilities.invokeAndWait(new SimpleViewerUIBuilder());
		}

		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
		
		catch (final InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void setTitle(String title)
	{
		super.setTitle("RCSB PDB Simple Viewer " + SVVersionInformation.version() + " (powered by the MBT): " + title);
	}

}
