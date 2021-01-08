package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "品牌接口")
public interface BrandService {

    @ApiOperation(value = "获取品牌信息")
    @GetMapping(value = "brand/list")
    Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO);

    @PostMapping(value = "brand/save")
    @ApiOperation(value = "增加品牌")
    Result<JSONObject> saveBranInfo(@RequestBody BrandDTO brandDTO);

    @PutMapping(value = "brand/save")
    @ApiOperation(value = "增加品牌")
    Result<JSONObject> editBranInfo(@RequestBody BrandDTO brandDTO);

    @ApiOperation("通过ID删除品牌")
    @DeleteMapping(value = "brand/delete")
    Result<JSONObject> deleteBrandInfo(Integer id);

    @ApiOperation(value = "通过分类id获取品牌")
    @GetMapping(value = "brand/getBrandInfoByCategoryId")
    Result<List<BrandEntity>> getBrandInfoByCategoryId(Integer cid);
}
