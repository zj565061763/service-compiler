package com.sd.lib.service.compiler.mapping

import com.squareup.kotlinpoet.ClassName

internal object LibPackage {
    const val main = "com.sd.lib.service"

    const val register = "$main.register"

    const val registerModule = "$register.module"
}

internal open class LibClass(
    val simpleName: String,
) {
    val packageName: String = LibPackage.main
    val fullName: String = "$packageName.$simpleName"
}

internal data class LibProperty(
    val name: String
) {
    override fun toString(): String {
        return name
    }
}

internal fun LibClass.className(): ClassName {
    return ClassName(packageName, simpleName)
}