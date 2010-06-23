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

import edu.cornell.med.icb.goby.reads.MessageChunksWriter;
import edu.cornell.med.icb.identifier.IndexedIdentifier;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

/**
 * This class write alignments in a Protocol Buffer, compressed, binary and splitable format.
 * See Alignements.proto for the specification of this format.
 *
 * @author Fabien Campagne
 *         Date: Apr 30, 2009
 *         Time: 5:53:50 PM
 */
public class AlignmentWriter implements Closeable {
    private final Alignments.AlignmentCollection.Builder collectionBuilder;
    private final MessageChunksWriter entriesChunkWriter;
    private IndexedIdentifier queryIdentifiers;
    private IndexedIdentifier targetIdentifiers;
    private boolean headerWritten;
    private final GZIPOutputStream headerOutput;

    /**
     * Length of each query sequence.
     */
    private int[] queryLengths;
    /**
     * Length of each target sequence.
     */
    private int[] targetLengths;

    private String[] queryIdentifiersArray;
    private String[] targetIdentifiersArray;
    private int maxTargetIndex = -1;
    private int minQueryIndex = Integer.MAX_VALUE;
    private int maxQueryIndex = Integer.MIN_VALUE;
    private int actualNumberOfQueries = Integer.MIN_VALUE;
    private final Properties stats;
    private boolean statsWritten;
    private final FileWriter statsWriter;
    private final String basename;
    private int numberOfAlignedReads;
    private int constantQueryLength;
    private boolean isConstantQueryLength;
    private Alignments.AlignmentEntry.Builder newEntry;
    private final FileOutputStream alignmentEntries;
    private int queryLength;
    private boolean sortedState;

    // data structures to build index:
    private int previousChunkOffset = 0;
    private int firstTargetIndexInChunk;
    private boolean firstEntryInChunk = true;
    private int firstPositionInChunk;
    private LongArrayList indexOffsets = new LongArrayList();
    private LongArrayList indexAbsolutePositions = new LongArrayList();
    private boolean indexWritten;
    private long[] targetPositionOffsets;

    public AlignmentWriter(final String outputBasename) throws IOException {
        alignmentEntries = new FileOutputStream(outputBasename + ".entries");
        headerOutput = new GZIPOutputStream(new FileOutputStream(outputBasename + ".header"));
        statsWriter = new FileWriter(outputBasename + ".stats");
        this.basename = outputBasename;
        collectionBuilder = Alignments.AlignmentCollection.newBuilder();
        entriesChunkWriter = new MessageChunksWriter(alignmentEntries);
        newEntry = Alignments.AlignmentEntry.newBuilder();
        queryIdentifiers = new IndexedIdentifier();
        targetIdentifiers = new IndexedIdentifier();
        stats = new Properties();
        // we assume stats were written until a client puts stats in this writer.
        statsWritten = true;

    }

    public void setSorted(boolean sortedState) {
        this.sortedState = sortedState;
        if (sortedState) {
            if (targetPositionOffsets == null) {
                throw new UnsupportedOperationException("Indexing sorted alignments requires knowning target lengths " +
                        "before entries are appended. setTargetLength must be called before setSorted(true).");
            }
        }
    }

    public final void setQueryIndex(final int queryIndex) {
        newEntry.setQueryIndex(queryIndex);
    }

    public final void setTargetIndex(final int referenceIndex) {
        newEntry.setTargetIndex(referenceIndex);
    }

    public final void setTargetPosition(final int position) {
        newEntry.setPosition(position);
    }

    public final void setAlignmentScore(final float score) {
        newEntry.setScore(score);
    }

    public void setNumAlignmentEntriesPerChunk(final int numEntriesPerChunk) {
        entriesChunkWriter.setNumEntriesPerChunk(numEntriesPerChunk);
    }

    public final void setAlignmentEntry(final int queryIndex, final int referenceIndex,
                                        final int position,
                                        final float score, final boolean matchesReverseStrand) {
        newEntry.setQueryIndex(queryIndex);
        newEntry.setTargetIndex(referenceIndex);
        newEntry.setScore(score);
        newEntry.setPosition(position);
        newEntry.setMatchingReverseStrand(matchesReverseStrand);
        newEntry.setMultiplicity(1);
    }

    /**
     * Obtain the alignment entry that is being prepared. Set values on the entry, then call appendAlignmentEntry()
     *
     * @return the current alignment entry.
     */
    public Alignments.AlignmentEntry.Builder getAlignmentEntry() {
        return newEntry;
    }

    /**
     * Append the current entry to the file being written.
     *
     * @throws IOException
     */
    public synchronized void appendEntry() throws IOException {

        if (!newEntry.hasQueryLength() && queryLengths != null) {
            // when the entry does not already have a defined query length,
            // set query Length from the length array if it is available 
            newEntry.setQueryLength(queryLengths[newEntry.getQueryIndex()]);
        }
        final Alignments.AlignmentEntry builtEntry = newEntry.build();

        final int currentQueryIndex = builtEntry.getQueryIndex();
        minQueryIndex = Math.min(currentQueryIndex, minQueryIndex);
        maxQueryIndex = Math.max(currentQueryIndex, maxQueryIndex);

        maxTargetIndex = Math.max(builtEntry.getTargetIndex(), maxTargetIndex);
        this.collectionBuilder.addAlignmentEntries(builtEntry);
        writeIndexEntry(builtEntry);

        newEntry = Alignments.AlignmentEntry.newBuilder();
    }

    private void writeIndexEntry(Alignments.AlignmentEntry builtEntry) throws IOException {
        if (firstEntryInChunk) {
            firstTargetIndexInChunk = builtEntry.getTargetIndex();
            firstPositionInChunk = builtEntry.getPosition();
            firstEntryInChunk = false;
        }
        int currentChunkOffset = entriesChunkWriter.writeAsNeeded(collectionBuilder, builtEntry.getMultiplicity());
        if (sortedState && currentChunkOffset != previousChunkOffset) {
            // we have just written a new chunk.
            pushIndex(previousChunkOffset, firstTargetIndexInChunk, firstPositionInChunk);
            previousChunkOffset = currentChunkOffset;
            firstEntryInChunk = true;

        } else {
            firstEntryInChunk = false;
        }
    }

    private void pushIndex(int previousChunkOffset, int firstTargetIndexInChunk, int firstPositionInChunk) {

        final int newOffset = Math.max(previousChunkOffset - 8, 0);
        final int size = indexOffsets.size();
        // remove duplicates because the behavior of binary search is undefined for duplicates:
        if (size == 0 || newOffset != indexOffsets.get(size - 1)) {

            indexOffsets.add(newOffset);
            final long codedPosition = recodePosition(firstTargetIndexInChunk, firstPositionInChunk);
            indexAbsolutePositions.add(codedPosition);
            System.out.printf("INDEX Pushing offset %d %d ", newOffset, codedPosition);
        }

    }

    protected long recodePosition(final int firstTargetIndexInChunk, final int firstPositionInChunk) {
        return targetPositionOffsets[firstTargetIndexInChunk] + firstPositionInChunk;
    }

    /**
     * Append an entry to the file being written. The entry must have been prepared
     * outside this writer.
     *
     * @param builtEntry
     * @throws IOException If an error occurs writting this entry.
     */
    public synchronized void appendEntry(final Alignments.AlignmentEntry builtEntry) throws IOException {
        this.collectionBuilder.addAlignmentEntries(builtEntry);
        writeIndexEntry(builtEntry);

        final int currentQueryIndex = builtEntry.getQueryIndex();
        minQueryIndex = Math.min(currentQueryIndex, minQueryIndex);
        maxQueryIndex = Math.max(currentQueryIndex, maxQueryIndex);

        maxTargetIndex = Math.max(builtEntry.getTargetIndex(), maxTargetIndex);
        numberOfAlignedReads += 1;
    }

    /**
     * Replace the target entry, then append the entry.
     *
     * @param entry
     * @param replacementTargetIndex the replacement targetIndex.
     * @throws IOException
     */
    public void appendEntry(Alignments.AlignmentEntry entry, final int replacementTargetIndex) throws IOException {
        entry = entry.newBuilderForType().mergeFrom(entry).setTargetIndex(replacementTargetIndex).build();
        this.appendEntry(entry);
    }

    /**
     * Set the query length on this entry, then append.
     *
     * @param entry       Entry to write
     * @param queryLength the query length.
     * @throws IOException
     */
    public void appendEntryWithLength(Alignments.AlignmentEntry entry, final int queryLength) throws IOException {
        entry = entry.newBuilderForType().mergeFrom(entry).setQueryLength(queryLength).build();
        this.appendEntry(entry);
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        if (sortedState) writeIndex();
        writeHeader();

        writeStats();
        IOUtils.closeQuietly(headerOutput);
        entriesChunkWriter.close(collectionBuilder);
        IOUtils.closeQuietly(alignmentEntries);
        IOUtils.closeQuietly(statsWriter);
    }

    private void writeIndex() throws IOException {
        if (!indexWritten) {
            GZIPOutputStream indexOutput = new GZIPOutputStream(new FileOutputStream(basename + ".index"));
            final Alignments.AlignmentIndex.Builder indexBuilder = Alignments.AlignmentIndex.newBuilder();
            assert (indexOffsets.size() == indexAbsolutePositions.size()) : "index sizes must be consistent.";
            indexBuilder.addAllOffsets(indexOffsets);
            indexBuilder.addAllAbsolutePositions(indexAbsolutePositions);
            indexBuilder.build().writeTo(indexOutput);
            indexOutput.close();
            indexWritten = true;
        }
    }

    private void writeHeader() throws IOException {
        if (!headerWritten) {
            final Alignments.AlignmentHeader.Builder headerBuilder = Alignments.AlignmentHeader.newBuilder();

            headerBuilder.setLargestSplitQueryIndex(maxQueryIndex);
            headerBuilder.setSmallestSplitQueryIndex(minQueryIndex);
            headerBuilder.setNumberOfTargets(maxTargetIndex + 1);
            headerBuilder.setNumberOfQueries(getNumQueries());
            headerBuilder.setSorted(sortedState);
            // The Java implementation always indexes an index written in sorted order.
            headerBuilder.setIndexed(sortedState);

            headerBuilder.setQueryNameMapping(getMapping(queryIdentifiers, queryIdentifiersArray));
            headerBuilder.setTargetNameMapping(getMapping(targetIdentifiers, targetIdentifiersArray));
            headerBuilder.setNumberOfAlignedReads(numberOfAlignedReads);

            // store query lengths:
            compactQueryLengths(queryLengths);
            if (isConstantQueryLength) {
                headerBuilder.setConstantQueryLength(constantQueryLength);
            } else if (queryLengths != null) {
                headerBuilder.addAllQueryLength(IntArrayList.wrap(queryLengths));
            }
            // store target lengths:
            if (targetLengths != null) {
                headerBuilder.addAllTargetLength(IntArrayList.wrap(targetLengths));
            }

            headerBuilder.build().writeTo(headerOutput);
            headerWritten = true;
        }
    }

    private void writeStats() throws IOException {
        if (!statsWritten) {
            stats.put("basename", FilenameUtils.getBaseName(basename));
            stats.put("min.query.index", Integer.toString(minQueryIndex));
            stats.put("max.query.index", Integer.toString(maxQueryIndex));
            stats.put("number.of.queries", Integer.toString(getNumQueries()));

            stats.put("basename.full", basename);
            stats.put("number.aligned.reads", Integer.toString(numberOfAlignedReads));
            stats.store(statsWriter, "Statistics for merged alignment. ");

            statsWritten = true;
        }
    }

    /**
     * Provide query identifiers as an array of strings, where queryIndex is the index of the
     * element in the array.
     *
     * @param queryIdentifiersArray Array of query identfiers
     */
    public void setQueryIdentifiersArray(final String[] queryIdentifiersArray) {
        this.queryIdentifiersArray = queryIdentifiersArray;
        maxQueryIndex = queryIdentifiersArray.length - 1;
    }

    /**
     * Provide target identifiers as an array of string, where targetIndex is the index of the
     * element in the array.
     *
     * @param targetIdentifiersArray Array of target identfiers
     */
    public void setTargetIdentifiersArray(final String[] targetIdentifiersArray) {
        this.targetIdentifiersArray = targetIdentifiersArray;
        maxTargetIndex = targetIdentifiersArray.length - 1;
    }

    private Alignments.IdentifierMapping getMapping(final IndexedIdentifier identifiers,
                                                    final String[] targetIdentifiersArray) {
        final Alignments.IdentifierMapping.Builder result = Alignments.IdentifierMapping.newBuilder();

        final ObjectList<Alignments.IdentifierInfo> mappings =
                new ObjectArrayList<Alignments.IdentifierInfo>();

        if (targetIdentifiersArray == null) {
            for (final MutableString id : identifiers.keySet()) {

                mappings.add(Alignments.IdentifierInfo.newBuilder()
                        .setName(id.toString())
                        .setIndex(identifiers.get(id))
                        .build());

            }
        } else {
            // prefer to use the array when available, for speed:

            for (int index = 0; index < targetIdentifiersArray.length; index++) {
                mappings.add(Alignments.IdentifierInfo.newBuilder()
                        .setName(targetIdentifiersArray[index])
                        .setIndex(index)
                        .build());

            }

        }

        result.addAllMappings(mappings);
        return result.build();
    }

    public void printStats(final PrintStream out) {
        this.entriesChunkWriter.printStats(out);
        out.println("Min query index: " + minQueryIndex);
        out.println("Max query index: " + maxQueryIndex);
        out.println("Number of queries: " + getNumQueries());
        out.println("Number of targets: " + (maxTargetIndex + 1));
    }


    public void setQueryIdentifiers(final IndexedIdentifier queryIdentifiers) {
        this.queryIdentifiers = queryIdentifiers;
        for (final int index : queryIdentifiers.values()) {
            maxQueryIndex = Math.max(maxQueryIndex, index);
        }
    }

    public void setTargetIdentifiers(final IndexedIdentifier targetIdentifiers) {
        this.targetIdentifiers = targetIdentifiers;
        for (final int index : targetIdentifiers.values()) {
            maxTargetIndex = Math.max(maxTargetIndex, index);
        }
    }

    /*  public void setQueryLengths(final int[] queryLengths) {
        assert queryLengths.length > maxQueryIndex :
                "The number of elements of queryLength is too small to accomodate queryIndex=" + maxQueryIndex;
        compactQueryLengths(queryLengths);
        if (!isConstantQueryLength) {
            this.queryLengths = queryLengths;
        }
    }*/
    /* public void setQueryLengths(final AlignmentReader reader) {
           if (reader.isConstantQueryLengths()) {
               setQueryLengths(new int[1]);
           }
       }
    */
    /**
     * Replace queryLength with constantQueryLength where the length is the same for all queries.
     * Use a smaller queryLength array if we are storing just a slice of a larger alignment. In this
     * case, the smaller array has size (maxQueryIndex-minQueryIndex+1)
     *
     * @param queryLengths An array of integers where each element indicates the length of a query
     */
    private void compactQueryLengths(final int[] queryLengths) {
        if (queryLengths == null) {
            return;
        }
        final IntSet uniqueLengths = new IntOpenHashSet();
        for (final int length : queryLengths) {
            if (length != 0) {
                uniqueLengths.add(length);

            }
        }
        if (uniqueLengths.size() == 1) {
            // detected constant read length.
            constantQueryLength = uniqueLengths.iterator().nextInt();
            isConstantQueryLength = true;
            this.queryLengths = null;
        } else {
            if (actualNumberOfQueries != Integer.MIN_VALUE) {
                // we know how many queries we are dealing with
                int smallerLength = maxQueryIndex - minQueryIndex + 1;
                if (smallerLength != getNumQueries()) {
                    int[] smaller = new int[smallerLength];
                    System.arraycopy(queryLengths, minQueryIndex, smaller, 0, smallerLength);
                    this.queryLengths = smaller;
                }
            } else {
                this.queryLengths = queryLengths;
            }
        }
    }

    public void setTargetLengths(final int[] targetLengths) {
        assert targetLengths != null : "Target lengths cannot be null.";
        assert targetLengths.length > maxTargetIndex
                : "The number of elements of targetLength is too small to accomodate targetIndex="
                + maxTargetIndex;

        // calculate the coding offset for each target index. This information will be used by recode
        targetPositionOffsets = new long[targetLengths.length];
        for (int targetIndex = 0; targetIndex < targetLengths.length; targetIndex++) {
            targetPositionOffsets[targetIndex] += targetLengths[targetIndex];
            targetPositionOffsets[targetIndex] += targetIndex < 1 ? 0 : targetPositionOffsets[targetIndex - 1];
        }

        this.targetLengths = targetLengths;
    }

    /**
     * Set the total number of queries.
     *
     * @param numQueries The number of query sequences.
     */
    public void setNumQueries(final int numQueries) {
        actualNumberOfQueries = numQueries;
        maxQueryIndex = numQueries - 1;
    }

    /**
     * Get the total number of queries.
     *
     * @return The number of query sequences.
     */
    public int getNumQueries() {
        if (actualNumberOfQueries != Integer.MIN_VALUE) {
            return actualNumberOfQueries;
        } else {
            if (minQueryIndex == Integer.MAX_VALUE) {
                minQueryIndex = 0;
            }
            return maxQueryIndex - minQueryIndex + 1;
        }
    }

    /**
     * Set the total number of targets.
     *
     * @param numTargets The number of target sequences.
     */
    public void setNumTargets(final int numTargets) {
        maxTargetIndex = numTargets - 1;
    }

    public void putStatistic(final String description, final String value) {
        statsWritten = false;
        stats.put(description, value);
    }

    public void putStatistic(final String description, final double value) {
        putStatistic(description, String.format("%3.3g", value));
    }

    public void putStatistic(final String description, final int value) {
        putStatistic(description, String.format("%d", value));
    }

    public void setStatistics(final Properties statistics) {
        for (final Map.Entry<Object, Object> statistic : statistics.entrySet()) {
            putStatistic(statistic.getKey().toString(), statistic.getValue().toString());
        }
    }

    public void setSmallestSplitQueryIndex(int smallestQueryIndex) {
        minQueryIndex = smallestQueryIndex;
    }

    public void setLargestSplitQueryIndex(int largestQueryIndex) {
        maxQueryIndex = largestQueryIndex;
    }


    public void setQueryLength(int queryLength) {
        newEntry.setQueryLength(queryLength);
    }

    public void setQueryLengths(int[] queryLengths) {
        assert queryLengths.length > maxQueryIndex :
                "The number of elements of queryLength is too small to accomodate queryIndex=" + maxQueryIndex;
        this.queryLengths = queryLengths;
    }
}
