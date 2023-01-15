package com.sd.lib.service

@Target(AnnotationTarget.CLASS)
annotation class FService

@Target(AnnotationTarget.CLASS)
annotation class FServiceImpl(
    val name: String = "",
    val singleton: Boolean = false,
)