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

package edu.cornell.med.icb.goby.modes;

import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import edu.cornell.med.icb.goby.alignments.AlignmentReader;
import edu.cornell.med.icb.goby.alignments.Alignments;
import edu.cornell.med.icb.goby.alignments.IterateAlignments;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FilenameUtils;
import it.unimi.dsi.fastutil.ints.*;

/**
 * Display the sequence variations found in alignments.
 *
 * @author Fabien Campagne
 */
public class DisplaySequenceVariationsMode extends AbstractGobyMode {
    /**
     * The mode name.
     */
    private static final String MODE_NAME = "display-sequence-variations";

    /**
     * The mode description help text.
     */
    private static final String MODE_DESCRIPTION = "Display the sequence variations found in an alignment";

    /**
     * The input filenames.
     */
    private String[] inputFilenames;

    /**
     * The output file.
     */
    private String outputFilename;
    /**
     * The input basenames.
     */
    private String[] basenames;
    private MyIterateAlignments alignmentIterator;
    private FirstPassIterateAlignments firstPassIterator;
    private boolean thresholds;
    private int minimumUniqueReadIndices = 1;


    @Override
    public String getModeName() {
        return MODE_NAME;
    }

    enum OutputFormat {
        CONCISE,
        TSV,
        TAB_DELIMITED,
        TAB_SINGLE_BASE,
    }

    private OutputFormat outputFormat;

    @Override
    public String getModeDescription() {
        return MODE_DESCRIPTION;
    }

    /**
     * Configure.
     *
     * @param args command line arguments
     * @return this object for chaining
     * @throws java.io.IOException error parsing
     * @throws com.martiansoftware.jsap.JSAPException
     *                             error parsing
     */
    @Override
    public AbstractCommandLineMode configure(final String[] args) throws IOException, JSAPException {
        final JSAPResult jsapResult = parseJsapArguments(args);

        inputFilenames = jsapResult.getStringArray("input");
        basenames = AlignmentReader.getBasenames(inputFilenames);
        outputFilename = jsapResult.getString("output");
        outputFormat = OutputFormat.valueOf(jsapResult.getString("format").toUpperCase());
        minimumUniqueReadIndices = jsapResult.getInt("minimum-read-indices");

        alignmentIterator = new MyIterateAlignments();
        alignmentIterator.parseIncludeReferenceArgument(jsapResult);

        thresholds = minimumUniqueReadIndices > 0;
        if (thresholds) {
            firstPassIterator = new FirstPassIterateAlignments();
            firstPassIterator.parseIncludeReferenceArgument(jsapResult);
        }
        return this;
    }

    /**
     * Display sequence variations.
     *
     * @throws java.io.IOException error reading / writing
     */
    @Override
    public void execute() throws IOException {
        final PrintWriter writer = outputFilename == null ? new PrintWriter(System.out) :
                new PrintWriter(new FileWriter(outputFilename));
        switch (outputFormat) {
            case CONCISE:

                break;
            case TAB_DELIMITED:
            case TAB_SINGLE_BASE:
            case TSV:
                writer.println("basename\tquery-index\ttarget-id\tposition-on-reference\tread-index\tvar-from\tvar-to\ttype");
                break;
        }

        try {
            if (thresholds) firstPassIterator.iterate(basenames);

            alignmentIterator.setOutputWriter(writer, outputFormat);
            if (thresholds) alignmentIterator.setFirstPass(firstPassIterator);
            // Iterate through each alignment and write sequence variations to output file:
            alignmentIterator.iterate(basenames);
        }


        finally {

            writer.close();
        }

    }

    /**
     * Main method.
     *
     * @param args command line args.
     * @throws com.martiansoftware.jsap.JSAPException
     *                             error parsing
     * @throws java.io.IOException error parsing or executing.
     */
    public static void main(final String[] args) throws JSAPException, IOException {
        new DisplaySequenceVariationsMode().configure(args).execute();
    }

    /**
     * Collect the list of read indices where each variation is observed, for a given reference position.
     */
    private class FirstPassIterateAlignments extends IterateAlignments {
        // reference index -> reference Position -> readIndex list
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArraySet>> readIndicesForReferencePositions =
                new Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArraySet>>();

        public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArraySet>> getReadIndicesForReferencePositions() {
            return readIndicesForReferencePositions;
        }

        private FirstPassIterateAlignments() {
            readIndicesForReferencePositions.defaultReturnValue(new Int2ObjectOpenHashMap<IntArraySet>());
        }

        public IntArraySet getReadIndices(int referenceIndex, int referencePosition) {
            final Int2ObjectOpenHashMap<IntArraySet> referencePositionsMap = readIndicesForReferencePositions.get(referenceIndex);
            final IntArraySet readIndexList = referencePositionsMap.get(referencePosition);
            return readIndexList;
        }

        public void processAlignmentEntry(AlignmentReader alignmentReader, Alignments.AlignmentEntry alignmentEntry) {
            final int referenceIndex = alignmentEntry.getTargetIndex();
            final int alignmentPositionOnReference = alignmentEntry.getPosition();
            final Int2ObjectOpenHashMap<IntArraySet> referencePositionsMap = readIndicesForReferencePositions.get(referenceIndex);
            for (Alignments.SequenceVariation var : alignmentEntry.getSequenceVariationsList()) {
                int referencePosition = var.getPosition() + alignmentPositionOnReference;
                IntArraySet readIndexList = referencePositionsMap.get(referencePosition);
                if (readIndexList == null) {
                    readIndexList = new IntArraySet();
                }
                readIndexList.add(var.getReadIndex());
                referencePositionsMap.put(referencePosition, readIndexList);
            }

        }
    }

    private class MyIterateAlignments extends IterateAlignments {
        PrintWriter outputWriter;
        private OutputFormat outputFormat;


        public void setOutputWriter(PrintWriter outputWriter, OutputFormat outputFormat) {
            this.outputWriter = outputWriter;
            this.outputFormat = outputFormat;
        }

        public void processAlignmentEntry(AlignmentReader alignmentReader, Alignments.AlignmentEntry alignmentEntry) {
            String basename = alignmentReader.basename();
            // remove the path:
            basename = FilenameUtils.getBaseName(basename);
            switch (outputFormat) {

                case CONCISE: {
                    if (alignmentEntry.getSequenceVariationsCount() > 0) {
                        outputWriter.print(String.format("%d %s ",

                                alignmentEntry.getQueryIndex(),
                                getReferenceId(alignmentEntry.getTargetIndex())));
                        boolean variations = false;
                        for (Alignments.SequenceVariation var : alignmentEntry.getSequenceVariationsList()) {
                            variations = true;
                            // convert variation position to position on the reference:
                            final int positionOnReference = alignmentEntry.getPosition() + var.getPosition();
                            int referenceIndex = alignmentEntry.getTargetIndex();
                            boolean keepVar = true;
                            keepVar = determineKeepVariation(positionOnReference, referenceIndex, keepVar);
                            if (keepVar) {
                                outputWriter.print(String.format("%d:%d:%s/%s,",


                                        positionOnReference,
                                        var.getReadIndex(),
                                        var.getFrom(),
                                        var.getTo()));
                            }
                        }
                        if (variations) {
                            outputWriter.println();

                        }
                    }
                }
                break;
                case TSV:
                case TAB_DELIMITED: {
                    boolean variations = false;

                    for (Alignments.SequenceVariation var : alignmentEntry.getSequenceVariationsList()) {

                        // convert variation position to position on the reference:
                        final int positionOnReference = alignmentEntry.getPosition() + var.getPosition();
                        final int readIndex = var.getReadIndex();
                        final String from = var.getFrom();
                        final String to = var.getTo();
                        int referenceIndex = alignmentEntry.getTargetIndex();
                        boolean keepVar = true;
                        keepVar = determineKeepVariation(positionOnReference, referenceIndex, keepVar);
                        if (keepVar && !isAllNs(to)) {


                            printTab(alignmentEntry, basename, positionOnReference, readIndex, from, to);
                        }
                    }


                }
                break;
                case TAB_SINGLE_BASE: {
                    boolean variations = false;

                    for (Alignments.SequenceVariation var : alignmentEntry.getSequenceVariationsList()) {

                        // convert variation position to position on the reference:
                        final int positionOnReference = alignmentEntry.getPosition() + var.getPosition();
                        final int readIndex = var.getReadIndex();
                        final String from = var.getFrom();
                        final String to = var.getTo();
                        int fromLength = from.length();
                        int toLength = to.length();
                        int referenceIndex = alignmentEntry.getTargetIndex();
                        boolean keepVar = true;
                        keepVar = determineKeepVariation(positionOnReference, referenceIndex, keepVar);
                        if (keepVar && !isAllNs(to)) {
                            variations = true;
                            int maxLength = Math.max(fromLength, toLength);
                            for (int i = 0; i < maxLength; i++) {
                                int offset = +i * (alignmentEntry.getMatchingReverseStrand() ? -1 : 1);
                                printTab(alignmentEntry, basename,
                                        positionOnReference + offset,
                                        readIndex + offset,
                                        i < fromLength ? from.substring(i, i + 1) : "",
                                        i < toLength ? to.substring(i, i + 1) : "");
                            }
                        }
                    }


                }
                break;
            }


        }

        private boolean determineKeepVariation(int positionOnReference, int referenceIndex, boolean keepVar) {
            if (thresholds) {
                final IntArraySet indices = firstPassIterator.getReadIndices(referenceIndex, positionOnReference);
                if (indices != null && indices.size() < minimumUniqueReadIndices) keepVar = false;
            }
            return keepVar;
        }

        private boolean isAllNs(final String to) {

            for (int i = 0; i < to.length(); ++i) {
                if (to.charAt(i) != 'N') return false;

            }
            return true;
        }

        private void printTab(Alignments.AlignmentEntry alignmentEntry, String basename, int positionOnReference, int readIndex, String from, String to) {
            String type;
            if (from.contains("-") || from.length() == 0) {
                // insertion in read sequence.
                type = "READ_INSERTION";
            } else if (to.contains("-") || to.length() == 0) {
                // deletion in read sequence.
                type = "READ_DELETION";
            } else {
                // one or more bases are mutated. no insertions or deletions.
                type = "MUTATION";
            }
            outputWriter.println(String.format("%s\t%d\t%s\t%d\t%d\t%s\t%s\t%s",
                    basename,
                    alignmentEntry.getQueryIndex(),
                    getReferenceId(alignmentEntry.getTargetIndex()),
                    positionOnReference,
                    readIndex,
                    from,
                    to,
                    type));
        }

        FirstPassIterateAlignments firstPassIterator;

        public void setFirstPass(FirstPassIterateAlignments firstPassIterator) {
            this.firstPassIterator = firstPassIterator;
        }
    }
}