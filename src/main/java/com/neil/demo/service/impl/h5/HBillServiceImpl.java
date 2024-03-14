package com.neil.demo.service.impl.h5;

import com.neil.demo.service.impl.BillServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class HBillServiceImpl extends BillServiceImpl {


    @Override
    public String getBill() {
        return "H5单据公共接口";
    }
}
