package com.sd.lib.service.compiler

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueArgument

internal const val LibVersion = "1.0.0-alpha03"

const val OptionsKeyModuleName = "FSERVICE_MODULE_NAME"
const val OptionsValueModuleMain = "FSERVICE_MODULE_MAIN"

internal fun KSAnnotated.fIsAnnotationPresent(fullName: String): Boolean {
    return fGetAnnotation(fullName) != null
}

fun KSAnnotated.fGetAnnotation(fullName: String): KSAnnotation? {
    return annotations.find {
        fullName == it.annotationType.resolve().declaration.qualifiedName?.asString()
    }
}

fun KSAnnotation.fGetValue(name: String): KSValueArgument? {
    return arguments.find {
        it.name?.asString() == name
    }
}

internal fun String.fReplaceDot(): String {
    return this.replace(".", "_")
}