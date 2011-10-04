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

package edu.cornell.med.icb.goby.methylation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Defines a genomic region that encompasses several genomic sites
 * @author Nyasha Chambwe
 * Date: 10/3/11
 * Time: 2:52 PM
 */
public class MethylationRegion {

 public int chromosome;

    public final int startPosition;
    public final int endPosition;
    public char strand;
    public ObjectArrayList<MethylationSite> sitesInRegion;


    public MethylationRegion(int chromosome, int startPosition, int endPosition) {
        this.chromosome = chromosome;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }


    public MethylationRegion(int chromosome, int startPosition, int endPosition, char strand) {
        this.chromosome = chromosome;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.strand = strand;
    }

    public MethylationRegion(int chromosome, int startPosition, int endPosition, char strand, ObjectArrayList<MethylationSite> sitesInRegion) {
        this.chromosome = chromosome;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.strand = strand;
        this.sitesInRegion = sitesInRegion;
    }

    
}
    




