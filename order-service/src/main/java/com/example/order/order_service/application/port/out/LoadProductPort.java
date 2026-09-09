package com.example.order.order_service.application.port.out;

import com.example.order.order_service.application.port.out.dto.ProductInfo;

public interface LoadProductPort {
    ProductInfo getProductInfo(Long goodsId);
}