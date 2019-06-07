// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: options.proto

package com.cloudera.thunderhead.service.common.options;

public final class Options {
  private Options() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
    registry.add(com.cloudera.thunderhead.service.common.options.Options.sensitive);
    registry.add(com.cloudera.thunderhead.service.common.options.Options.skipLogging);
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public static final int SENSITIVE_FIELD_NUMBER = 50000;
  /**
   * <pre>
   * The field is sensitive. It will not be logged and may receive other special
   * handling in the future.
   * </pre>
   *
   * <code>extend .google.protobuf.FieldOptions { ... }</code>
   */
  public static final
    com.google.protobuf.GeneratedMessage.GeneratedExtension<
      com.google.protobuf.DescriptorProtos.FieldOptions,
      java.lang.Boolean> sensitive = com.google.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        java.lang.Boolean.class,
        null);
  public static final int SKIPLOGGING_FIELD_NUMBER = 50001;
  /**
   * <pre>
   * The field should not be logged. This may be useful on fields that have very
   * large values.
   * </pre>
   *
   * <code>extend .google.protobuf.FieldOptions { ... }</code>
   */
  public static final
    com.google.protobuf.GeneratedMessage.GeneratedExtension<
      com.google.protobuf.DescriptorProtos.FieldOptions,
      java.lang.Boolean> skipLogging = com.google.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        java.lang.Boolean.class,
        null);

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\roptions.proto\022\007options\032 google/protobu" +
      "f/descriptor.proto:2\n\tsensitive\022\035.google" +
      ".protobuf.FieldOptions\030\320\206\003 \001(\010:4\n\013skipLo" +
      "gging\022\035.google.protobuf.FieldOptions\030\321\206\003" +
      " \001(\010B:\n/com.cloudera.thunderhead.service" +
      ".common.optionsB\007Optionsb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.DescriptorProtos.getDescriptor(),
        }, assigner);
    sensitive.internalInit(descriptor.getExtensions().get(0));
    skipLogging.internalInit(descriptor.getExtensions().get(1));
    com.google.protobuf.DescriptorProtos.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}