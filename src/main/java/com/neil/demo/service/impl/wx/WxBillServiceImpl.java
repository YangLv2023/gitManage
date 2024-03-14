package com.neil.demo.service.impl.wx;

import com.neil.demo.service.impl.BillServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class WxBillServiceImpl extends BillServiceImpl {


    @Override
    public String getBill() {
        return "wx单据公共接口";
    }
}
