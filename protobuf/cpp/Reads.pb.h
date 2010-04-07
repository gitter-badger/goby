// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Reads.proto

#ifndef PROTOBUF_Reads_2eproto__INCLUDED
#define PROTOBUF_Reads_2eproto__INCLUDED

#include <string>

#include <google/protobuf/stubs/common.h>

#if GOOGLE_PROTOBUF_VERSION < 2003000
#error This file was generated by a newer version of protoc which is
#error incompatible with your Protocol Buffer headers.  Please update
#error your headers.
#endif
#if 2003000 < GOOGLE_PROTOBUF_MIN_PROTOC_VERSION
#error This file was generated by an older version of protoc which is
#error incompatible with your Protocol Buffer headers.  Please
#error regenerate this file with a newer version of protoc.
#endif

#include <google/protobuf/generated_message_util.h>
#include <google/protobuf/repeated_field.h>
#include <google/protobuf/extension_set.h>
#include <google/protobuf/generated_message_reflection.h>
// @@protoc_insertion_point(includes)

namespace goby {

// Internal implementation detail -- do not call these.
void  protobuf_AddDesc_Reads_2eproto();
void protobuf_AssignDesc_Reads_2eproto();
void protobuf_ShutdownFile_Reads_2eproto();

class ReadCollection;
class ReadEntry;

// ===================================================================

class ReadCollection : public ::google::protobuf::Message {
 public:
  ReadCollection();
  virtual ~ReadCollection();
  
  ReadCollection(const ReadCollection& from);
  
  inline ReadCollection& operator=(const ReadCollection& from) {
    CopyFrom(from);
    return *this;
  }
  
  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }
  
  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }
  
  static const ::google::protobuf::Descriptor* descriptor();
  static const ReadCollection& default_instance();
  
  void Swap(ReadCollection* other);
  
  // implements Message ----------------------------------------------
  
  ReadCollection* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const ReadCollection& from);
  void MergeFrom(const ReadCollection& from);
  void Clear();
  bool IsInitialized() const;
  
  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:
  
  ::google::protobuf::Metadata GetMetadata() const;
  
  // nested types ----------------------------------------------------
  
  // accessors -------------------------------------------------------
  
  // repeated .goby.ReadEntry reads = 1;
  inline int reads_size() const;
  inline void clear_reads();
  static const int kReadsFieldNumber = 1;
  inline const ::goby::ReadEntry& reads(int index) const;
  inline ::goby::ReadEntry* mutable_reads(int index);
  inline ::goby::ReadEntry* add_reads();
  inline const ::google::protobuf::RepeatedPtrField< ::goby::ReadEntry >&
      reads() const;
  inline ::google::protobuf::RepeatedPtrField< ::goby::ReadEntry >*
      mutable_reads();
  
  // @@protoc_insertion_point(class_scope:goby.ReadCollection)
 private:
  ::google::protobuf::UnknownFieldSet _unknown_fields_;
  mutable int _cached_size_;
  
  ::google::protobuf::RepeatedPtrField< ::goby::ReadEntry > reads_;
  friend void  protobuf_AddDesc_Reads_2eproto();
  friend void protobuf_AssignDesc_Reads_2eproto();
  friend void protobuf_ShutdownFile_Reads_2eproto();
  
  ::google::protobuf::uint32 _has_bits_[(1 + 31) / 32];
  
  // WHY DOES & HAVE LOWER PRECEDENCE THAN != !?
  inline bool _has_bit(int index) const {
    return (_has_bits_[index / 32] & (1u << (index % 32))) != 0;
  }
  inline void _set_bit(int index) {
    _has_bits_[index / 32] |= (1u << (index % 32));
  }
  inline void _clear_bit(int index) {
    _has_bits_[index / 32] &= ~(1u << (index % 32));
  }
  
  void InitAsDefaultInstance();
  static ReadCollection* default_instance_;
};
// -------------------------------------------------------------------

class ReadEntry : public ::google::protobuf::Message {
 public:
  ReadEntry();
  virtual ~ReadEntry();
  
  ReadEntry(const ReadEntry& from);
  
  inline ReadEntry& operator=(const ReadEntry& from) {
    CopyFrom(from);
    return *this;
  }
  
  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }
  
  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }
  
  static const ::google::protobuf::Descriptor* descriptor();
  static const ReadEntry& default_instance();
  
  void Swap(ReadEntry* other);
  
  // implements Message ----------------------------------------------
  
  ReadEntry* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const ReadEntry& from);
  void MergeFrom(const ReadEntry& from);
  void Clear();
  bool IsInitialized() const;
  
  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:
  
  ::google::protobuf::Metadata GetMetadata() const;
  
  // nested types ----------------------------------------------------
  
  // accessors -------------------------------------------------------
  
  // required uint32 readIndex = 1;
  inline bool has_readindex() const;
  inline void clear_readindex();
  static const int kReadIndexFieldNumber = 1;
  inline ::google::protobuf::uint32 readindex() const;
  inline void set_readindex(::google::protobuf::uint32 value);
  
  // optional string readIdentifier = 23;
  inline bool has_readidentifier() const;
  inline void clear_readidentifier();
  static const int kReadIdentifierFieldNumber = 23;
  inline const ::std::string& readidentifier() const;
  inline void set_readidentifier(const ::std::string& value);
  inline void set_readidentifier(const char* value);
  inline void set_readidentifier(const char* value, size_t size);
  inline ::std::string* mutable_readidentifier();
  
  // optional string description = 22;
  inline bool has_description() const;
  inline void clear_description();
  static const int kDescriptionFieldNumber = 22;
  inline const ::std::string& description() const;
  inline void set_description(const ::std::string& value);
  inline void set_description(const char* value);
  inline void set_description(const char* value, size_t size);
  inline ::std::string* mutable_description();
  
  // required uint32 readLength = 2;
  inline bool has_readlength() const;
  inline void clear_readlength();
  static const int kReadLengthFieldNumber = 2;
  inline ::google::protobuf::uint32 readlength() const;
  inline void set_readlength(::google::protobuf::uint32 value);
  
  // optional bytes sequence = 3;
  inline bool has_sequence() const;
  inline void clear_sequence();
  static const int kSequenceFieldNumber = 3;
  inline const ::std::string& sequence() const;
  inline void set_sequence(const ::std::string& value);
  inline void set_sequence(const char* value);
  inline void set_sequence(const void* value, size_t size);
  inline ::std::string* mutable_sequence();
  
  // optional bytes qualityScores = 4;
  inline bool has_qualityscores() const;
  inline void clear_qualityscores();
  static const int kQualityScoresFieldNumber = 4;
  inline const ::std::string& qualityscores() const;
  inline void set_qualityscores(const ::std::string& value);
  inline void set_qualityscores(const char* value);
  inline void set_qualityscores(const void* value, size_t size);
  inline ::std::string* mutable_qualityscores();
  
  // @@protoc_insertion_point(class_scope:goby.ReadEntry)
 private:
  ::google::protobuf::UnknownFieldSet _unknown_fields_;
  mutable int _cached_size_;
  
  ::google::protobuf::uint32 readindex_;
  ::std::string* readidentifier_;
  static const ::std::string _default_readidentifier_;
  ::std::string* description_;
  static const ::std::string _default_description_;
  ::google::protobuf::uint32 readlength_;
  ::std::string* sequence_;
  static const ::std::string _default_sequence_;
  ::std::string* qualityscores_;
  static const ::std::string _default_qualityscores_;
  friend void  protobuf_AddDesc_Reads_2eproto();
  friend void protobuf_AssignDesc_Reads_2eproto();
  friend void protobuf_ShutdownFile_Reads_2eproto();
  
  ::google::protobuf::uint32 _has_bits_[(6 + 31) / 32];
  
  // WHY DOES & HAVE LOWER PRECEDENCE THAN != !?
  inline bool _has_bit(int index) const {
    return (_has_bits_[index / 32] & (1u << (index % 32))) != 0;
  }
  inline void _set_bit(int index) {
    _has_bits_[index / 32] |= (1u << (index % 32));
  }
  inline void _clear_bit(int index) {
    _has_bits_[index / 32] &= ~(1u << (index % 32));
  }
  
  void InitAsDefaultInstance();
  static ReadEntry* default_instance_;
};
// ===================================================================


// ===================================================================

// ReadCollection

// repeated .goby.ReadEntry reads = 1;
inline int ReadCollection::reads_size() const {
  return reads_.size();
}
inline void ReadCollection::clear_reads() {
  reads_.Clear();
}
inline const ::goby::ReadEntry& ReadCollection::reads(int index) const {
  return reads_.Get(index);
}
inline ::goby::ReadEntry* ReadCollection::mutable_reads(int index) {
  return reads_.Mutable(index);
}
inline ::goby::ReadEntry* ReadCollection::add_reads() {
  return reads_.Add();
}
inline const ::google::protobuf::RepeatedPtrField< ::goby::ReadEntry >&
ReadCollection::reads() const {
  return reads_;
}
inline ::google::protobuf::RepeatedPtrField< ::goby::ReadEntry >*
ReadCollection::mutable_reads() {
  return &reads_;
}

// -------------------------------------------------------------------

// ReadEntry

// required uint32 readIndex = 1;
inline bool ReadEntry::has_readindex() const {
  return _has_bit(0);
}
inline void ReadEntry::clear_readindex() {
  readindex_ = 0u;
  _clear_bit(0);
}
inline ::google::protobuf::uint32 ReadEntry::readindex() const {
  return readindex_;
}
inline void ReadEntry::set_readindex(::google::protobuf::uint32 value) {
  _set_bit(0);
  readindex_ = value;
}

// optional string readIdentifier = 23;
inline bool ReadEntry::has_readidentifier() const {
  return _has_bit(1);
}
inline void ReadEntry::clear_readidentifier() {
  if (readidentifier_ != &_default_readidentifier_) {
    readidentifier_->clear();
  }
  _clear_bit(1);
}
inline const ::std::string& ReadEntry::readidentifier() const {
  return *readidentifier_;
}
inline void ReadEntry::set_readidentifier(const ::std::string& value) {
  _set_bit(1);
  if (readidentifier_ == &_default_readidentifier_) {
    readidentifier_ = new ::std::string;
  }
  readidentifier_->assign(value);
}
inline void ReadEntry::set_readidentifier(const char* value) {
  _set_bit(1);
  if (readidentifier_ == &_default_readidentifier_) {
    readidentifier_ = new ::std::string;
  }
  readidentifier_->assign(value);
}
inline void ReadEntry::set_readidentifier(const char* value, size_t size) {
  _set_bit(1);
  if (readidentifier_ == &_default_readidentifier_) {
    readidentifier_ = new ::std::string;
  }
  readidentifier_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* ReadEntry::mutable_readidentifier() {
  _set_bit(1);
  if (readidentifier_ == &_default_readidentifier_) {
    readidentifier_ = new ::std::string;
  }
  return readidentifier_;
}

// optional string description = 22;
inline bool ReadEntry::has_description() const {
  return _has_bit(2);
}
inline void ReadEntry::clear_description() {
  if (description_ != &_default_description_) {
    description_->clear();
  }
  _clear_bit(2);
}
inline const ::std::string& ReadEntry::description() const {
  return *description_;
}
inline void ReadEntry::set_description(const ::std::string& value) {
  _set_bit(2);
  if (description_ == &_default_description_) {
    description_ = new ::std::string;
  }
  description_->assign(value);
}
inline void ReadEntry::set_description(const char* value) {
  _set_bit(2);
  if (description_ == &_default_description_) {
    description_ = new ::std::string;
  }
  description_->assign(value);
}
inline void ReadEntry::set_description(const char* value, size_t size) {
  _set_bit(2);
  if (description_ == &_default_description_) {
    description_ = new ::std::string;
  }
  description_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* ReadEntry::mutable_description() {
  _set_bit(2);
  if (description_ == &_default_description_) {
    description_ = new ::std::string;
  }
  return description_;
}

// required uint32 readLength = 2;
inline bool ReadEntry::has_readlength() const {
  return _has_bit(3);
}
inline void ReadEntry::clear_readlength() {
  readlength_ = 0u;
  _clear_bit(3);
}
inline ::google::protobuf::uint32 ReadEntry::readlength() const {
  return readlength_;
}
inline void ReadEntry::set_readlength(::google::protobuf::uint32 value) {
  _set_bit(3);
  readlength_ = value;
}

// optional bytes sequence = 3;
inline bool ReadEntry::has_sequence() const {
  return _has_bit(4);
}
inline void ReadEntry::clear_sequence() {
  if (sequence_ != &_default_sequence_) {
    sequence_->clear();
  }
  _clear_bit(4);
}
inline const ::std::string& ReadEntry::sequence() const {
  return *sequence_;
}
inline void ReadEntry::set_sequence(const ::std::string& value) {
  _set_bit(4);
  if (sequence_ == &_default_sequence_) {
    sequence_ = new ::std::string;
  }
  sequence_->assign(value);
}
inline void ReadEntry::set_sequence(const char* value) {
  _set_bit(4);
  if (sequence_ == &_default_sequence_) {
    sequence_ = new ::std::string;
  }
  sequence_->assign(value);
}
inline void ReadEntry::set_sequence(const void* value, size_t size) {
  _set_bit(4);
  if (sequence_ == &_default_sequence_) {
    sequence_ = new ::std::string;
  }
  sequence_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* ReadEntry::mutable_sequence() {
  _set_bit(4);
  if (sequence_ == &_default_sequence_) {
    sequence_ = new ::std::string;
  }
  return sequence_;
}

// optional bytes qualityScores = 4;
inline bool ReadEntry::has_qualityscores() const {
  return _has_bit(5);
}
inline void ReadEntry::clear_qualityscores() {
  if (qualityscores_ != &_default_qualityscores_) {
    qualityscores_->clear();
  }
  _clear_bit(5);
}
inline const ::std::string& ReadEntry::qualityscores() const {
  return *qualityscores_;
}
inline void ReadEntry::set_qualityscores(const ::std::string& value) {
  _set_bit(5);
  if (qualityscores_ == &_default_qualityscores_) {
    qualityscores_ = new ::std::string;
  }
  qualityscores_->assign(value);
}
inline void ReadEntry::set_qualityscores(const char* value) {
  _set_bit(5);
  if (qualityscores_ == &_default_qualityscores_) {
    qualityscores_ = new ::std::string;
  }
  qualityscores_->assign(value);
}
inline void ReadEntry::set_qualityscores(const void* value, size_t size) {
  _set_bit(5);
  if (qualityscores_ == &_default_qualityscores_) {
    qualityscores_ = new ::std::string;
  }
  qualityscores_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* ReadEntry::mutable_qualityscores() {
  _set_bit(5);
  if (qualityscores_ == &_default_qualityscores_) {
    qualityscores_ = new ::std::string;
  }
  return qualityscores_;
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace goby

#ifndef SWIG
namespace google {
namespace protobuf {


}  // namespace google
}  // namespace protobuf
#endif  // SWIG

// @@protoc_insertion_point(global_scope)

#endif  // PROTOBUF_Reads_2eproto__INCLUDED
