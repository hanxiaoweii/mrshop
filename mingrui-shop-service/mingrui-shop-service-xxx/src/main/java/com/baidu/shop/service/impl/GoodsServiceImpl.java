package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.mapper.SpuMapper;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.utils.ObjectUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Override
    public Result<PageInfo<SpuEntity>> getSpuInfo(SpuDTO spuDTO) {

        //分页
        //判断分页参数是否为空 用自己封装得工具类 ObjectUtils的isNotNull方法 参数不为空 则继续执行(拼接sql)
        if(ObjectUtils.isNotNull(spuDTO.getPage()) && ObjectUtils.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        //前台 上架下架状态的分页
        Example example = new Example(SpuEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if(ObjectUtils.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() < 2 )
            criteria.andEqualTo("saleable" , spuDTO.getSaleable());

        //条件查询
        //模糊查询
        if(!StringUtil.isEmpty(spuDTO.getTitle()))
            criteria.andLike("title" , "%" + spuDTO.getTitle() + "%");

        List<SpuEntity> spuEntities = spuMapper.selectByExample(example);

        PageInfo<SpuEntity> spuEntityPageInfo = new PageInfo<>(spuEntities);
        return this.setResultSuccess(spuEntityPageInfo);
    }
}
