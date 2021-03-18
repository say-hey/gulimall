package com.wkl.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {
    private String id;

    private String name;

    private String catalog1Id;
    //三级菜单
    private List<Catelog3Vo> catalog3List;
}
