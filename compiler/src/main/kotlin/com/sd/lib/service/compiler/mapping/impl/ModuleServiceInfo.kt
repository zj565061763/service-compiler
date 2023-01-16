package com.sd.lib.service.compiler.mapping.impl

import com.sd.lib.service.compiler.mapping.LibClass
import com.sd.lib.service.compiler.mapping.LibProperty

internal object ModuleServiceInfo : LibClass("ModuleServiceInfo") {
    val module = LibProperty(name = "module")
    val service = LibProperty(name = "service")
    val impl = LibProperty(name = "impl")
}