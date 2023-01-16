package com.sd.lib.service.compiler.processor

import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.sd.lib.service.compiler.fIsAnnotationPresent
import com.sd.lib.service.compiler.fReplaceDot
import com.sd.lib.service.compiler.mapping.LibPackage
import com.sd.lib.service.compiler.mapping.impl.FService
import com.sd.lib.service.compiler.mapping.impl.FServiceImpl
import com.sd.lib.service.compiler.mapping.impl.ModuleServiceInfo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

class FServiceImplProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FServiceImplProcessor(environment)
    }
}

class FServiceImplProcessor(
    env: SymbolProcessorEnvironment
) : BaseProcessor(env) {

    private val _serviceHolder: MutableMap<KSClassDeclaration, MutableSet<KSClassDeclaration>> = hashMapOf()

    override fun processImpl(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(FServiceImpl.fullName).toList()
        log("---------- $moduleName process symbols:${symbols.size} ----------")
        symbols.forEach {
            if (it is KSClassDeclaration) {
                findServiceInterface(it).also { service ->
                    addService(service, it)
                }
            }
        }
        return listOf()
    }

    private fun addService(key: KSClassDeclaration, value: KSClassDeclaration) {
        _serviceHolder.let { map ->
            val holder = map[key] ?: hashSetOf<KSClassDeclaration>().also {
                map[key] = it
            }
            holder.add(value)
        }
    }

    override fun errorImpl() {
        super.errorImpl()
        log("---------- $moduleName error ----------")
    }

    override fun finishImpl() {
        super.finishImpl()
        log("---------- $moduleName finish ----------")
        createModuleFiles()
    }

    private fun createModuleFiles() {
        _serviceHolder.forEach { item ->
            createServiceFile(
                service = item.key,
                listImpl = item.value,
            )
        }
    }

    private fun createServiceFile(
        service: KSClassDeclaration,
        listImpl: Set<KSClassDeclaration>,
    ) {
        val filename = service.qualifiedName!!.asString().fReplaceDot() + "_$moduleName"
        log("createServiceFile $filename impl:${listImpl.size}")

        val typeSpec = TypeSpec.classBuilder(filename)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(
                AnnotationSpec.builder(ModuleServiceInfo.className)
                    .addMember("module = %S", moduleName)
                    .addMember("service = %S", service.qualifiedName!!.asString())
                    .addMember("impl = %S", listImpl.joinToString(separator = ",") { it.qualifiedName!!.asString() })
                    .build()
            )
            .build()

        val fileSpec = FileSpec.builder(LibPackage.registerModule, filename)
            .addType(typeSpec)
            .build()

        fileSpec.writeTo(env.codeGenerator, true)
    }
}

private fun findServiceInterface(source: KSClassDeclaration): KSClassDeclaration {
    require(source.classKind == ClassKind.CLASS) { "@${FServiceImpl.simpleName} should be used in ClassKind.CLASS" }
    require(!source.isAbstract()) { "@${FServiceImpl.simpleName} should not be used in abstract class" }

    var ret: KSClassDeclaration? = null

    var current = source
    while (true) {
        val superInfo = current.getSuperInfo()
        val interfaces = superInfo.second
        if (interfaces.isEmpty()) break

        for (item in interfaces) {
            if (item.fIsAnnotationPresent(FService.fullName)) {
                if (ret == null) {
                    ret = item
                } else {
                    error("More than one service interface present in ${source.qualifiedName!!.asString()}")
                }
            }
        }

        current = superInfo.first ?: break
    }

    return checkNotNull(ret) {
        "Interface marked with annotation @${FService.simpleName} was not found in ${source.qualifiedName!!.asString()} super types"
    }
}

private fun KSClassDeclaration.getSuperInfo(): Pair<KSClassDeclaration?, List<KSClassDeclaration>> {
    val superTypes = superTypes.toList()
    if (superTypes.isEmpty()) return (null to listOf())

    var parent: KSClassDeclaration? = null
    val interfaces = mutableListOf<KSClassDeclaration>()

    for (item in superTypes) {
        val declaration = item.resolve().declaration
        if (declaration !is KSClassDeclaration) continue

        when (declaration.classKind) {
            ClassKind.CLASS -> {
                check(parent == null)
                parent = declaration
            }
            ClassKind.INTERFACE -> interfaces.add(declaration)
            else -> {}
        }
    }

    return (parent to interfaces)
}