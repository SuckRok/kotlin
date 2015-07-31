/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.generators.protobuf

import com.google.protobuf.Descriptors
import org.jetbrains.kotlin.generators.util.GeneratorsFileUtil
import org.jetbrains.kotlin.serialization.DebugExtOptionsProtoBuf
import org.jetbrains.kotlin.serialization.DebugProtoBuf
import org.jetbrains.kotlin.serialization.jvm.DebugJvmProtoBuf
import org.jetbrains.kotlin.utils.Printer
import java.io.File
import java.util.*
import kotlin.platform.platformStatic

class GenerateProtoBufCompare {
    companion object {
        val DEST_FILE: File = File("jps-plugin/src/org/jetbrains/kotlin/jps/incremental/ProtoCompareGenerated.kt")

        platformStatic
        fun main(args: Array<String>) {
            generate(DEST_FILE)
        }

        fun generate(destFile: File) {
            GeneratorsFileUtil.writeFileIfContentChanged(destFile, GenerateProtoBufCompare().generate())
        }
    }

    private val JAVA_TYPES_WITH_INLINED_EQUALS: EnumSet<com.google.protobuf.Descriptors.FieldDescriptor.JavaType> = EnumSet.of(
            Descriptors.FieldDescriptor.JavaType.INT,
            Descriptors.FieldDescriptor.JavaType.LONG,
            Descriptors.FieldDescriptor.JavaType.FLOAT,
            Descriptors.FieldDescriptor.JavaType.DOUBLE,
            Descriptors.FieldDescriptor.JavaType.BOOLEAN,
            Descriptors.FieldDescriptor.JavaType.STRING,
            Descriptors.FieldDescriptor.JavaType.ENUM

    )

    val extentionsMap = DebugJvmProtoBuf.getDescriptor().extensions.groupBy { it.containingType }

    val doneMessages: MutableSet<Descriptors.Descriptor> = hashSetOf()
    val messages: MutableList<Descriptors.Descriptor> = arrayListOf()
    val repeatedFields: MutableList<Descriptors.FieldDescriptor> = arrayListOf()

    fun generate(): String {
        val sb = StringBuilder()
        val p = Printer(sb)
        p.println(File("license/LICENSE.txt").readText())
        p.println("package org.jetbrains.kotlin.jps.incremental")
        p.println()

        p.println("import org.jetbrains.kotlin.serialization.ProtoBuf")
        p.println("import org.jetbrains.kotlin.serialization.deserialization.NameResolver")
        p.println("import org.jetbrains.kotlin.serialization.jvm.JvmProtoBuf")
        p.println()
        p.println("/** This file is generated by org.jetbrains.kotlin.generators.protobuf.GenerateProtoBufCompare. DO NOT MODIFY MANUALLY */")
        p.println()

        p.println("open class ProtoCompareGenerated(private val oldNameResolver: NameResolver, private val newNameResolver: NameResolver) {")
        p.pushIndent()

        p.println("private val stringIdMap: MutableMap<Int, Int> = hashMapOf()")
        p.println("private val fqNameIdMap: MutableMap<Int, Int> = hashMapOf()")
        p.println()

        val fileDescriptor = DebugProtoBuf.getDescriptor()

        messages.add(fileDescriptor.findMessageTypeByName("Package"))
        messages.add(fileDescriptor.findMessageTypeByName("Class"))

        while (!messages.isEmpty()) {
            val messageDescriptor = messages.remove(0)
            doneMessages.add(messageDescriptor)
            p.println()
            generateForMessage(messageDescriptor, p)
        }

        p.println()

        repeatedFields.forEach { generateHelperMethodForRepeatedField(it, p) }

        p.println()
        generatePredefined(p)

        p.popIndent()
        p.println("}")

        return sb.toString()
    }

    fun generatePredefined(p: Printer) {
        p.println()
        p.println("fun checkStringIdEquals(old: Int, new: Int): Boolean {")
        p.println("    stringIdMap.get(old)?.let { return it == new }")
        p.println()
        p.println("    val oldValue = oldNameResolver.stringTable.getString(old)")
        p.println("    val newValue = newNameResolver.stringTable.getString(new)")
        p.println()
        p.println("    return if (oldValue == newValue) { stringIdMap[old] = new; true } else false")
        p.println("}")

        p.println()
        p.println("fun checkNameIdEquals(old: Int, new: Int): Boolean  = checkStringIdEquals(old, new)")

        p.println()
        p.println("fun checkFqNameIdEquals(old: Int, new: Int): Boolean {")
        p.println("    fqNameIdMap.get(old)?.let { return it == new }")
        p.println()
        p.println("    val oldValue = oldNameResolver.getFqName(old).asString()")
        p.println("    val newValue = newNameResolver.getFqName(new).asString()")
        p.println()
        p.println("    return if (oldValue == newValue) { fqNameIdMap[old] = new; true } else false")
        p.println("}")
    }

    fun generateForMessage(descriptor: Descriptors.Descriptor, p: Printer) {
        val typeName = descriptor.typeName()

        p.println("open fun checkEquals(old: $typeName, new: $typeName): Boolean {")
        p.pushIndent()

        descriptor.fields.forEach { generateForField(it, p) }

        extentionsMap[descriptor]?.let { it.forEach { field -> generateForExtensionField(field, p) }}

        p.println("return true")

        p.popIndent()
        p.println("}")
    }

    fun generateForField(field: Descriptors.FieldDescriptor, p: Printer) {
        val fieldName = field.name.toJavaName()
        val capFieldName = fieldName.capitalize()

        if (field.options.getExtension(DebugExtOptionsProtoBuf.skipInComparison)) return

        if (field.isRepeated) {
            repeatedFields.add(field)
            p.println("if (!${field.helperMethodName()}(old, new)) return false")
        }
        else if (field.isRequired) {
            p.printlnIfWithComparison(field, fieldName)
        }
        else if (field.isOptional) {
            p.println("if (old.has$capFieldName() != new.has$capFieldName()) return false")
            p.println("if (old.has$capFieldName()) {")
            p.printlnIfWithComparison(field, fieldName, withIndent = true)
            p.println("}")
        }

        p.println()

        addMessageTypeToProcessIfNeeded(field)
    }

    fun generateHelperMethodForRepeatedField(field: Descriptors.FieldDescriptor, p: Printer) {
        assert(field.isRepeated, "expected repeated field: ${field.name}")

        val typeName = field.containingType.typeName()
        val fieldName = field.name.toJavaName()
        val capFieldName = fieldName.capitalize()
        val methodName = field.helperMethodName()

        p.println("open fun $methodName(old: $typeName, new: $typeName): Boolean {")

        p.pushIndent()
        p.println("if (old.${fieldName}Count != new.${fieldName}Count) return false")
        p.println()
        p.println("for(i in 0..old.${fieldName}Count - 1) {")
        p.printlnIfWithComparison(field, "get$capFieldName(i)", withIndent = true)
        p.println("}")
        p.println()
        p.println("return true")

        p.popIndent()
        p.println("}")
        p.println()
    }

    fun generateForExtensionField(field: Descriptors.FieldDescriptor, p: Printer) {
        val outerClassName = field.file.options.javaOuterClassname.removePrefix("Debug")
        val fieldName = field.name.toJavaName()
        if (field.options.getExtension(DebugExtOptionsProtoBuf.skipInComparison)) return

        val fullFieldName = "$outerClassName.$fieldName"

        if (field.isRepeated) {
            p.println("if (old.getExtensionCount($fullFieldName) != new.getExtensionCount($fullFieldName)) return false")
            p.println()
            p.println("for(i in 0..old.getExtensionCount($fullFieldName) - 1) {")
            p.printlnIfWithComparison(field, "getExtension($fullFieldName, i)", withIndent = true)
            p.println("}")
            p.println()
        }
        else if (field.isRequired) {
            p.printlnIfWithComparison(field, "getExtension($fullFieldName)")
        }
        else if (field.isOptional) {
            p.println("if (old.hasExtension($fullFieldName) != new.hasExtension($fullFieldName)) return false")
            p.println("if (old.hasExtension($fullFieldName)) {")
            p.printlnIfWithComparison(field, "getExtension($fullFieldName)", withIndent = true)
            p.println("}")
        }

        p.println()

        addMessageTypeToProcessIfNeeded(field)
    }

    private fun addMessageTypeToProcessIfNeeded(field: Descriptors.FieldDescriptor) {
        if (field.javaType == Descriptors.FieldDescriptor.JavaType.MESSAGE &&
            !doneMessages.contains(field.messageType) && !messages.contains(field.messageType)) {
            messages.add(field.messageType)
        }
    }

    private fun Printer.printlnIfWithComparison(field: Descriptors.FieldDescriptor, expr: String, withIndent: Boolean = false) {
        val line = when {
            field.options.getExtension(DebugExtOptionsProtoBuf.stringIdInTable) ->
                "if (!checkStringIdEquals(old.$expr, new.$expr)) return false"
            field.options.getExtension(DebugExtOptionsProtoBuf.nameIdInTable) ->
                "if (!checkNameIdEquals(old.$expr, new.$expr)) return false"
            field.options.getExtension(DebugExtOptionsProtoBuf.fqNameIdInTable) ->
                "if (!checkFqNameIdEquals(old.$expr, new.$expr)) return false"
            field.javaType in JAVA_TYPES_WITH_INLINED_EQUALS ->
                "if (old.$expr != new.$expr) return false"
            else ->
                "if (!checkEquals(old.$expr, new.$expr)) return false"
        }
        if (withIndent) {
            this.pushIndent()
        }
        this.println(line)
        if (withIndent) {
            this.popIndent()
        }
    }

    private fun Descriptors.Descriptor.typeName(): String {
        val outerClassName = this.file.options.javaOuterClassname.removePrefix("Debug")
        val packageHeader = this.file.`package`
        return outerClassName + this.fullName.removePrefix(packageHeader)
    }

    private fun Descriptors.FieldDescriptor.helperMethodName(): String {
        val packageHeader = this.file.`package`
        val descriptor = this.containingType
        val className = descriptor.fullName.removePrefix(packageHeader).replace(".", "")
        val capFieldName = this.name.toJavaName().capitalize()
        return "checkEquals$className$capFieldName"
    }

    private fun String.toJavaName() = this.split("_").map { it.capitalize() }.join("").decapitalize()
}