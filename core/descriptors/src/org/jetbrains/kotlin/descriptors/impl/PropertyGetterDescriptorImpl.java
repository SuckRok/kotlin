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

package org.jetbrains.kotlin.descriptors.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.descriptors.annotations.Annotations;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.types.JetType;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PropertyGetterDescriptorImpl extends PropertyAccessorDescriptorImpl implements PropertyGetterDescriptor {
    private JetType returnType;

    @NotNull
    private final PropertyGetterDescriptor original;

    public PropertyGetterDescriptorImpl(
            @NotNull PropertyDescriptor correspondingProperty,
            @NotNull Annotations annotations,
            @NotNull Modality modality,
            @NotNull Visibility visibility,
            boolean hasBody,
            boolean isDefault,
            @NotNull Kind kind,
            @Nullable PropertyGetterDescriptor original,
            @NotNull SourceElement source
    )
    {
        super(modality, visibility, correspondingProperty, annotations, Name.special("<get-" + correspondingProperty.getName() + ">"),
              hasBody, isDefault, kind, source);
        this.original = original != null ? original : this;
    }
    
    public void initialize(JetType returnType) {
        this.returnType = returnType == null ? getCorrespondingProperty().getType() : returnType;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Set<? extends PropertyGetterDescriptor> getOverriddenDescriptors() {
        return (Set) super.getOverriddenDescriptors(true);
    }

    @NotNull
    @Override
    public List<ValueParameterDescriptor> getValueParameters() {
        return Collections.emptyList();
    }

    @Override
    public JetType getReturnType() {
        return returnType;
    }

    @Override
    public <R, D> R accept(DeclarationDescriptorVisitor<R, D> visitor, D data) {
        return visitor.visitPropertyGetterDescriptor(this, data);
    }

    @NotNull
    @Override
    public PropertyGetterDescriptor getOriginal() {
        return this.original;
    }
}
