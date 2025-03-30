package com.poly.controller.response;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ProductPageResponse extends PageResponseAbstract implements Serializable {
    private List<ProductResponse> products;
}
