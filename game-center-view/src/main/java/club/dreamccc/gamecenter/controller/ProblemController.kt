package club.dreamccc.gamecenter.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class ProblemController {


    @RequestMapping("/test")
    fun test(username: String?, password: String?): Any {

        return "test";
    }

}


