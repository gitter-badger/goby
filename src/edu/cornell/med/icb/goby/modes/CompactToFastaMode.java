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

package edu.cornell.med.icb.goby.modes;

import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import edu.cornell.med.icb.goby.reads.ColorSpaceConverter;
import edu.cornell.med.icb.goby.reads.ReadSet;
import edu.cornell.med.icb.goby.reads.Reads;
import edu.cornell.med.icb.goby.reads.ReadsReader;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.logging.ProgressLogger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math.optimization.general.ConjugateGradientFormula;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Converts a Compact file to <a href="http://en.wikipedia.org/wiki/FASTA_format">FASTA</a> format.
 *
 * @author Fabien Campagne
 *         Date: May 4 2009
 *         Time: 12:28 PM
 */
public class CompactToFastaMode extends AbstractGobyMode {
    private static final int FASTA_LINE_LENGTH = 60;

    // TODO: generate colorspace results using BWA without using Stu's ouputFakeQualityMode HACK !
    private static final char FAKE_QUALITY_CHARACTER = '~';

    private String inputFilename;
    private String outputFilename;
    private boolean indexToHeader;

    /**
     * The mode name.
     */
    private static final String MODE_NAME = "compact-to-fasta";
    /**
     * The mode description help text.
     */
    private static final String MODE_DESCRIPTION = "Converts a Compact file to Fasta format.";

    private boolean referenceConversion;
    private String alphabet;
    private File readIndexFilterFile;
    private int numberOfFilteredSequences;

    private int numberOfSequences;
    private final Int2IntMap queryLengths = new Int2IntOpenHashMap();
    private boolean hashOutputFilename;

    private static final char[] FAKE_NT_ALPHABET = {'A', 'C', 'G', 'T', 'N'};

    private boolean outputColorMode;
    private boolean outputFakeNtMode;
    private int trimAdaptorLength;

    // TODO: generate colorspace results using BWA without using Stu's ouputFakeQualityMode HACK !
    private boolean outputFakeQualityMode;
    private FastaToCompactMode.QualityEncoding qualityEncoding = FastaToCompactMode.QualityEncoding.ILLUMINA;
    private int qualityToAsciiOffset;

    /**
     * Select the desired output format.
     *
     * @param format
     */
    public void setOutputFormat(OutputFormat format) {
        outputFormat = format;
    }

    public enum OutputFormat {
        FASTA, FASTQ
    }

    /**
     * Set the type of encoding to write quality scores.
     *
     * @param qualityEncoding
     */
    public void setQualityEncoding(FastaToCompactMode.QualityEncoding qualityEncoding) {
        this.qualityEncoding = qualityEncoding;
    }

    private OutputFormat outputFormat = OutputFormat.FASTA;

    @Override
    public String getModeName() {
        return MODE_NAME;
    }

    @Override
    public String getModeDescription() {
        return MODE_DESCRIPTION;
    }

    public int getNumberOfSequences() {
        return numberOfSequences;
    }

    public int getMinSequenceLength() {
        int minLength = Integer.MAX_VALUE;
        for (final int length : queryLengths.values()) {
            minLength = Math.min(minLength, length);
        }
        return minLength;
    }

    /**
     * Configure.
     *
     * @param args command line arguments
     * @return this object for chaining
     * @throws IOException   error parsing
     * @throws JSAPException error parsing
     */
    @Override
    public AbstractCommandLineMode configure(final String[] args)
            throws IOException, JSAPException {
        final JSAPResult jsapResult = parseJsapArguments(args);

        inputFilename = jsapResult.getString("input");
        outputFilename = jsapResult.getString("output");
        outputFormat = OutputFormat.valueOf(jsapResult.getString("output-format").toUpperCase());
        alphabet = jsapResult.getString("alphabet");
        indexToHeader = jsapResult.getBoolean("index-to-header");
        outputColorMode = jsapResult.getBoolean("output-color-space");
        outputFakeNtMode = jsapResult.getBoolean("output-fake-nt");
        trimAdaptorLength = jsapResult.getInt("trim-adaptor-length");
        outputFakeQualityMode = jsapResult.getBoolean("output-fake-quality");
        referenceConversion = jsapResult.getBoolean("reference");
        readIndexFilterFile = jsapResult.getFile("read-index-filter");
        qualityEncoding = FastaToCompactMode.QualityEncoding.valueOf(jsapResult.getString("quality-encoding").toUpperCase());

        return this;
    }

    public void setTrimAdaptorLength(final int trimAdaptorLength) {
        this.trimAdaptorLength = trimAdaptorLength;
    }

    public void setOutputColorMode(final boolean outputColorMode) {
        this.outputColorMode = outputColorMode;
    }

    public void setOutputFakeNtMode(final boolean outputFakeNtMode) {
        this.outputFakeNtMode = outputFakeNtMode;
    }

    public void setOutputFakeQualityMode(final boolean outputFakeQualityMode) {
        this.outputFakeQualityMode = outputFakeQualityMode;
    }

    public void setIndexToHeader(final boolean indexToHeader) {
        this.indexToHeader = indexToHeader;
    }

    public void setReadIndexFilterFile(final File readIndexFilterFile) {
        this.readIndexFilterFile = readIndexFilterFile;
    }

    public void setAlphabet(final String alphabet) {
        this.alphabet = alphabet;
    }

    public void setReferenceConversion(final boolean referenceConversion) {
        this.referenceConversion = referenceConversion;
    }

    public void setInputFilename(final String inputFilename) {
        this.inputFilename = inputFilename;
    }

    public void setOutputFilename(final String outputFilename) {
        this.outputFilename = outputFilename;
    }

    @Override
    public void execute() throws IOException {
        if (outputFilename == null) {
            outputFilename = FilenameUtils.removeExtension(inputFilename)
                    + (hashOutputFilename ? hash() : "") + ".fasta";
        } else if (hashOutputFilename) {
            outputFilename = FilenameUtils.removeExtension(outputFilename)
                    + (hashOutputFilename ? hash() : "") + ".fasta";
        }
        ReadSet readIndexFilter = new ReadSet();
        if (readIndexFilterFile == null) {
            readIndexFilter = null;
        } else {
            readIndexFilter.load(readIndexFilterFile);
        }

        final ProgressLogger progress = new ProgressLogger();
        progress.start();
        progress.displayFreeMemory = true;

        // set the offset used to convert Phred scores to ASCII representation according to the encoding required:
        switch (qualityEncoding) {
            case SANGER:
                qualityToAsciiOffset = 33;
                break;
            case ILLUMINA:
                qualityToAsciiOffset = 64;
                break;

        }

        ReadsReader reader = null;
        Writer writer = null;

        final int NEW_ENTRY_CHARACTER;
        switch (outputFormat) {
            default:
            case FASTA:
                NEW_ENTRY_CHARACTER = '>';
                break;
            case FASTQ:
                NEW_ENTRY_CHARACTER = '@';
        }
        try {
            writer = new OutputStreamWriter(new FastBufferedOutputStream(new FileOutputStream(outputFilename)));
            final MutableString colorSpaceBuffer = new MutableString();
            final MutableString sequence = new MutableString();

            reader = new ReadsReader(new FileInputStream(inputFilename));
            for (final Reads.ReadEntry readEntry : reader) {
                if (readIndexFilter == null || readIndexFilter.contains(readEntry.getReadIndex())) {
                    final String description;

                    if (indexToHeader || !readEntry.hasDescription()) {
                        description = Integer.toString(readEntry.getReadIndex());
                    } else {
                        description = readEntry.getDescription();
                    }

                    writer.write(NEW_ENTRY_CHARACTER);
                    writer.write(description);
                    writer.write('\n');
                    ReadsReader.decodeSequence(readEntry, sequence);
                    if (queryLengths != null) {
                        queryLengths.put(readEntry.getReadIndex(), sequence.length());
                    }

                    MutableString transformedSequence = sequence;
                    if (outputColorMode) {
                        ColorSpaceConverter.convert(transformedSequence, colorSpaceBuffer, referenceConversion);
                        transformedSequence = colorSpaceBuffer;
                    }
                    if (outputFakeNtMode) {
                        for (int i = 0; i < transformedSequence.length(); i++) {
                            transformedSequence.charAt(i, getFakeNtCharacter(transformedSequence.charAt(i)));
                        }
                    }
                    if (trimAdaptorLength > 0) {
                        transformedSequence = transformedSequence.substring(trimAdaptorLength);
                    }
                    // filter unrecognized bases from output
                    if (alphabet != null) {
                        for (int i = 0; i < transformedSequence.length(); i++) {
                            if (alphabet.indexOf(transformedSequence.charAt(i)) == -1) {
                                transformedSequence.charAt(i, 'N');
                            }
                        }
                    }
                    writeSequence(writer, transformedSequence, outputFakeQualityMode);
                    if (outputFormat == OutputFormat.FASTQ) {
                        writeQualityScores(writer, readEntry.hasQualityScores(), readEntry.getQualityScores().toByteArray(), outputFakeQualityMode);
                    }
                    ++numberOfFilteredSequences;

                    progress.lightUpdate();
                }
                ++numberOfSequences;
            }
        } finally {
            IOUtils.closeQuietly(writer);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) { // NOPMD
                    // silently ignore
                }
            }
        }

        progress.stop();
    }


    /**
     * Return a fake nucleotide corresponding to a color space character, or leave unchanged if
     * input is not color space.
     *
     * @param c The character to get the fake nucleotide for
     * @return he fake nucleotide represented by the input
     */
    private char getFakeNtCharacter(final char c) {
        final int i = Character.getNumericValue(c);
        return i >= 0 && i < FAKE_NT_ALPHABET.length ? FAKE_NT_ALPHABET[i] : c;
    }

    private String hash() {
        if (readIndexFilterFile == null) {
            return "";
        } else {
            return Integer.toHexString(readIndexFilterFile.hashCode() ^ inputFilename.hashCode());
        }
    }

    /**
     * Write the sequence at 60 chars per line.
     *
     * @param writer   the writer
     * @param sequence the sequence
     * @throws IOException error reading
     */
    public static void writeSequence(final Writer writer, final MutableString sequence)
            throws IOException {
        writeSequence(writer, sequence, false);
    }

    public static void writeSequence(final Writer writer, final MutableString sequence,
                                     final boolean outputFakeQualityMode) throws IOException {
        final int length = sequence.length();
        for (int i = 0; i < length; i++) {
            if (i != 0 && (i % FASTA_LINE_LENGTH == 0)) {
                writer.write('\n');
            }
            writer.write(sequence.charAt(i));
        }
        writer.write('\n');

    }

    /**
     * Write quality scores if the input has them, or fake some anyway if the outputFakeQualityMode is active.
     *
     * @param writer
     * @param hasScores
     * @param qualityScores
     * @param outputFakeQualityMode
     * @throws IOException
     */
    private void writeQualityScores(Writer writer, boolean hasScores, byte[] qualityScores, boolean outputFakeQualityMode) throws IOException {
        final int length = qualityScores.length;
        if (hasScores || outputFakeQualityMode) {
            writer.write('+');
            writer.write('\n');
            for (int i = 0; i < length; i++) {
                if (i != 0 && (i % FASTA_LINE_LENGTH == 0)) {
                    writer.write('\n');
                }
                if (hasScores) {
                    writer.write(qualityScoreToAscii(qualityScores[i]));
                } else {
                    writer.write(FAKE_QUALITY_CHARACTER);
                }
            }
            writer.write('\n');
        }
    }

    private char qualityScoreToAscii(final byte qualityScore) {


        return (char) (qualityScore + qualityToAsciiOffset);
    }

    public static void main(final String[] args) throws IOException, JSAPException {
        new CompactToFastaMode().configure(args).execute();
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public int getNumberOfFilteredSequences() {
        return numberOfFilteredSequences;
    }

    public void setHashOutputFilename(final boolean hash) {
        hashOutputFilename = hash;
    }
}
