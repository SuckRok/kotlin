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

package org.jetbrains.kotlin.idea.caches

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.HashSet
import com.intellij.util.indexing.*
import com.intellij.util.io.ExternalIntegerKeyDescriptor
import org.jetbrains.kotlin.idea.util.application.runReadAction
import java.util.ArrayList

public object JarMetaInformationIndex : ScalarIndexExtension<Int>() {
    override fun dependsOnFileContent(): Boolean = false
    override fun getKeyDescriptor() = ExternalIntegerKeyDescriptor()
    override fun getName(): ID<Int, Void> = ID.create<Int, Void>(JarMetaInformationIndex::class.qualifiedName)
    override fun getVersion(): Int = 0

    override fun getInputFilter() = FileBasedIndex.InputFilter() {
        file: VirtualFile -> file.getUrl().startsWith("jar://") && file.getUrl().endsWith("!/")
    }

    override fun indexDirectories(): Boolean = true

    private val collectors: MutableList<JarMetaInformationCollector<*>> = ArrayList()

    public fun register(collector: JarMetaInformationCollector<*>) {
        collectors.add(collector)
    }

    override fun getIndexer() = INDEXER

    private val INDEXER = DataIndexer<Int, Void, FileContent>() { inputData: FileContent ->
        val jarFile = findJarFile(inputData.getFile())
        if (jarFile != null) {
            collectors.forEach { collector ->
//                println("Canceled: $jarFile ${jarFile.hashCode()} $collector")
                jarFile.putUserData(collector.key, null)
            }
        }

        // Do not store anything
        mapOf()
    }


    // Sdk list can be outdated if some new jdks are added
    val allJDKRoots = ProjectJdkTable.getInstance().getAllJdks().flatMapTo(HashSet<VirtualFile>()) { jdk ->
        jdk.rootProvider.getFiles(OrderRootType.CLASSES).toList()
    }

    public fun <T: Any> getValue(collector: JarMetaInformationCollector<T>, file: VirtualFile): T? {
        val jarFile = findJarFile(file) ?: return null

        if (VfsUtilCore.isUnder(jarFile, allJDKRoots)) return collector.countForSdkJar(jarFile)

        val kotlinState = jarFile.getUserData(collector.key)
        if (kotlinState != null) {
            return kotlinState
        }

        scheduleCountJarState(collector, jarFile)

        return null
    }

    private fun findJarFile(file: VirtualFile): VirtualFile? {
        if (!file.getUrl().startsWith("jar://")) return null

        var jarFile = file
        while (jarFile.getParent() != null) jarFile = jarFile.getParent()

        return jarFile
    }

    private fun <T: Any> scheduleCountJarState(collector: JarMetaInformationCollector<T>, jarFile: VirtualFile) {
        if (jarFile.getUserData(collector.key) != null) return

        jarFile.putUserData(collector.key, collector.init)

//        println("Scheduled: $jarFile ${jarFile.hashCode()} $collector")

        ApplicationManager.getApplication().executeOnPooledThread {
            runReadAction {
                var result = jarFile.getUserData(collector.key)!!

                VfsUtilCore.processFilesRecursively(jarFile) { file ->
                    result = collector.count(result, file)

                    // Continue while no result found
                    result != collector.stopAt
                }

//                println("Finished: $jarFile ${jarFile.hashCode()} $collector $result")

                jarFile.putUserData(collector.key, result)
            }
        }
    }

    interface JarMetaInformationCollector<T> {
        val key: Key<T>
        val init: T
        val stopAt: T

        fun count(result: T, nextFile: VirtualFile): T
        fun countForSdkJar(file: VirtualFile): T
    }
}
