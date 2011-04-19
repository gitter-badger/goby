/*
 * Copyright (C) 2010 Institute for Computational Biomedicine,
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

package edu.cornell.med.icb.goby.alignments;

import com.google.protobuf.TextFormat;
import edu.cornell.med.icb.goby.modes.AlignMode;
import edu.cornell.med.icb.goby.reads.ReadsWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Fabien Campagne
 *         Date: Apr 30, 2009
 *         Time: 6:15:52 PM
 */
public class TestSequenceVariations {
    private static final Log LOG = LogFactory.getLog(TestSequenceVariations.class);
    private static final String BASE_TEST_DIR = "test-results/sequence-variations";
    private final String referenceFilename =
            FilenameUtils.concat(BASE_TEST_DIR, "reference-sequence-vars.compact-reads");
    private final String readsFilename =
            FilenameUtils.concat(BASE_TEST_DIR, "query-sequence-vars.compact-reads");
    private final String referenceFilename2 =
            FilenameUtils.concat(BASE_TEST_DIR, "reference-sequence-vars-bug.compact-reads");
    private final String readsFilename2 =
            FilenameUtils.concat(BASE_TEST_DIR, "query-sequence-vars-bug.compact-reads");
    private String lastagAlignmentFilename;
    private String bwaAlignmentFilename;
    private String bwaAlignmentFilename2;


    @BeforeClass
    public static void initializeTestDirectory() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating base test directory: " + BASE_TEST_DIR);
        }

        FileUtils.forceMkdir(new File(BASE_TEST_DIR));
    }

    @AfterClass
    public static void cleanupTestDirectory() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting base test directory: " + BASE_TEST_DIR);
        }
        // FileUtils.forceDeleteOnExit(new File(BASE_TEST_DIR));
    }

    @Test
    public void testLastagSequenceVariationParsing() throws IOException {
        final AlignmentReader reader = new AlignmentReaderImpl(lastagAlignmentFilename);
        while (reader.hasNext()) {
            final Alignments.AlignmentEntry alignmentEntry = reader.next();

            assertTrue("alignment must have variation", alignmentEntry.getSequenceVariationsCount() > 0);
            int variationIndex = 0;
            for (final Alignments.SequenceVariation var : alignmentEntry.getSequenceVariationsList()) {
                variationIndex++;
                System.out.println(String.format("last entry score=%f referenceIndex=%d  queryIndex=%d variation: %s",
                        alignmentEntry.getScore(),
                        alignmentEntry.getQueryIndex(),
                        alignmentEntry.getTargetIndex(),
                        var.toString()));
                assertLength(alignmentEntry, var, alignments);

                switch (alignmentEntry.getQueryIndex()) {
                    case 7:
                        //last finds this alignment. We test the case when the read does not match the reference at the beginning:
                        assertEquals(19, var.getPosition());
                        assertEquals(21, var.getReadIndex());
                        assertEquals("A", var.getFrom());
                        assertEquals("T", var.getTo());

                        break;
                    case 5:
                        //last finds this alignment. We test the case when the read does not match the reference at the beginning:
                        assertEquals(24, var.getPosition());
                        assertEquals(24, var.getReadIndex());
                        assertEquals("T", var.getFrom());
                        assertEquals("-", var.getTo());

                        break;

                    case 1:
                    case 4:
                        //last finds this alignment. We test deletion of TCC from the reference.
                        assertEquals(14, var.getPosition());
                        assertEquals(14, var.getReadIndex());
                        assertEquals("TCC", var.getFrom());
                        assertEquals("---", var.getTo());
                        break;
                    case 3:
                        //last finds this alignment. We test deletion of TCC from the reference.
                        assertEquals(8, var.getPosition());
                        assertEquals(9, var.getReadIndex());
                        assertEquals("-", var.getFrom());
                        assertEquals("C", var.getTo());

                        break;
                    case 0:
                        //last finds this alignment. We test deletion of TCC from the reference.
                        assertEquals(8, var.getPosition());
                        assertEquals(8, var.getReadIndex());
                        assertEquals("--", var.getFrom());
                        assertEquals("CC", var.getTo());
                        break;
                    case 8:
                        switch (variationIndex) {
                            case 1:
                                assertEquals(36, var.getPosition());
                                assertEquals(36, var.getReadIndex());
                                assertEquals("A", var.getFrom());
                                assertEquals("G", var.getTo());
                                break;

                        }
                        break;
                }

            }
        }
    }

    private void assertLength(final Alignments.AlignmentEntry alignmentEntry, final Alignments.SequenceVariation var, Alignment[] alignments) {
        final String readRaw = alignments[alignmentEntry.getQueryIndex()].read;
        final String readProcessed = readRaw.replaceAll("-", "");
        assertTrue(String.format("read index %d must be less than read length %d.", var.getReadIndex(), readProcessed.length()),
                var.getReadIndex() <= readProcessed.length());
    }


    @Test
    // TODO: Check these manually
    public void testBwaSequenceVariationParsing() throws IOException {
        final AlignmentReader reader = new AlignmentReaderImpl(bwaAlignmentFilename);
        while (reader.hasNext()) {
            final Alignments.AlignmentEntry alignmentEntry = reader.next();

            assertTrue("alignment must have variation", alignmentEntry.getSequenceVariationsCount() > 0);

            for (final Alignments.SequenceVariation var : alignmentEntry.getSequenceVariationsList()) {
                System.out.println(String.format("bwa entry reverseStrand=%b score=%f referenceIndex=%d  queryIndex=%d variation: %s",
                       alignmentEntry.getMatchingReverseStrand(),
                        alignmentEntry.getScore(),
                        alignmentEntry.getQueryIndex(),
                        alignmentEntry.getTargetIndex(),
                        var.toString()));
                assertLength(alignmentEntry, var, alignments);
                switch (alignmentEntry.getQueryIndex()) {
                    case 0:
                        //last finds this alignment. We test deletion of TCC from the reference.
                        assertEquals(7, var.getPosition());
                        assertEquals(8, var.getReadIndex());
                        assertEquals("--", var.getFrom());
                        assertEquals("CC", var.getTo());
                        break;
                    /*
                    case 1:
                        //last finds this alignment. We test deletion of TCC from the reference.
                        assertEquals(14, var.getPosition());
                        assertEquals(14, var.getReadIndex());
                        assertEquals("TCC", var.getFrom());
                        assertEquals("---", var.getTo());
                        break;
                    case 2:
                        if (alignmentEntry.getMatchingReverseStrand()) {
                             assertEquals(6,alignmentEntry.getTargetIndex());
                            // in the forward strand:
                            assertEquals(alignmentEntry.getQueryLength() - var.getReadIndex(), var.getPosition());
                        } else {
                            // in the forward strand:
                            assertEquals(var.getReadIndex(), var.getPosition());
                        }
                        // last finds this alignment. We test mutation from A to C at position 8 or 14
                        //  and T to C at position 21
                        switch (var.getPosition()) {
                            case 8:
                                assertEquals("A", var.getFrom());
                                assertEquals("C", var.getTo());
                                break;
                            case 14:
                                assertEquals("A", var.getFrom());
                                assertEquals("C", var.getTo());
                                break;
                            case 21:
                                assertEquals("T", var.getFrom());
                                assertEquals("C", var.getTo());
                                break;
                            default:
                                assertTrue("Invalid mutation detected.", false);
                        }
                        break;

                    case 3:
                        //last finds this alignment. We test deletion of TCC from the reference.
                        assertEquals(8, var.getPosition());
                        assertEquals(9, var.getReadIndex());
                        assertEquals("-", var.getFrom());
                        assertEquals("C", var.getTo());
                        break;

                    case 4:
                        //last finds this alignment. We test deletion of TCC from the reference.
                        assertEquals(14, var.getPosition());
                        assertEquals(14, var.getReadIndex());
                        assertEquals("TCC", var.getFrom());
                        assertEquals("---", var.getTo());
                        break;
                    */

                    case 5:
                        //last finds this alignment. We test the case when the read does not match the reference at the beginning:
                        assertEquals(24, var.getPosition());
                        assertEquals(23, var.getReadIndex());
                        assertEquals("T", var.getFrom());
                        assertEquals("-", var.getTo());
                        break;

                    case 6:
                        // last finds this alignment. We test mutation from A to C at position 8 or 14
                        //  and T to C at position 21
                        switch (var.getPosition()) {
                            case 8:
                                assertEquals(37, var.getReadIndex());
                                assertEquals("A", var.getFrom());
                                assertEquals("C", var.getTo());
                                break;
                            case 14:
                                assertEquals(31, var.getReadIndex());
                                assertEquals("A", var.getFrom());
                                assertEquals("C", var.getTo());
                                break;
                            case 21:
                                assertEquals(24, var.getReadIndex());
                                assertEquals("T", var.getFrom());
                                assertEquals("C", var.getTo());
                                break;
                            default:
                                assertTrue("Invalid mutation detected.", false);
                        }
                        break;

                    case 8:
                        //last finds this alignment. We test the case when the read does not match the reference at the beginning:
                        assertEquals(50, var.getPosition());
                        assertEquals(36, var.getReadIndex());
                        assertEquals("A", var.getFrom());
                        assertEquals("G", var.getTo());
                        break;

                    case 9:
                        // TODO: add assertions
                        switch (var.getPosition()) {
                            case 1:
                                assertEquals(1, var.getReadIndex());
                                assertEquals("T", var.getFrom());
                                assertEquals("C", var.getTo());
                                break;
                            case 26:
                                assertEquals(26, var.getReadIndex());
                                assertEquals("T", var.getFrom());
                                assertEquals("G", var.getTo());
                                break;
                            default:
                                assertTrue("Invalid mutation detected.", false);
                        }
                        break;
                    case 10:
                        // TODO: add assertions
                        switch (var.getPosition()) {
                            case 1:
                                assertEquals(36, var.getReadIndex());
                                assertEquals("T", var.getFrom());
                                assertEquals("C", var.getTo());
                                break;
                            case 26:
                                assertEquals(11, var.getReadIndex());
                                assertEquals("T", var.getFrom());
                                assertEquals("G", var.getTo());
                                break;
                            default:
                                assertTrue("Invalid mutation detected.", false);
                        }
                        break;
                }
            }
        }
    }

    @Test
    public void testBwaSequenceVariationParsingBug() throws IOException {

        ReadsWriter referenceWriter = new ReadsWriter(new FileOutputStream(referenceFilename2));
        ReadsWriter queryWriter = new ReadsWriter(new FileOutputStream(readsFilename2));
        for (final Alignment entry : alignmentsBug) {
            referenceWriter.setDescription("reference:" + entry.description);
            referenceWriter.setIdentifier("reference:" + entry.description);
            referenceWriter.setSequence(entry.reference.replaceAll("-", ""));
            referenceWriter.appendEntry();
            queryWriter.setDescription("read:" + entry.description);
            queryWriter.setSequence(entry.read.replaceAll("-", ""));
            queryWriter.appendEntry();

        }
        referenceWriter.close();
        queryWriter.close();

        // run the alignment process
        bwaAlignmentFilename2 = alignWith("bwa", readsFilename2, referenceFilename2, "bug");
        final AlignmentReader reader = new AlignmentReaderImpl(bwaAlignmentFilename2);
        while (reader.hasNext()) {
            final Alignments.AlignmentEntry alignmentEntry = reader.next();

            assertTrue("alignment must have variation", alignmentEntry.getSequenceVariationsCount() > 0);

            for (final Alignments.SequenceVariation var : alignmentEntry.getSequenceVariationsList()) {
                System.out.println(String.format("bwa entry reverseStrand=%b score=%f referenceIndex=%d  queryIndex=%d variation: %s",
                        alignmentEntry.getMatchingReverseStrand(),
                        alignmentEntry.getScore(),
                        alignmentEntry.getQueryIndex(),
                        alignmentEntry.getTargetIndex(),
                        var.toString()));
                assertLength(alignmentEntry, var, alignmentsBug);
                switch (alignmentEntry.getQueryIndex()) {


                    case 0:
                        // TODO: add assertions
                        System.out.println(TextFormat.printToString(var));
                        break;
                }
            }
        }
    }

    @Before
    public void setUp() throws IOException {
        ReadsWriter referenceWriter = new ReadsWriter(new FileOutputStream(referenceFilename));
        ReadsWriter queryWriter = new ReadsWriter(new FileOutputStream(readsFilename));
        for (final Alignment entry : alignments) {
            referenceWriter.setDescription("reference:" + entry.description);
            referenceWriter.setIdentifier("reference:" + entry.description);
            referenceWriter.setSequence(entry.reference.replaceAll("-", ""));
            referenceWriter.appendEntry();
            queryWriter.setDescription("read:" + entry.description);
            queryWriter.setSequence(entry.read.replaceAll("-", ""));
            queryWriter.appendEntry();

        }
        referenceWriter.close();
        queryWriter.close();

        // run the alignment process
        lastagAlignmentFilename = alignWith("lastag", readsFilename, referenceFilename, "1");
        bwaAlignmentFilename = alignWith("bwa", readsFilename, referenceFilename, "1");

    }

    private String alignWith(final String alignerName, String readsFilename, String referenceFilename, String suffix) throws IOException {
        final String alignmentFilename = FilenameUtils.concat(BASE_TEST_DIR, alignerName + "-alignment-" + suffix);
        final AlignMode aligner = new AlignMode();
        aligner.setWorkDirectory(new File(BASE_TEST_DIR));
        aligner.setAlignerName(alignerName);
        aligner.setColorSpace(false);
        aligner.setReadsFile(new File(readsFilename));
        aligner.setReferenceFile(new File(referenceFilename));
        aligner.setSearchFlag(true);

        aligner.setOutputBasename(alignmentFilename);
        aligner.setAlignerOptions("matchQuality=BEST_MATCH");
        aligner.setQualityFilterParameters("threshold=1");
        aligner.setKeepTemporaryFiles(true);
        aligner.setDatabaseDirectory(new File(BASE_TEST_DIR));
        aligner.execute();
        return alignmentFilename;
    }

    private final class Alignment {
        private final String reference;
        private final String read;
        private final String description;

        public Alignment(final String description, final String reference, final String read) {
            this.description = description;
            this.reference = reference;
            this.read = read;
        }
    }

    private final Alignment[] alignments = {
            new Alignment("0_insertion",
                    //1234567891111111111222
                    //         0123456789012
                    "TTTAAAA--TAAAAAAAAAAAAAAACCCC",
                    "TTTAAAACCTAAAAAAAAAAAAAAACCCC"),
            //  "TTTTAAAACTAAAAAAAAAAAAAAACCCC")
            new Alignment("1_deletion",
                    //1234567891111111111222
                    //         0123456789012
                    "CCAAAAAAAAAAATCCAAAAAAAAAACCCAAAAAAAAAA",
                    "CCAAAAAAAAAAA---AAAAAAAAAACCCAAAAAAAAAA"),
            new Alignment("2_mutations",
                   //1234567891111111111222
                   //         0123456789012
                    "TTTCCCAAATTTCACATCACTACTACTACGGATACAGAACGGGG",
                    "TTTCCCACATTTCCCATCACCACTACTACGGATACAGAACGGGG"),
            //.......M.....M......M.......................
            new Alignment("3_insertion",
                    //1234567891111111111222
                    //         0123456789012
                    "NNNNNNNNNNNNNNNNNNNNNNNNNNNNN", // use Ns when the query already matches another reference.
                    "TTTTAAAACTAAAAAAAAAAAAAAACCCC"),  // has a C inserted (compare to 0_insertion), at position 8 in the reference, and 9 in the read.
            new Alignment("4_deletion",
                    //1234567891111111111222
                    //         0123456789012
                    "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN", // use Ns when the query already matches another reference.
                    "CCAAAAAAAAAAA---AAAAAAAAAACCCAAAAAAAAAA"),
            new Alignment("5_deletion",
                    //1234567891111111111222222
                    //         0123456789012345
                    "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN", // use Ns when the query already matches another reference.
                    "TTTCCCAAATTTCACATCACTAC-ACTACGGATACAGAACGGGG"),     // The reference has a T instead of the gap character at position 24.
            new Alignment("6_mutations-reversed",
                    //1234567891111111111222
                    //         0123456789012
                    "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN",
                    "CCCCGTTCTGTATCCGTAGTAGTGGTGATGGGAAATGTGGGAAA"),    // reverse complement of sequence 2_mutations, used to check positions for reverse strand matches
            new Alignment("7_mutations_padding",
                    //1234567891111111111222
                    //         0123456789012
                    "--CCTTCCTTCCTTCCTTCCACTATCATTTTAACTACTCATACTATCCCATATA",
                    "AACCTTCCTTCCTTCCTTCCTCTATCATTTTAACTACTCATACTATCCCATATA"),
            new Alignment("8_read_padding",
                    //1234567891111111111222
                    //         0123456789012
                    "NNNN",
                    "TTCCACTATCATTTTAACTACTCATACTATCCCATGTA"),    //   A->G  readIndex=36, position=
            new Alignment("9_0T24T10",
                    //1234567891111111111222
                    //         0123456789012
                    "TTCCAGAACTGTAAGATAATAAGTTTGTGTTGTTTT",
                    "CTCCAGAACTGTAAGATAATAAGTTGGTGTTGTTTT"),
            new Alignment("10_0T24T10",          // same as previous but matches reverse strand
                    //1234567891111111111222
                    //         0123456789012
                    "TTCCAGAACTGTAAGATAATAAGTTTGTGTTGTTTT",
                    "AAAACAACACCAACTTATTATCTTACAGTTCTGGAG"),

    };
    private final Alignment[] alignmentsBug = {

            new Alignment("0_0T24T10",          // same as previous but matches reverse strand
                    //1234567891111111111222
                    //         0123456789012
                    "TTCCAGAACTGTAAGATAATAAGTTTGTGTTGTTTT",
                    "AAAACAACACCAACTTATTATCTTACAGTTCTGGAG"),

    };
}
