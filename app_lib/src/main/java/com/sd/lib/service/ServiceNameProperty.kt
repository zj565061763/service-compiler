package com.sd.lib.service

@Target(AnnotationTarget.PROPERTY)
annotation class ServiceNameProperty(
    val name: String,
)
