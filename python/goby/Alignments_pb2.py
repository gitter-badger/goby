# Generated by the protocol buffer compiler.  DO NOT EDIT!

from google.protobuf import descriptor
from google.protobuf import message
from google.protobuf import reflection
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)


DESCRIPTOR = descriptor.FileDescriptor(
  name='Alignments.proto',
  package='goby',
  serialized_pb='\n\x10\x41lignments.proto\x12\x04goby\"E\n\x13\x41lignmentCollection\x12.\n\x10\x61lignmentEntries\x18\x01 \x03(\x0b\x32\x14.goby.AlignmentEntry\"\xeb\x02\n\x0e\x41lignmentEntry\x12\x14\n\x0cmultiplicity\x18\x07 \x01(\r\x12\x13\n\x0bquery_index\x18\x01 \x02(\r\x12\x14\n\x0ctarget_index\x18\x02 \x02(\r\x12\x10\n\x08position\x18\x03 \x02(\r\x12\x1f\n\x17matching_reverse_strand\x18\x06 \x02(\x08\x12\x16\n\x0equery_position\x18\x05 \x01(\r\x12\r\n\x05score\x18\x04 \x01(\x02\x12\x1c\n\x14number_of_mismatches\x18\x08 \x01(\r\x12\x18\n\x10number_of_indels\x18\t \x01(\r\x12\x1c\n\x14query_aligned_length\x18\x0b \x01(\r\x12\x1d\n\x15target_aligned_length\x18\x0c \x01(\r\x12\x34\n\x13sequence_variations\x18\r \x03(\x0b\x32\x17.goby.SequenceVariation\x12\x13\n\x0bqueryLength\x18\n \x01(\r\"f\n\x11SequenceVariation\x12\x0c\n\x04\x66rom\x18\x02 \x02(\t\x12\n\n\x02to\x18\x01 \x02(\t\x12\x10\n\x08position\x18\x03 \x02(\r\x12\x11\n\treadIndex\x18\x05 \x02(\r\x12\x12\n\nto_quality\x18\x04 \x01(\x0c\"\xf2\x02\n\x0f\x41lignmentHeader\x12\x1f\n\x17smallestSplitQueryIndex\x18\t \x01(\r\x12\x1e\n\x16largestSplitQueryIndex\x18\x0b \x01(\r\x12\x33\n\x12query_name_mapping\x18\x01 \x01(\x0b\x32\x17.goby.IdentifierMapping\x12\x34\n\x13target_name_mapping\x18\x02 \x01(\x0b\x32\x17.goby.IdentifierMapping\x12\x19\n\x11number_of_queries\x18\x05 \x01(\r\x12\x19\n\x11number_of_targets\x18\x06 \x01(\r\x12\x1f\n\x17number_of_aligned_reads\x18\x07 \x01(\r\x12\x18\n\x0cquery_length\x18\x03 \x03(\rB\x02\x18\x01\x12\x1b\n\x13\x63onstantQueryLength\x18\n \x01(\r\x12\x15\n\rtarget_length\x18\x08 \x03(\r\x12\x0e\n\x06sorted\x18\r \x01(\x08\";\n\x11IdentifierMapping\x12&\n\x08mappings\x18\x01 \x03(\x0b\x32\x14.goby.IdentifierInfo\"-\n\x0eIdentifierInfo\x12\x0c\n\x04name\x18\x01 \x02(\t\x12\r\n\x05index\x18\x02 \x02(\r\"W\n\x14\x41lignmentTooManyHits\x12\x18\n\x10\x61lignerThreshold\x18\x02 \x02(\r\x12%\n\x04hits\x18\x01 \x03(\x0b\x32\x17.goby.AmbiguousLocation\"b\n\x11\x41mbiguousLocation\x12\x13\n\x0bquery_index\x18\x01 \x02(\r\x12\x1f\n\x17\x61t_least_number_of_hits\x18\x02 \x02(\r\x12\x17\n\x0flength_of_match\x18\x03 \x01(\rB\'\n#edu.cornell.med.icb.goby.alignmentsH\x01')




_ALIGNMENTCOLLECTION = descriptor.Descriptor(
  name='AlignmentCollection',
  full_name='goby.AlignmentCollection',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='alignmentEntries', full_name='goby.AlignmentCollection.alignmentEntries', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=26,
  serialized_end=95,
)


_ALIGNMENTENTRY = descriptor.Descriptor(
  name='AlignmentEntry',
  full_name='goby.AlignmentEntry',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='multiplicity', full_name='goby.AlignmentEntry.multiplicity', index=0,
      number=7, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_index', full_name='goby.AlignmentEntry.query_index', index=1,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='target_index', full_name='goby.AlignmentEntry.target_index', index=2,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='position', full_name='goby.AlignmentEntry.position', index=3,
      number=3, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='matching_reverse_strand', full_name='goby.AlignmentEntry.matching_reverse_strand', index=4,
      number=6, type=8, cpp_type=7, label=2,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_position', full_name='goby.AlignmentEntry.query_position', index=5,
      number=5, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='score', full_name='goby.AlignmentEntry.score', index=6,
      number=4, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='number_of_mismatches', full_name='goby.AlignmentEntry.number_of_mismatches', index=7,
      number=8, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='number_of_indels', full_name='goby.AlignmentEntry.number_of_indels', index=8,
      number=9, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_aligned_length', full_name='goby.AlignmentEntry.query_aligned_length', index=9,
      number=11, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='target_aligned_length', full_name='goby.AlignmentEntry.target_aligned_length', index=10,
      number=12, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='sequence_variations', full_name='goby.AlignmentEntry.sequence_variations', index=11,
      number=13, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='queryLength', full_name='goby.AlignmentEntry.queryLength', index=12,
      number=10, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=98,
  serialized_end=461,
)


_SEQUENCEVARIATION = descriptor.Descriptor(
  name='SequenceVariation',
  full_name='goby.SequenceVariation',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='from', full_name='goby.SequenceVariation.from', index=0,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='to', full_name='goby.SequenceVariation.to', index=1,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='position', full_name='goby.SequenceVariation.position', index=2,
      number=3, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='readIndex', full_name='goby.SequenceVariation.readIndex', index=3,
      number=5, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='to_quality', full_name='goby.SequenceVariation.to_quality', index=4,
      number=4, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value="",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=463,
  serialized_end=565,
)


_ALIGNMENTHEADER = descriptor.Descriptor(
  name='AlignmentHeader',
  full_name='goby.AlignmentHeader',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='smallestSplitQueryIndex', full_name='goby.AlignmentHeader.smallestSplitQueryIndex', index=0,
      number=9, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='largestSplitQueryIndex', full_name='goby.AlignmentHeader.largestSplitQueryIndex', index=1,
      number=11, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_name_mapping', full_name='goby.AlignmentHeader.query_name_mapping', index=2,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='target_name_mapping', full_name='goby.AlignmentHeader.target_name_mapping', index=3,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='number_of_queries', full_name='goby.AlignmentHeader.number_of_queries', index=4,
      number=5, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='number_of_targets', full_name='goby.AlignmentHeader.number_of_targets', index=5,
      number=6, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='number_of_aligned_reads', full_name='goby.AlignmentHeader.number_of_aligned_reads', index=6,
      number=7, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_length', full_name='goby.AlignmentHeader.query_length', index=7,
      number=3, type=13, cpp_type=3, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=descriptor._ParseOptions(descriptor_pb2.FieldOptions(), '\030\001')),
    descriptor.FieldDescriptor(
      name='constantQueryLength', full_name='goby.AlignmentHeader.constantQueryLength', index=8,
      number=10, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='target_length', full_name='goby.AlignmentHeader.target_length', index=9,
      number=8, type=13, cpp_type=3, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='sorted', full_name='goby.AlignmentHeader.sorted', index=10,
      number=13, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=568,
  serialized_end=938,
)


_IDENTIFIERMAPPING = descriptor.Descriptor(
  name='IdentifierMapping',
  full_name='goby.IdentifierMapping',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='mappings', full_name='goby.IdentifierMapping.mappings', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=940,
  serialized_end=999,
)


_IDENTIFIERINFO = descriptor.Descriptor(
  name='IdentifierInfo',
  full_name='goby.IdentifierInfo',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='name', full_name='goby.IdentifierInfo.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='index', full_name='goby.IdentifierInfo.index', index=1,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=1001,
  serialized_end=1046,
)


_ALIGNMENTTOOMANYHITS = descriptor.Descriptor(
  name='AlignmentTooManyHits',
  full_name='goby.AlignmentTooManyHits',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='alignerThreshold', full_name='goby.AlignmentTooManyHits.alignerThreshold', index=0,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='hits', full_name='goby.AlignmentTooManyHits.hits', index=1,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=1048,
  serialized_end=1135,
)


_AMBIGUOUSLOCATION = descriptor.Descriptor(
  name='AmbiguousLocation',
  full_name='goby.AmbiguousLocation',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='query_index', full_name='goby.AmbiguousLocation.query_index', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='at_least_number_of_hits', full_name='goby.AmbiguousLocation.at_least_number_of_hits', index=1,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='length_of_match', full_name='goby.AmbiguousLocation.length_of_match', index=2,
      number=3, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=1137,
  serialized_end=1235,
)


_ALIGNMENTCOLLECTION.fields_by_name['alignmentEntries'].message_type = _ALIGNMENTENTRY
_ALIGNMENTENTRY.fields_by_name['sequence_variations'].message_type = _SEQUENCEVARIATION
_ALIGNMENTHEADER.fields_by_name['query_name_mapping'].message_type = _IDENTIFIERMAPPING
_ALIGNMENTHEADER.fields_by_name['target_name_mapping'].message_type = _IDENTIFIERMAPPING
_IDENTIFIERMAPPING.fields_by_name['mappings'].message_type = _IDENTIFIERINFO
_ALIGNMENTTOOMANYHITS.fields_by_name['hits'].message_type = _AMBIGUOUSLOCATION

class AlignmentCollection(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ALIGNMENTCOLLECTION
  
  # @@protoc_insertion_point(class_scope:goby.AlignmentCollection)

class AlignmentEntry(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ALIGNMENTENTRY
  
  # @@protoc_insertion_point(class_scope:goby.AlignmentEntry)

class SequenceVariation(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _SEQUENCEVARIATION
  
  # @@protoc_insertion_point(class_scope:goby.SequenceVariation)

class AlignmentHeader(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ALIGNMENTHEADER
  
  # @@protoc_insertion_point(class_scope:goby.AlignmentHeader)

class IdentifierMapping(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _IDENTIFIERMAPPING
  
  # @@protoc_insertion_point(class_scope:goby.IdentifierMapping)

class IdentifierInfo(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _IDENTIFIERINFO
  
  # @@protoc_insertion_point(class_scope:goby.IdentifierInfo)

class AlignmentTooManyHits(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ALIGNMENTTOOMANYHITS
  
  # @@protoc_insertion_point(class_scope:goby.AlignmentTooManyHits)

class AmbiguousLocation(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _AMBIGUOUSLOCATION
  
  # @@protoc_insertion_point(class_scope:goby.AmbiguousLocation)

# @@protoc_insertion_point(module_scope)
