package com.neil.demo.service.impl.pc;

import com.neil.demo.service.impl.BillServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class PcBillServiceImpl extends BillServiceImpl {


    @Override
    public String getBill() {
        return "pc单据公共接口";
    }
}
