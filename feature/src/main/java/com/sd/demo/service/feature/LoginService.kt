package com.sd.demo.service.feature

import com.sd.demo.service.app_common.LoginService
import com.sd.demo.service.app_common.logMsg
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