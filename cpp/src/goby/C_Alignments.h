//
// Copyright (C) 2009-2012 Institute for Computational Biomedicine,
//                         Weill Medical College of Cornell University
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation; either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

/*
 * Helper structs to assist with read / writing the Goby compact formats using the C API.
 */

#ifndef C_ALIGNMENTS_H_
#define C_ALIGNMENTS_H_

#include "C_CompactHelpers.h"

#define GOBY_NO_QUAL '\0'

// CAPTURE results are mutable. If you want a copy that will live
// beyond the next gobyCapture_startNew(), use CAPTURE_DUPE.
// which will duplicate the string returned by CAPTURE().
#define GOBY_CAPTURE(RESULT,HELPER,CODE) {         \
        gobyCapture_startNew(HELPER);              \
        CODE;                                      \
        gobyCapture_flush(HELPER);                 \
        RESULT = gobyCapture_capturedData(HELPER); \
}

// Execute CAPTURE and duplicate the string so it won't be destroyed
// during the next CAPTURE. The char* in RESULT after you call this
// needs to be free()'d.
#define GOBY_CAPTURE_DUPE(RESULT,HELPER,CODE) {    \
        CAPTURE(RESULT,HELPER,CODE);               \
        RESULT = goby_copy_string(RESULT, -1);     \
}

#define GOBY_COMPLEMENT_CODE "???????????????????????????????? ??#$%&')(*+,-./0123456789:;>=<??TVGHEFCDIJMLKNOPQYSAABWXRZ]?[^_`tvghefcdijmlknopqysaabwxrz}|{~?"

#ifdef __cplusplus

extern "C" {
#endif
    void gobyAlignments_openAlignmentsWriterDefaultEntriesPerChunk(char *basename, CAlignmentsWriterHelper **writerHelperpp);
    void gobyAlignments_openAlignmentsWriter(char *basename, unsigned int number_of_entries_per_chunk, CAlignmentsWriterHelper **writerHelperpp);

    /**
     * Provides a way to write data to a memory stream (or a temporary file) for methods that
     * want to write data to a FILE, but we want to capture that data without it being written
     * to a file.
     */
    void gobyCapture_open(CAlignmentsWriterHelper *writerHelper, int openIgnoredFile);
    FILE *gobyCapture_fileHandle(CAlignmentsWriterHelper *writerHelper);
    FILE *gobyCapture_ignoredFileHandle(CAlignmentsWriterHelper *writerHelper);
    void gobyCapture_startNew(CAlignmentsWriterHelper *writerHelper);
    char *gobyCapture_capturedData(CAlignmentsWriterHelper *writerHelper);
    char *gobyCapture_ignoredData(CAlignmentsWriterHelper *writerHelper);
    void gobyCapture_flush(CAlignmentsWriterHelper *writerHelper);
    void gobyCapture_close(CAlignmentsWriterHelper *writerHelper);

    void gobyAlignments_setAlignerName(CAlignmentsWriterHelper *writerHelper, char *value);
    void gobyAlignments_setAlignerVersion(CAlignmentsWriterHelper *writerHelper, char *value);
    int  gobyAlignments_getQualityAdjustment(CAlignmentsWriterHelper *writerHelper);
    void gobyAlignments_setQualityAdjustment(CAlignmentsWriterHelper *writerHelper, int value);
    void gobyAlignments_setSorted(CAlignmentsWriterHelper *writerHelper, int sorted /* bool */);
    void gobyAlignments_setIndexed(CAlignmentsWriterHelper *writerHelper, int indexed /* bool */);
    void gobyAlignments_setTargetLengths(CAlignmentsWriterHelper *writerHelper, const unsigned int* target_lengths);
    void gobyAlignments_addStatisticStr(CAlignmentsWriterHelper *writerHelper, const char *description, const char *value);
    void gobyAlignments_addStatisticInt(CAlignmentsWriterHelper *writerHelper, const char *description, const int value);
    void gobyAlignments_addStatisticDouble(CAlignmentsWriterHelper *writerHelper, const char *description, const double value);
    void gobyAlignments_addTargetWithTranslation(CAlignmentsWriterHelper *writerHelper, const unsigned int gobyTargetIndex, const unsigned int alignerTargetIndex, const char *targetName, const unsigned int targetLength);
    void gobyAlignments_addTarget(CAlignmentsWriterHelper *writerHelper, const unsigned int targetIndex, const char *targetName, unsigned int targetLength);
    int gobyAlignments_isTargetIdentifierRegistered(CAlignmentsWriterHelper *writerHelper, const char *targetName);
    unsigned int gobyAlignments_targetIndexForIdentifier(CAlignmentsWriterHelper *writerHelper, const char *targetName);
    unsigned gobyAlignments_addQueryIdentifier(CAlignmentsWriterHelper *writerHelper, const char *queryIdentifier);
    void gobyAlignments_addQueryIdentifierWithIndex(CAlignmentsWriterHelper *writerHelper, const char *queryIdentifier, unsigned int newQueryIndex);
    void gobyAlignments_setQueryIndexOccurrencesStoredInEntries(CAlignmentsWriterHelper *writerHelper, int value /* bool */);

    // get an empty alignment entry to populate
    void gobyAlignments_appendEntry(CAlignmentsWriterHelper *writerHelper);
    void gobyAlignments_debugSequences(CAlignmentsWriterHelper *writerHelper, int hitType, char *refSequence, char *readSequence, int padding_left, int padding_right);
    void gobyAlEntry_setMultiplicity(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlignments_observeQueryIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setQueryIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    unsigned int gobyAlEntry_getQueryIndex(CAlignmentsWriterHelper *writerHelper);
    void gobyAlEntry_setTargetIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setPosition(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setMatchingReverseStrand(CAlignmentsWriterHelper *writerHelper, int value /* bool */);
    void gobyAlEntry_setQueryPosition(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setScoreInt(CAlignmentsWriterHelper *writerHelper, int value);
    void gobyAlEntry_setNumberOfMismatches(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setNumberOfIndels(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setQueryAlignedLength(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setTargetAlignedLength(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setQueryLength(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setMappingQuality(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setSoftClippedLeft(CAlignmentsWriterHelper *writerHelper,
            int start, int size, const char *query, const char *quality);
    void gobyAlEntry_setSoftClippedRight(CAlignmentsWriterHelper *writerHelper,
            int start, int size, const char *query, const char *quality);
    void gobyAlEntry_setPlacedUnmapped(CAlignmentsWriterHelper *writerHelper,
            int length, int translateQuery /*bool*/, int reverseStrand /*bool*/,
            const char *query, const char *quality);
    void gobyAlEntry_setFragmentIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setInsertSize(CAlignmentsWriterHelper *writerHelper, unsigned int value);

    void gobyAlEntry_setAmbiguity(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setQueryIndexOccurrences(CAlignmentsWriterHelper *writerHelper, unsigned int value);

    // These are only used when dealing with a Query Pair
    void gobyAlEntry_setPairFlags(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setPairTargetIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setPairPosition(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setPairFragmentIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);

    // These are only used when dealing with a Splice
    void gobyAlEntry_setSplicedFlags(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setSplicedForwardTargetIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setSplicedForwardFragmentIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setSplicedForwardPosition(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setSplicedBackwardTargetIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setSplicedBackwardFragmentIndex(CAlignmentsWriterHelper *writerHelper, unsigned int value);
    void gobyAlEntry_setSplicedBackwardPosition(CAlignmentsWriterHelper *writerHelper, unsigned int value);

    void gobyAlEntry_appendTooManyHits(CAlignmentsWriterHelper *writerHelper, unsigned int queryIndex, unsigned int alignedLength, int numberOfHits);
    void gobyAlEntry_addSequenceVariation(CAlignmentsWriterHelper *writerHelper, unsigned int readIndex, unsigned int refPosition, char refChar, char readChar, int hasQualCharInt /* bool */, char readQualChar);
    void gobyAlignments_outputSequenceVariations(CAlignmentsWriterHelper *writerHelper, const char *reference, const char *query, const char *quality, int queryStart, int queryEnd, int reverseStrand, int *outMatches, int *outSubs, int *outInserts, int *outDeletes);

    /**
     * Methods to assist reconstructing query and reference for SAM data.
     * Reconstructed query and ref strings will have '-' in the appropriate
     * sequence to denote insertions and deletions.
     */
    CSamHelper *samHelper_getResetSamHelper(CAlignmentsWriterHelper *writerHelper);
    void samHelper_addCigarItem(CSamHelper *samHelper, int length, char op);
    void samHelper_setCigar(CSamHelper *samHelper, const char *cigar);
    const char *samHelper_getCigarStr(CSamHelper *samHelper);
    void samHelper_setMd(CSamHelper *samHelper, const char *md);
    void samHelper_setQueryTranslate(CSamHelper *samHelper, unsigned char *reads, unsigned char *qual, unsigned int length, unsigned int reverseStrand);
    void samHelper_setQuery(CSamHelper *samHelper, const char *reads, const char *qual, unsigned int length, unsigned int reverseStrand);
    void samHelper_constructRefAndQuery(CSamHelper *samHelper);
    const char *samHelper_sourceQuery(CSamHelper *samHelper);
    const char *samHelper_constructedQuery(CSamHelper *samHelper);
    const char *samHelper_sourceQual(CSamHelper *samHelper);
    const char *samHelper_constructedQual(CSamHelper *samHelper);
    const char *samHelper_constructedRef(CSamHelper *samHelper);

	void gobyAlignments_finished(CAlignmentsWriterHelper *alWriterHelper, unsigned int numberOfReads);

	char *goby_copy_string(char *str, int length);
#ifdef __cplusplus
}
#endif


#endif /* C_ALIGNMENTS_H_ */
