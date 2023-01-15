package com.sd.lib.service.compiler.processor

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.sd.lib.service.compiler.mapping.LibPackage
import com.sd.lib.service.compiler.mapping.impl.FService
import com.sd.lib.service.compiler.mapping.impl.FServiceImpl
import com.sd.lib.service.compiler.mapping.impl.ServiceImplClassProvider
import com.sd.lib.service.compiler.mapping.impl.ServiceNameProperty
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

class FServiceImplProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FServiceImplProcessor(environment)
    }
}

class FServiceImplProcessor(
    env: SymbolProcessorEnvironment
) : BaseProcessor(env) {

    private val _mapModuleDeclaration: MutableMap<KSClassDeclaration, MutableSet<KSClassDeclaration>> = hashMapOf()
    private val _mapMainModule: MutableMap<String, MutableSet<String>> = hashMapOf()

    private val _mapFinal: MutableMap<String, MutableSet<String>> = hashMapOf()

    private fun addMapModuleDeclaration(key: KSClassDeclaration, value: KSClassDeclaration) {
        _mapModuleDeclaration.let { map ->
            val holder = map[key] ?: hashSetOf<KSClassDeclaration>().also {
                map[key] = it
            }
            holder.add(value)
        }

        if (isMainModule) {
            _mapMainModule.let { map ->
                val keyString = key.qualifiedName!!.asString()
                val valueString = value.qualifiedName!!.asString()
                val holder = map[keyString] ?: hashSetOf<String>().also {
                    map[keyString] = it
                }
                holder.add(valueString)
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

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(FServiceImpl.fullName).toList()
        val ret = symbols.filter { !it.validate() }

        log("---------- $moduleName process symbols:${symbols.size} ----------")

        symbols.forEach { annotated ->
            if (annotated is KSClassDeclaration && annotated.validate()) {
                findServiceInterface(annotated).also { service ->
                    addMapModuleDeclaration(service, annotated)
                }
            }
        }

        if (isMainModule) {
            resolver.getDeclarationsFromPackage(LibPackage.registerModule).forEach { declaration ->
                if (declaration is KSClassDeclaration) {
                    with(declaration.getServiceInfo()) {
                        first?.let { serviceName ->
                            log("add module sub ${declaration.simpleName.asString()} impl:${second.size}")
                            addMapFinal(serviceName, second)
                        }
                    }
                }
            }
        }

        return ret
    }

    override fun onError() {
        super.onError()
        log("---------- $moduleName onError ----------")
        _mapModuleDeclaration.clear()
        _mapMainModule.clear()
        _mapFinal.clear()
    }

    override fun finish() {
        super.finish()
        log("---------- $moduleName finish ----------")
        if (isMainModule) {
            _mapMainModule.forEach { item ->
                log("add module main ${item.key} impl:${item.value.size}")
                addMapFinal(item.key, item.value)
            }
            createFinalFiles()
        } else {
            createModuleFiles()
        }
    }

    private fun createModuleFiles() {
        val map = _mapModuleDeclaration
        map.forEach { item ->
            createModuleFile(
                service = item.key,
                listImpl = item.value,
            )
        }
    }

    private fun createModuleFile(
        /** FService子接口 */
        service: KSClassDeclaration,
        /** FService子接口的实现类 */
        listImpl: Set<KSClassDeclaration>,
    ) {
        val filename = service.qualifiedName!!.asString().replaceDot() + "_$moduleName"
        log("createModuleFile $filename impl:${listImpl.size}")

        val typeSpec = TypeSpec.classBuilder(filename)
            .addModifiers(KModifier.INTERNAL)
            .addProperty(
                PropertySpec.builder("service", STRING)
                    .initializer("%S", "")
                    .addAnnotation(
                        AnnotationSpec.builder(ServiceNameProperty.className)
                            .addMember("name = %S", service.qualifiedName!!.asString())
                            .build()
                    )
                    .build()
            )
            .apply {
                listImpl.forEachIndexed { index, item ->
                    addProperty(
                        PropertySpec.builder("impl$index", STRING)
                            .initializer("%S", "")
                            .addAnnotation(
                                AnnotationSpec.builder(ServiceNameProperty.className)
                                    .addMember("name = %S", item.qualifiedName!!.asString())
                                    .build()
                            )
                            .build()
                    )
                }
            }
            .build()

        val fileSpec = FileSpec.builder(LibPackage.registerModule, filename)
            .addType(typeSpec)
            .build()

        fileSpec.writeTo(env.codeGenerator, true)
    }

    private fun createFinalFiles() {
        val map = _mapFinal
        map.forEach { item ->
            createFinalFile(
                service = item.key,
                listImpl = item.value,
            )
        }
    }

    private fun createFinalFile(
        /** FService子接口 */
        service: String,
        /** FService子接口的实现类 */
        listImpl: Set<String>,
    ) {
        val filename = service.replaceDot()
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
            if (item.isAnnotationPresent(FService.fullName)) {
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

private fun KSAnnotated.isAnnotationPresent(fullName: String): Boolean {
    val annotations = annotations.toList()
    if (annotations.isEmpty()) return false
    for (item in annotations) {
        val declaration = item.annotationType.resolve().declaration
        val qualifiedName = declaration.qualifiedName ?: continue
        if (qualifiedName.asString() == fullName) return true
    }
    return false
}

private fun KSClassDeclaration.getServiceInfo(): Pair<String?, Set<String>> {
    val properties = getDeclaredProperties()

    val service = properties.find { it.simpleName.asString() == "service" } ?: return (null to setOf())
    val serviceName = service.getServiceNamePropertyAnnotationName() ?: return (null to setOf())

    val listImplName = mutableSetOf<String>()
    properties.forEach {
        if (it.simpleName.asString().startsWith("impl")) {
            it.getServiceNamePropertyAnnotationName()?.let { name ->
                listImplName.add(name)
            }
        }
    }
    return (serviceName to listImplName)
}

private fun KSPropertyDeclaration.getServiceNamePropertyAnnotationName(): String? {
    val annotation = annotations.find {
        val qualifiedName = it.annotationType.resolve().declaration.qualifiedName?.asString()
        qualifiedName == ServiceNameProperty.fullName
    } ?: return null

    return annotation.arguments.find {
        it.name?.asString() == "name"
    }!!.value!!.toString().also { check(it.isNotEmpty()) }
}

private fun String.replaceDot(): String {
    return this.replace(".", "_")
}