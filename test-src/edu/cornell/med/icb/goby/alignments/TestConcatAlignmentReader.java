/*
 * Copyright (C) 2009-2010 Institute for Computational Biomedicine,
 *                         Weill Medical College of Cornell University
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Fabien Campagne
 *         Date: May 20, 2009
 *         Time: 6:33:41 PM
 */
public class TestConcatAlignmentReader {
    /**
     * Used to log debug and informational messages.
     */
    private static final Log LOG = LogFactory.getLog(TestConcatAlignmentReader.class);

    private static final String BASE_TEST_DIR = "test-results/alignments/concat/";

    private int numEntriesIn101;
    private int numQueries101;
    private int numEntriesIn102;
    private int numQueries102;
    private final int numTargets = 5;
    private String outputBasename1;
    private String outputBasename2;
    private int count102;
    private int count101;

    @Test
    public void testLoadTwo() throws IOException {
        final int count;

        final ConcatAlignmentReader concatReader = new ConcatAlignmentReader(outputBasename1, outputBasename2);
        count = countAlignmentEntries(concatReader);
        assertEquals(count101 + count102, count);
        concatReader.readHeader();

        assertEquals(numQueries101 + numQueries102, concatReader.getNumberOfQueries());
        assertEquals(numTargets, concatReader.getNumberOfTargets());

    }

    @Test
    public void testLoadTwoAdjustFalse() throws IOException {
        final int count;

        final ConcatAlignmentReader concatReader = new ConcatAlignmentReader(false, outputBasename2, outputBasename1);
        count = countAlignmentEntries(concatReader);
        assertEquals(count101 + count102, count);
        concatReader.readHeader();

        assertEquals(numQueries101 + numQueries102, concatReader.getNumberOfQueries());
        assertEquals(numTargets, concatReader.getNumberOfTargets());

    }

    @Test
    public void testQueryIndices() throws IOException {
        final ConcatAlignmentReader concatReader = new ConcatAlignmentReader(outputBasename1, outputBasename2);
        while (concatReader.hasNext()) {
            final Alignments.AlignmentEntry alignmentEntry = concatReader.next();

            if (alignmentEntry.getScore() == 50) {
                assertTrue(alignmentEntry.getQueryIndex() >= numQueries101);
            } else if (alignmentEntry.getScore() == 30) {
                assertTrue(alignmentEntry.getQueryIndex() < numQueries101);
            } else {
                fail("only scores possible are 30 and 50.");
            }

        }
    }

    @Test
    public void testQueryIndicesNoAdjustment() throws IOException {
        final ConcatAlignmentReader concatReader = new ConcatAlignmentReader(false,outputBasename1, outputBasename2);
       
        while (concatReader.hasNext()) {
            final Alignments.AlignmentEntry alignmentEntry = concatReader.next();

            if (alignmentEntry.getScore() == 50) {
                assertTrue(alignmentEntry.getQueryIndex() <= Math.max(numQueries101, numQueries102));
            } else if (alignmentEntry.getScore() == 30) {
                assertTrue(alignmentEntry.getQueryIndex() <= Math.max(numQueries101, numQueries102));
            } else {
                fail("only scores possible are 30 and 50.");
            }

        }
    }


    private int countAlignmentEntries(final AbstractAlignmentReader reader) {
        int count = 0;

        while (reader.hasNext()) {
            final Alignments.AlignmentEntry alignmentEntry = reader.next();
            //   System.out.println("found entry: " + alignmentEntry);
            assert alignmentEntry.hasPosition();
            count++;
        }
        return count;
    }

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
        FileUtils.forceDeleteOnExit(new File(BASE_TEST_DIR));
    }

    @Before
    public void setUp() throws IOException {
        {
            outputBasename1 = FilenameUtils.concat(BASE_TEST_DIR, "concat-align-101");
            final AlignmentWriter writer = new AlignmentWriter(outputBasename1);
            writer.setNumAlignmentEntriesPerChunk(1000);

            final int numQuery = 10;
            int position = 1;
            final int score = 30;
            for (int targetIndex = 0; targetIndex < numTargets; targetIndex++) {
                for (int queryIndex = 0; queryIndex < numQuery; queryIndex++) {
                    writer.setAlignmentEntry(queryIndex, targetIndex, position++, score, false);
                    writer.appendEntry();
                    numEntriesIn101++;
                    count101++;
                }
            }
            numQueries101 = numQuery;
            int[] queryLengths = new int[numQuery];
            for (int i = 0; i < queryLengths.length; i++) {
                queryLengths[i] = i;
            }
            writer.setQueryLengths(queryLengths);
            writer.close();
        }
        {
            outputBasename2 = FilenameUtils.concat(BASE_TEST_DIR, "concat-align-102");
            final AlignmentWriter writer = new AlignmentWriter(outputBasename2);
            writer.setNumAlignmentEntriesPerChunk(1000);

            final int numQuery = 13;
            int position = 1;
            final int score = 50;
            for (int targetIndex = 0; targetIndex < numTargets; targetIndex++) {
                for (int queryIndex = 0; queryIndex < numQuery; queryIndex++) {
                    writer.setAlignmentEntry(queryIndex, targetIndex, position++, score, false);
                    writer.appendEntry();
                    numEntriesIn102++;
                    count102++;
                }
            }
            numQueries102 = numQuery;
            int[] queryLengths = new int[numQuery];
            for (int i = 0; i < queryLengths.length; i++) {
                queryLengths[i] = i;
            }
            writer.setQueryLengths(queryLengths);
            writer.close();
        }


    }
}
