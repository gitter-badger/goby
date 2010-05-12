#
# Copyright (C) 2010 Institute for Computational Biomedicine,
#                    Weill Medical College of Cornell University
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

import gzip
import Alignments_pb2
from MessageChunksReader import MessageChunksReader

# Java properties - http://pypi.python.org/pypi/pyjavaproperties/
from pyjavaproperties import Properties

#
# Reads alignments written in the Goby "compact" format
#
class AlignmentReader():
    # basename for this alignment
    basename = None

    # statistics from this alignment
    statistics = Properties()

    # alignment header
    header = Alignments_pb2.AlignmentHeader()

    # reader for the alignment entries
    entries_reader = None

    def __init__(self, basename, verbose = False):
        # store the basename
        self.basename = basename

        # read the "stats" file
        stats_filename = basename + ".stats"
        if verbose:
            print "Reading properties from", stats_filename
        try:
            self.statistics.load(open(stats_filename))
        except IOError, err:
            print str(err)
            pass

        # read the header
        header_filename = basename + ".header"
        if verbose:
            print "Reading header from", header_filename
        f = gzip.open(header_filename, "rb")
        self.header.ParseFromString(f.read())
        f.close()

        # open the entries
        self.entries_reader = MessageReader(basename)

    def __str__(self):
        return self.basename

#
# Reads "Too Many Hits information from alignments
# written in the Goby "compact" format
#
class TooManyHitsReader():
    # basename for this alignment
    basename = None

    # too many hits
    tmh = Alignments_pb2.AlignmentTooManyHits()

    # query index to number of hits
    queryindex_to_numhits = dict()

    # query index to depth/length of match.
    queryindex_to_depth = dict()

    def __init__(self, basename, verbose = False):
        # store the basename
        self.basename = basename

        # read the "too many hits" info
        tmh_filename = basename + ".tmh"
        if verbose:
            print "Reading too many hits info from", tmh_filename

        try:
            f = open(tmh_filename, "rb")
            self.tmh.ParseFromString(f.read())
            f.close()

            for hit in self.tmh.hits:
                self.queryindex_to_numhits[hit.query_index] = hit.at_least_number_of_hits
                if hit.HasField("length_of_match"):
                    self.queryindex_to_depth[hit.query_index] = hit.length_of_match
        except IOError, err:
            print str(err)
            pass

    def __str__(self):
        return self.basename

#
# Iterator for alignment collections within the alignment entries 
#
class AlignmentCollectionReader(MessageChunksReader):
    def __init__(self, basename, verbose = False):
        MessageChunksReader.__init__(self, basename, verbose)

    #
    # Return next alignment collection from the entries file
    #
    def next(self):
        buf = MessageChunksReader.next(self)
        collection = Alignments_pb2.AlignmentCollection()
        collection.ParseFromString(buf)
        return collection

    def __iter__(self):
        return self

    def __str__(self):
        return self.basename
