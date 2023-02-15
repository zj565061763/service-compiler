package com.sd.lib.service.compiler.mapping

internal object FService : LibClass("FService")

internal object FServiceImpl : LibClass("FServiceImpl")

internal object ModuleServiceInfo : LibClass("ModuleServiceInfo") {
    val module = LibProperty("module")
    val service = LibProperty("service")
    val impl = LibProperty("impl")
}

internal object ServiceImplClassProvider : LibClass("ServiceImplClassProvider")