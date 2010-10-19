/*
 * CompactFormatsHelpers.h
 *
 *  Created on: Sep 14, 2010
 *      Author: kdorff
 */

#ifndef COMPACTFORMATSHELPERS_H_
#define COMPACTFORMATSHELPERS_H_

/**************************************************************
 * Helper for reading Compact-Reads format.
 **************************************************************/
#ifdef __cplusplus
	#include <queue>
	#include <string>
	#include "Reads.h"
	#include "GsnapStructs.h"
	// More complex structure for C++
	struct CReadsHelper {
		goby::ReadsReader *readsReader;
		goby::ReadEntryIterator *it;
		const goby::ReadEntryIterator *end;
		std::queue<std::string> *unopenedFiles;
		unsigned char circular;
		unsigned int numberOfReads;
	};
#else
	// Opaque structure for C
	typedef struct {
		void *readsReader;
		void *it;
		void *end;
		void *unopenedFiles;
		unsigned char circular;
		unsigned int numberOfReads;
	} CReadsHelper;
#endif

/**************************************************************
 * Helper for writing Compact-Alignments format.
 **************************************************************/

#ifdef __cplusplus
	#include "Alignments.h"
	#include "TooManyHits.h"
	#include "GsnapStructs.h"
	// More complex structure for C++
	struct CAlignmentsWriterHelper {
	    goby::AlignmentWriter *alignmentWriter;
	    goby::TooManyHitsWriter *tmhWriter;
	    goby::AlignmentEntry *alignmentEntry;
	    goby::SequenceVariation *sequenceVariation;
	    int lastSeqVarReadIndex;
	    unsigned int smallestQueryIndex;
	    unsigned int largestQueryIndex;
	    unsigned int numberOfAlignedReads;
	};
#else
	// Opaque structure for C
	typedef struct {
	    void *alignmentWriter;
	    void *tmhWriter;
	    void *alignmentEntry;
	    void *sequenceVariation;
	    int lastSeqVarReadIndex;
	    unsigned int smallestQueryIndex;
	    unsigned int largestQueryIndex;
	    unsigned int numberOfAlignedReads;
	} CAlignmentsWriterHelper;
#endif

#endif /* COMPACTFORMATSHELPERS_H_ */
