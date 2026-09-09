package com.example.order.order_service.adapter.out.productService;


import com.example.order.order_service.adapter.out.productService.resposne.ProductResponse;
import com.example.order.order_service.application.port.out.LoadProductPort;
import com.example.order.order_service.application.port.out.dto.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// 동기적으로  상품 정보가 필요하니까 web 통신으로함
@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements LoadProductPort {

    private final RestClient restClient;

    @Override
    public ProductInfo getProductInfo(Long goodsId) {
        ProductResponse response = restClient
                .get()
                .uri("/product/{goodsId}", goodsId)
                .retrieve()
                .body(ProductResponse.class);

        if (response == null) throw new IllegalStateException("상품 정보를 조회할 수 없습니다.");

        return new ProductInfo(
                response.productId(),
                response.price()
        );
    }
}
