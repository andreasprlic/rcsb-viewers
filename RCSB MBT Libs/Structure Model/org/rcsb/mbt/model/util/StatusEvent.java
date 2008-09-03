//  $Id: StatusEvent.java,v 1.1 2007/02/08 02:38:52 jbeaver Exp $
//
//  Copyright 2000-2004 The Regents of the University of California.
//  All Rights Reserved.
//
//  Permission to use, copy, modify and distribute any part of this
//  Molecular Biology Toolkit (MBT)
//  for educational, research and non-profit purposes, without fee, and without
//  a written agreement is hereby granted, provided that the above copyright
//  notice, this paragraph and the following three paragraphs appear in all
//  copies.
//
//  Those desiring to incorporate this MBT into commercial products
//  or use for commercial purposes should contact the Technology Transfer &
//  Intellectual Property Services, University of California, San Diego, 9500
//  Gilman Drive, Mail Code 0910, La Jolla, CA 92093-0910, Ph: (858) 534-5815,
//  FAX: (858) 534-7345, E-MAIL:invent@ucsd.edu.
//
//  IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
//  DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING
//  LOST PROFITS, ARISING OUT OF THE USE OF THIS MBT, EVEN IF THE
//  UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//  THE MBT PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE
//  UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
//  UPDATES, ENHANCEMENTS, OR MODIFICATIONS. THE UNIVERSITY OF CALIFORNIA MAKES
//  NO REPRESENTATIONS AND EXTENDS NO WARRANTIES OF ANY KIND, EITHER IMPLIED OR
//  EXPRESS, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE, OR THAT THE USE OF THE
//  MBT WILL NOT INFRINGE ANY PATENT, TRADEMARK OR OTHER RIGHTS.
//
//  For further information, please see:  http://mbt.sdsc.edu
//
//  History:
//  $Log: StatusEvent.java,v $
//  Revision 1.1  2007/02/08 02:38:52  jbeaver
//  version 1.50
//
//  Revision 1.1  2006/09/20 16:50:43  jbeaver
//  first commit - branched from ProteinWorkshop
//
//  Revision 1.1  2006/08/24 17:39:03  jbeaver
//  *** empty log message ***
//
//  Revision 1.1  2006/03/09 00:18:55  jbeaver
//  Initial commit
//
//  Revision 1.5  2004/04/09 00:15:21  moreland
//  Updated copyright to new UCSD wording.
//
//  Revision 1.4  2004/01/29 17:29:07  moreland
//  Updated copyright and class block comments.
//
//  Revision 1.3  2003/04/03 22:40:33  moreland
//  Added "progress" handling support.
//
//  Revision 1.2  2003/02/27 21:23:05  moreland
//  Corrected javadoc "see" reference paths.
//
//  Revision 1.1  2003/01/31 21:15:50  moreland
//  Added classes to provide a toolkit-wide static status message output mechanism.
//
//  Revision 1.0  2002/10/24 17:54:01  moreland
//  First revision.
//


package org.rcsb.mbt.model.util;


/**
 *  A status message container used by the Status class to propagate
 *  toolkit-wide status messages to any interested listeners.
 *  <P>
 *  @author	John L. Moreland
 *  @see	org.rcsb.mbt.model.util.Status
 *  @see	org.rcsb.mbt.model.util.StatusListener
 */
public class StatusEvent
{
	/**
	 * An output message type of event.
	 */
	public static final int TYPE_OUTPUT = 0;

	/**
	 * A progress type of event.
	 */
	public static final int TYPE_PROGRESS = 1;

	/**
	 * The message type.
	 */
	public int type = StatusEvent.TYPE_OUTPUT;

	/**
	 * The message level.
	 */
	public int level = 0;

	/**
	 * The message text.
	 */
	public String message = null;

	/**
	 * The progress percentage (0.0-1.0).
	 */
	public int percent = 0;
}

