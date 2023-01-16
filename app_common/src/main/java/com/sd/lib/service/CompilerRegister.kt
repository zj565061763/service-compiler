package com.sd.lib.service

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ModuleServiceInfo(
    val service: String,
    val impl: String,
)

interface ServiceImplClassProvider {
    fun classes(): List<String>
}