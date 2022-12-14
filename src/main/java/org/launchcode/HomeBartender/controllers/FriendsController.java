package org.launchcode.HomeBartender.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FriendsController {

    @GetMapping("friendsList")
    public String search(){
        //This will return a list of users when updated
        return "/friendsList";
    }

    //This is to add a Friend to user's list & will return an updated list of Friends
    @PostMapping("friendsList_add")
    public String add(){
        //will return updated list of Friends
        return "friendsList";
    }

}
