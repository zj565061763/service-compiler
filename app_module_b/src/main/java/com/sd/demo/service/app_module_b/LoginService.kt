package com.sd.demo.service.app_module_b

import com.sd.demo.service.LoginService
import com.sd.demo.service.logMsg
import com.sd.lib.service.FServiceImpl

@FServiceImpl(name = "B1")
class LoginServiceB1 : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}

@FServiceImpl(name = "B2")
class LoginServiceB2 : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}