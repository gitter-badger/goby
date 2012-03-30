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

package edu.cornell.med.icb.goby.modes;

import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import edu.cornell.med.icb.goby.alignments.AlignmentReader;
import edu.cornell.med.icb.goby.alignments.Alignments;
import edu.cornell.med.icb.goby.alignments.ExportableAlignmentEntryData;
import edu.cornell.med.icb.goby.alignments.FileSlice;
import edu.cornell.med.icb.goby.alignments.IterateAlignments;
import edu.cornell.med.icb.goby.alignments.perms.ReadNameToIndex;
import edu.cornell.med.icb.goby.reads.QualityEncoding;
import edu.cornell.med.icb.goby.reads.RandomAccessSequenceCache;
import edu.cornell.med.icb.identifier.DoubleIndexedIdentifier;
import edu.cornell.med.icb.identifier.IndexedIdentifier;
import edu.cornell.med.icb.util.VersionUtils;
import it.unimi.dsi.Util;
import net.sf.samtools.DefaultSAMRecordFactory;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMProgramRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.SAMRecordFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Converts binary BWA alignments in the SAM format to the compact alignment format.
 *
 * @author Fabien Campagne
 */
public class CompactToSAMMode extends AbstractGobyMode {

    /**
     * Used to log debug and informational messages.
     */
    private static final Logger LOG = Logger.getLogger(CompactToSAMMode.class);

    /**
     * The mode name.
     */
    private static final String MODE_NAME = "compact-to-sam";

    /**
     * The mode description help text.
     */
    private static final String MODE_DESCRIPTION = "Converts a compact alignment to the SAM/BAM format.";

    /**
     * BAM/SAM output alignment filename.
     */
    private String output;

    /**
     * Compact-alignment input basename (or one of the files of the input, such as "/path/something.entries").
     */
    private String inputBasename;

    /**
     * The input genome in either 'compact random-access-genome' format or 'fa.gz + fa.gz.fai' format.
     * The random-access-genome file can be made from the .fa.gz reference using the build-sequence-cache mode.
     * If using the random-access-genome input, specify any one of the files in the random-access-genome.
     * If using the 'fa.gz + fa.gz.fai' input, specify the 'fa.gz' file but make sure the '.fa.gz.fai' file
     *    is located in the same directory.
     * Using the random-access-genome format will be considerably faster.
     */
    private String inputGenome;

    /**
     * An "unset" value for startPosition and endPosition.
     */
    private boolean hasStartOrEndPosition;

    /**
     * The start position for the reformat.
     */
    private long startPosition;

    /**
     * The end position for the reformat.
     */
    private long endPosition = Long.MAX_VALUE;

    private QualityEncoding qualityEncoding = QualityEncoding.SANGER;

    private CompactToSAMIterateAlignments alignmentIterator;

    private SAMFileWriter outputSam;

    private SAMFileHeader samHeader;

    private SAMRecordFactory samRecordFactory;

    private RandomAccessSequenceCache genome;

    private ExportableAlignmentEntryData exportData;

    // Keep?
    private ReadNameToIndex nameToQueryIndices = new ReadNameToIndex("ignore-this-for-now");

    /**
     * Flag to indicate if log4j was configured.
     */
    private boolean debug;

    @Override
    public String getModeName() {
        return MODE_NAME;
    }

    @Override
    public String getModeDescription() {
        return MODE_DESCRIPTION;
    }

    /**
     * Get the quality encoding scale used for the input fastq file.
     *
     * @return the quality encoding scale used for the input fastq file
     */
    public QualityEncoding getQualityEncoding() {
        return qualityEncoding;
    }

    /**
     * Set the quality encoding scale to be used for the input fastq file.
     * Acceptable values are "Illumina", "Sanger", and "Solexa".
     *
     * @param qualityEncoding the quality encoding scale to be used for the input fastq file
     */
    public void setQualityEncoding(final QualityEncoding qualityEncoding) {
        this.qualityEncoding = qualityEncoding;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(final String output) {
        this.output = output;
    }

    public String getInputBasename() {
        return inputBasename;
    }

    public void setInputBasename(final String inputBasename) {
        this.inputBasename = inputBasename;
    }

    public String getInputGenome() {
        return inputGenome;
    }

    public void setInputGenome(final String inputGenome) {
        this.inputGenome = inputGenome;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(final long endPosition) {
        this.endPosition = endPosition;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(final long startPosition) {
        this.startPosition = startPosition;
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

        qualityEncoding = QualityEncoding.valueOf(jsapResult.getString("quality-encoding").toUpperCase());
        // don't even dare go through the debugging code if log4j was not configured. The debug code
        // is way too slow to run unintentionally in production!
        debug = Util.log4JIsConfigured();

        inputBasename = jsapResult.getString("input-basename");
        inputGenome = jsapResult.getString("input-genome");
        output = jsapResult.getString("output");
        alignmentIterator = new CompactToSAMIterateAlignments();
        alignmentIterator.parseIncludeReferenceArgument(jsapResult);
        genome = new RandomAccessSequenceCache();
        try {
            genome.load(inputGenome);
        } catch (ClassNotFoundException e) {
            throw new IOException("Could not load genome", e);
        }

        if (jsapResult.contains("start-position") || jsapResult.contains("end-position")) {
            hasStartOrEndPosition = true;
            startPosition = jsapResult.getLong("start-position", 0L);
            endPosition = jsapResult.getLong("end-position", Long.MAX_VALUE);
            if (startPosition < 0) {
                startPosition = 0;
            }
            if (endPosition < 0) {
                endPosition = Long.MAX_VALUE;
            }
            if (startPosition == 0 && endPosition == 0) {
                endPosition = Long.MAX_VALUE;
            }
        }

        if (startPosition > endPosition) {
            throw new JSAPException("Start position must not be greater than the end position");
        }
        return this;
    }

    @Override
    public void execute() throws IOException {
        final boolean outputIsSam = output.toUpperCase().endsWith(".SAM");

        ExportableAlignmentEntryData exportData = new ExportableAlignmentEntryData(genome);
        final String[] basenames = new String[1];
        basenames[0] = inputBasename;
        if (hasStartOrEndPosition) {
            alignmentIterator.iterate(new FileSlice(startPosition, endPosition), basenames);
        } else {
            alignmentIterator.iterate(basenames);
        }

    }

    private class CompactToSAMIterateAlignments extends IterateAlignments {

        private long numWritten;

        @Override
        public void processAlignmentEntry(final AlignmentReader alignmentReader,
                                          final Alignments.AlignmentEntry alignmentEntry) {

            if (outputSam == null) {
                // First entry to output.
                boolean inputIsSorted = alignmentReader.isSorted();

                // Gather the target identifiers, supply them to the SAM file
                samHeader = new SAMFileHeader();
                final SAMSequenceDictionary samTargetDictionary = new SAMSequenceDictionary();
                final IndexedIdentifier gobyTargetIdentifiers = alignmentReader.getTargetIdentifiers();
                final DoubleIndexedIdentifier gobyBackTargetIdentifiers =
                        new DoubleIndexedIdentifier(gobyTargetIdentifiers);
                for (int i = 0; i < gobyTargetIdentifiers.size(); i++) {
                    final String gobyTargetName = gobyBackTargetIdentifiers.getId(i).toString();
                    final int gobyTargetLength = alignmentReader.getTargetLength()[i];
                    final SAMSequenceRecord samSequenceRecord = new SAMSequenceRecord(gobyTargetName, gobyTargetLength);
                    samTargetDictionary.addSequence(samSequenceRecord);
                }

                samHeader.setSequenceDictionary(samTargetDictionary);
                SAMProgramRecord gobyVersionProgRec = new SAMProgramRecord("Goby");
                gobyVersionProgRec.setProgramVersion(VersionUtils.getImplementationVersion(GobyDriver.class));
                samHeader.addProgramRecord(gobyVersionProgRec);
                outputSam = new SAMFileWriterFactory().makeSAMOrBAMWriter(samHeader, inputIsSorted, new File(output));
                samRecordFactory = new DefaultSAMRecordFactory();
            }

            final SAMRecord samRecord = samRecordFactory.createSAMRecord(samHeader);

            samRecord.setReferenceIndex(alignmentEntry.getTargetIndex());
            samRecord.setFlags(alignmentEntry.getPairFlags());
            samRecord.setReadName(String.valueOf(alignmentEntry.getQueryIndex()));
            samRecord.setAlignmentStart(alignmentEntry.getQueryPosition() + 1);
            samRecord.setMappingQuality(alignmentEntry.getMappingQuality());


            exportData.buildFrom(alignmentEntry);

            samRecord.setReadString(exportData.getReadBasesOriginal().toString());
            samRecord.setBaseQualities(exportData.getReadQualities().toByteArray());
            samRecord.setCigarString(exportData.getCigarString().toString());
            samRecord.setAttribute("MD:Z", exportData.getMismatchString().toString());

            if (alignmentEntry.hasPairAlignmentLink()) {
                /*
                samRecord.setMateReferenceIndex(int referenceIndex);
                samRecord.setMateAlignmentStart(int mateAlignmentStart);
                samRecord.setInferredInsertSize(int inferredInsertSize);
                */
            }

            outputSam.addAlignment(samRecord);
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
        final CompactToSAMMode processor = new CompactToSAMMode();
        processor.configure(args);
        processor.execute();
    }




}
