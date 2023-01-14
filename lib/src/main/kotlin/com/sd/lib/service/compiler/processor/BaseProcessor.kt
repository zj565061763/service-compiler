package com.sd.lib.service.compiler.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSNode
import com.sd.lib.service.compiler.LibVersion
import com.sd.lib.service.compiler.OptionsKeyModuleName
import com.sd.lib.service.compiler.OptionsValueModuleMain

abstract class BaseProcessor(
    val env: SymbolProcessorEnvironment
) : SymbolProcessor {

    val moduleName: String
        get() {
            val moduleName = env.options[OptionsKeyModuleName]
            if (moduleName.isNullOrEmpty()) error("$OptionsKeyModuleName was not found in ksp options")
            return moduleName
        }

    val isMainModule: Boolean
        get() = OptionsValueModuleMain == moduleName

    fun log(message: String, symbol: KSNode? = null) {
        env.logger.warn("$LibVersion ${javaClass.simpleName} $message", symbol)
    }
}