/*
 * Copyright (C) 2009-2012 Institute for Computational Biomedicine,
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

package edu.cornell.med.icb.goby.stats;

import edu.cornell.med.icb.goby.algorithmic.data.GroupComparison;
import edu.cornell.med.icb.goby.reads.RandomAccessSequenceTestSupport;
import org.junit.Test;

import java.io.StringWriter;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author Nyasha Chambwe
 *         Date: 1/25/12
 *         Time: 12:06 PM
 */
public class TestVCFAveragingWriter {


    String[] sequences = {
            "GTTACGCGATGATTTAAGAAT",
            "TTAATCGCGTAATCGCGATAAT",
            "TAATCGCGTAACGCGATACGATTCGCGACGT",
            "TTAATCTCTTAATCACATAAT",
    };

    String[] sequenceNames = {"Case1", "Case2", "Case3", "Case4"};

    RandomAccessSequenceTestSupport genome = new RandomAccessSequenceTestSupport(sequences) {
        @Override
        public int getReferenceIndex(String referenceId) {

            if (referenceId.equals("Case1")) {
                return 0;
            }
            if (referenceId.equals("Case2")) {
                return 1;
            }
            if (referenceId.equals("Case3")) {
                return 2;
            }
            if (referenceId.equals("Case4")) {
                return 3;
            }

            return -1;
        }

        @Override
        public String getReferenceName(int index) {
            return sequenceNames[index];
        }
    };


    private MethylCountProvider testSupport;


    @Test
    public void testCase1() {
        String[] samples = new String[]{"sample1"};
        int[] positions = new int[]{5, 7};
        int[][] C = {{5, 3}};
        int[][] Cm = {{9, 8}};
        testSupport = new MethylCountProviderTestSupport(samples, positions, "Case1", C, Cm);
        final StringWriter stringWriter = new StringWriter();
        VCFAveragingWriter testWriter = new VCFAveragingWriter(stringWriter, genome, testSupport);
        testWriter.setAnnotationFilename("test-data/vcf-averaging/annotations-1.tsv");
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.close();
        assertEquals("Test Case 1 result: ", "Chromosome\tStart\tEnd\tFeature\tMR[sample1][CpG]\tMR[sample1][CpA]\t" +
                "MR[sample1][CpC]\tMR[sample1][CpT]\tMR[sample1][CpN]\n" +
                "Case1\t4\t8\tannotation0\t68.0000\tNaN\tNaN\tNaN\tNaN\n", stringWriter.getBuffer().toString());
    }

    @Test
    public void testCase2() {
        String[] samples = new String[]{"sample1"};
        int[] positions = new int[]{6, 8, 14, 16};
        int[][] C = {{5, 3, 9, 8}};
        int[][] Cm = {{9, 7, 1, 5}};

        testSupport = new MethylCountProviderTestSupport(samples, positions, "Case2", C, Cm);
        final StringWriter stringWriter = new StringWriter();
        VCFAveragingWriter testWriter = new VCFAveragingWriter(stringWriter, genome, testSupport);
        testWriter.setAnnotationFilename("test-data/vcf-averaging/annotations-1.tsv");
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.close();
        assertEquals("Test Case 2 result: ",
                "Chromosome\tStart\tEnd\tFeature\tMR[sample1][CpG]\tMR[sample1][CpA]\t" +
                        "MR[sample1][CpC]\tMR[sample1][CpT]\tMR[sample1][CpN]\n" +
                        "Case2\t5\t9\tannotation1\t66.6667\tNaN\tNaN\tNaN\tNaN\n" +
                        "Case2\t13\t17\tannotation2\t26.0870\tNaN\tNaN\tNaN\tNaN\n", stringWriter.getBuffer().toString());
    }

    @Test
    public void testCase3() {
        String[] samples = new String[]{"sample1"};
        int[] positions = new int[]{5, 7, 12, 14, 19, 24, 26, 29};
        int[][] C = {{5, 3, 9, 8, 9, 9, 8, 6}};
        int[][] Cm = {{9, 7, 1, 5, 3, 7, 8, 3}};
        testSupport = new MethylCountProviderTestSupport(samples, positions, "Case3", C, Cm);
        final StringWriter stringWriter = new StringWriter();
        VCFAveragingWriter testWriter = new VCFAveragingWriter(stringWriter, genome, testSupport);
        testWriter.setAnnotationFilename("test-data/vcf-averaging/annotations-1.tsv");
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.close();
        assertEquals("Test Case 3 result: ", "Chromosome\tStart\tEnd\tFeature\tMR[sample1][CpG]\tMR[sample1][CpA]\t" +
                "MR[sample1][CpC]\tMR[sample1][CpT]\tMR[sample1][CpN]\n" +
                "Case3\t4\t21\tannotation4\t52.7778\tNaN\tNaN\tNaN\tNaN\n" +
                "Case3\t11\t26\tannotation6\t35.8209\tNaN\tNaN\tNaN\tNaN\n" +
                "Case3\t4\t30\tannotation3\t43.0000\tNaN\tNaN\tNaN\tNaN\n" +
                "Case3\t11\t30\tannotation5\t28.1250\tNaN\tNaN\tNaN\tNaN\n", stringWriter.getBuffer().toString());
    }

    @Test
    public void testCase4() {
        String[] samples = new String[]{"sample1", "sample2", "sample3"};
        int[] positions = new int[]{6, 8, 14, 16};
        int[][] C = {{5, 3, 9, 8}, {4, 6, 3, 2}, {8, 3, 8, 9}};
        int[][] Cm = {{9, 7, 1, 5}, {9, 7, 9, 3}, {2, 3, 2, 8}};
        testSupport = new MethylCountProviderTestSupport(samples, positions, "Case2", C, Cm);
        final StringWriter stringWriter = new StringWriter();
        VCFAveragingWriter testWriter = new VCFAveragingWriter(stringWriter, genome, testSupport);
        testWriter.setAnnotationFilename("test-data/vcf-averaging/annotations-1.tsv");
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.close();
        assertEquals("Test Case 4 result: ",
                "Chromosome\tStart\tEnd\tFeature\tMR[sample1][CpG]\tMR[sample2][CpG]\tMR[sample3][CpG]\tMR[sample1][CpA]\tMR[sample2][CpA]\tMR[sample3][CpA]\tMR[sample1][CpC]\tMR[sample2][CpC]\tMR[sample3][CpC]\tMR[sample1][CpT]\tMR[sample2][CpT]\tMR[sample3][CpT]\tMR[sample1][CpN]\tMR[sample2][CpN]\tMR[sample3][CpN]\n" +
                        "Case2\t5\t9\tannotation1\t66.6667\t61.5385\t31.2500\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\n" +
                        "Case2\t13\t17\tannotation2\t26.0870\t70.5882\t37.0370\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\n", stringWriter.getBuffer().toString());
    }

    @Test
    public void testCase5() {
        String[] groups = new String[]{"group1"};
        String[] samples = new String[]{"sample1", "sample2", "sample3"};
        int[] positions = new int[]{6, 8, 14, 16};
        int[][] C = {{5, 3, 9, 8}, {4, 6, 3, 2}, {8, 3, 8, 9}};
        int[][] Cm = {{9, 7, 1, 5}, {9, 7, 9, 3}, {2, 3, 2, 8}};
        testSupport = new MethylCountProviderTestSupport(groups, samples, positions, "Case2", C, Cm);
        final StringWriter stringWriter = new StringWriter();
        VCFAveragingWriter testWriter = new VCFAveragingWriter(stringWriter, genome, testSupport);
        testWriter.setAnnotationFilename("test-data/vcf-averaging/annotations-1.tsv");
        int[] a = {0, 0, 0};
        testWriter.setSampleIndexToGroupIndex(a);
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.close();
        assertEquals("Test Case 5 result: ",
                "Chromosome\tStart\tEnd\tFeature\tMR[sample1][CpG]\tMR[sample2][CpG]\tMR[sample3][CpG]\tMR[group1][CpG]\tMR[sample1][CpA]\tMR[sample2][CpA]\tMR[sample3][CpA]\tMR[group1][CpA]\tMR[sample1][CpC]\tMR[sample2][CpC]\tMR[sample3][CpC]\tMR[group1][CpC]\tMR[sample1][CpT]\tMR[sample2][CpT]\tMR[sample3][CpT]\tMR[group1][CpT]\tMR[sample1][CpN]\tMR[sample2][CpN]\tMR[sample3][CpN]\tMR[group1][CpN]\n" +
                        "Case2\t5\t9\tannotation1\t66.6667\t61.5385\t31.2500\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t56.0606\tNaN\tNaN\tNaN\tNaN\n" +
                        "Case2\t13\t17\tannotation2\t26.0870\t70.5882\t37.0370\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t41.7910\tNaN\tNaN\tNaN\tNaN\n", stringWriter.getBuffer().toString());

    }

    @Test
    public void testCase6() {
        String[] groups = new String[]{"group1", "group2"};
        String[] samples = new String[]{"sample1", "sample2", "sample3", "sample4", "sample5", "sample6"};
        int[] positions = new int[]{5, 7, 12, 14, 19, 24, 26, 29};
        int[][] C = {{5, 3, 4, 8, 5, 2, 8, 7}, {5, 3, 9, 8, 9, 8, 4, 9}, {5, 3, 9, 8, 7, 9, 8, 0}, {5, 3, 9, 8, 9, 9, 8, 0},
                {5, 3, 9, 8, 2, 9, 8, 0}, {0, 3, 5, 8, 8, 7, 8, 0}};
        int[][] Cm = {{9, 7, 1, 5, 3, 7, 0, 3}, {9, 3, 1, 7, 4, 7, 0, 3}, {9, 6, 1, 2, 8, 7, 8, 3}, {9, 1, 1, 9, 2, 7, 8, 3},
                {0, 7, 1, 8, 8, 7, 8, 3}, {8, 7, 1, 5, 3, 7, 8, 3}};
        testSupport = new MethylCountProviderTestSupport(groups, samples, positions, "Case3", C, Cm);

        final StringWriter stringWriter = new StringWriter();
        VCFAveragingWriter testWriter = new VCFAveragingWriter(stringWriter, genome, testSupport);
        testWriter.setAnnotationFilename("test-data/vcf-averaging/annotations-1.tsv");
        int[] a = {0, 0, 0, 1, 1, 1};
        testWriter.setSampleIndexToGroupIndex(a);

        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.close();
        assertEquals("Test Case 6 result: ", "Chromosome\tStart\tEnd\tFeature\tMR[sample1][CpG]\tMR[sample2][CpG]\tMR[sample3][CpG]\tMR[sample4][CpG]\tMR[sample5][CpG]\tMR[sample6][CpG]\tMR[group1][CpG]\tMR[group2][CpG]\tMR[sample1][CpA]\tMR[sample2][CpA]\tMR[sample3][CpA]\tMR[sample4][CpA]\tMR[sample5][CpA]\tMR[sample6][CpA]\tMR[group1][CpA]\tMR[group2][CpA]\tMR[sample1][CpC]\tMR[sample2][CpC]\tMR[sample3][CpC]\tMR[sample4][CpC]\tMR[sample5][CpC]\tMR[sample6][CpC]\tMR[group1][CpC]\tMR[group2][CpC]\tMR[sample1][CpT]\tMR[sample2][CpT]\tMR[sample3][CpT]\tMR[sample4][CpT]\tMR[sample5][CpT]\tMR[sample6][CpT]\tMR[group1][CpT]\tMR[group2][CpT]\tMR[sample1][CpN]\tMR[sample2][CpN]\tMR[sample3][CpN]\tMR[sample4][CpN]\tMR[sample5][CpN]\tMR[sample6][CpN]\tMR[group1][CpN]\tMR[group2][CpN]\n" +
                "Case3\t4\t21\tannotation4\t59.3750\t48.4848\t60.5263\t41.3793\t60.0000\t62.0690\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t56.3107\t54.2169\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\n" +
                "Case3\t11\t26\tannotation6\t37.2093\t33.3333\t38.8060\t38.5714\t47.0588\t40.0000\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t36.5269\t41.9192\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\n" +
                "Case3\t4\t30\tannotation3\t45.4545\t38.2022\t47.3118\t43.9560\t48.8372\t51.8519\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t43.6293\t48.0620\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\n" +
                "Case3\t11\t30\tannotation5\t32.1429\t29.7297\t26.0870\t43.3333\t41.3793\t40.9091\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t29.5455\t41.9753\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\n", stringWriter.getBuffer().toString());

    }


    @Test
    public void testCase7() {
        String[] groups = new String[]{"group1"};
        String[] samples = new String[]{"sample1", "sample2", "sample3"};
        int[] positions = new int[]{6, 8, 14, 16};
        int[][] C = {{5, 3, 9, 8}, {4, 6, 3, 2}, {8, 3, 8, 9}};
        int[][] Cm = {{9, 7, 1, 5}, {9, 7, 9, 3}, {2, 3, 2, 8}};
        testSupport = new MethylCountProviderTestSupport(groups, samples, positions, "Case4", C, Cm);
        final StringWriter stringWriter = new StringWriter();
        VCFAveragingWriter testWriter = new VCFAveragingWriter(stringWriter, genome, testSupport);
        testWriter.setAnnotationFilename("test-data/vcf-averaging/annotations-1.tsv");
        int[] a = {0, 0, 0};
        testWriter.setSampleIndexToGroupIndex(a);
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.close();
        assertEquals("Test Case 7 result: ",
                "Chromosome\tStart\tEnd\tFeature\tMR[sample1][CpG]\tMR[sample2][CpG]\tMR[sample3][CpG]\tMR[group1][CpG]\tMR[sample1][CpA]\tMR[sample2][CpA]\tMR[sample3][CpA]\tMR[group1][CpA]\tMR[sample1][CpC]\tMR[sample2][CpC]\tMR[sample3][CpC]\tMR[group1][CpC]\tMR[sample1][CpT]\tMR[sample2][CpT]\tMR[sample3][CpT]\tMR[group1][CpT]\tMR[sample1][CpN]\tMR[sample2][CpN]\tMR[sample3][CpN]\tMR[group1][CpN]\n" +
                        "Case4\t5\t9\tannotation7\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t66.6667\t61.5385\t31.2500\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t56.0606\tNaN\n" +
                        "Case4\t13\t17\tannotation8\tNaN\tNaN\tNaN\t26.0870\t70.5882\t37.0370\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t41.7910\tNaN\tNaN\tNaN\n", stringWriter.getBuffer().toString());

    }

    @Test
    public void testCase8() {
        // Test multiple samples, multiple groups, one comparison
        String[] groups = new String[]{"group1", "group2"};
        String[] samples = new String[]{"sample1", "sample2", "sample3", "sample4"};
        int[] positions = new int[]{6, 8, 14, 16};
        int[][] C = {{5, 3, 9, 8}, {4, 6, 3, 2}, {8, 3, 8, 9}, {7, 4, 9, 7}};
        int[][] Cm = {{9, 7, 1, 5}, {9, 7, 9, 3}, {2, 3, 2, 8}, {4, 1, 3, 6}};
        testSupport = new MethylCountProviderTestSupport(groups, samples, positions, "Case4", C, Cm);
        final StringWriter stringWriter = new StringWriter();
        VCFAveragingWriter testWriter = new VCFAveragingWriter(stringWriter, genome, testSupport);
        testWriter.setAnnotationFilename("test-data/vcf-averaging/annotations-1.tsv");
        int[] a = {0, 0, 1, 1};
        testWriter.setSampleIndexToGroupIndex(a);
        ArrayList<GroupComparison> groupComparisons = new ArrayList<GroupComparison>();
        GroupComparison case8 = new GroupComparison("group1", "group2", 0, 1, 0);
        groupComparisons.add(case8);
        testWriter.setGroupComparisons(groupComparisons);
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.close();
        assertEquals("Test Case 8 result: ",
                "Chromosome\tStart\tEnd\tFeature\tMR[sample1][CpG]\tMR[sample2][CpG]\tMR[sample3][CpG]\tMR[sample4][CpG]\tMR[group1][CpG]\tMR[group2][CpG]\tMR[sample1][CpA]\tMR[sample2][CpA]\tMR[sample3][CpA]\tMR[sample4][CpA]\tMR[group1][CpA]\tMR[group2][CpA]\tMR[sample1][CpC]\tMR[sample2][CpC]\tMR[sample3][CpC]\tMR[sample4][CpC]\tMR[group1][CpC]\tMR[group2][CpC]\tMR[sample1][CpT]\tMR[sample2][CpT]\tMR[sample3][CpT]\tMR[sample4][CpT]\tMR[group1][CpT]\tMR[group2][CpT]\tMR[sample1][CpN]\tMR[sample2][CpN]\tMR[sample3][CpN]\tMR[sample4][CpN]\tMR[group1][CpN]\tMR[group2][CpN]\tfisherP[group1/group2][CpG]\tfisherP[group1/group2][CpA]\tfisherP[group1/group2][CpC]\tfisherP[group1/group2][CpT]\tfisherP[group1/group2][CpN]\n" +
                        "Case4\t5\t9\tannotation7\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t66.6667\t61.5385\t31.2500\t31.2500\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t64.0000\t31.2500\tNaN\tNaN\t1.00000\t1.00000\t1.00000\t0.00622665\t1.00000\n" +
                        "Case4\t13\t17\tannotation8\tNaN\tNaN\tNaN\tNaN\t26.0870\t70.5882\t37.0370\t36.0000\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t45.0000\t36.5385\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t1.00000\t0.520511\t1.00000\t1.00000\t1.00000\n", stringWriter.getBuffer().toString());
    }

     @Test
    public void testCase9() {
     // test multiple samples, 4 groups, 3 comparisons
       // Test multiple samples, multiple groups, one comparison
        String[] groups = new String[]{"group1", "group2", "group3", "group4"};
        String[] samples = new String[]{"sample1", "sample2", "sample3", "sample4"};
        int[] positions = new int[]{6, 8, 14, 16};
        int[][] C = {{5, 3, 9, 8}, {4, 6, 3, 2}, {8, 3, 8, 9}, {7, 4, 9, 7}};
        int[][] Cm = {{9, 7, 1, 5}, {9, 7, 9, 3}, {2, 3, 2, 8}, {4, 1, 3, 6}};
        testSupport = new MethylCountProviderTestSupport(groups, samples, positions, "Case4", C, Cm);
        final StringWriter stringWriter = new StringWriter();
        VCFAveragingWriter testWriter = new VCFAveragingWriter(stringWriter, genome, testSupport);
        testWriter.setAnnotationFilename("test-data/vcf-averaging/annotations-1.tsv");
        int[] a = {0, 1, 2, 3};
        testWriter.setSampleIndexToGroupIndex(a);
        ArrayList<GroupComparison> groupComparisons = new ArrayList<GroupComparison>();
        GroupComparison comparisonToMake = new GroupComparison("group1", "group2", 0, 1, 0);
        groupComparisons.add(comparisonToMake);
        comparisonToMake= new GroupComparison("group1", "group3", 0, 2, 1);
        groupComparisons.add(comparisonToMake);
        comparisonToMake= new GroupComparison("group1", "group4", 0, 3, 2);
        groupComparisons.add(comparisonToMake);
        testWriter.setGroupComparisons(groupComparisons);
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.writeRecord();
        testWriter.close();
        assertEquals("Test Case 9 result: ",
                "Chromosome\tStart\tEnd\tFeature\tMR[sample1][CpG]\tMR[sample2][CpG]\tMR[sample3][CpG]\tMR[sample4][CpG]\tMR[group1][CpG]\tMR[group2][CpG]\tMR[group3][CpG]\tMR[group4][CpG]\tMR[sample1][CpA]\tMR[sample2][CpA]\tMR[sample3][CpA]\tMR[sample4][CpA]\tMR[group1][CpA]\tMR[group2][CpA]\tMR[group3][CpA]\tMR[group4][CpA]\tMR[sample1][CpC]\tMR[sample2][CpC]\tMR[sample3][CpC]\tMR[sample4][CpC]\tMR[group1][CpC]\tMR[group2][CpC]\tMR[group3][CpC]\tMR[group4][CpC]\tMR[sample1][CpT]\tMR[sample2][CpT]\tMR[sample3][CpT]\tMR[sample4][CpT]\tMR[group1][CpT]\tMR[group2][CpT]\tMR[group3][CpT]\tMR[group4][CpT]\tMR[sample1][CpN]\tMR[sample2][CpN]\tMR[sample3][CpN]\tMR[sample4][CpN]\tMR[group1][CpN]\tMR[group2][CpN]\tMR[group3][CpN]\tMR[group4][CpN]\tfisherP[group1/group2][CpG]\tfisherP[group1/group3][CpG]\tfisherP[group1/group4][CpG]\tfisherP[group1/group2][CpA]\tfisherP[group1/group3][CpA]\tfisherP[group1/group4][CpA]\tfisherP[group1/group2][CpC]\tfisherP[group1/group3][CpC]\tfisherP[group1/group4][CpC]\tfisherP[group1/group2][CpT]\tfisherP[group1/group3][CpT]\tfisherP[group1/group4][CpT]\tfisherP[group1/group2][CpN]\tfisherP[group1/group3][CpN]\tfisherP[group1/group4][CpN]\n" +
                        "Case4\t5\t9\tannotation7\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t66.6667\t61.5385\t31.2500\t31.2500\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t66.6667\t61.5385\t31.2500\t31.2500\tNaN\tNaN\tNaN\tNaN\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\t0.773708\t0.0514794\t0.0514794\t1.00000\t1.00000\t1.00000\n" +
                        "Case4\t13\t17\tannotation8\tNaN\tNaN\tNaN\tNaN\t26.0870\t70.5882\t37.0370\t36.0000\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t26.0870\t70.5882\t37.0370\t36.0000\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\t1.00000\t1.00000\t1.00000\t0.00952120\t0.545556\t0.541875\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\t1.00000\n", stringWriter.getBuffer().toString());

     }
}
