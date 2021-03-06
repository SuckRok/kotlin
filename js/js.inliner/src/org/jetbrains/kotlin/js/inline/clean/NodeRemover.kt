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

package org.jetbrains.kotlin.js.inline.clean

import com.google.dart.compiler.backend.js.ast.JsVisitorWithContextImpl
import com.google.dart.compiler.backend.js.ast.JsNode
import com.google.dart.compiler.backend.js.ast.JsContext

private class NodeRemover<T>(val klass: Class<T>, val predicate: (T) -> Boolean): JsVisitorWithContextImpl() {

    override fun <T : JsNode> doTraverse(node: T, ctx: JsContext<*>) {
        if (klass.isInstance(node)) {
            val instance = klass.cast(node)!!

            if (predicate(instance)) {
                ctx.removeMe()
                return
            }
        }

        super.doTraverse(node, ctx)
    }
}
