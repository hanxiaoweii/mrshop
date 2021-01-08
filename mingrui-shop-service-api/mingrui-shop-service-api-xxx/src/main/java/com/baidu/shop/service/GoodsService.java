package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品接口")
public interface GoodsService {

    @ApiOperation(value = "查询spu信息")
    @GetMapping(value = "/goods/getSpuInfo")
    Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO);

    @ApiOperation(value = "新建商品")
    @PostMapping(value = "/goods/save")
    Result<JsonObject> save(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "修改商品")
    @PutMapping(value = "/goods/save")
    Result<JsonObject> edit(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "修改商品")
    @DeleteMapping(value = "/goods/delete")
    Result<JsonObject> deleteGoods(Integer spuId);

    @ApiOperation(value = "通过spuId查询spudetail信息")
    @GetMapping(value = "/goods/getSpuDetailBySpuId")
    Result<SpuDetailEntity> getSpuDetailBySpuId(Integer spuId);

    @ApiOperation(value = "通过spuId查询sku信息")
    @GetMapping(value = "/goods/getSkusBySpuId")
    Result<List<SkuDTO>> getSkusBySpuId(Integer spuId);

    @ApiOperation(value = "通过saleable来修改上下架")
    @PutMapping(value = "/goods/loadingOrUnloading")
    Result<JsonObject> loadingOrUnloading(@RequestBody SpuDTO spuDTO);
}
