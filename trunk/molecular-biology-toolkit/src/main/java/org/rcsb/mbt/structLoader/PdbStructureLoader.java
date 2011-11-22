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
package org.rcsb.mbt.structLoader;



import java.util.*;
import java.io.*;
import java.net.*;
import java.util.zip.*;

import org.rcsb.mbt.model.StructureComponentRegistry.ComponentType;
import org.rcsb.mbt.model.geometry.ModelTransformationList;
import org.rcsb.mbt.model.*;
import org.rcsb.mbt.model.util.*;



/**
 *  Implements the StructureLoader interface to enable reading of PDB files
 *  either from local disk or from a URL.
 *  <P>
 *  This loader follows (somewhat) the PDB file format as documented by:
 *  http://www.rcsb.org/pdb/docs/format/pdbguide2.2/guide2.2_frame.html
 *  <P>
 *  <h3>Rules for non-protein/ligand classification: ('breakoutByResId' defined)</h3>
 *  <p>
 *  In the XML reader, there are secondary identifiers that can be used to break out
 *  non-protein chains more clearly.  Those aren't available in the PDB file.</p>
 *  <p>
 *  However, by examining the HETATM records and looking at residue and compound id, it is
 *  possible to break out chains like the XML reader.  The structure is essentially the same,
 *  however the name specifics aren't, because we don't have those in the pdb file.</p>
 *  <p>
 *  In general, it is probably better and more accurate to bring up the .xml file, if
 *  possible.  If not, the reconstructed pdb reader with the 'breakoutByResId' flag
 *  (currently set explicitly in the doc controller) will do a reasonable job of
 *  mimic-ing the .xml reader behavior.</p>
 *  <ul>
 *  <li>
 *  HETATM records are considered to be ligands, no matter what their chain affiliation.</li>
 *  <li>
 *  Waters are broken out into their own chain.</li>
 *  <li>
 *  HETATM records that have no chain id are assigned a pseudo-id, beginning with an underscore
 *  and having a sequential numeric component for the rest of the id.</li>
 *  <li>
 *  HETATM records that have a chain id corresponding to a previously defined chain id in the
 *  ATOM record set (protein atoms) are broken out into their own chain with an id of
 *  &lt;original id&gt;'</li>
 *  </ul>
 *  <p>
 *  21-Oct-08 - rickb</p>
 *  <p style="red">
 *  Note: The loader currently only responds to ATOM, HETATM records and the first MODEL encountered.
 *  In particular, CONECT and all the secondary structure stuff are completely ignored.<br/>
 *  The rest of the system is relying on either the dictionaries or
 *  internal calculations/determinations for bond information.
 *  10-Oct-08 - rickb</p>
 *  
 *  @author	John L. Moreland
 *  @see	org.rcsb.mbt.structLoader.IStructureLoader
 *  @see	org.rcsb.mbt.structLoader.StructureFactory
 */
public class PdbStructureLoader
	implements IFileStructureLoader
{
	protected String urlString = null;
	private long expectedInputBytes = 1;
	private Structure structure;
	private boolean treatModelsAsSubunits = false;
	private Map<Integer, String> entityNameMap = new HashMap<Integer, String>();
	
	/**
	 * Set this if the models are part of a greater whole.
	 * This is normally set from a flag in the scene manager.
	 * 
	 * @param flag
	 */
	public void setTreatModelsAsSubunits(boolean flag) { treatModelsAsSubunits = flag; }
	
	// A hashtable of vectors where
	// each hash KEY is the StructureComponent type String.
	// each hash VALUE is a Vector of StructureComponent objects.
	// NOTE: This protected variable is used only to enable each new
	// Structure object to access the newly loaded data. The Structure
	// constructor stores a copy of the object so this variable can
	// be re-used for the next load request. This is not thread-safe
	// but there doesn't seem to be any other way to enable an
	// anonymous class to be handed state (asside from parameters).
	protected Hashtable<ComponentType, Vector<StructureComponent>> passComponents = null;

	// Container for any general meta-data that describes the structure.
	protected StructureInfo structureInfo = null;
	
	//
	// StructureLoader interface methods
	//

	/**
	 * Returns the common name for the loader implementation.
	 * This is the string that might appear in a user-selectable menu.
	 */
	public String getLoaderName( )
	{
		return new String( "PDB Structure Loader" );
	}


	/**
	 * Returns a reference to a named structure as a Structure object.
	 * The "name" may be interpreted by the specific implementation
	 * of the StructureLoader class. For example, a file loader would
	 * interpret the "name" as a file or URL path, while a database loader
	 * would interpret the "name" as a structure name. This enables a
	 * common interface for all StructureLoader classes, yet does not
	 * prevent a specific implementation from implementing additional
	 * methods. Also, since each StructureLoader sub-class must
	 * implement the "canLoad" method, an application can always
	 * determine if a given loader is capable of delivering a specific
	 * structure or not.
	 */
	public Structure load( final String name) throws IOException
	{
		structure = null;
		
		try
		{	
			final File file = new File( name );
			structure = this.load( file);
		}
		catch( final NullPointerException e )
		{
			final URL url = new URL( name );
			structure = this.load( url );
		}
		
		if ( structure != null ) {
			this.urlString = name;
		}
		
		return structure;
	}


	/**
	 * Returns true if the loader is capable of loading the structure,
	 * or false otherwise. This enables higher-level code to be able
	 * to build a context sensitive menu of only the loaders that can
	 * load a given structure name.
	 */
	public boolean canLoad( final String name)
	{
		if (! (name.contains(".pdb")  || name.contains(".ent"))) {
			return false;
		}

		if ( name.startsWith( "file:" ) ||
			name.startsWith( "http:" ) ||
			name.startsWith( "ftp:" ) ) {
			return true;
		}

		return false;
	}

	//
	// FileStructureLoader interface methods
	//

	/**
	 * Returns a reference to a Structure read from the given File object.
	 */
	public Structure load( final File file ) throws IOException
	{
		if ( ! this.canLoad( file ) ) {
			return null;
		}
		if ( ! file.exists() ) {
			return null;
		}
		if ( ! file.canRead() ) {
			return null;
		}

		this.expectedInputBytes = file.length( );
		if ( this.expectedInputBytes <= 0 ) {
			return null;
		}

		final FileInputStream fileInputStream = new FileInputStream( file );
		if ( fileInputStream == null ) {
			return null;
		}
		
		this.urlString = file.toURL().toExternalForm();
		InputStreamReader ir = null;
		if ( this.urlString.endsWith( ".gz" ) )
		{
			final GZIPInputStream gzipInputStream =
				new GZIPInputStream( fileInputStream );
			ir = new InputStreamReader( gzipInputStream );

			// JLM DEBUG: crude hack for progress, because with
			// a gzip stream we can't tell how much data there will be!
			// A 4:1 compression is typical for PDB files.
			this.expectedInputBytes *= 4;
		}
		else
			ir = new InputStreamReader(fileInputStream);
			
		return this.load( new BufferedReader(ir) );
	}


	/**
	 * Returns true if the Structure can be read from the given File object.
	 */
	public boolean canLoad( final File file )
	{
		if ( ! file.exists() ) {
			return false;
		}

		try
		{
			// String path = file.getCanonicalPath( );
			final String path = file.toURL().toExternalForm();
			return this.canLoad( path );
		}
		catch( final IOException e )
		{
			return false;
		}
	}


	/**
	 * Returns a reference to a Structure read from the given URL object.
	 */
	public Structure load( final URL url ) throws IOException
	{
		if ( ! this.canLoad( url ) ) {
			return null;
		}

		URLConnection urlConnection = url.openConnection( );
        urlConnection.addRequestProperty("User-agent", "Mozilla/4.0 (compatible; MSIE 6.0;Windows NT 5.1; SV1)");
		this.expectedInputBytes = urlConnection.getContentLength( );
		final InputStream inputStream = urlConnection.getInputStream( );

		InputStreamReader ir = null;
		
		if ( inputStream != null )
			this.urlString = url.toExternalForm( );

		if ( this.urlString.endsWith( ".gz" ) )
		{
			final GZIPInputStream gzipInputStream =
				new GZIPInputStream( inputStream );
			ir =
				new InputStreamReader( gzipInputStream );
		}
		else
			ir = new InputStreamReader(inputStream);

		return this.load( new BufferedReader(ir) );
	}


	/**
	 * Returns true if the Structure can be read from the given URL object.
	 */
	public boolean canLoad( final URL url )
	{
		return this.canLoad( url.toExternalForm( ) );
	}

	//
	// PdbStructureLoader methods
	//

	/**
	 * Returns a reference to a Structure read from the given (uncompressed)
	 * InputStream.
	 */
	public Structure load( final BufferedReader rdr ) throws IOException
	{
		if ( rdr == null ) {
			return null;
		}

		this.passComponents = new Hashtable<ComponentType, Vector<StructureComponent>>( );
		final long expectedBytes = this.expectedInputBytes;

		String line;

		int percentDone = 0;

		Status.progress( percentDone, "Loading " + this.urlString );

		// Create a hash for atom numbers that we can use
		// later if we need to process CONECT records.
		// We'll free memory when we're done with it.
		Hashtable<Integer, Atom> atomNumberHash = new Hashtable<Integer, Atom>( );
		Vector<int[]> conectRecords = new Vector<int[]>( );
		
		int bytesRead = 0;
		int lines = 0;
		int modelCount = 0; // How many models have we seen?
		int currentEntityId = 0;
		Map<String, Integer> entityMap = new HashMap<String, Integer>();

		while ( (line = rdr.readLine()) != null )
		{

			bytesRead += line.length() + 1;
			percentDone = (int)((bytesRead * 100L)/ expectedBytes);
			Status.progress( percentDone, "Loading " + this.urlString );

			lines++;
			
			boolean isHetAtom = line.startsWith("HETATM");

			// Parse the line buffer
			if (line.startsWith("ATOM") || isHetAtom)
			{
				// PDB File Atom Record offsets as documented by
				// http://www.rcsb.org/pdb/docs/format/pdbguide2.2/part_62.html
				// 1 -  6   RecordName
				// 7 - 11   serial
				// 12       -
				// 13 - 16  name
				// 17       altLoc
				// 18 - 20  resName
				// 21       -
				// 22       chainID
				// 23 - 26  resSeq
				// 27       iCode
				// 28 - 30  -
				// 31 - 38  x
				// 39 - 46  y
				// 47 - 54  z
				// 55 - 60  occupancy
				// 61 - 66  tempFactor
				// 67 - 72  -
				// 73 - 76  segID
				// 77 - 78  element
				// 79 - 80  charge
				// NOTE: In this application, we need to subtract 1 from
				// each index in order to match the 0-based array offsets.
				
				// handle the case of a non-standard pdb file
				// without COMPOUND information
				if (entityNameMap.size() == 0) {
					entityNameMap.put(currentEntityId,"");
				}

				Atom atom = new Atom( );
				String str = null;

				atom.number =  Integer.parseInt(line.substring( 6, 11 ).trim());

				atom.name = line.substring(12, 16 ).trim().replace('*', '\'');	//**JB quick fix: the dictionary expects ' instead of *

				atom.element = line.substring(76, 78).trim();
				atom.element = atom.element.replaceAll( "[0-9]", "" );
				if ( (atom.element == null) || atom.element.equals("") ||
						(PeriodicTable.getElement( atom.element ) == null))
				{
					// The element field was not an element,
					// so, try the first letter of the name.
					atom.element = atom.name.substring( 0, 1 ).trim();
					if ( PeriodicTable.getElement( atom.element ) == null ) {
						throw new IllegalArgumentException( "no atom element symbol around line " + lines );
					}
				}

				atom.altLoc = line.substring(16, 17 ).trim();

				atom.compound = line.substring(17, 20 ).trim();

				atom.chain_id = line.substring(21, 22 ).trim();
				if (entityMap.size() > 0) {
					atom.entity_id = entityMap.get(atom.chain_id);
				} else {
					atom.entity_id = 1;
				}

				if(treatModelsAsSubunits)
				{
					atom.chain_id = atom.chain_id + "$$$" + modelCount;
				}
				
				atom.authorChain_id = atom.chain_id;
				
				// Split het atoms into separate chains so they can be displayed as ligands.
				// Note, ligands and water are included in the protein chain that is closed to the ligand or water.
				// However, for MBT these entities must be in a different chain for visualization purposes.
				// For display purposes the original "authorChain_id" is used.
				
				if (isHetAtom) {
					atom.chain_id += " ";
				}
				// Make sure waters don't have the same chain id as the macromolecules or ligands,
				// since they are expected to be in a separate chain, i.e. for ProteinWorkshop
				// Add a space to enforce uniqueness
				if (atom.compound.equals("HOH")) {
					atom.chain_id += " ";
				}
				
				String temp = line.substring(22, 26).trim();
				atom.residue_id = Integer.parseInt(temp);
				atom.authorResidue_id = atom.residue_id;
			
				atom.insertionCode = line.substring(26, 27).trim();
	
				atom.coordinate = new double[3];
				atom.coordinate[0] = Double.parseDouble(line.substring(30, 38 ).trim());
				atom.coordinate[1] = Double.parseDouble(line.substring(38, 46 ).trim());
				atom.coordinate[2] = Double.parseDouble(line.substring(46, 54 ).trim());

				str = line.substring(54, 60 ).trim();
				atom.occupancy = ( str.length() == 0 )? 1.0f : Float.parseFloat( str );

				str = line.substring(60, 66 ).trim();
				atom.bfactor = ( str.length() == 0 )? 0.0f : Float.parseFloat( str );

				Vector<StructureComponent> records = this.passComponents.get(ComponentType.ATOM);
				
				if ( records == null )
				{
					records = new Vector<StructureComponent>( );
					this.passComponents.put(
							ComponentType.ATOM, records );
				}
				
				records.add( atom );

				// Add atom to cache for conect record processing.
				atomNumberHash.put( new Integer( atom.number ), atom );

				continue;
			}

			//
			// MODEL record
			//
			else if (line.startsWith("MODEL"))
			{
				modelCount++; // How many models have we seen?

				if ( !this.shouldRecordMoreModels(modelCount) ) {
					break; // Only load 1st model
				}

				// Reset linePos to the start of the line buffer.
				continue;
				
			} 
			//
			// Compound record
			//
			else if (line.startsWith("COMPND")) {
				if (line.contains("MOL_ID:")) {
                   String id = line.substring(18);
                   id = id.replace(';', ' ');
                   id = id.trim();
                   currentEntityId = Integer.parseInt(id);
				} else if (line.contains("CHAIN:")) {
				   String chainId = line.substring(18);
				   chainId = chainId.replaceAll(";", "");
				   chainId = chainId.trim();
				   String[] ids = chainId.split(",");
				   for (String id: ids) {
					   entityMap.put(id.trim(), currentEntityId);
				   }
				} else if (line.contains("MOLECULE:")) {
					String entityName = line.substring(20).trim();
					entityName = entityName.replaceAll(";","");
					entityName = entityName.toLowerCase();
					entityNameMap.put(currentEntityId, entityName);
				}
			}

			if ( !this.shouldRecordMoreModels(modelCount) )
				break; // Only load 1st model
		}
		//
		// Post-process cached CONECT records to produce Bond objects.
		//
		final int conectCount = conectRecords.size( );
		if ( conectCount > 0 )
		{
			final Vector<StructureComponent> bonds = new Vector<StructureComponent>( );
			this.passComponents.put( ComponentType.BOND, bonds );

			for ( int i=0; i<conectCount; i++ )
			{
				final int conect[] = (int[]) conectRecords.elementAt( i );
				final Atom atom0 = atomNumberHash.get( new Integer( conect[0] ) );
				for ( int j=1; j<conect.length; j++ )
				{
					if ( conect[j] < 0 ) {
						continue;
					}
					final Atom atom1 = (Atom) atomNumberHash.get( new Integer( conect[j] ) );
					bonds.add( new Bond( atom0, atom1 ) );
				}
			}
		}
		
		conectRecords.clear( );
		conectRecords = null;
		atomNumberHash.clear( );
		atomNumberHash = null;

		//
		// Create the Structure object
		//
		structure = new Structure()
		{
			// A hashtable of vectors where
			// each hash KEY is the StructureComponent type String.
			// each hash VALUE is a Vector of StructureComponent objects.
			protected Hashtable<ComponentType, Vector<StructureComponent>> structureComponents = null;

			// To free up the global state for another load call.
			private String localUrlString;

			// public Structure()  Anonymous inner class constructor.
			{
				this.structureComponents = passComponents;
				PdbStructureLoader.this.passComponents = null;

				this.localUrlString = PdbStructureLoader.this.urlString;
				PdbStructureLoader.this.urlString = null;

				if ( PdbStructureLoader.this.structureInfo != null ) {
					this.setStructureInfo( PdbStructureLoader.this.structureInfo );
				}
			}

			
			public String getUrlString( )
			{
				return this.localUrlString;
			}

			@Override
			public int getStructureComponentCount( ComponentType scType )
			{
				Vector<StructureComponent> records = this.structureComponents.get( scType );
				if ( records == null ) {
					return 0;
				} else {
					return records.size( );
				}
			}

			@Override
			public StructureComponent getStructureComponentByIndex( ComponentType type,
				int index )
				throws IndexOutOfBoundsException, IllegalArgumentException
			{
				Vector<StructureComponent> records = this.structureComponents.get( type );
				if ( records == null ) {
					throw new IllegalArgumentException( "no records of type " + type );
				}

				StructureComponent structureComponent =
					(StructureComponent) records.elementAt( index );
				structureComponent.setStructure( this );

				return structureComponent;
            }
		};

		// Progress is done.
		Status.progress( 100, null );

		return structure;
	}

	private boolean shouldRecordMoreModels(final int modelCount)
	{
		return treatModelsAsSubunits || modelCount < 2;
	}
	
	/**
	 * get the completed structure.
	 * @return
	 */
    public Structure getStructure() { return structure; }

	public boolean hasUnitCell() {
		return false;
	};
	
	/**
	 * get the unit cell for biological units
	 * @return
	 */
	public UnitCell getUnitCell() { return null; }


	public ModelTransformationList getBiologicalUnitTransformationMatrices() {
		return null;
	}


	public ModelTransformationList getNonCrystallographicOperations() {
		return null;
	}


	public boolean hasBiologicUnitTransformationMatrices() {
		return false;
	}


	public boolean hasNonCrystallographicOperations() {
		return false;
	}

	public Map<Integer, String> getEntityNameMap() {
		return entityNameMap;
	}

}

