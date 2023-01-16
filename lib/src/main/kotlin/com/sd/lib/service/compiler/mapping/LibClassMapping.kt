package com.sd.lib.service.compiler.mapping

import com.squareup.kotlinpoet.ClassName

internal open class LibClassMapping(
    val simpleName: String,
) {
    val packageName: String = LibPackage.main

    val fullName: String = "$packageName.$simpleName"

    val className: ClassName get() = ClassName(packageName, simpleName)
}