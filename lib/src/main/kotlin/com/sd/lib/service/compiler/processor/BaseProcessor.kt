package com.sd.lib.service.compiler.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
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

    final override fun process(resolver: Resolver): List<KSAnnotated> {
        return processImpl(resolver)
    }

    final override fun onError() {
        super.onError()
        errorImpl()
    }

    final override fun finish() {
        super.finish()
        finishImpl()
    }

    abstract fun processImpl(resolver: Resolver): List<KSAnnotated>

    protected open fun errorImpl() {}

    protected open fun finishImpl() {}
}