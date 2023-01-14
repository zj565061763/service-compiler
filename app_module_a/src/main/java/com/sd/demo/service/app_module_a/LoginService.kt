package com.sd.demo.service.app_module_a

import com.sd.demo.service.LoginService
import com.sd.demo.service.logMsg
import com.sd.lib.service.FServiceImpl

@FServiceImpl(name = "A1")
class LoginServiceA1 : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}

@FServiceImpl(name = "A2")
class LoginServiceA2 : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}