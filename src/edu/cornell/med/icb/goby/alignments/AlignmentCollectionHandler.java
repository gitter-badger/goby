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

package edu.cornell.med.icb.goby.alignments;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import edu.cornell.med.icb.goby.algorithmic.compression.FastArithmeticCoder;
import edu.cornell.med.icb.goby.algorithmic.compression.FastArithmeticDecoder;
import edu.cornell.med.icb.goby.reads.ProtobuffCollectionHandler;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * A handler for collections that contain alignment entries.
 *
 * @author Fabien Campagne
 *         Date: 3/3/12
 *         Time: 11:45 AM
 */
public class AlignmentCollectionHandler implements ProtobuffCollectionHandler {
    /**
     * Used to log informational and debug messages.
     */
    private static final Log LOG = LogFactory.getLog(AlignmentCollectionHandler.class);


    private int previousPosition;
    private int previousTargetIndex;
    int deltaPosIndex = 0;
    private static final int NO_VALUE = 0;
    private final int debug = 1;
    /**
     * This variable keeps track of the number of chunks compressed or decompressed.
     */
    private int chunkIndex = 0;
    //Two types of encoding currently supported for query indices:
    private static final int DELTA_ENCODING_SCHEME = 0;
    private static final int MINIMAL_BINARY_ENCODING_SCHEME = 1;
    private static final int MISSING_VALUE = -1;
    private boolean multiplicityFieldsAllMissing = true;
    private long writtenBits;
    private long writtenBases;

    @Override
    public int getType() {
        return TYPE_ALIGNMENTS;
    }

    @Override
    public GeneratedMessage parse(final InputStream uncompressedStream) throws IOException {
        return Alignments.AlignmentCollection.parseFrom(uncompressedStream);
    }

    int numChunksProcessed = 0;

    @Override
    public Message compressCollection(final Message collection, final ByteArrayOutputStream compressedBits) throws IOException {
        reset();

        final Alignments.AlignmentCollection alignmentCollection = (Alignments.AlignmentCollection) collection;
        final Alignments.AlignmentCollection.Builder remainingCollection = Alignments.AlignmentCollection.newBuilder();
        final int size = alignmentCollection.getAlignmentEntriesCount();
        int indexInReducedCollection = 0;
        for (int index = 0; index < size; index++) {
            final Alignments.AlignmentEntry entry = alignmentCollection.getAlignmentEntries(index);

            final Alignments.AlignmentEntry transformed = transform(index, indexInReducedCollection, entry);
            if (transformed != null) {
                remainingCollection.addAlignmentEntries(transformed);
                indexInReducedCollection++;
                //          System.out.println("not a duplicate");
            } else {

            }
        }
        final OutputBitStream outputBitStream = new OutputBitStream(compressedBits);

        writeCompressed(outputBitStream);
        outputBitStream.flush();
        writtenBits += outputBitStream.writtenBits();
        if (numChunksProcessed++ % 200 == 0) {
            displayStats();
        }
        ++chunkIndex;
        return remainingCollection.build();
    }

    @Override
    public Message decompressCollection(Message reducedCollection, byte[] compressedBytes) throws IOException {
        reset();
        //TODO optimize away the copy:
        byte[] moreRoom = new byte[compressedBytes.length + 100];
        System.arraycopy(compressedBytes, 0, moreRoom, 0, compressedBytes.length);

        final Alignments.AlignmentCollection alignmentCollection = (Alignments.AlignmentCollection) reducedCollection;
        final Alignments.AlignmentCollection.Builder result = Alignments.AlignmentCollection.newBuilder();
        final InputBitStream bitInput = new InputBitStream(new FastByteArrayInputStream(moreRoom));
        final int numEntriesInChunk = alignmentCollection.getAlignmentEntriesCount();

        decompressBits(bitInput, numEntriesInChunk);
        int originalIndex = 0;
        for (int templateIndex = 0; templateIndex < numEntriesInChunk; templateIndex++) {
            final int templatePositionIndex = varPositionIndex;
            final int templateVarFromToIndex = varFromToIndex;
            while (multiplicities.get(templateIndex) >= 1) {
                result.addAlignmentEntries(
                        andBack(templateIndex, originalIndex, alignmentCollection.getAlignmentEntries(templateIndex)));
                if (multiplicities.get(templateIndex) >= 1) {
                    // go back to the indices for the template:
                    varPositionIndex = templatePositionIndex;
                    varFromToIndex = templateVarFromToIndex;
                }
                originalIndex++;
            }
        }
        ++chunkIndex;
        return result.build();
    }


    public void displayStats() {
        if (debug(1)) {
            for (String label : typeToNumEntries.keySet()) {
                int n = typeToNumEntries.getInt(label);
                long written = typeToWrittenBits.getLong(label);
                double average = (double) written / (double) n;
                LOG.info
                        (String.format("encoded %d %s in %d bits, average %g bits /element. ", n, label,
                                written, average));
            }
            LOG.info(String.format("entries aggregated with multiplicity= %d", countAggregatedWithMultiplicity));
            LOG.info(String.format("Overall: bits per read bases= %g", divide(writtenBits, writtenBases)));

        }
    }

    private double divide(long a, long b) {
        return ((double) a / (double) b);
    }

    protected final boolean debug(int level) {
        return debug >= level;
    }


    private void writeInts(final String label, final IntList list, final OutputBitStream out) throws IOException {
        final long writtenStart = out.writtenBits();

        for (final int value : list) {
            out.writeInt(value, 32);
        }
        final long writtenStop = out.writtenBits();
        final long written = writtenStop - writtenStart;
        recordStats(label, list, written);
    }

    private void writeQueryIndices(final String label, final IntList list, final OutputBitStream out) throws IOException {
        boolean success = tryWriteDeltas(label, list, out);
        if (success) {
            return;
        } else {
            out.writeDelta(MINIMAL_BINARY_ENCODING_SCHEME);
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        final int size = list.size();
        //   System.out.printf("encoding, chunk=%d delta-positions.size=%d%n", chunkIndex, deltaPositions.size());
        for (final int value : list) {

            min = Math.min(value, min);
            max = Math.max(value, max);

        }

        out.writeNibble(size);
        if (size == 0) {
            return;
        }
        out.writeNibble(min);
        out.writeNibble(max);
        // add one to each value, since we cannot write zeroes in minimal binary.

        final long writtenStart = out.writtenBits();
        final int b = max - min + 1;
        final int log2b = Fast.mostSignificantBit(b);
        for (final int value : list) {

            out.writeMinimalBinary(value - min, b, log2b);
            // out.writeLongMinimalBinary(value-min, max-min+1);
        }
        if (debug(1)) {
            //   out.flush();
            final long writtenStop = out.writtenBits();
            final long written = writtenStop - writtenStart;
            recordStats(label, list, written);
        }
    }

    /**
     * Try to write query indices as delta. If the number of unique deltas is larger than 10% of list size, do not
     * write anything and return false. Otherwise, write as delta and return true.
     *
     * @param label
     * @param list
     * @param out
     * @return
     * @throws java.io.IOException
     */
    private boolean tryWriteDeltas(String label, IntList list, OutputBitStream out) throws IOException {
        if (list.size() == 0) {
            return false;
        }
        final IntArrayList deltas = new IntArrayList();
        int first = list.getInt(0);
        // write the first value as is:
        int previous = first;
        int index = 0;
        for (int value : list) {
            if (index > 0) {
                deltas.add(Fast.int2nat(value - previous));
                previous = value;
            }
            ++index;
        }

        final IntAVLTreeSet tokens = getTokens(deltas);
        //   System.out.printf("tokenSize=%d listSize=%d%n", tokens.size(), list.size());
        if (divide(tokens.size(), list.size()) > 0.2f) {
            return false;
        } else {
            //     System.out.println("Using delta encoding scheme");
            out.writeDelta(DELTA_ENCODING_SCHEME);
            out.writeNibble(first);
            writeArithmetic(label, deltas, out);
            return true;
        }

    }

    private float divide(int a, int b) {
        return ((float) a) / ((float) b);
    }

    private void decodeQueryIndices(final String label, final int numEntriesInChunk, final InputBitStream bitInput, final IntList list) throws IOException {
        switch (bitInput.readDelta()) {
            case MINIMAL_BINARY_ENCODING_SCHEME:
                readMinimalUnary(label, numEntriesInChunk, bitInput, list);
                break;
            case DELTA_ENCODING_SCHEME:
                readAsDeltas(label, numEntriesInChunk, bitInput, list);
                break;
        }
    }

    private void readAsDeltas(String label, int numEntriesInChunk, InputBitStream bitInput, IntList list) throws IOException {
        IntArrayList deltas = new IntArrayList();
        int previous = bitInput.readNibble();
        list.add(previous);
        decodeArithmetic(label, numEntriesInChunk - 1, bitInput, deltas);

        for (int delta : deltas) {
            final int newValue = Fast.nat2int(delta) + previous;
            list.add(newValue);
            previous = newValue;
        }

    }

    private void readMinimalUnary(final String label, final int numEntriesInChunk, final InputBitStream bitInput, final IntList list) throws IOException {
        final int size = bitInput.readNibble();
        if (size == 0) {
            return;
        }
        final int min = bitInput.readNibble();
        final int max = bitInput.readNibble();

        for (int i = 0; i < size; i++) {
            final int reducedReadIndex = bitInput.readMinimalBinary(max - min + 1);
            list.add(reducedReadIndex + min);
        }

    }

    private void writeNibble(String label, IntList list, OutputBitStream out) throws IOException {
        long writtenStart = out.writtenBits();
        for (int value : list) {
            out.writeNibble(value);
        }
        long writtenStop = out.writtenBits();
        long written = writtenStop - writtenStart;
        recordStats(label, list, written);
    }

    Object2IntMap<String> typeToNumEntries = new Object2IntAVLTreeMap<String>();
    Object2LongMap<String> typeToWrittenBits = new Object2LongAVLTreeMap<String>();

    protected final void decodeArithmetic(final String label, final int numEntriesInChunk, final InputBitStream bitInput, final IntList list) throws IOException {

        if (debug(2)) {
            System.err.flush();
            System.err.println("\nreading " + label + " with available=" + bitInput.available());
            System.err.flush();
        }
        if (numEntriesInChunk == 0) {
            return;
        }
        // TODO see if we can avoid reading the number of elements in some cases.
        final int size = bitInput.readNibble();
        if (size == 0) {
            return;
        }
        final int numTokens = bitInput.readNibble();
        final int[] distinctvalue = new int[numTokens];
        for (int i = 0; i < numTokens; i++) {
            // -1 makes 0 symbol -1 (missing value) again
            distinctvalue[i] = bitInput.readNibble() - 1;
        }

        final FastArithmeticDecoder decoder = new FastArithmeticDecoder(numTokens);
        for (int i = 0; i < size; i++) {
            final int tokenValue = distinctvalue[decoder.decode(bitInput)];
            list.add(tokenValue);
        }
        decoder.reposition(bitInput);

    }

    protected final void writeArithmetic(final String label, final IntList list, OutputBitStream out) throws IOException {
        if (debug(2)) {
            System.err.flush();
            System.err.println("\nwriting " + label);
            System.err.flush();
        }
        final long writtenStart = out.writtenBits();
        out.writeNibble(list.size());
        if (list.isEmpty()) {
            // no list to write.
            return;
        }
        final IntAVLTreeSet distinctSymbols = getTokens(list);

        final int[] symbolValues = distinctSymbols.toIntArray();
        out.writeNibble(distinctSymbols.size());
        for (final int token : distinctSymbols) {
            // +1 makes -1 (missing value) symbol 0 so it can be written Nibble:
            out.writeNibble(token + 1);
        }

        final FastArithmeticCoder coder = new FastArithmeticCoder(distinctSymbols.size());
        for (final int dp : list) {
            final int symbolCode = Arrays.binarySearch(symbolValues, dp);
            assert symbolCode >= 0 : "symbol code must exist.";
            coder.encode(symbolCode, out);
        }
        coder.flush(out);
        if (debug(1)) {
            System.err.flush();
            final long writtenStop = out.writtenBits();
            final long written = writtenStop - writtenStart;
            recordStats(label, list, written);
        }
    }

    private void recordStats(String label, IntList list, long written) {
        if (debug(1)) {
            double average = ((double) written) / list.size();
            typeToNumEntries.put(label, list.size() + typeToNumEntries.getInt(label));
            typeToWrittenBits.put(label, written + typeToWrittenBits.getLong(label));
        }
    }

    private IntAVLTreeSet getTokens(IntList list) {
        IntAVLTreeSet result = new IntAVLTreeSet();
        for (int value : list) {
            result.add(value);
        }
        return result;
    }


    private IntList deltaPositions = new IntArrayList();
    private IntList deltaTargetIndices = new IntArrayList();
    private IntList queryLengths = new IntArrayList();
    private IntList mappingQualities = new IntArrayList();
    private IntList matchingReverseStrand = new IntArrayList();
    private IntList multiplicity = new IntArrayList();
    private IntList numberOfIndels = new IntArrayList();
    private IntList numberOfMismatches = new IntArrayList();
    private IntList queryAlignedLengths = new IntArrayList();
    private IntList targetAlignedLengths = new IntArrayList();
    private IntList queryIndices = new IntArrayList();
    private IntList queryPositions = new IntArrayList();
    private IntList fragmentIndices = new IntArrayList();
    private IntList variationCount = new IntArrayList();

    private IntList fromLengths = new IntArrayList();
    private IntList toLengths = new IntArrayList();
    private IntList varPositions = new IntArrayList();
    private IntList varReadIndex = new IntArrayList();
    private IntList varFromTo = new IntArrayList();
    private IntList varQuals = new IntArrayList();
    private IntList varHasToQuals = new IntArrayList();

    IntArrayList multiplicities = new IntArrayList();


    private void decompressBits(InputBitStream bitInput, final int numEntriesInChunk) throws IOException {
        multiplicityFieldsAllMissing = bitInput.readBit() == 1;

        decodeArithmetic("deltaPositions", numEntriesInChunk, bitInput, deltaPositions);
        decodeArithmetic("deltaTargetIndices", numEntriesInChunk, bitInput, deltaTargetIndices);
        decodeArithmetic("queryLengths", numEntriesInChunk, bitInput, queryLengths);
        decodeArithmetic("mappingQualities", numEntriesInChunk, bitInput, mappingQualities);
        decodeArithmetic("matchingReverseStrand", numEntriesInChunk, bitInput, matchingReverseStrand);
        decodeArithmetic("numberOfIndels", numEntriesInChunk, bitInput, numberOfIndels);
        decodeArithmetic("numberOfMismatches", numEntriesInChunk, bitInput, numberOfMismatches);
        decodeArithmetic("queryAlignedLength", numEntriesInChunk, bitInput, queryAlignedLengths);
        decodeArithmetic("targetAlignedLength", numEntriesInChunk, bitInput, targetAlignedLengths);
        decodeArithmetic("queryPositions", numEntriesInChunk, bitInput, queryPositions);
        decodeArithmetic("fragmentIndex", numEntriesInChunk, bitInput, fragmentIndices);
        decodeArithmetic("variationCount", numEntriesInChunk, bitInput, variationCount);
        decodeArithmetic("varPositions", numEntriesInChunk, bitInput, varPositions);
        decodeArithmetic("fromLengths", numEntriesInChunk, bitInput, fromLengths);
        decodeArithmetic("toLengths", numEntriesInChunk, bitInput, toLengths);
        decodeArithmetic("varReadIndex", numEntriesInChunk, bitInput, varReadIndex);
        decodeArithmetic("varFromTo", numEntriesInChunk, bitInput, varFromTo);
        decodeArithmetic("varQuals", numEntriesInChunk, bitInput, varQuals);
        decodeArithmetic("varHasToQuals", numEntriesInChunk, bitInput, varHasToQuals);
        decodeArithmetic("multiplicities", numEntriesInChunk, bitInput, multiplicities);
        pairLinks.read(numEntriesInChunk, bitInput);
        forwardSpliceLinks.read(numEntriesInChunk, bitInput);
        backwardSpliceLinks.read(numEntriesInChunk, bitInput);

        decodeQueryIndices("queryIndices", numEntriesInChunk, bitInput, queryIndices);
    }

    private void writeCompressed(final OutputBitStream out) throws IOException {
        //   out.writeNibble(0);

        out.writeBit(multiplicityFieldsAllMissing);

        writeArithmetic("positions", deltaPositions, out);
        writeArithmetic("targets", deltaTargetIndices, out);
        writeArithmetic("queryLengths", queryLengths, out);
        writeArithmetic("mappingQualities", mappingQualities, out);
        writeArithmetic("matchingReverseStrand", matchingReverseStrand, out);
        writeArithmetic("numberOfIndels", numberOfIndels, out);
        writeArithmetic("numberOfMismatches", numberOfMismatches, out);
        writeArithmetic("queryAlignedLength", queryAlignedLengths, out);
        writeArithmetic("targetAlignedLength", targetAlignedLengths, out);
        writeArithmetic("queryPositions", queryPositions, out);
        writeArithmetic("fragmentIndex", fragmentIndices, out);
        writeArithmetic("variationCount", variationCount, out);
        writeArithmetic("varPositions", varPositions, out);
        writeArithmetic("fromLengths", fromLengths, out);
        writeArithmetic("toLengths", toLengths, out);
        writeArithmetic("varReadIndex", varReadIndex, out);
        writeArithmetic("varFromTo", varFromTo, out);
        writeArithmetic("varQuals", varQuals, out);
        writeArithmetic("varHasToQuals", varHasToQuals, out);
        writeArithmetic("multiplicities", multiplicities, out);
        pairLinks.write(out);
        forwardSpliceLinks.write(out);
        backwardSpliceLinks.write(out);
        writeQueryIndices("queryIndices", queryIndices, out);

    }

    private void reset() {
        multiplicityFieldsAllMissing = true;

        previousPosition = -1;
        previousTargetIndex = -1;
        deltaPositions.clear();
        deltaTargetIndices.clear();
        queryLengths.clear();
        mappingQualities.clear();
        matchingReverseStrand.clear();
        multiplicity.clear();
        numberOfIndels.clear();
        queryAlignedLengths.clear();
        targetAlignedLengths.clear();
        numberOfMismatches.clear();
        queryIndices.clear();
        queryPositions.clear();
        fragmentIndices.clear();
        queryIndices.clear();
        variationCount.clear();
        varPositions.clear();
        fromLengths.clear();
        toLengths.clear();
        varReadIndex.clear();
        varFromTo.clear();
        varQuals.clear();
        varQualIndex = 0;
        varPositionIndex = 0;
        varFromToIndex = 0;
        varHasToQuals.clear();

        multiplicities.clear();
        countAggregatedWithMultiplicity = 0;
        previousPartial = null;
        deltaPosIndex = 0;
        pairLinks.reset();
        forwardSpliceLinks.reset();
        backwardSpliceLinks.reset();
    }

    private final LinkInfo pairLinks = new LinkInfo(this, "pairs");
    private final LinkInfo forwardSpliceLinks = new LinkInfo(this, "forward-splice");
    private final LinkInfo backwardSpliceLinks = new LinkInfo(this, "backward-splice");

    /**
     * An empty sequence variation.
     */
    private final Alignments.SequenceVariation EMPTY_SEQ_VAR = Alignments.SequenceVariation.newBuilder().build();
    private Alignments.AlignmentEntry previousPartial;
    private int countAggregatedWithMultiplicity;

    private Alignments.AlignmentEntry transform(final int index, int indexInReducedCollection, final Alignments.AlignmentEntry source) {
        final Alignments.AlignmentEntry.Builder result = Alignments.AlignmentEntry.newBuilder(source);
        final int position = source.getPosition();
        final int targetIndex = source.getTargetIndex();

        if (index > 0 && targetIndex == previousTargetIndex) {
            result.clearPosition();
            result.clearTargetIndex();

            deltaPositions.add(position - previousPosition);
            deltaTargetIndices.add(targetIndex - previousTargetIndex);
        }

        final int queryIndex = source.getQueryIndex();
        queryIndices.add(queryIndex);

        previousPosition = position;
        previousTargetIndex = targetIndex;
        if (debug(1) && source.hasQueryLength()) {
            writtenBases += source.getQueryLength();
        }
        result.clearQueryIndex();

        recordVariationQualitiesAndClear(result, result.getSequenceVariationsList());

        Alignments.RelatedAlignmentEntry link = pairLinks.code(source.hasPairAlignmentLink(), source.getPairAlignmentLink());
        if (link == null) {
            result.clearPairAlignmentLink();
        } else {
            result.setPairAlignmentLink(link);
        }

        link = forwardSpliceLinks.code(source.hasSplicedForwardAlignmentLink(), source.getSplicedForwardAlignmentLink());
        if (link == null) {
            result.clearSplicedForwardAlignmentLink();
        } else {
            result.setSplicedForwardAlignmentLink(link);
        }

        link = backwardSpliceLinks.code(source.hasSplicedBackwardAlignmentLink(), source.getSplicedBackwardAlignmentLink());
        if (link == null) {
            result.clearSplicedBackwardAlignmentLink();
        } else {
            result.setSplicedBackwardAlignmentLink(link);
        }
        final Alignments.AlignmentEntry partial = result.clone().build();

        if (previousPartial != null && indexInReducedCollection >= 1 && previousPartial.equals(partial)) {
            //   System.out.println("same");
            //  print(partial);
            int m = multiplicities.get(indexInReducedCollection - 1);
            multiplicities.set(indexInReducedCollection - 1, m + 1);
            // do not add this one, we just increased the multiplicity of the previous one.
            countAggregatedWithMultiplicity++;
            //      System.out.printf("Returning for template match to previous, current queryIndex=%d%n",queryIndex);
            return null;
        } else {
            previousPartial = partial;
            multiplicityFieldsAllMissing &= !source.hasMultiplicity();
            multiplicities.add(Math.max(1, source.getMultiplicity()));
        }
        //System.out.printf("encoding query-index=%d varPositionIndex=%d %n",queryIndex, varPositionIndex);

        queryLengths.add(source.hasQueryLength() ? source.getQueryLength() : MISSING_VALUE);
        mappingQualities.add(source.hasMappingQuality() ? source.getMappingQuality() : MISSING_VALUE);
        matchingReverseStrand.add(source.hasMatchingReverseStrand() ? source.getMatchingReverseStrand() ? 1 : 0 : MISSING_VALUE);
        numberOfIndels.add(source.hasNumberOfIndels() ? source.getNumberOfIndels() : MISSING_VALUE);
        numberOfMismatches.add(source.hasNumberOfMismatches() ? source.getNumberOfMismatches() : MISSING_VALUE);
        queryAlignedLengths.add(source.hasQueryAlignedLength() ? source.getQueryAlignedLength() : MISSING_VALUE);
        targetAlignedLengths.add(source.hasTargetAlignedLength() ? source.getTargetAlignedLength() : MISSING_VALUE);
        fragmentIndices.add(source.hasFragmentIndex() ? source.getFragmentIndex() : MISSING_VALUE);
        variationCount.add(source.getSequenceVariationsCount());
        queryPositions.add(source.hasQueryPosition() ? source.getQueryPosition() : MISSING_VALUE);

        result.clearQueryLength();
        result.clearMappingQuality();
        result.clearMatchingReverseStrand();
        result.clearMultiplicity();
        result.clearNumberOfIndels();
        result.clearNumberOfMismatches();
        result.clearQueryAlignedLength();
        result.clearTargetAlignedLength();

        result.clearQueryPosition();
        result.clearFragmentIndex();


        boolean canFullyRemoveThisOne = true;
        boolean canFullyRemoveCollection = true;
        int seqVarIndex = 0;

        for (final Alignments.SequenceVariation seqVar : result.getSequenceVariationsList()) {

            encodeVar(seqVar);
            Alignments.SequenceVariation.Builder varBuilder = Alignments.SequenceVariation.newBuilder(seqVar);
            varBuilder.clearPosition();
            varBuilder.clearFrom();
            varBuilder.clearTo();
            varBuilder.clearToQuality();
            varBuilder.clearReadIndex();
            if (!EMPTY_SEQ_VAR.equals(varBuilder.build())) {
                canFullyRemoveThisOne = false;
                canFullyRemoveCollection = false;
            }
            if (canFullyRemoveThisOne) {
                result.removeSequenceVariations(seqVarIndex);
                seqVarIndex--;
            }
            seqVarIndex++;
        }
        if (canFullyRemoveCollection) {
            result.clearSequenceVariations();
        }
        //     print(result);


        final Alignments.AlignmentEntry alignmentEntry = result.build();
        //    System.out.println(alignmentEntry);
        return alignmentEntry;
    }


    private void recordVariationQualitiesAndClear(Alignments.AlignmentEntry.Builder result, List<Alignments.SequenceVariation> sequenceVariationsList) {

        int index = 0;
        for (final Alignments.SequenceVariation seqVar : sequenceVariationsList) {
            final String from = seqVar.getFrom();

            final ByteString toQualities = seqVar.getToQuality();
            final int length = from.length();
            final boolean hasToQuals = seqVar.hasToQuality();
            varHasToQuals.add(hasToQuals ? 1 : 0);
            for (int i = 0; i < length; i++) {
                if (hasToQuals && i < toQualities.size()) {
                    varQuals.add(toQualities.byteAt(i));
                } else {
                    varQuals.add(NO_VALUE);
                }
            }
            Alignments.SequenceVariation.Builder varBuilder = Alignments.SequenceVariation.newBuilder(seqVar);
            varBuilder.clearToQuality();
            result.setSequenceVariations(index, varBuilder.buildPartial());
            index++;
        }
    }

    private void print(Alignments.AlignmentEntry result) {

        System.out.println(result);
    }

    private void encodeVar(final Alignments.SequenceVariation seqVar) {

        final String from = seqVar.getFrom();
        final String to = seqVar.getTo();
        final ByteString toQualities = seqVar.getToQuality();
        final int fromLength = from.length();
        final int toLength = to.length();
        final boolean hasToQuals = seqVar.hasToQuality();
        varPositions.add(seqVar.getPosition());
        varPositionIndex++;
        varReadIndex.add(seqVar.getReadIndex());
        fromLengths.add(fromLength);
        toLengths.add(toLength);
        final int maxLength = Math.max(fromLength, toLength);
        for (int i = 0; i < maxLength; i++) {

            final char baseFrom = i < fromLength ? from.charAt(i) : '\0';
            final char baseTo = i < toLength ? to.charAt(i) : '\0';
            final byte byteFrom = (byte) baseFrom;
            final byte byteTo = (byte) baseTo;
            varFromTo.add(byteFrom << 8 | byteTo);
            /* if (hasToQuals) {
                varQuals.add(toQualities.byteAt(i));
            } else {
                varQuals.add(NO_VALUE);
            }*/
        }
    }

    MutableString from = new MutableString();
    MutableString to = new MutableString();


    private Alignments.AlignmentEntry andBack(final int index, int originalIndex, final Alignments.AlignmentEntry reduced) {
        final Alignments.AlignmentEntry.Builder result = Alignments.AlignmentEntry.newBuilder(reduced);

        final int multiplicity = multiplicities.get(index);
        final int k = multiplicity - 1;

        multiplicities.set(index, k);
        //if (k > 1) {
        if (!multiplicityFieldsAllMissing) {
            result.setMultiplicity(1);
        }
        final int queryIndex = queryIndices.getInt(originalIndex);
        result.setQueryIndex(queryIndex);
        // System.out.printf("decoding query-index=%d (originalIndex=%d) varPositionIndex=%d %n",queryIndex,originalIndex, varPositionIndex);

        if (originalIndex == 0 || reduced.hasPosition() || reduced.hasTargetIndex()) {
            previousPosition = reduced.getPosition();
            previousTargetIndex = reduced.getTargetIndex();
        } else {


            final int deltaPos = deltaPositions.getInt(deltaPosIndex);
            final int deltaTarget = deltaTargetIndices.getInt(deltaPosIndex);
            final int position = previousPosition + deltaPos;
            final int targetIndex = previousTargetIndex + deltaTarget;
            result.setPosition(position);
            result.setTargetIndex(targetIndex);
            previousPosition += deltaPos;
            previousTargetIndex += deltaTarget;
            deltaPosIndex++;
        }

        int anInt = mappingQualities.getInt(index);
        if (anInt != MISSING_VALUE) {
            result.setMappingQuality(anInt);
        }
        anInt = fragmentIndices.getInt(index);
        if (anInt != MISSING_VALUE) {
            result.setFragmentIndex(anInt);
        }
        anInt = matchingReverseStrand.getInt(index);
        if (anInt != MISSING_VALUE) {
            result.setMatchingReverseStrand(anInt == 1);
        }
        anInt = numberOfMismatches.getInt(index);
        if (anInt != MISSING_VALUE) {
            result.setNumberOfMismatches(anInt);
        }
        anInt = numberOfIndels.getInt(index);
        if (anInt != MISSING_VALUE) {
            result.setNumberOfIndels(anInt);

        }
        anInt = queryLengths.getInt(index);
        if (anInt != MISSING_VALUE) {
            result.setQueryLength(anInt);
        }
        anInt = queryAlignedLengths.getInt(index);
        if (anInt != MISSING_VALUE) {
            result.setQueryAlignedLength(anInt);
        }
        anInt = queryPositions.getInt(index);
        if (anInt != MISSING_VALUE) {
            result.setQueryPosition(anInt);
        }
        anInt = targetAlignedLengths.getInt(index);
        if (anInt != MISSING_VALUE) {
            result.setTargetAlignedLength(anInt);
        }

        Alignments.RelatedAlignmentEntry link = pairLinks.decode(originalIndex, reduced.getPairAlignmentLink());
        if (link != null) {
            result.setPairAlignmentLink(link);
        }
        link = forwardSpliceLinks.decode(originalIndex, reduced.getSplicedForwardAlignmentLink());
        if (link != null) {
            result.setSplicedForwardAlignmentLink(link);
        }
        link = backwardSpliceLinks.decode(originalIndex, reduced.getSplicedBackwardAlignmentLink());
        if (link != null) {
            result.setSplicedBackwardAlignmentLink(link);
        }
        final boolean templateHasSequenceVariations = reduced.getSequenceVariationsCount() > 0;
        final int numVariations = variationCount.getInt(index);
        if (queryIndex==72) {
            System.out.println("STOP");
        }
        for (int varIndex = 0; varIndex < numVariations; varIndex++) {
            final Alignments.SequenceVariation template = templateHasSequenceVariations ? reduced.getSequenceVariations(varIndex) : null;
            final Alignments.SequenceVariation.Builder varBuilder = templateHasSequenceVariations ?
                    Alignments.SequenceVariation.newBuilder(template) : Alignments.SequenceVariation.newBuilder();

            from.setLength(0);
            to.setLength(0);

            final int fromLength = fromLengths.getInt(varPositionIndex);
            final int toLength = toLengths.getInt(varPositionIndex);
            varBuilder.setPosition(varPositions.getInt(varPositionIndex));
            varBuilder.setReadIndex(varReadIndex.getInt(varPositionIndex));
            final boolean hasToQual = varHasToQuals.getInt(varPositionIndex) == 1;
           // TODO optimize away array creation.
            final byte[] quals = hasToQual ? new byte[toLength] : null;
            ++varPositionIndex;
            final int maxLength = Math.max(fromLength, toLength);
            for (int i = 0; i < maxLength; i++) {

                final int fromTo = varFromTo.getInt(varFromToIndex++);
                if (i < fromLength) {
                    from.append((char) (fromTo >> 8));
                }
                if (i < toLength) {
                    to.append((char) (fromTo & 0xFF));
                }
                if (hasToQual) {
                    quals[i] = (byte) varQuals.getInt(varQualIndex);

                    ++varQualIndex;
                }
            }
            varBuilder.setFrom(from.toString());
            varBuilder.setTo(to.toString());
            if (hasToQual) {
                varBuilder.setToQuality(ByteString.copyFrom(quals));
            }
            if (templateHasSequenceVariations) {
                result.setSequenceVariations(varIndex, varBuilder);
            } else {
                result.addSequenceVariations(varBuilder);
            }

        }
        return result.build();
    }

    private int varQualIndex = 0;
    private int varPositionIndex = 0;
    private int varFromToIndex = 0;

}
