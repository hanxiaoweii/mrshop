package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.PinYinUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Transactional
    @Override
    public Result<JSONObject> deleteBrandInfo(Integer id) {
        brandMapper.deleteByPrimaryKey(id);

        this.getCategoryBrandByBrandId(id);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> editBranInfo(BrandDTO brandDTO) {

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        brandEntity.setLetter(PinYinUtil.getUpperCase(String.valueOf(brandEntity.getName().toCharArray()[0]),false).toCharArray()[0]);
        brandMapper.updateByPrimaryKeySelective(brandEntity);

        //先通过brandId删除中间表的数据
        this.getCategoryBrandByBrandId(brandDTO.getId());

        //批量新增 ||新增
        //分类集合的字符串
        this.insertCategoryBrandList(brandDTO.getCategories(),brandEntity.getId());

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> saveBranInfo(BrandDTO brandDTO) {
        //新增返回主键
        //两种方式实现 select-key insert加两个属性
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        //处理品牌首字母
        brandEntity.setLetter(PinYinUtil.getUpperCase(String.valueOf(brandEntity.getName().toCharArray()[0]),false).toCharArray()[0]);
        brandMapper.insertSelective(brandEntity);

        //维护中间表数据
//        String categories = brandDTO.getCategories();//得到分类集合字符串
//        if(StringUtils.isEmpty(brandDTO.getCategories())) return this.setResultError("");
//
//        //判断分类集合字符串中是否包含,
//        if(categories.contains(",")){
////            String[] split = categories.split(",");
////            categoryBrandMapper.insertList(Arrays.asList(split).stream().map(categoryIdStr -> {
////                CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
////                categoryBrandEntity.setCategoryId(Integer.valueOf(categoryIdStr));
////                categoryBrandEntity.setBrandId(brandEntity.getId());
////                return categoryBrandEntity;
////           }).collect(Collectors.toList()));
//            categoryBrandMapper.insertList(
//                    Arrays.asList(categories.split(","))
//                            .stream()
//                            .map(categoryIdStr -> new CategoryBrandEntity(Integer.valueOf(categoryIdStr),brandEntity.getId()))
//                            .collect(Collectors.toList())
//            );
//        }else{//普通单个新增
//
//            CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
//            categoryBrandEntity.setBrandId(brandEntity.getId());
//            categoryBrandEntity.setCategoryId(Integer.valueOf(categories));
//
//            categoryBrandMapper.insertSelective(categoryBrandEntity);
//        }

        this.insertCategoryBrandList(brandDTO.getCategories(),brandEntity.getId());
        return this.setResultSuccess();
    }



    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());

        //判断排序字段是否为空 sort
        if(!StringUtils.isEmpty(brandDTO.getSort())) PageHelper.orderBy(brandDTO.getOrderBy());


        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        Example example = new Example(BrandEntity.class);
        example.createCriteria().andLike("name","%"+brandEntity.getName()+"%");

        List<BrandEntity> brandEntities = brandMapper.selectByExample(example);
        PageInfo<BrandEntity> pageInfo = new PageInfo<>(brandEntities);

        return this.setResultSuccess(pageInfo);
    }


    //先通过brandId删除中间表的数据
    private void getCategoryBrandByBrandId(Integer id){
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        categoryBrandMapper.deleteByExample(example);
    }

    private void insertCategoryBrandList(String categories, Integer id){

        if(StringUtils.isEmpty(categories)) throw new RuntimeException("分类信息不能为空");

        //判断分类集合中是否包含,号
        if(categories.contains(",")){
            categoryBrandMapper.insertList(
                    Arrays.asList(categories.split(","))
                            .stream()
                            .map(categoryIdStr -> new CategoryBrandEntity(Integer.valueOf(categoryIdStr)
                                    , id))
                            .collect(Collectors.toList()));
        }else{
            CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
            categoryBrandEntity.setBrandId(id);
            categoryBrandEntity.setCategoryId(Integer.valueOf(categories));

            categoryBrandMapper.insertSelective(categoryBrandEntity);
        }
    }

}
