package com.sd.demo.servicecompiler.service

import java.util.logging.Logger

inline fun logMsg(block: () -> String) {
    Logger.getLogger("service-compiler-demo").info(block())
}