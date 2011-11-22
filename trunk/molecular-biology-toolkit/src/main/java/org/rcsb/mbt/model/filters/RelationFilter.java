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
package org.rcsb.mbt.model.filters;


import org.rcsb.mbt.model.StructureComponent;
import org.rcsb.mbt.model.StructureComponentRelation;
import org.rcsb.mbt.model.StructureComponentRegistry.ComponentType;


/**
 *  Defines a StructureComponentFilter sub-class which handles the filtering
 *  of StructureComponent objects that are related to the StructureComponent
 *  subject parameter. This is used by the Structure class to process
 *  getStructureComponentRelations method calls.
 *  <P>
 *  @author	John L. Moreland
 *  @see	org.rcsb.mbt.model.filters.IStructureComponentFilter
 *  @see	org.rcsb.mbt.model.StructureComponent
 *  @see	org.rcsb.mbt.model.StructureComponentIterator
 *  @see	org.rcsb.mbt.model.Structure
 */
public class RelationFilter
	implements IStructureComponentFilter
{
	// The relationship implementation object
	private StructureComponentRelation relation = null;

	// The relationship subject to be compared to objects
	private StructureComponent subject = null;

	/**
	 * Constructs a RelationFilter for the given StructureComponentRelation
	 * instance and StructureComponent subject parameters.
	 * <P>
	 */
	public RelationFilter( final StructureComponentRelation relation,
		final StructureComponent subject )
			throws IllegalArgumentException
	{
		this.relation = relation;
		this.subject = subject;
	}

	/**
	 *  Return the StructureComponentRegistry type for returned object types.
	 */
	public ComponentType type( )
	{
		return this.relation.getObjectType();
	}

	/**
	 *  Ask the relation object if the object should be accepted or not.
	 *  <P>
	 */
	public boolean accept( final StructureComponent structureComponent )
	{
		return this.relation.isRelated( this.subject, structureComponent );
	}
}

