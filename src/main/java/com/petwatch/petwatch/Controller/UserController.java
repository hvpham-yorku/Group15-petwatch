package com.petwatch.petwatch.Controller;

import com.petwatch.petwatch.DAO.UserDAO;
import com.petwatch.petwatch.Model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {

    UserDAO userDAO;
    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @GetMapping("/user/id/{id}")
    public User getUser(@PathVariable int id) {
        return userDAO.getUserById(id);
    }

    @GetMapping("/user/email/{email}")
    public User getUser(@PathVariable String email) {
        return userDAO.getUserByEmail(email);
    }

    @PostMapping("/user")
    public String saveUser(@RequestBody User user) {
        userDAO.addUser(user);
        return "login-choice";
    }

    @RequestMapping(value = "/validateUser", method = RequestMethod.GET)
    public String validateUser(@ModelAttribute  User user, HttpServletRequest request) {
        var dbUser = userDAO.getUserByEmail(user.getEmail());

        if(user.getEmail().equals(dbUser.getEmail()) && user.getPassword().equals(dbUser.getPassword()))  {
            return "job-posting"; //placeholder for user dashboard
        }
        System.out.println("user email: " + dbUser.getEmail());
        return "redirect:" + request.getHeader("Referer");
    }

    @RequestMapping(value = "/saveUser", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute  User user) {
        System.out.println(user.toString());
        var dbUser = userDAO.addUser(user);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("job-posting");
        modelAndView.addObject(user);
        return modelAndView;
    }
}
