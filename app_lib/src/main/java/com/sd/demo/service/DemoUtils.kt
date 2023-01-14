package com.sd.demo.service

import java.util.logging.Logger

inline fun logMsg(block: () -> String) {
    Logger.getLogger("service-demo").info(block())
}