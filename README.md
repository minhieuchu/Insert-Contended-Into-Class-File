# Insert-Contended-Into-Class-File
Insert `@Contended` annotation into Java class files

## Introduction
`@Contended` annotation has been available from Java 8 to help programmers add padding to separate contended fields or classes
to avoid false sharing. Originally, this annotation needs to be inserted directly into source code. When false sharing occurs
where source code access is not possible, such as in class files, `@Contended` is not applicable.

To improve this limitation, our tool inserts the annotation into Java class files instead of source code.

## Usage

To compile / execute the program, Apache Commons BCEL 6.5.0 is necessary. Other versions may also work but we have not tested.

### Compile

```
javac -cp .:bcel-6.5.0.jar InsertContendedIntoClassFile.java
```

### Insert the annotation for the field `myField` in the class file `MyClass.class`

```
java -cp .:bcel-6.5.0.jar InsertContendedIntoClassFile MyClass myField
```

### Insert the annotation for the class `MyClass` itself in the class file `MyClass.class`

```
java -cp .:bcel-6.5.0.jar InsertContendedIntoClassFile MyClass
```

## Limitations

At the moment, fields can only be specified by names. We can improve the program to let programmers specify fields by both names and descriptors, but this will require programmers to input field descriptors.

We assume that fields or classes to be annotated do not have any runtime visible annotation before insertion. Existing annotations will be overriden. This could simply be improved by adding an annotation entry for `@Contended` to the current annotation table, instead of overriding it. 

Our tool currently only supports inserting the annotation for specific fields or classes. The usage of `@Contended` also allows specifying 
contention groups by passing the group name as a parameter to the annotation. We consider to support this use case in the near future.
