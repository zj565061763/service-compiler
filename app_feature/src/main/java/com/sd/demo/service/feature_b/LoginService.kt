package com.sd.demo.service.feature_b

import com.sd.demo.service.app_common.LoginService
import com.sd.demo.service.app_common.logMsg
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