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

package edu.cornell.med.icb.goby.modes;

import edu.cornell.med.icb.goby.R.GobyRengine;
import edu.cornell.med.icb.goby.alignments.*;
import edu.cornell.med.icb.goby.stats.*;
import edu.cornell.med.icb.goby.readers.vcf.ColumnType;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rosuda.JRI.Rengine;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.io.PrintWriter;

/**
 * A Variant Call Format output to compare genomic variation across groups
 *
 * @author Fabien Campagne
 *         Date: April 4 2011
 *         Time: 2:38:13 AM
 */
public class BetweenGroupsVCFOutputFormat implements SequenceVariationOutputFormat {

    /**
     * Used to log debug and informational messages.
     */
    private static final Log LOG = LogFactory.getLog(BetweenGroupsVCFOutputFormat.class);

    VCFWriter statWriter;
    private int refIdColumnIndex;
    private int positionColumnIndex;

    private boolean fisherRInstalled;
    private String[] groups;
    private String[] samples;
    private ObjectArrayList<ReadIndexStats> readIndexStats;
    private int log2OddsRatioColumnIndex;
    private int fisherExactPValueColumnIndex;
    private int numberOfGroups;

    private DifferentialExpressionAnalysis deAnalyzer;
    private DifferentialExpressionCalculator deCalculator;

    private int log2OddsRatioStandardErrorColumnIndex;
    private int log2OddsRatioZColumnIndex;
    int[] readerIndexToGroupIndex;
    private int[] refCountsPerGroup;
    private int[] variantsCountPerGroup;
    private int[] distinctReadIndexCountPerGroup;
    private IntSet[] distinctReadIndicesCountPerGroup;
    private float[] averageVariantQualityScorePerGroup;
    private int[] variantsCountPerSample;
    private int[] refCountsPerSample;
    private int numberOfSamples;
    private int biomartFieldIndex;
    private GenotypesOutputFormat genotypeFormatter;
    private int depthFieldIndex;


    public void defineColumns(PrintWriter writer, DiscoverSequenceVariantsMode mode) {
        deAnalyzer = mode.getDiffExpAnalyzer();
        deCalculator = mode.getDiffExpCalculator();
        groups = mode.getGroups();
        samples = mode.getSamples();
        readerIndexToGroupIndex = mode.getReaderIndexToGroupIndex();
        ObjectArrayList<ReadIndexStats> readIndexStats = mode.getReadIndexStats();
        this.statWriter = new VCFWriter(writer);

        //activate R only if we need it:
        final Rengine rEngine = GobyRengine.getInstance().getRengine();
        fisherRInstalled = rEngine != null && rEngine.isAlive();

        if (groups.length != 2) {
            System.err.println("BetweenGroupsVCFOutputFormat requires exactly two groups.");
            System.exit(1);
        }

        this.readIndexStats = readIndexStats;


        log2OddsRatioColumnIndex = -1;
        fisherExactPValueColumnIndex = -1;
        numberOfGroups = groups.length;
        biomartFieldIndex = statWriter.defineField("INFO", "BIOMART_COORDS", 1, ColumnType.String, "Coordinates for use with Biomart.");


        log2OddsRatioColumnIndex = statWriter.defineField("INFO", String.format("LOD[%s/%s]", groups[0], groups[1]),
                1, ColumnType.Float, String.format("Log2 of the odds-ratio between group %s and group %s", groups[0], groups[1]));

        log2OddsRatioStandardErrorColumnIndex = statWriter.defineField("INFO", String.format("LOD_SE[%s/%s]", groups[0], groups[1]),
                1, ColumnType.Float, String.format("Standard Error of the log2 of the odds-ratio between group %s and group %s", groups[0], groups[1]));

        log2OddsRatioZColumnIndex = statWriter.defineField("INFO", String.format("LOD_Z[%s/%s]", groups[0], groups[1]),
                1, ColumnType.Float, String.format("Z value of the odds-ratio between group %s and group %s", groups[0], groups[1]));
        fisherExactPValueColumnIndex = statWriter.defineField("INFO", String.format("FisherP[%s/%s])", groups[0], groups[1]),
                1, ColumnType.Float, String.format("Fisher exact P-value of observing as large a difference by chance between group %s and group %s.", groups[0], groups[1]));

        depthFieldIndex = statWriter.defineField("INFO", "DP",
                1, ColumnType.Integer, "Total depth of sequencing across groups at this site");

        statWriter.defineSamples(samples);
        genotypeFormatter.defineGenotypeField(statWriter);
        statWriter.writeHeader();
    }

    public void allocateStorage(int numberOfSamples, int numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
        this.numberOfSamples = numberOfSamples;
        refCountsPerGroup = new int[numberOfGroups];
        variantsCountPerGroup = new int[numberOfGroups];
        distinctReadIndexCountPerGroup = new int[numberOfGroups];
        averageVariantQualityScorePerGroup = new float[numberOfGroups];

        refCountsPerSample = new int[numberOfSamples];
        variantsCountPerSample = new int[numberOfSamples];
        distinctReadIndicesCountPerGroup = new IntSet[numberOfGroups];
        for (int i = 0; i < numberOfGroups; i++) {
            distinctReadIndicesCountPerGroup[i] = new IntArraySet();
        }
        genotypeFormatter = new GenotypesOutputFormat();
        genotypeFormatter.allocateStorage(numberOfSamples, numberOfGroups);
    }

    public void writeRecord(DiscoverVariantIterateSortedAlignments iterator,
                            SampleCountInfo[] sampleCounts,
                            int referenceIndex,
                            int position,
                            ObjectArrayList<PositionBaseInfo> list,
                            int groupIndexA,
                            int groupIndexB) {

        int totalCount = 0;

        for (int sampleIndex = 0; sampleIndex < numberOfSamples; sampleIndex++) {
            SampleCountInfo sci = sampleCounts[sampleIndex];

            for (int sampleCount : sci.counts) {
                totalCount += sampleCount;
                assert sampleCount >= 0 : "counts must not be negative.";
            }
        }
        if (totalCount == 0) return;

        statWriter.setInfo(depthFieldIndex, totalCount);
        fillVariantCountArrays(sampleCounts);

        CharSequence currentReferenceId = iterator.getReferenceId(referenceIndex);

        statWriter.setChromosome(currentReferenceId);
        statWriter.setPosition(position);
        statWriter.setReferenceAllele(Character.toString(sampleCounts[0].referenceBase));

        // construct a biomart region span in the format chr:pos1:chr:pos
        String biomartRegionSpan = String.format("%s:%s:%s", currentReferenceId, position,
                position);

        statWriter.setInfo(biomartFieldIndex, biomartRegionSpan);

        final double denominator = (double) refCountsPerGroup[groupIndexA] * (double) variantsCountPerGroup[groupIndexB];
        double oddsRatio = denominator == 0 ? Double.NaN :
                ((double) refCountsPerGroup[groupIndexB] * (double) variantsCountPerGroup[groupIndexA]) /
                        denominator;
        double logOddsRatioSE;

        if (variantsCountPerGroup[groupIndexA] < 10 ||
                variantsCountPerGroup[groupIndexB] < 10 ||
                refCountsPerGroup[groupIndexA] < 10 ||
                refCountsPerGroup[groupIndexB] < 10) {
            // standard error estimation is unreliable when any of the counts are less than 10.
            logOddsRatioSE = Double.NaN;
        } else {
            logOddsRatioSE = Math.sqrt(1d / refCountsPerGroup[groupIndexB] +
                    1d / variantsCountPerGroup[groupIndexA] +
                    1d / variantsCountPerGroup[groupIndexB] +
                    1d / refCountsPerGroup[groupIndexA]);
        }
        double log2OddsRatio = Math.log(oddsRatio) / Math.log(2);
        double log2OddsRatioZValue = log2OddsRatio / logOddsRatioSE;

        double fisherP = Double.NaN;

        boolean ok = checkCounts();
        if (ok) {
            fisherP = fisherRInstalled ? FisherExactRCalculator.getFisherPValue(
                    refCountsPerGroup[groupIndexB], variantsCountPerGroup[groupIndexB],
                    refCountsPerGroup[groupIndexA], variantsCountPerGroup[groupIndexA]) : Double.NaN;
        } else {
            System.err.printf("An exception was caught evaluating the Fisher Exact test P-value. Details are provided below%n" +
                    "referenceId=%s referenceIndex=%d position=%d %n" +
                    "refCountsPerGroup[1]=%d variantsCountPerGroup[1]=%d%n" +
                    "refCountsPerGroup[0]=%d, variantsCountPerGroup[0]=%d",
                    currentReferenceId, referenceIndex,
                    position + 1,
                    refCountsPerGroup[groupIndexB], variantsCountPerGroup[groupIndexB],
                    refCountsPerGroup[groupIndexA], variantsCountPerGroup[groupIndexA]
            );
        }
        statWriter.setInfo(log2OddsRatioColumnIndex, log2OddsRatio);
        statWriter.setInfo(log2OddsRatioStandardErrorColumnIndex, logOddsRatioSE);
        statWriter.setInfo(log2OddsRatioZColumnIndex, log2OddsRatioZValue);
        statWriter.setInfo(fisherExactPValueColumnIndex, fisherP);

        genotypeFormatter.writeGenotypes(statWriter, sampleCounts);

        statWriter.writeRecord();
    }


    public void close() {
        statWriter.close();
    }


    private void fillVariantCountArrays(SampleCountInfo[] sampleCounts) {

        Arrays.fill(variantsCountPerGroup, 0);
        Arrays.fill(refCountsPerGroup, 0);
        Arrays.fill(distinctReadIndexCountPerGroup, 0);

        for (int groupIndex = 0; groupIndex < numberOfGroups; groupIndex++) {
            distinctReadIndicesCountPerGroup[groupIndex].clear();
        }
        for (SampleCountInfo csi : sampleCounts) {
            final int sampleIndex = csi.sampleIndex;
            variantsCountPerSample[sampleIndex] = csi.varCount;
            refCountsPerSample[sampleIndex] = csi.refCount;

            int groupIndex = readerIndexToGroupIndex[sampleIndex];
            variantsCountPerGroup[groupIndex] += csi.varCount;
            refCountsPerGroup[groupIndex] += csi.refCount;
            distinctReadIndicesCountPerGroup[groupIndex].addAll(csi.distinctReadIndices);
        }

        for (int groupIndex = 0; groupIndex < numberOfGroups; groupIndex++) {
            distinctReadIndexCountPerGroup[groupIndex] = distinctReadIndicesCountPerGroup[groupIndex].size();
        }
    }

    private boolean checkCounts() {
        boolean ok = true;
        // detect if any count is negative (that's a bug)
        for (int count : refCountsPerGroup) {

            if (count < 0) ok = false;
        }
        for (int count : variantsCountPerGroup) {
            if (count < 0) ok = false;
        }
        return ok;
    }

    private void summarizeVariations
            (TSVWriter
                    statWriter, ObjectArrayList<PositionBaseInfo> list,
             int groupIndex) {

        final Object2IntMap<MutableString> tally = new Object2IntArrayMap<MutableString>();
        tally.defaultReturnValue(0);
        for (PositionBaseInfo info : list) {
            final int varGroupIndex = readerIndexToGroupIndex[info.readerIndex];

            if (!info.matchesReference && varGroupIndex == groupIndex) {
                MutableString variation = new MutableString();
                variation.append(info.from);
                variation.append('/');
                variation.append(info.to);
                int count = tally.getInt(variation);
                tally.put(variation, count + 1);
            }
        }
        // sort variations by decreasing tally:
        ObjectSet<MutableString> varSet = (tally.keySet());
        ObjectList<MutableString> varList = new ObjectArrayList<MutableString>();
        varList.addAll(varSet);
        Collections.sort(varList, new Comparator<MutableString>() {
            public int compare(MutableString variationA, MutableString variationB) {
                // sort by decreasing tally:
                return tally.getInt(variationB) - tally.getInt(variationA);
            }
        });

        StringBuffer buffer = new StringBuffer();
        for (MutableString variation : varList) {
            buffer.append(String.format("%d:%s,", tally.getInt(variation), variation));
        }
        statWriter.setValue(buffer.toString(),
                "observed variations at position ([frequency:from/to,]+) group %s", groups[groupIndex]);
    }

    private double estimateWithinGroupDiscoveryPalue
            (
                    int position,
                    int groupIndex,
                    ObjectArrayList<PositionBaseInfo> list,
                    int[] variantsCount,
                    int[] refCounts) {
        double pValue = 1;
        if (!deAnalyzer.eval("within-groups")) return Double.NaN;
        int observedReferenceCount = 0;
        double expectedVariationRate = 0;
        int expectedVariationCount = 0;
        int expectedReferenceCount = 0;
        int observedVariationCount = 0;
        observedReferenceCount = refCounts[groupIndex];
        observedVariationCount = variantsCount[groupIndex];
        if (observedReferenceCount + observedVariationCount == 0) {
            // cannot call a variant if we do not observe this position in this group at all.
            return 1;
        }
        long sum = 0;
        for (PositionBaseInfo info : list) {
            final ReadIndexStats stats = readIndexStats.get(info.readerIndex);
            final int readIndex = info.readIndex;
            if (readIndex < 1 ||
                    readIndex > stats.countVariationBases.length ||
                    readIndex > stats.countReferenceBases.length) {
                // TODO this test is a kludge: readIndex should always be within the bounds of countVariationBases or countReferenceBases
                // the fact that we need this test indicates that there is a bug in the calculation of readIndex, probably when read insertions or deletions are present.
                //              assert false : "should not fail" + String.format("readIndex =%d || readIndex >= stats.countVariationBases.length || readIndex >= stats.countReferenceBases.length",
                //                     readIndex);
                continue;
            }
            long variationBases = stats.countVariationBases[readIndex - 1];
            long referenceBases = stats.countReferenceBases[readIndex - 1];
            //  System.out.printf("readIndex=%d variationBases=%d referenceBases=%d %n",readIndexInfo.readIndex, variationBases, referenceBases);
            expectedVariationRate += variationBases;
            sum += variationBases + referenceBases;
            //   refBaseCountAnyPosition += referenceBases;
        }
        expectedVariationRate /= sum;
        //   System.out.printf("Expected variation rate: %f%n", expectedVariationRate);
        expectedVariationCount = (int) Math.round(expectedVariationRate * (double) (observedVariationCount + observedReferenceCount));
        expectedReferenceCount = (int) Math.round((1 - expectedVariationRate) * (double) (observedVariationCount + observedReferenceCount));
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("contingency position=%d: %n" +
                    "    [  exp    obs ]%n" +
                    "ref [ %d       %d ]%n" +
                    "var [ %d       %d ] %n",
                    position,
                    expectedVariationCount, observedVariationCount,
                    expectedReferenceCount, observedReferenceCount));
        }
        pValue = fisherRInstalled ? FisherExactRCalculator.getFisherOneTailedLesserPValue(
                expectedVariationCount, observedVariationCount,
                expectedReferenceCount, observedReferenceCount
        ) : Double.NaN;
        //  System.out.printf("position=%d P-Value=%f%n", position, pValue);
        return pValue;


    }


}