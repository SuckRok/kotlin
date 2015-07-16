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

package org.jetbrains.kotlin.load.java.lazy

import org.jetbrains.kotlin.descriptors.annotations.AnnotationWithTarget
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.load.java.lazy.descriptors.resolveAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.DescriptorUtils

class LazyJavaAnnotations(
        private val c: LazyJavaResolverContext,
        private val annotationOwner: JavaAnnotationOwner
) : Annotations {
    private val annotationDescriptors = c.storageManager.createMemoizedFunctionWithNullableValues {
        annotation: JavaAnnotation ->
        c.resolveAnnotation(annotation)
    }

    override fun findAnnotation(fqName: FqName) =
            annotationOwner.findAnnotation(fqName)?.let(annotationDescriptors)

    override fun findExternalAnnotation(fqName: FqName) =
            c.externalAnnotationResolver.findExternalAnnotation(annotationOwner, fqName)?.let(annotationDescriptors)

    override fun getUseSiteTargetedAnnotations() = emptyList<AnnotationWithTarget>()

    override fun getAllAnnotations() = this.map { AnnotationWithTarget(it, null) }

    override fun iterator() =
            annotationOwner.getAnnotations().asSequence().map(annotationDescriptors).filterNotNull().iterator()

    override fun isEmpty() = !iterator().hasNext()
}

fun LazyJavaResolverContext.resolveAnnotations(annotationsOwner: JavaAnnotationOwner): Annotations
        = LazyJavaAnnotations(this, annotationsOwner)
