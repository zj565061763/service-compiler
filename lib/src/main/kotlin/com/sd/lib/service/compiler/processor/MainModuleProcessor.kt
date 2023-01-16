package com.sd.lib.service.compiler.processor

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.sd.lib.service.compiler.fGetAnnotation
import com.sd.lib.service.compiler.fGetValue
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
) : BaseProcessor(env) {

    private val _mapFinal: MutableMap<String, MutableSet<String>> = hashMapOf()


    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!isMainModule) return listOf()

        val symbols = resolver.getDeclarationsFromPackage(LibPackage.registerModule).toList()
        val ret = symbols.filter { !it.validate() }

        log("---------- $moduleName process symbols:${symbols.size} ----------")

        symbols.forEach {
            if (it is KSClassDeclaration && it.validate()) {
                addMapFinalFromModule(it)
            }
        }

        return ret
    }

    private fun addMapFinalFromModule(declaration: KSClassDeclaration) {
        with(declaration.getServiceInfo()) {
            first?.let { serviceName ->
                log("add module ${declaration.simpleName.asString()} impl:${second.size}")
                addMapFinal(serviceName, second)
            }
        }
    }

    private fun addMapFinal(key: String, values: Set<String>) {
        _mapFinal.let { map ->
            val holder = map[key] ?: hashSetOf<String>().also {
                map[key] = it
            }
            holder.addAll(values)
        }
    }

    override fun onError() {
        super.onError()
        if (!isMainModule) return
        log("---------- $moduleName onError ----------")
        _mapFinal.clear()
    }

    override fun finish() {
        super.finish()
        if (!isMainModule) return
        log("---------- $moduleName finish ----------")
        _mapFinal.forEach { item ->
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
}

private fun KSClassDeclaration.getServiceInfo(): Pair<String?, Set<String>> {
    val annotation = fGetAnnotation(ModuleServiceInfo.fullName) ?: return (null to setOf())

    val serviceArgument = annotation.fGetValue("service") ?: error("member 'service' not found.")
    val serviceArgumentValue = serviceArgument.value?.toString() ?: ""
    if (serviceArgumentValue.isEmpty()) error("member 'service' value is empty.")

    val implArgument = annotation.fGetValue("impl") ?: error("member 'impl' not found.")
    val implArgumentValue = implArgument.value?.toString() ?: ""
    if (implArgumentValue.isEmpty()) error("member 'impl' value is empty.")

    val implNames = implArgumentValue.split(",").toSet()
    return (serviceArgumentValue to implNames)
}