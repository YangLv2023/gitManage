package com.neil.demo.service.impl;

import com.neil.demo.service.BillService;
import org.springframework.stereotype.Service;


@Service
public abstract class BillServiceImpl implements BillService {


    @Override
    public abstract String getBill();

    @Override
    public String saveBill() {
        return "所有单据公共保存接口";
    }
}
