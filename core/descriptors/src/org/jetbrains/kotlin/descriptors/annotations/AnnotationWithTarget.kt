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

package org.jetbrains.kotlin.descriptors.annotations

import org.jetbrains.kotlin.descriptors.annotations
import org.jetbrains.kotlin.name.FqName

public data class AnnotationWithTarget(val annotation: AnnotationDescriptor, val target: AnnotationUseSiteTarget?)

private class UseSiteTargetedAnnotations(
        private val original: Annotations,
        private val annotated: Annotated
) : Annotations {
 override fun isEmpty() = original.isEmpty()

 override fun findAnnotation(fqName: FqName) = original.findAnnotation(fqName)

 override fun findExternalAnnotation(fqName: FqName) = original.findExternalAnnotation(fqName)

 private fun getAdditionalTargetedAnnotations() = annotated.getAnnotations().getUseSiteTargetedAnnotations()

 override fun getUseSiteTargetedAnnotations(): List<AnnotationWithTarget> {
  return original.getUseSiteTargetedAnnotations() + getAdditionalTargetedAnnotations()
 }

 override fun getAllAnnotations(): List<AnnotationWithTarget> {
  return original.getAllAnnotations() + getAdditionalTargetedAnnotations()
 }

 override fun iterator() = original.iterator()
}

public class AnnotatedWithAdditionalAnnotations(
        delegate: Annotated?,
        additional: Annotated
) : Annotated {
 private val annotations: Annotations = UseSiteTargetedAnnotations(delegate?.getAnnotations() ?: Annotations.EMPTY, additional)

 override fun getAnnotations() = annotations
}