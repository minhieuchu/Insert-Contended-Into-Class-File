import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.RuntimeVisibleAnnotations;
import org.apache.bcel.generic.ConstantPoolGen;

public class InsertContendedIntoClassFile{

  private static String inputClassFile, outputClassFile;
  private static String
      fieldName; // name of the field to be annotated, specified from command line argument
  private static int fieldIndex; // index of the field to be annotated
  private static int indexRuntimeVisibleAnnotations;
  private static int indexContendedAnnotation;
  private static JavaClass java_class;
  private static RuntimeVisibleAnnotations runtimeVisibleAnnotation;

  public static void writeConstantPool() {
    try {

      ConstantPool constants = java_class.getConstantPool();
      ConstantPoolGen cp = new ConstantPoolGen(constants);

      indexRuntimeVisibleAnnotations = cp.addUtf8("RuntimeVisibleAnnotations");
      indexContendedAnnotation = cp.addUtf8("Ljdk/internal/vm/annotation/Contended;");
      java_class.setConstantPool(cp.getFinalConstantPool());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void findFieldIndex() {
    Field[] fields = java_class.getFields();

    for (int i = 0; i < fields.length; ++i) {

      if (fieldName.equals(fields[i].getName())) {
        fieldIndex = i;
        return;
      }
    }
  }

  public static void createRuntimeVisibleAnnotationAttribute() {

    AnnotationEntry contendedAnnotation;

    // Create an annotation entry for @Contended annotation
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

      dataOutputStream.writeShort(indexContendedAnnotation); // index into the constant pool
      dataOutputStream.writeShort(0); // number of element-value pairs

      byte[] byteArray = byteArrayOutputStream.toByteArray();

      // Create annotation with 'AnnotationEntry.read' instead of the constructor so that
      // field 'elementValuePairs' is initialized, the 'dump' method can be executed later
      contendedAnnotation =
          AnnotationEntry.read(
              new DataInputStream(new ByteArrayInputStream(byteArray)),
              java_class.getConstantPool(),
              true);

    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    // Create the RuntimeVisibleAnnotations attribute
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

      // Write the first 2 bytes (the number of annotation entries)
      // In this case, we only need 1 annotation, @Contended annotation
      dataOutputStream.writeShort(1);
      // Write the Contended annotation to the output stream
      contendedAnnotation.dump(dataOutputStream);

      // get the internal byte array to create input stream
      byte[] byteArray = byteArrayOutputStream.toByteArray();

      runtimeVisibleAnnotation =
          new RuntimeVisibleAnnotations(
              indexRuntimeVisibleAnnotations,
              6,
              new DataInputStream(new ByteArrayInputStream(byteArray)),
              java_class.getConstantPool());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // We assume the field does not have runtime-visible annotation yet
  public static void writeFieldAttributes() {
    Field[] fields = java_class.getFields();
    Attribute[] currentAttributes = fields[fieldIndex].getAttributes();
    Attribute[] newAttributes = new Attribute[currentAttributes.length + 1];

    for (int i = 0; i < currentAttributes.length; ++i) {
      newAttributes[i] = currentAttributes[i];
    }
    newAttributes[newAttributes.length - 1] = runtimeVisibleAnnotation;

    fields[fieldIndex].setAttributes(newAttributes);
  }

  public static void writeClassAttributes() {
    Attribute[] currentAttributes = java_class.getAttributes();
    Attribute[] newAttributes = new Attribute[currentAttributes.length + 1];

    for (int i = 0; i < currentAttributes.length; ++i) {
      newAttributes[i] = currentAttributes[i];
    }
    newAttributes[newAttributes.length - 1] = runtimeVisibleAnnotation;

    java_class.setAttributes(newAttributes);
  }

  public static void dump() {
    try {
      java_class.dump(outputClassFile);
    } catch (IOException e) {
    }
  }

  public static void main(String[] args) {
    inputClassFile = args[0];
    fieldName = args.length > 1 ? args[1] : "";
    outputClassFile = "modified" + inputClassFile + ".class";

    try {
      java_class = Repository.lookupClass(inputClassFile);
    } catch (Exception e) {
    }

    InsertContendedIntoClassFile.writeConstantPool();
    InsertContendedIntoClassFile.createRuntimeVisibleAnnotationAttribute();
    if (args.length > 1) {
      InsertContendedIntoClassFile.findFieldIndex();
      InsertContendedIntoClassFile.writeFieldAttributes();
    } else {
      InsertContendedIntoClassFile.writeClassAttributes();
    }
    InsertContendedIntoClassFile.dump();
  }
}