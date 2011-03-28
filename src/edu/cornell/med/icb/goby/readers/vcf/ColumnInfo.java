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

package edu.cornell.med.icb.goby.readers.vcf;

import java.util.Arrays;

/**
 * Meta-information about columns of a VCF file. This information is parsed from the VCF header.
 *
 * @author Fabien Campagne
 *         Date: Mar 26, 2011
 *         Time: 7:31:33 PM
 */
public class ColumnInfo {
    /**
     * Name of the column.
     */
    String columnName;
    /**
     * Set of fields for the column.
     */
    ColumnFields fields = new ColumnFields();
    /**
     * Index of this column in the file being parsed. Index is zero-based. Zero is the first (left-most) column.
     */
    public int columnIndex;

    /**
     * Create a ColumnInfo with provided information.
     *
     * @param columnName Name of the new column.
     * @param fields     Fields in this column.
     */
    public ColumnInfo(String columnName, ColumnField... fields) {
        this.columnName = columnName;
        this.fields.addAll(Arrays.asList(fields));
    }

    /**
     * Create a ColumnInfo with empty information.
     */
    public ColumnInfo() {

    }

    public void addField(ColumnField field) {
        fields.add(field);
    }

    public boolean hasField(String fieldName) {
        return fields.hasFieldName(fieldName);
    }

    public ColumnField getField(String fieldName) {
        return fields.find(fieldName);
    }
}
