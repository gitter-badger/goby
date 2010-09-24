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

import java.io.*;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

import edu.cornell.med.icb.goby.alignments.*;
import edu.cornell.med.icb.goby.stats.DifferentialExpressionCalculator;
import edu.cornell.med.icb.goby.stats.DifferentialExpressionAnalysis;
import edu.cornell.med.icb.goby.stats.FisherExactRCalculator;
import edu.cornell.med.icb.goby.algorithmic.algorithm.SequenceVariationPool;
import edu.cornell.med.icb.goby.R.GobyRengine;
import edu.cornell.med.icb.identifier.IndexedIdentifier;
import edu.cornell.med.icb.io.TSVReader;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.lang.MutableString;
import org.rosuda.JRI.Rengine;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * This mode discovers sequence variants within groups of samples or between groups of samples.
 * Within mode discovery is useful to identify sequence variations that cannot be explained by
 * sequencing error in a given sample. Between mode discovery identify those variants that are found
 * more often in one group versus another.
 *
 * @author Fabien Campagne
 *         Date: Aug 30, 2010
 *         Time: 12:04:59 PM
 */
public class DiscoverSequenceVariantsMode extends AbstractGobyMode {
    /**
     * The mode name.
     */
    private static final String MODE_NAME = "discover-sequence-variants";
    /**
     * The mode description help text.
     */
    private static final String MODE_DESCRIPTION =
            "Discover sequence variants within and across groups of samples. Identify variations significantly enriched" +
                    "in one group or the other. This mode will either (i) identify sequence variants within a group of sample\n" +
                    "  or (ii) identify variants whose frequency is significantly enriched in one of two groups. \n" +
                    "  This mode requires sorted/indexed alignments as input. (Since Goby 1.8) ";

    private static final Logger LOG = Logger.getLogger(DiscoverSequenceVariantsMode.class);
    private String[] inputFilenames;
    private String outputFile;
    private int[] readerIndexToGroupIndex;
    private int numberOfGroups;
    private CharSequence currentReferenceId;
    private int thresholdDistinctReadIndices = 3;
    private int minimumVariationSupport = 10;
    private PrintWriter outWriter;
    private boolean fisherRInstalled;
    private int currentReferenceIndex;
    private String[] groups;
    /**
     * The maximum value of read index, indexed by readerIndex;
     */
    private int numberOfReadIndices[];

    @Override
    public String getModeName() {
        return MODE_NAME;
    }

    @Override
    public String getModeDescription() {
        return MODE_DESCRIPTION;
    }

    private final DifferentialExpressionCalculator deCalculator = new DifferentialExpressionCalculator();
    private final DifferentialExpressionAnalysis deAnalyzer = new DifferentialExpressionAnalysis();


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

        outputFile = jsapResult.getString("output");
        outWriter = "-".equals(outputFile) ? new PrintWriter(System.out) : new PrintWriter(outputFile);

        final String groupsDefinition = jsapResult.getString("groups");
        deAnalyzer.parseGroupsDefinition(groupsDefinition, deCalculator, inputFilenames);
        final String compare = jsapResult.getString("compare");

        deAnalyzer.parseCompare(compare);
        Map<String, String> sampleToGroupMap = deCalculator.getSampleToGroupMap();
        readerIndexToGroupIndex = new int[inputFilenames.length];

        IndexedIdentifier groupIds = new IndexedIdentifier();
        for (String group : sampleToGroupMap.values()) {
            groupIds.registerIdentifier(new MutableString(group));
        }
        minimumVariationSupport = jsapResult.getInt("minimum-variation-support");
        thresholdDistinctReadIndices = jsapResult.getInt("threshold-distinct-read-indices");
        CompactAlignmentToAnnotationCountsMode.parseEval(jsapResult, deAnalyzer);

        numberOfGroups = deAnalyzer.getGroups().length;
        groups = deAnalyzer.getGroups();
        variationPool = new SequenceVariationPool(numberOfGroups);
        for (String sample : sampleToGroupMap.keySet()) {
            final String group = sampleToGroupMap.get(sample);
            System.out.printf("sample: %s group %s%n", sample, group);
            for (int readerIndex = 0; readerIndex < inputFilenames.length; readerIndex++) {
                if (AlignmentReader.getBasename(inputFilenames[readerIndex]).endsWith(sample)) {
                    readerIndexToGroupIndex[readerIndex] = groupIds.get(new MutableString(group));

                }
            }
        }
        File statFile = jsapResult.getFile("variation-stats");
        if (statFile != null) {
            loadStatFile(statFile);
        } else {
            if (deAnalyzer.eval("within-groups")) {
                System.err.println("To evaluate statistics within-groups you must provide a --variation-stats argument.");
                System.exit(1);
            }

        }

        refCount = new int[numberOfGroups];
        variantsCount = new int[numberOfGroups];
        distinctReadIndexCount = new int[numberOfGroups];

        sortedPositionIterator = new DiscoverVariantIterateSortedAlignments();
        sortedPositionIterator.parseIncludeReferenceArgument(jsapResult);
        sortedPositionIterator.setReaderIndexToGroupIndex(readerIndexToGroupIndex);
        sortedPositionIterator.setMinimumVariationSupport(minimumVariationSupport);
        sortedPositionIterator.setThresholdDistinctReadIndices(thresholdDistinctReadIndices);
        return this;
    }

    DiscoverVariantIterateSortedAlignments sortedPositionIterator;

    public class ReadIndexStats {
        public String basename;
        /**
         * The index of the alignment reader that is reading over this basename, will be populated when we know.
         */
        public int readerIndex = -1;
        /**
         * Indexed by readIndex
         */
        public int[] countVariationBases;
        /**
         * Indexed by readIndex
         */
        public int[] countReferenceBases;
    }

    ObjectArrayList<ReadIndexStats> readIndexStats;

    private void loadStatFile(File statFile) {
        try {
            TSVReader reader = new TSVReader(new FileReader(statFile), '\t');
            reader.setCommentPrefix("basename");
            readIndexStats = new ObjectArrayList<ReadIndexStats>();
            ReadIndexStats stat = new ReadIndexStats();
            String lastBasename = null;
            IntArrayList countVariationBases = new IntArrayList();
            IntArrayList countReferenceBases = new IntArrayList();

            String basename = null;
            while (reader.hasNext()) {
                if (reader.isCommentLine() || reader.isEmptyLine()) {
                    // Do nothing, this is a comment or empty line
                    reader.skip();
                } else {
                    reader.next();
                    //if (reader.isCommentLine()) continue;
                    basename = reader.getString();

                    if (lastBasename != null && !lastBasename.equals(basename)) {
                        //we are now processing a new basename. Save the previous stat and start a new one.
                        stat.basename = lastBasename;
                        stat.countVariationBases = countVariationBases.toIntArray();
                        stat.countReferenceBases = countReferenceBases.toIntArray();
                        readIndexStats.add(stat);

                        stat = new ReadIndexStats();
                        countVariationBases.clear();
                        countReferenceBases.clear();
                        lastBasename = basename;

                    }
                    int readIndex = reader.getInt();
                    countVariationBases.add(reader.getInt());

                    assert readIndex == countVariationBases.size();
                    reader.getFloat(); // ignore
                    reader.getFloat(); // ignore
                    reader.getInt(); // ignore
                    countReferenceBases.add(reader.getInt());
                    lastBasename = basename;
                }
            }
            stat.basename = basename;
            stat.countVariationBases = countVariationBases.toIntArray();
            stat.countReferenceBases = countReferenceBases.toIntArray();
            readIndexStats.add(stat);

        } catch (FileNotFoundException e) {
            System.err.printf("Error. The -v file argument cannot be found (%s)%n", statFile);
            System.exit(1);
        }

        catch (IOException e) {
            System.err.println("Cannot parse stats file. Details may be provided below." +
                    " The file should have been produced with --mode sequence-variation-stats");
            e.printStackTrace(System.err);
            System.exit(1);
        }

    }

    /**
     * Perform the concatenation.
     *
     * @throws java.io.IOException
     */
    @Override
    public void execute() throws IOException {
        final String outputFilename = outputFile;

        final String[] basenames = AlignmentReader.getBasenames(inputFilenames);
        final boolean allSorted = ConcatenateAlignmentMode.isAllSorted(basenames);
        if (!allSorted) {
            System.out.println("Each input alignment must be sorted. Aborting.");
            System.exit(10);
        }
        if (readIndexStats != null) {
            // associate reader index to basename in the stats, then sort by readerIndex:
            int readerIndex = 0;
            for (String basename : basenames) {
                boolean found = false;
                for (ReadIndexStats stat : readIndexStats) {
                    if (FilenameUtils.getBaseName(basename).equals(stat.basename)) {
                        stat.readerIndex = readerIndex;
                        found = true;
                    }
                }
                if (!found) {
                    System.err.printf("Cannot find basename %s in stat file.", basename);
                }
                readerIndex++;
            }
            Collections.sort(readIndexStats, new Comparator<ReadIndexStats>() {
                public int compare(ReadIndexStats readIndexStats, ReadIndexStats readIndexStatsFirst) {
                    return readIndexStats.readerIndex - readIndexStatsFirst.readerIndex;
                }
            });
            // Determine the maximum read length for each input sample (fill numberOfReadIndices)
            numberOfReadIndices = new int[this.deCalculator.getSampleToGroupMap().keySet().size()];
            ObjectSet<ReadIndexStats> toRemove = new ObjectArraySet<ReadIndexStats>();

            for (ReadIndexStats stat : readIndexStats) {
                if (stat.readerIndex == -1) {
                    // this sample was not loaded, remove it from consideration
                    toRemove.add(stat);
                    continue;
                }
                numberOfReadIndices[stat.readerIndex] = Math.max(numberOfReadIndices[stat.readerIndex], stat.countReferenceBases.length);
            }

            readIndexStats.removeAll(toRemove);
        }

        sortedPositionIterator.initialize(deAnalyzer,deCalculator, groups, readIndexStats, outWriter);
        sortedPositionIterator.iterate(basenames);
        sortedPositionIterator.finish();


    }

    int[] refCount;
    int[] variantsCount;
    int[] distinctReadIndexCount;




    SequenceVariationPool variationPool;

    private void pushVariations(ObjectArrayList<Alignments.AlignmentEntry> entriesAtPosition,
                                IntArrayList readerIndices) {

        int alignmentReaderIndex = 0;
        // organize all variations found in these entries by their  position on the reference sequence:
        for (Alignments.AlignmentEntry entry : entriesAtPosition) {
            final int groupIndex = readerIndexToGroupIndex[readerIndices.get(alignmentReaderIndex)];

            variationPool.store(entry, groupIndex, alignmentReaderIndex);

        }
        alignmentReaderIndex += 1;
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
        new DiscoverSequenceVariantsMode().configure(args).execute();
    }

}
