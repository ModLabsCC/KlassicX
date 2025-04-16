package cc.modlabs.klassicx.tools

import com.google.common.reflect.ClassPath
import dev.fruxz.ascend.extension.logging.getThisFactoryLogger
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class ClazzLoader(private val baseName: String) {
    private val logger = getThisFactoryLogger()

    fun <T : Any> loadClassesInPackage(
        packageName: String,
        clazzType: KClass<T>
    ): List<KClass<out T>> {
        try {
            val classLoader = this.javaClass.classLoader
            val allClasses = ClassPath.from(classLoader).allClasses
                .filter { it.packageName.startsWith(packageName) }

            val classes = mutableListOf<KClass<out T>>()
            for (classInfo in allClasses) {
                if (!classInfo.name.startsWith(baseName)) continue
                if (classInfo.packageName.startsWith(packageName) && !classInfo.name.contains('$')) {
                    try {
                        val loadedClass = classInfo.load().kotlin
                        // Use isAssignableFrom to ensure the loaded class is of the correct type
                        if (clazzType.java.isAssignableFrom(loadedClass.java)) {
                            classes.add(loadedClass as KClass<out T>)
                        }
                    } catch (exception: Exception) {
                        // Ignore classes that cannot be loaded
                    }
                }
            }
            return classes
        } catch (exception: Exception) {
            logger.error("Failed to load classes", exception)
            return emptyList()
        }
    }
}