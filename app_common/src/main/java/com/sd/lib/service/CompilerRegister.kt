package com.sd.lib.service

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ModuleServiceInfo(
    val module: String,
    val service: String,
    val impl: String,
)

interface ServiceImplClassProvider {
    fun classes(): List<String>
}