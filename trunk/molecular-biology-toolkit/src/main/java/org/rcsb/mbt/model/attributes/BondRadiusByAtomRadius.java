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
 * Created on 2007/02/08
 *
 */ 
package org.rcsb.mbt.model.attributes;


import org.rcsb.mbt.model.*;
import org.rcsb.mbt.model.util.PeriodicTable;


/**
 *  This class implements the BondRadius interface by applying a radius
 *  to the given Bond by using the AtomRadius class.
 *  <P>
 *  @author	John L. Moreland
 *  @author Peter Rose (revisions)
 *  @see	org.rcsb.mbt.model.attributes.IBondRadius
 *  @see	org.rcsb.mbt.model.Bond
 */
public class BondRadiusByAtomRadius
	implements IBondRadius
{
	public static final String NAME = "By Atom Radius (Smooth Bonds)";

	// Holds a singleton instance of this class.
	private static BondRadiusByAtomRadius singleton = null;

	/**
	 *  The constructor is PRIVATE so that the "create" method
	 *  is used to produce a singleton instance of this class.
	 */
	private BondRadiusByAtomRadius( )
	{
	}

	/**
	 *  Return the singleton instance of this class.
	 */
	public static BondRadiusByAtomRadius create( )
	{
		if ( BondRadiusByAtomRadius.singleton == null ) {
			BondRadiusByAtomRadius.singleton = new BondRadiusByAtomRadius( );
		}
		return BondRadiusByAtomRadius.singleton;
	}

	/**
	 *  Produce the primary radius based upon the first atom radius.
	 */
	public float getBondRadius( final Bond bond )
	{
		final Atom atom = bond.getAtom( 0 );
		final StructureMap structureMap = atom.getStructure().getStructureMap( );
		final StructureStyles structureStyles = structureMap.getStructureStyles( );
		final AtomStyle atomStyle = (AtomStyle) structureStyles.getStyle( atom );
//		return atomStyle.getAtomRadius( atom );
        // all bonds should have the same radius based on the relative size of
		// the atoms
		int atomicNumber = PeriodicTable.getElementNumber(atom.element);
		float radius = ElementStyles.getElementRadius(atomicNumber);
		float actualRadius = atomStyle.getAtomRadius(atom);
		float scale = actualRadius/radius;
		// make sure we don't draw bonds that appear to thick
		scale = Math.min(scale, 0.2f);
		return scale * 1.7f; // radius of a typical atom
	}

	/**
	 *  Produce the secondary radius based upon the second atom radius.
	 */
	public float getSplitBondRadius( final Bond bond )
	{
		final Atom atom = bond.getAtom( 1 );
		final StructureMap structureMap = atom.getStructure().getStructureMap( );
		final StructureStyles structureStyles = structureMap.getStructureStyles( );
		final AtomStyle atomStyle = (AtomStyle) structureStyles.getStyle( atom );
//		return atomStyle.getAtomRadius( atom );
		// all bonds should have the same radius based on the relative size of
		// the atoms
		int atomicNumber = PeriodicTable.getElementNumber(atom.element);
		float radius = ElementStyles.getElementRadius(atomicNumber);
		float actualRadius = atomStyle.getAtomRadius(atom);
		float scale = actualRadius/radius;
		// make sure we don't draw bonds that appear to thick
		scale = Math.min(scale, 0.2f);
		return scale * 1.7f; // radius of a typical atom
	}
}

