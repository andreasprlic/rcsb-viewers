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
package org.rcsb.pw.ui.mutatorPanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.rcsb.mbt.model.attributes.AtomRadiusByCpk;
import org.rcsb.mbt.model.attributes.AtomRadiusByScaledCpk;
import org.rcsb.pw.controllers.app.ProteinWorkshop;
import org.rcsb.pw.controllers.scene.mutators.MutatorEnum;
import org.rcsb.pw.controllers.scene.mutators.options.StylesOptions;
import org.rcsb.pw.ui.FullWidthBoxLayout;
import org.rcsb.uiApp.controllers.app.AppBase;
import org.rcsb.uiApp.controllers.update.IUpdateListener;
import org.rcsb.uiApp.controllers.update.UpdateEvent;
import org.rcsb.vf.controllers.scene.mutators.MutatorBase;
import org.rcsb.vf.glscene.jogl.ChainGeometry.RibbonForm;



public class StylesOptionsPanel extends JPanel implements IUpdateListener
{
	private static final long serialVersionUID = -238303835306014563L;

	private final class RibbonFormComboListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JComboBox source = (JComboBox)e.getSource();
			
			ProteinWorkshop.sgetSceneController().getMutatorEnum().getStylesMutator().getOptions().setCurrentRibbonForm(RibbonForm.values()[source.getSelectedIndex()]);
		}
	}
	
	private final class RibbonSmoothingCheckListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JCheckBox source = (JCheckBox)e.getSource();
			
			ProteinWorkshop.sgetSceneController().getMutatorEnum().getStylesMutator().getOptions().setAreRibbonsSmoothed(source.isSelected());
		}
	}
	
//	private final class AtomFormListener implements ActionListener {
//		public void actionPerformed(final ActionEvent e)
//		{
//			final JComboBox source = (JComboBox)e.getSource();
//			MutatorEnum mutEnum = ProteinWorkshop.sgetSceneController().getMutatorEnum();
//			switch(source.getSelectedIndex())
//			{
//			case 0:
//				mutEnum.getStylesMutator().getOptions().setCurrentAtomForm(Geometry.FORM_THICK);
//				break;
//			case 1:
//				mutEnum.getStylesMutator().getOptions().setCurrentAtomForm(Geometry.FORM_POINTS);
//				break;
//			default:
//				(new Exception(source.getSelectedIndex() + " not a valid index")).printStackTrace();
//			}
//		}
//	}
	
	private final class AtomRadiusListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JComboBox source = (JComboBox)e.getSource();
			MutatorEnum mutEnum = ProteinWorkshop.sgetSceneController().getMutatorEnum();
			switch(source.getSelectedIndex()) {
			case 0:
				mutEnum.getStylesMutator().getOptions().setCurrentAtomRadius(AtomRadiusByScaledCpk.create());
				break;
			case 1:
				mutEnum.getStylesMutator().getOptions().setCurrentAtomRadius(AtomRadiusByCpk.create());
				break;
			default:
				(new Exception(source.getSelectedIndex() + " not a valid index")).printStackTrace();
			}
		}
	}
	
	private final class BondShowOrderListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JCheckBox source = (JCheckBox)e.getSource();
			ProteinWorkshop.sgetSceneController().getMutatorEnum().getStylesMutator().getOptions().setShowBondOrder(source.isSelected());
		}
	}
	
	private final class ComboDescriptorPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6357559779726764605L;

		public ComboDescriptorPanel(final JLabel label, final JComponent combo) {
			super(null, false);
			super.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			super.add(label);
			super.add(combo);
		}
	}
	
//	private ComboDescriptorPanel atomFormComboDescriptor = null;
//	private JLabel atomFormLabel = null;
//	private JComboBox atomFormBox = null;
//	private static final String[] AVAILABLE_ATOM_FORM_LABELS = {"Solid", "Point"};	// indices from this array will be hard-coded elsewhere in this source file only.
	
	private ComboDescriptorPanel atomRadiusComboDescriptor = null;
	private JLabel atomRadiusLabel = null;
	private JComboBox atomRadiusBox = null;
	private static final String[] AVAILABLE_ATOM_RADIUS_LABELS = {"Small", "CPK"};	// indices from this array will be hard-coded elsewhere in this source file only.
	
	private ComboDescriptorPanel bondOrderComboDescriptor = null;
	private JLabel bondOrderLabel = null;
	private JCheckBox isBondOrderShownBox = null;
	
	private ComboDescriptorPanel ribbonFormComboDescriptor = null;
	private JLabel ribbonFormLabel = null;
	private JComboBox ribbonFormStyles = null;
	private JCheckBox areRibbonsSmoothedBox = null;
	
    public StylesOptionsPanel() {
        super(null, false);
        super.setLayout(new FullWidthBoxLayout());
        super.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("3)  Change the tool's options, if necessary."),
                BorderFactory.createEmptyBorder(-1,1,1,1)));
        
//        this.atomFormLabel = new JLabel("Drawing Completeness of Atoms: ");
//		this.atomFormBox = new JComboBox(StylesOptionsPanel.AVAILABLE_ATOM_FORM_LABELS);
//		this.atomFormComboDescriptor = new ComboDescriptorPanel(this.atomFormLabel, this.atomFormBox);
//		super.add(this.atomFormComboDescriptor);
//		this.setCurrentAtomForm();
//		this.atomFormBox.addActionListener(new AtomFormListener());
		
		this.atomRadiusLabel = new JLabel("Radius of Atoms: ");
		this.atomRadiusBox = new JComboBox(StylesOptionsPanel.AVAILABLE_ATOM_RADIUS_LABELS);
		this.atomRadiusComboDescriptor = new ComboDescriptorPanel(this.atomRadiusLabel, this.atomRadiusBox);
		super.add(this.atomRadiusComboDescriptor);
		this.setCurrentAtomRadius();
		this.atomRadiusBox.addActionListener(new AtomRadiusListener());
		
		this.bondOrderLabel = new JLabel("Indicate Bond Orders: ");
		this.isBondOrderShownBox = new JCheckBox();
		this.bondOrderComboDescriptor = new ComboDescriptorPanel(this.bondOrderLabel, this.isBondOrderShownBox);
		super.add(this.bondOrderComboDescriptor);
		this.setCurrentBondOrder();
		this.isBondOrderShownBox.addActionListener(new BondShowOrderListener());
		
		
        // note that the indices of these JComboBoxes must conform to the indices of the constants in StylesOptions.
        this.ribbonFormLabel = new JLabel("Ribbon style:  ");
        this.ribbonFormStyles = new JComboBox();
        this.ribbonFormStyles.addItem(RibbonForm.RIBBON_SIMPLE_LINE.getDescription());
        this.ribbonFormStyles.addItem(RibbonForm.RIBBON_TRADITIONAL.getDescription());
        this.ribbonFormStyles.addItem(RibbonForm.RIBBON_CYLINDRICAL_HELICES.getDescription());
        this.setCurrentRibbonForm();
        this.ribbonFormStyles.addActionListener(new RibbonFormComboListener());
        
        this.areRibbonsSmoothedBox = new JCheckBox("Ribbons Smoothed");
        this.setCurrentAreRibbonsSmoothed();
        this.areRibbonsSmoothedBox.addActionListener(new RibbonSmoothingCheckListener());
        
        this.ribbonFormComboDescriptor = new ComboDescriptorPanel(this.ribbonFormLabel, this.ribbonFormStyles);   
        
        super.add(this.ribbonFormComboDescriptor);
        super.add(this.areRibbonsSmoothedBox);
        
        this.reset();
        
        AppBase.sgetUpdateController().registerListener(this);
        ProteinWorkshop.sgetActiveFrame().setStylesOptionsPanel(this);
    }

    // mode corresponds to the index in AVAILABLE_MODE_LABELS.
    public void updateMutatorActivation(final MutatorBase.ActivationType activationType) {
//    	this.atomFormComboDescriptor.setVisible(false);
    	this.atomRadiusComboDescriptor.setVisible(false);
    	this.bondOrderComboDescriptor.setVisible(false);
    	this.ribbonFormComboDescriptor.setVisible(false);
    	this.areRibbonsSmoothedBox.setVisible(false);
    	
    	switch(activationType) {
    	case ATOMS_AND_BONDS:	// atoms and bonds
    		this.bondOrderComboDescriptor.setVisible(true);
//    		this.atomFormComboDescriptor.setVisible(true);
    		this.atomRadiusComboDescriptor.setVisible(true);
    		break;
    	case RIBBONS:	// ribbons
        	this.ribbonFormComboDescriptor.setVisible(true);
        	this.areRibbonsSmoothedBox.setVisible(true);
    		break;
    	case SURFACE:
    		// to do
    		break;
    	default:
    		(new Exception(activationType + " is an invalid pick level")).printStackTrace();
    	}
    	
    	super.revalidate();
    	super.repaint();
//    	super.doLayout();
    }
    
//    public void setCurrentAtomForm() {
//    	final StylesOptions options = Model.getSingleton().getMutatorModel().getStylesMutator().getOptions();
//    	
//    	switch(options.getCurrentAtomForm()) {
//		case Geometry.FORM_THICK:	
//			this.atomFormBox.setSelectedIndex(0);
//			break;
//		case Geometry.FORM_POINTS:
//			this.atomFormBox.setSelectedIndex(1);
//			break;
//		default:
//			(new Exception(options.getCurrentAtomForm() + " not a valid index")).printStackTrace();
//		}
//    }
    
    public void setCurrentAtomRadius() {
    	final StylesOptions options = ProteinWorkshop.sgetSceneController().getMutatorEnum().getStylesMutator().getOptions();
    	
    	if(options.getCurrentAtomRadius() instanceof AtomRadiusByScaledCpk) {
			this.atomRadiusBox.setSelectedIndex(0);
    	} else if(options.getCurrentAtomRadius() instanceof AtomRadiusByCpk) {
    		this.atomRadiusBox.setSelectedIndex(1);
    	} else {
			(new Exception(options.getCurrentAtomRadius().getClass().getName() + " not a handled radius class")).printStackTrace();
		}
    }
    
    public void setCurrentBondOrder() {
    	final StylesOptions options = ProteinWorkshop.sgetSceneController().getMutatorEnum().getStylesMutator().getOptions();
    	
    	this.isBondOrderShownBox.setSelected(options.isBondOrderShown());
    }
    
    public void setCurrentRibbonForm() {
    	final StylesOptions options = ProteinWorkshop.sgetSceneController().getMutatorEnum().getStylesMutator().getOptions();
    	
    	this.ribbonFormStyles.setSelectedIndex(options.getCurrentRibbonForm().ordinal());
    }
    
    public void setCurrentAreRibbonsSmoothed() {
    	final StylesOptions options = ProteinWorkshop.sgetSceneController().getMutatorEnum().getStylesMutator().getOptions();
    	
    	this.areRibbonsSmoothedBox.setSelected(options.areRibbonsSmoothed());
    }
    
    public void reset()
    {
    	final StylesOptions options = ProteinWorkshop.sgetSceneController().getMutatorEnum().getStylesMutator().getOptions();
    	
    	// reset all the components back to their defaults
    	options.setCurrentAtomForm(StylesOptions.DEFAULT_ATOM_FORM);
    	options.setCurrentAtomRadius(StylesOptions.DEFAULT_ATOM_RADIUS);
    	options.setShowBondOrder(StylesOptions.DEFAULT_IS_BOND_ORDER_SHOWN); // just to make sure; actionlistener should do this.
    	options.setCurrentRibbonForm(StylesOptions.DEFAULT_RIBBON_FORM);
    	options.setAreRibbonsSmoothed(StylesOptions.DEFAULT_RIBBON_SMOOTHING);
    	
    	this.areRibbonsSmoothedBox.setSelected(true);
//    	this.atomFormBox.setSelectedIndex(0);
    	this.atomRadiusBox.setSelectedIndex(0);
    	this.isBondOrderShownBox.setSelected(true);
    	this.ribbonFormStyles.setSelectedIndex(1);
    	
    	// enact the default mode
    	this.updateMutatorActivation(MutatorBase.ActivationType.ATOMS_AND_BONDS);
    }

	/* (non-Javadoc)
	 * @see edu.sdsc.mbt.views_controller.IViewUpdateListener#handleModelChangedEvent(edu.sdsc.mbt.views_controller.ViewUpdateEvent)
	 */
	public void handleUpdateEvent(UpdateEvent evt)
	{
		if (evt.action == UpdateEvent.Action.VIEW_RESET)
			reset();
	}
}
