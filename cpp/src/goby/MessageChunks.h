/**
 * Copyright (C) 2010 Institute for Computational Biomedicine,
 *                    Weill Medical College of Cornell University
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#pragma once

#ifndef GOBY_MESSAGE_CHUNKS_H
#define GOBY_MESSAGE_CHUNKS_H

#include <fstream>
#include <iostream>
#include <istream>
#include <iterator>
#include <string>
#include <vector>
#include <google/protobuf/io/gzip_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl_lite.h>

#include "common.h"
#include "Alignments.pb.h"

namespace goby {
#define GOBY_MESSAGE_CHUNK_DELIMITER_LENGTH 8    // length of the delimiter tag (in bytes)

  // details of an individual chunk of protocol buffer messages
  struct MessageChunk {
    // the position within the stream
    std::streampos position;
    // the length of the chunk
    size_t length;
  };

  template <class T> class LIBGOBY_EXPORT MessageChunksReader : public std::iterator<std::input_iterator_tag, T> {
    // the name of the chunked file
    std::string filename;

    // the underlying stream
    std::ifstream *stream;

    // positions of compressed alignment collections within the goby chunked file
    std::vector<MessageChunk> chunks;

    // the current chunk details
    std::vector<MessageChunk>::const_iterator chunkIterator;

    // the current processed chunk
    T currentChunk;

    // Java DataInput.readInt()
    static int readInt(std::istream &stream) {
      // TODO? Do we need to worry about endian order here?
      const int ch1 = stream.get();
      const int ch2 = stream.get();
      const int ch3 = stream.get();
      const int ch4 = stream.get();
      return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
    };

  public:
    MessageChunksReader(const std::string& filename) {
      this->filename = filename;
      this->stream = new std::ifstream(filename.c_str(), std::ios::in | std::ios::binary);

      int chunkNumber = 0;
      // get the positions each of the chunks in the file
      while (stream->good()) {
        // each chunk is delimited by DELIMITER_LENGTH bytes
        stream->seekg(GOBY_MESSAGE_CHUNK_DELIMITER_LENGTH, std::ios::cur);

        // then the size of the next chunk follows
        const int size = readInt(*stream);
#if GOBY_DEBUG
        std::cout << "length of chunk #" << chunkNumber++ << " is " << size << std::endl;
#endif // GOBY_DEBUG

        // the last chunk has a size of zero bytes
        if (!stream->eof() && size != 0) {
          const std::streampos position = stream->tellg();
          MessageChunk chunk;
          chunk.position = position;
          chunk.length = size;
          chunks.push_back(chunk);
          stream->seekg(size, std::ios::cur);
        } else {
          break;
        }
      }

      this->chunkIterator = chunks.begin();
      this->currentChunk = T::default_instance();

    };

    virtual ~MessageChunksReader(void) {
      stream->close();
      delete stream;
    };

    // TODO MessageChunksReader(const MessageChunksReader& reader);

    // Prefix increment operator
    MessageChunksReader& operator++() {
      std::cout << "Prefix operator++() " << std::endl;
      ++chunkIterator;
      return *this;
    };

    // Postfix increment operator
    MessageChunksReader& operator++(int) {
      std::cout << "Postfix operator++(int) " << std::endl;
      chunkIterator++;
      return *this;
    };

    bool operator==(const MessageChunksReader& rhs) {
      return chunkIterator == rhs.chunkIterator;
    };

    bool operator!=(const MessageChunksReader& rhs) {
      return chunkIterator != rhs.chunkIterator;
    };

    // return the parsed results for the current chunk
    // the contract of the input iterator is that this is only done once
    // so we uncompress at this time rather than during the increment
    T& operator*() {
      // position the stream to the current chunk location
      stream->seekg((*chunkIterator).position, std::ios::beg);

      // read the compressed buffer into memory
      const size_t compressedChunkLength = (*chunkIterator).length;
      char *compressedChunk = new char[compressedChunkLength];
      stream->read(compressedChunk, compressedChunkLength);
      if (stream->bad()) {
        std::cerr << "There was a problem reading raw data from " << filename << std::endl;
      }
      // uncompress the buffer so that it can be parsed
      google::protobuf::io::ArrayInputStream rawChunkStream(compressedChunk, compressedChunkLength);
      google::protobuf::io::GzipInputStream chunkStream(&rawChunkStream);

      // the stream may not get all read in at once so we may need to copy in stages
      void* uncompressedChunk = NULL;
      size_t chunkSize = 0;

      const void* buffer;
      int bufferSize;
      while (chunkStream.Next(&buffer, &bufferSize)) {
        // store the end location of the chunk buffer
        int index = chunkSize;

        // resize the chunk buffer to fit the new data just read
        chunkSize += bufferSize;
        uncompressedChunk = (void *)realloc(uncompressedChunk, chunkSize);

        // and append the new data over to the end of the header buffer
        ::memcpy(reinterpret_cast<char*>(uncompressedChunk) + index, buffer, bufferSize);
      }

      // populate the current object from the uncompressed data
      if (!currentChunk.ParseFromArray(uncompressedChunk, chunkSize)) {
        std::cerr << "Failed to parse message chunk from " << filename << std::endl;
      }

      // free up the temporary buffers
      ::free(uncompressedChunk);
      ::free(compressedChunk);

      // and retrun the processed chunk
      return currentChunk;
    };

    friend std::ostream &operator<<(std::ostream &out, const MessageChunksReader& reader) {
      out << "ostream &operator<< " << reader.chunkIterator->length;
      return out;
    };

    MessageChunksReader begin() {
      MessageChunksReader newReader = MessageChunksReader(this);
      newReader.chunkIterator = newReader.chunks.begin();
      return(newReader);
    };

    MessageChunksReader end() {
      MessageChunksReader newReader = MessageChunksReader(this);
      newReader.chunkIterator = newReader.chunks.end();
      return(newReader);
    };
  };
}

#endif // GOBY_MESSAGE_CHUNKS_H
