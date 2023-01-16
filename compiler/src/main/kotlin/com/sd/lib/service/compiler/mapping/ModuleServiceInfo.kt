package com.sd.lib.service.compiler.mapping

internal object ModuleServiceInfo : LibClass("ModuleServiceInfo") {
    val module = LibProperty(name = "module")
    val service = LibProperty(name = "service")
    val impl = LibProperty(name = "impl")
}