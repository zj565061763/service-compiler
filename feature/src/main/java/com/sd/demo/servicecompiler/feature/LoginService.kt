package com.sd.demo.servicecompiler.feature

import com.sd.demo.servicecompiler.service.LoginService
import com.sd.demo.servicecompiler.service.logMsg
import com.sd.lib.service.FServiceImpl

@FServiceImpl(name = "F1")
class LoginServiceF1 : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}

@FServiceImpl(name = "F2")
class LoginServiceF2 : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}