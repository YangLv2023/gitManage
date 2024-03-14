package com.neil.demo.controller;

import com.neil.demo.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/h5")
public class HBillController {


    @Qualifier("HBillServiceImpl")
    @Autowired
    private BillService billService;


    @GetMapping("/get_bill/{type}")
    public Object getBill(@PathVariable Integer type) {
        return billService.getBill();
    }

    @GetMapping("/save_bill")
    public Object saveBill() {
        return billService.saveBill();
    }
}
