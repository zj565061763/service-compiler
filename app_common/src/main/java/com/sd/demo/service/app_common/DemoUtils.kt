package com.sd.demo.service.app_common

import java.util.logging.Logger

inline fun logMsg(block: () -> String) {
    Logger.getLogger("service-compiler-demo").info(block())
}