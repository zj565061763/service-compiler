package com.sd.lib.service.compiler.mapping

internal object FService : LibClass(simpleName = "FService")

internal object FServiceImpl : LibClass("FServiceImpl")

internal object ModuleServiceInfo : LibClass("ModuleServiceInfo") {
    val module = LibProperty(name = "module")
    val service = LibProperty(name = "service")
    val impl = LibProperty(name = "impl")
}

internal object ServiceImplClassProvider : LibClass(simpleName = "ServiceImplClassProvider")