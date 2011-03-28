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

package edu.cornell.med.icb.goby.readers;

import edu.cornell.med.icb.goby.readers.vcf.ColumnInfo;
import edu.cornell.med.icb.goby.readers.vcf.Columns;
import edu.cornell.med.icb.goby.readers.vcf.VCFParser;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Test the VCF parser
 * @author Fabien Campagne
 *         Date: Mar 26, 2011
 *         Time: 3:15:13 PM
 */
public class TestVCFParser {


    @Test
    public void testParse1() throws FileNotFoundException, VCFParser.SyntaxException {
        VCFParser parser = new VCFParser(new FileReader("test-data/vcf/example1.vcf"));
        parser.readHeader();
        Columns cols = parser.getColumns();
        ColumnInfo info = cols.find("INFO");
        assertTrue(info.hasField("DP"));
        assertTrue(info.hasField("MQ"));
        assertTrue(info.hasField("FQ"));
        ColumnInfo format = cols.find("FORMAT");
        assertTrue(format.hasField("GT"));
        assertTrue(format.hasField("GQ"));
        assertTrue(format.hasField("GL"));
        assertTrue(format.hasField("SP"));
        assertEquals("Likelihoods for RR,RA,AA genotypes (R=ref,A=alt)",
                format.getField("GL").description);
    }

    @Test
    public void testParse1WithGroups() throws FileNotFoundException, VCFParser.SyntaxException {
        VCFParser parser = new VCFParser(new FileReader("test-data/vcf/example-with-groups.vcf"));
        parser.readHeader();
        Columns cols = parser.getColumns();
        ColumnInfo info = cols.find("INFO");
        assertTrue(info.hasField("DP"));
        assertTrue(info.getField("DP").group.equals("DEPTHS"));
        assertTrue(info.hasField("MQ"));
        assertTrue(info.getField("MQ").group.equals("RMS"));
    }
    @Test
    public void testParseExample2() throws FileNotFoundException, VCFParser.SyntaxException {
        VCFParser parser = new VCFParser(new FileReader("test-data/vcf/example2.tsv"));
        parser.readHeader();
        Columns cols = parser.getColumns();

        assertNotNull("Fixed CHROM column must exist", cols.find("CHROM"));
        assertNotNull("Fixed POS column must exist", cols.find("POS"));
        assertNotNull("Fixed ID column must exist", cols.find("ID"));
        assertNotNull("Fixed ALT column must exist", cols.find("ALT"));
        assertNotNull("Fixed QUAL column must exist", cols.find("QUAL"));
        assertNotNull("Fixed QUAL column must exist", cols.find("INFO"));
        assertNotNull("optional sample column 1 must exist", cols.find("results/IPBKRNW/IPBKRNW-replicate.bam"));
        assertNotNull("optional sample column w must exist", cols.find("results/IPBKRNW/IPBKRNW-sorted.bam"));


    }

    @Test
    public void testParseExample3() throws FileNotFoundException, VCFParser.SyntaxException {
        VCFParser parser = new VCFParser(new FileReader("test-data/vcf/example3.tsv"));
        parser.readHeader();
        Columns cols = parser.getColumns();

        assertNotNull("Fixed CHROM column must exist", cols.find("CHROM"));
        assertNotNull("Fixed POS column must exist", cols.find("POS"));
        assertNotNull("Fixed ID column must exist", cols.find("ID"));
        assertNotNull("Fixed ALT column must exist", cols.find("ALT"));
        assertNotNull("Fixed QUAL column must exist", cols.find("QUAL"));
        assertNotNull("Fixed QUAL column must exist", cols.find("INFO"));
        assertNotNull("optional sample column 1 must exist", cols.find("results/IPBKRNW/IPBKRNW-replicate.bam"));
        assertNotNull("optional sample column w must exist", cols.find("results/IPBKRNW/IPBKRNW-sorted.bam"));

    }

    @Test
    public void testParseValue2() throws FileNotFoundException, VCFParser.SyntaxException {
        VCFParser parser = new VCFParser(new FileReader("test-data/vcf/example2.tsv"));
        parser.readHeader();
        assertTrue(parser.hasNextDataLine());

        assertEquals("145497099", parser.getStringColumnValue(1));
        assertEquals(".", parser.getStringColumnValue(2));
        assertEquals("A", parser.getStringColumnValue(3));
        parser.next();
        assertTrue(parser.hasNextDataLine());
        assertEquals("29389393", parser.getStringColumnValue(1));
        assertEquals("1", parser.getStringColumnValue(0));
        assertEquals("AC", parser.getStringColumnValue(3));
    }

      @Test
    public void testParseValue3() throws FileNotFoundException, VCFParser.SyntaxException {
        VCFParser parser = new VCFParser(new FileReader("test-data/vcf/example3.tsv"));
        parser.readHeader();
        assertTrue(parser.hasNextDataLine());

        assertEquals("145497099", parser.getStringColumnValue(1));
        assertEquals(".", parser.getStringColumnValue(2));
        assertEquals("A", parser.getStringColumnValue(3));
        parser.next();
        assertTrue(parser.hasNextDataLine());
        assertEquals("29389393", parser.getStringColumnValue(1));
        assertEquals("1", parser.getStringColumnValue(0));
        assertEquals("AC", parser.getStringColumnValue(3));
    }
    public void testHasNext() throws FileNotFoundException, VCFParser.SyntaxException {
        VCFParser parser = new VCFParser(new FileReader("test-data/vcf/example2.tsv"));
        parser.readHeader();
        int lineCounter = 0;
        while (parser.hasNextDataLine()) {
            lineCounter++;
            parser.next();

        }
        assertEquals(3177, lineCounter);


    }

    @Test
    public void testParseFixedColumns() throws FileNotFoundException, VCFParser.SyntaxException {
        VCFParser parser = new VCFParser(new FileReader("test-data/vcf/example1.vcf"));
        parser.readHeader();

        Columns cols = parser.getColumns();
        assertNotNull("Fixed CHROM column must exist", cols.find("CHROM"));
        assertNotNull("Fixed POS column must exist", cols.find("POS"));
        assertNotNull("Fixed ID column must exist", cols.find("ID"));
        assertNotNull("Fixed ALT column must exist", cols.find("ALT"));
        assertNotNull("Fixed QUAL column must exist", cols.find("QUAL"));
        assertNotNull("Fixed QUAL column must exist", cols.find("INFO"));
        assertNotNull("optional sample column 1 must exist", cols.find("results/IPBKRNW/IPBKRNW-replicate.bam"));
        assertNotNull("optional sample column w must exist", cols.find("results/IPBKRNW/IPBKRNW-sorted.bam"));
    }

    @Test
    public void testColumnOrder() throws FileNotFoundException, VCFParser.SyntaxException {
        VCFParser parser = new VCFParser(new FileReader("test-data/vcf/example1.vcf"));
        parser.readHeader();

        Columns cols = parser.getColumns();
        assertEquals(0, cols.find("CHROM").columnIndex);
        assertEquals(1, cols.find("POS").columnIndex);
        assertEquals(2, cols.find("ID").columnIndex);
        assertEquals(3, cols.find("REF").columnIndex);
        assertEquals(4, cols.find("ALT").columnIndex);
        assertEquals(5, cols.find("QUAL").columnIndex);
        assertEquals(6, cols.find("FILTER").columnIndex);
        assertEquals(7, cols.find("INFO").columnIndex);
        assertEquals(8, cols.find("FORMAT").columnIndex);
        assertEquals(9, cols.find("results/IPBKRNW/IPBKRNW-replicate.bam").columnIndex);
        assertEquals(10, cols.find("results/IPBKRNW/IPBKRNW-sorted.bam").columnIndex);
    }
    /*
##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
##FORMAT=<ID=GQ,Number=1,Type=Integer,Description="Genotype Quality">
##FORMAT=<ID=GL,Number=3,Type=Float,Description="Likelihoods for RR,RA,AA genotypes (R=ref,A=alt)">
##FORMAT=<ID=DP,Number=1,Type=Integer,Description="# high-quality bases">
##FORMAT=<ID=SP,Number=1,Type=Integer,Description="Phred-scaled strand bias P-value">

    */
    /* ##INFO=<ID=DP4,Number=4,Type=Integer,Description="# high-quality ref-forward bases, ref-reverse, alt-forward and alt-reverse bases">
##INFO=<ID=MQ,Number=1,Type=Integer,Description="Root-mean-square mapping quality of covering reads">
##INFO=<ID=FQ,Number=1,Type=Float,Description="Phred probability of all samples being the same">
##INFO=<ID=AF1,Number=1,Type=Float,Description="Max-likelihood estimate of the site allele frequency of the first ALT allele">

   */


}
