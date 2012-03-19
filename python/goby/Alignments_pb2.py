# Generated by the protocol buffer compiler.  DO NOT EDIT!

from google.protobuf import descriptor
from google.protobuf import message
from google.protobuf import reflection
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)



DESCRIPTOR = descriptor.FileDescriptor(
  name='Alignments.proto',
  package='goby',
  serialized_pb='\n\x10\x41lignments.proto\x12\x04goby\"F\n\x13\x41lignmentCollection\x12/\n\x11\x61lignment_entries\x18\x01 \x03(\x0b\x32\x14.goby.AlignmentEntry\"\xf2\x05\n\x0e\x41lignmentEntry\x12\x14\n\x0cmultiplicity\x18\x07 \x01(\r\x12\x17\n\x0f\x63ompressed_data\x18\x17 \x01(\x0c\x12\x13\n\x0bquery_index\x18\x01 \x01(\r\x12\x14\n\x0ctarget_index\x18\x02 \x01(\r\x12\x10\n\x08position\x18\x03 \x01(\r\x12\x1f\n\x17matching_reverse_strand\x18\x06 \x01(\x08\x12\x16\n\x0equery_position\x18\x05 \x01(\r\x12\r\n\x05score\x18\x04 \x01(\x02\x12\x1c\n\x14number_of_mismatches\x18\x08 \x01(\r\x12\x18\n\x10number_of_indels\x18\t \x01(\r\x12\x1c\n\x14query_aligned_length\x18\x0b \x01(\r\x12\x1d\n\x15target_aligned_length\x18\x0c \x01(\r\x12\x34\n\x13sequence_variations\x18\r \x03(\x0b\x32\x17.goby.SequenceVariation\x12\x14\n\x0cquery_length\x18\n \x01(\r\x12\x17\n\x0fmapping_quality\x18\x0e \x01(\x05\x12\x12\n\npair_flags\x18\x0f \x01(\r\x12\x38\n\x13pair_alignment_link\x18\x10 \x01(\x0b\x32\x1b.goby.RelatedAlignmentEntry\x12\x16\n\x0e\x66ragment_index\x18\x11 \x01(\r\x12\x43\n\x1espliced_forward_alignment_link\x18\x12 \x01(\x0b\x32\x1b.goby.RelatedAlignmentEntry\x12\x44\n\x1fspliced_backward_alignment_link\x18\x16 \x01(\x0b\x32\x1b.goby.RelatedAlignmentEntry\x12\x15\n\rspliced_flags\x18\x13 \x01(\r\x12\x13\n\x0binsert_size\x18\x14 \x01(\r\x12\x14\n\x0csample_index\x18\x15 \x01(\r\x12\x1f\n\x17query_index_occurrences\x18\x19 \x01(\r\"W\n\x15RelatedAlignmentEntry\x12\x14\n\x0ctarget_index\x18\x01 \x01(\r\x12\x10\n\x08position\x18\x02 \x01(\r\x12\x16\n\x0e\x66ragment_index\x18\x03 \x01(\r\"g\n\x11SequenceVariation\x12\x0c\n\x04\x66rom\x18\x02 \x01(\t\x12\n\n\x02to\x18\x01 \x01(\t\x12\x10\n\x08position\x18\x03 \x01(\r\x12\x12\n\nread_index\x18\x05 \x01(\r\x12\x12\n\nto_quality\x18\x04 \x01(\x0c\"\xd2\x04\n\x0f\x41lignmentHeader\x12\"\n\x1asmallest_split_query_index\x18\t \x01(\r\x12!\n\x19largest_split_query_index\x18\x0b \x01(\r\x12\x33\n\x12query_name_mapping\x18\x01 \x01(\x0b\x32\x17.goby.IdentifierMapping\x12\x34\n\x13target_name_mapping\x18\x02 \x01(\x0b\x32\x17.goby.IdentifierMapping\x12\x19\n\x11number_of_queries\x18\x05 \x01(\r\x12\x19\n\x11number_of_targets\x18\x06 \x01(\r\x12\x1f\n\x17number_of_aligned_reads\x18\x07 \x01(\r\x12\x18\n\x0cquery_length\x18\x03 \x03(\rB\x02\x18\x01\x12\x1d\n\x15\x63onstant_query_length\x18\n \x01(\r\x12\x15\n\rtarget_length\x18\x08 \x03(\r\x12\x0e\n\x06sorted\x18\r \x01(\x08\x12\x0f\n\x07indexed\x18\x0e \x01(\x08\x12\'\n\x1fquery_lengths_stored_in_entries\x18\x0f \x01(\x08\x12\x14\n\x0c\x61ligner_name\x18\x11 \x01(\t\x12\x17\n\x0f\x61ligner_version\x18\x12 \x01(\t\x12\x0f\n\x07version\x18\x19 \x01(\t\x12\x17\n\x0fsample_basename\x18\x1e \x03(\t\x12#\n\x1bquery_indices_were_permuted\x18\x1a \x01(\x08\x12\x1e\n\x16query_index_occurences\x18\x1b \x01(\x08\";\n\x11IdentifierMapping\x12&\n\x08mappings\x18\x01 \x03(\x0b\x32\x14.goby.IdentifierInfo\"-\n\x0eIdentifierInfo\x12\x0c\n\x04name\x18\x01 \x02(\t\x12\r\n\x05index\x18\x02 \x02(\r\"X\n\x14\x41lignmentTooManyHits\x12\x19\n\x11\x61ligner_threshold\x18\x02 \x02(\r\x12%\n\x04hits\x18\x01 \x03(\x0b\x32\x17.goby.AmbiguousLocation\"b\n\x11\x41mbiguousLocation\x12\x13\n\x0bquery_index\x18\x01 \x02(\r\x12\x1f\n\x17\x61t_least_number_of_hits\x18\x02 \x02(\r\x12\x17\n\x0flength_of_match\x18\x03 \x01(\r\"j\n\x0e\x41lignmentIndex\x12#\n\x17target_position_offsets\x18\x01 \x03(\rB\x02\x10\x01\x12\x13\n\x07offsets\x18\x02 \x03(\x04\x42\x02\x10\x01\x12\x1e\n\x12\x61\x62solute_positions\x18\x03 \x03(\x04\x42\x02\x10\x01\x42\'\n#edu.cornell.med.icb.goby.alignmentsH\x01')




_ALIGNMENTCOLLECTION = descriptor.Descriptor(
  name='AlignmentCollection',
  full_name='goby.AlignmentCollection',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='alignment_entries', full_name='goby.AlignmentCollection.alignment_entries', index=0,
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
  serialized_end=96,
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
      name='compressed_data', full_name='goby.AlignmentEntry.compressed_data', index=1,
      number=23, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value="",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_index', full_name='goby.AlignmentEntry.query_index', index=2,
      number=1, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='target_index', full_name='goby.AlignmentEntry.target_index', index=3,
      number=2, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='position', full_name='goby.AlignmentEntry.position', index=4,
      number=3, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='matching_reverse_strand', full_name='goby.AlignmentEntry.matching_reverse_strand', index=5,
      number=6, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_position', full_name='goby.AlignmentEntry.query_position', index=6,
      number=5, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='score', full_name='goby.AlignmentEntry.score', index=7,
      number=4, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='number_of_mismatches', full_name='goby.AlignmentEntry.number_of_mismatches', index=8,
      number=8, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='number_of_indels', full_name='goby.AlignmentEntry.number_of_indels', index=9,
      number=9, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_aligned_length', full_name='goby.AlignmentEntry.query_aligned_length', index=10,
      number=11, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='target_aligned_length', full_name='goby.AlignmentEntry.target_aligned_length', index=11,
      number=12, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='sequence_variations', full_name='goby.AlignmentEntry.sequence_variations', index=12,
      number=13, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_length', full_name='goby.AlignmentEntry.query_length', index=13,
      number=10, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='mapping_quality', full_name='goby.AlignmentEntry.mapping_quality', index=14,
      number=14, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='pair_flags', full_name='goby.AlignmentEntry.pair_flags', index=15,
      number=15, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='pair_alignment_link', full_name='goby.AlignmentEntry.pair_alignment_link', index=16,
      number=16, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='fragment_index', full_name='goby.AlignmentEntry.fragment_index', index=17,
      number=17, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='spliced_forward_alignment_link', full_name='goby.AlignmentEntry.spliced_forward_alignment_link', index=18,
      number=18, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='spliced_backward_alignment_link', full_name='goby.AlignmentEntry.spliced_backward_alignment_link', index=19,
      number=22, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='spliced_flags', full_name='goby.AlignmentEntry.spliced_flags', index=20,
      number=19, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='insert_size', full_name='goby.AlignmentEntry.insert_size', index=21,
      number=20, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='sample_index', full_name='goby.AlignmentEntry.sample_index', index=22,
      number=21, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_index_occurrences', full_name='goby.AlignmentEntry.query_index_occurrences', index=23,
      number=25, type=13, cpp_type=3, label=1,
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
  serialized_start=99,
  serialized_end=853,
)


_RELATEDALIGNMENTENTRY = descriptor.Descriptor(
  name='RelatedAlignmentEntry',
  full_name='goby.RelatedAlignmentEntry',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='target_index', full_name='goby.RelatedAlignmentEntry.target_index', index=0,
      number=1, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='position', full_name='goby.RelatedAlignmentEntry.position', index=1,
      number=2, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='fragment_index', full_name='goby.RelatedAlignmentEntry.fragment_index', index=2,
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
  serialized_start=855,
  serialized_end=942,
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
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='to', full_name='goby.SequenceVariation.to', index=1,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='position', full_name='goby.SequenceVariation.position', index=2,
      number=3, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='read_index', full_name='goby.SequenceVariation.read_index', index=3,
      number=5, type=13, cpp_type=3, label=1,
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
  serialized_start=944,
  serialized_end=1047,
)


_ALIGNMENTHEADER = descriptor.Descriptor(
  name='AlignmentHeader',
  full_name='goby.AlignmentHeader',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='smallest_split_query_index', full_name='goby.AlignmentHeader.smallest_split_query_index', index=0,
      number=9, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='largest_split_query_index', full_name='goby.AlignmentHeader.largest_split_query_index', index=1,
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
      name='constant_query_length', full_name='goby.AlignmentHeader.constant_query_length', index=8,
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
    descriptor.FieldDescriptor(
      name='indexed', full_name='goby.AlignmentHeader.indexed', index=11,
      number=14, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_lengths_stored_in_entries', full_name='goby.AlignmentHeader.query_lengths_stored_in_entries', index=12,
      number=15, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='aligner_name', full_name='goby.AlignmentHeader.aligner_name', index=13,
      number=17, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='aligner_version', full_name='goby.AlignmentHeader.aligner_version', index=14,
      number=18, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='version', full_name='goby.AlignmentHeader.version', index=15,
      number=25, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='sample_basename', full_name='goby.AlignmentHeader.sample_basename', index=16,
      number=30, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_indices_were_permuted', full_name='goby.AlignmentHeader.query_indices_were_permuted', index=17,
      number=26, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='query_index_occurences', full_name='goby.AlignmentHeader.query_index_occurences', index=18,
      number=27, type=8, cpp_type=7, label=1,
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
  serialized_start=1050,
  serialized_end=1644,
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
  serialized_start=1646,
  serialized_end=1705,
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
  serialized_start=1707,
  serialized_end=1752,
)


_ALIGNMENTTOOMANYHITS = descriptor.Descriptor(
  name='AlignmentTooManyHits',
  full_name='goby.AlignmentTooManyHits',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='aligner_threshold', full_name='goby.AlignmentTooManyHits.aligner_threshold', index=0,
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
  serialized_start=1754,
  serialized_end=1842,
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
  serialized_start=1844,
  serialized_end=1942,
)


_ALIGNMENTINDEX = descriptor.Descriptor(
  name='AlignmentIndex',
  full_name='goby.AlignmentIndex',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='target_position_offsets', full_name='goby.AlignmentIndex.target_position_offsets', index=0,
      number=1, type=13, cpp_type=3, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=descriptor._ParseOptions(descriptor_pb2.FieldOptions(), '\020\001')),
    descriptor.FieldDescriptor(
      name='offsets', full_name='goby.AlignmentIndex.offsets', index=1,
      number=2, type=4, cpp_type=4, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=descriptor._ParseOptions(descriptor_pb2.FieldOptions(), '\020\001')),
    descriptor.FieldDescriptor(
      name='absolute_positions', full_name='goby.AlignmentIndex.absolute_positions', index=2,
      number=3, type=4, cpp_type=4, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=descriptor._ParseOptions(descriptor_pb2.FieldOptions(), '\020\001')),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=1944,
  serialized_end=2050,
)

_ALIGNMENTCOLLECTION.fields_by_name['alignment_entries'].message_type = _ALIGNMENTENTRY
_ALIGNMENTENTRY.fields_by_name['sequence_variations'].message_type = _SEQUENCEVARIATION
_ALIGNMENTENTRY.fields_by_name['pair_alignment_link'].message_type = _RELATEDALIGNMENTENTRY
_ALIGNMENTENTRY.fields_by_name['spliced_forward_alignment_link'].message_type = _RELATEDALIGNMENTENTRY
_ALIGNMENTENTRY.fields_by_name['spliced_backward_alignment_link'].message_type = _RELATEDALIGNMENTENTRY
_ALIGNMENTHEADER.fields_by_name['query_name_mapping'].message_type = _IDENTIFIERMAPPING
_ALIGNMENTHEADER.fields_by_name['target_name_mapping'].message_type = _IDENTIFIERMAPPING
_IDENTIFIERMAPPING.fields_by_name['mappings'].message_type = _IDENTIFIERINFO
_ALIGNMENTTOOMANYHITS.fields_by_name['hits'].message_type = _AMBIGUOUSLOCATION
DESCRIPTOR.message_types_by_name['AlignmentCollection'] = _ALIGNMENTCOLLECTION
DESCRIPTOR.message_types_by_name['AlignmentEntry'] = _ALIGNMENTENTRY
DESCRIPTOR.message_types_by_name['RelatedAlignmentEntry'] = _RELATEDALIGNMENTENTRY
DESCRIPTOR.message_types_by_name['SequenceVariation'] = _SEQUENCEVARIATION
DESCRIPTOR.message_types_by_name['AlignmentHeader'] = _ALIGNMENTHEADER
DESCRIPTOR.message_types_by_name['IdentifierMapping'] = _IDENTIFIERMAPPING
DESCRIPTOR.message_types_by_name['IdentifierInfo'] = _IDENTIFIERINFO
DESCRIPTOR.message_types_by_name['AlignmentTooManyHits'] = _ALIGNMENTTOOMANYHITS
DESCRIPTOR.message_types_by_name['AmbiguousLocation'] = _AMBIGUOUSLOCATION
DESCRIPTOR.message_types_by_name['AlignmentIndex'] = _ALIGNMENTINDEX

class AlignmentCollection(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ALIGNMENTCOLLECTION
  
  # @@protoc_insertion_point(class_scope:goby.AlignmentCollection)

class AlignmentEntry(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ALIGNMENTENTRY
  
  # @@protoc_insertion_point(class_scope:goby.AlignmentEntry)

class RelatedAlignmentEntry(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _RELATEDALIGNMENTENTRY
  
  # @@protoc_insertion_point(class_scope:goby.RelatedAlignmentEntry)

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

class AlignmentIndex(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ALIGNMENTINDEX
  
  # @@protoc_insertion_point(class_scope:goby.AlignmentIndex)

# @@protoc_insertion_point(module_scope)
