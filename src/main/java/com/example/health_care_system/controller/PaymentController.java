package com.example.health_care_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class PaymentController {

    @GetMapping
    public String index(){
        return "index";
    }

    @GetMapping("/success")
    public String success(){
        return "success";
    }

    @GetMapping("/cancel")
    public String cancel(){
        return "cancel";
    }

    @GetMapping("/InsuranceCollection")
    public String InsuranceCollection(){
        return "InsuranceCollection";
    }

    @GetMapping("/PendingInsuranceRequest")
    public String PendingInsuranceRequest(){
        return "PendingInsuranceRequest";
    }

}
