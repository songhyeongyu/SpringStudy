package hello.springmvc.basic.requestmapping;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mapping/users")
public class MappingClassController {

    @GetMapping
    public String users() {
        return "get users";
    }

    @PostMapping
    public String addUser() {
        return "post user";
    }

    @GetMapping("/{usersId}")
    public String findUser(@PathVariable String usersId) {
        return "get userId =" + usersId;
    }

    @PatchMapping("/{usersId}")
    public String updateUser(@PathVariable String usersId) {
        return "update userId =" + usersId;
    }

    @DeleteMapping("/{usersId}")
    public String deleteUser(@PathVariable String usersId) {
        return "delete userId =" + usersId;
    }
}
