package com.sd.lib.service.compiler.processor

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.sd.lib.service.compiler.fGetAnnotation
import com.sd.lib.service.compiler.fGetArgument
import com.sd.lib.service.compiler.fReplaceDot
import com.sd.lib.service.compiler.mapping.LibPackage
import com.sd.lib.service.compiler.mapping.impl.ModuleServiceInfo
import com.sd.lib.service.compiler.mapping.impl.ServiceImplClassProvider
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

class MainModuleProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MainModuleProcessor(environment)
    }
}

class MainModuleProcessor(
    env: SymbolProcessorEnvironment
) : BaseProcessor(env, main = true) {

    private val _serviceHolder: MutableMap<String, MutableSet<String>> = hashMapOf()

    @OptIn(KspExperimental::class)
    override fun processImpl(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getDeclarationsFromPackage(LibPackage.registerModule).toList()
        val ret = symbols.filter { !it.validate() }

        log("---------- $moduleName process symbols:${symbols.size} ----------")

        symbols.forEach {
            if (it.validate() && it is KSClassDeclaration) {
                addServiceFromModule(it)
            }
        }

        return ret
    }

    private fun addServiceFromModule(declaration: KSClassDeclaration) {
        declaration.getServiceInfo()?.let {
            log("(${it.module}) (${it.service}) (impl:${it.implNames.size})")
            addService(it.service, it.implNames)
        }
    }

    private fun addService(key: String, values: Set<String>) {
        _serviceHolder.let { map ->
            val holder = map[key] ?: hashSetOf<String>().also {
                map[key] = it
            }
            holder.addAll(values)
        }
    }

    override fun errorImpl() {
        super.errorImpl()
        log("---------- $moduleName error ----------")
        _serviceHolder.clear()
    }

    override fun finishImpl() {
        super.finishImpl()
        log("---------- $moduleName finish ----------")
        _serviceHolder.forEach { item ->
            createFinalFile(
                service = item.key,
                listImpl = item.value,
            )
        }
    }

    private fun createFinalFile(
        service: String,
        listImpl: Set<String>,
    ) {
        val filename = service.fReplaceDot()
        log("createFinalFile $filename impl:${listImpl.size}")

        val typeSpec = TypeSpec.classBuilder(filename)
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(ServiceImplClassProvider.className)
            .addFunction(
                FunSpec.builder("classes")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(LIST.parameterizedBy(STRING))
                    .addCode("return listOf(\n")
                    .apply {
                        listImpl.forEach {
                            addCode("  \"$it\"")
                            addCode(",\n")
                        }
                    }
                    .addCode(")")
                    .build()
            )
            .build()

        val fileSpec = FileSpec.builder(LibPackage.register, filename)
            .addType(typeSpec)
            .build()

        fileSpec.writeTo(env.codeGenerator, Dependencies.ALL_FILES)
    }

    companion object {
        private var sIsLocked = false

        internal fun lock() {
            synchronized(this@Companion) {
                sIsLocked = true
            }
        }

        internal fun unlock() {
            synchronized(this@Companion) {
                sIsLocked = false
            }
        }
    }
}

private fun KSClassDeclaration.getServiceInfo(): ServiceInfo? {
    val annotation = fGetAnnotation(ModuleServiceInfo.fullName) ?: return null

    val module = annotation.fGetArgument("module") ?: error("member 'module' not found.")
    val moduleValue = module.value?.toString() ?: ""
    if (moduleValue.isEmpty()) error("member 'module' value is empty.")

    val service = annotation.fGetArgument("service") ?: error("member 'service' not found.")
    val serviceValue = service.value?.toString() ?: ""
    if (serviceValue.isEmpty()) error("member 'service' value is empty.")

    val impl = annotation.fGetArgument("impl") ?: error("member 'impl' not found.")
    val implValue = impl.value?.toString() ?: ""
    if (implValue.isEmpty()) return null

    if (!implValue.contains(",")) return null
    val implNames = implValue.split(",").toSet()
    if (implNames.isEmpty()) return null

    return ServiceInfo(
        module = moduleValue,
        service = serviceValue,
        implNames = implNames,
    )
}

private data class ServiceInfo(
    val module: String,
    val service: String,
    val implNames: Set<String>,
)