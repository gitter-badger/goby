/*
 * Copyright (C) 2009-2011 Institute for Computational Biomedicine,
 *                    Weill Medical College of Cornell University
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.cornell.med.icb.goby.reads;

/**
 * An interface to obtain bases from a RandomAccessSequence cache implementation.
 * @author Fabien Campagne
 *         Date: May 1, 2011
 *         Time: 1:36:27 PM
 */
public interface RandomAccessSequenceInterface {
    /**
     * Return the base in the specified reference at the given position.
      * @param referenceIndex index of the reference sequence.
     * @param position zero-based position in the reference for which the base is sought.
     * @return base at position in reference sequence.
     */
    char get(final int referenceIndex, final int position);
}
