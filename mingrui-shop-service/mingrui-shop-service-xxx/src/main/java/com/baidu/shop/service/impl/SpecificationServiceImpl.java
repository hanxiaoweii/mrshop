package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.service.SpecificationService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@RestController
public class SpecificationServiceImpl extends BaseApiService implements SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Transactional
    @Override
    public Result<JSONObject> saveSpecGroup(SpecGroupDTO specGroupDTO) {
        specGroupMapper.insertSelective(BaiduBeanUtil.copyProperties(specGroupDTO,SpecGroupEntity.class));
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> editSpecGroup(SpecGroupDTO specGroupDTO) {
        specGroupMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specGroupDTO,SpecGroupEntity.class));
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> deleteSpecGroup(Integer id) {
        specGroupMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<SpecGroupEntity>> getSpecGroupInfo(SpecGroupDTO SpecGroupDTO) {
        //通过分类Id查询数据
        Example example = new Example(SpecGroupEntity.class);
        example.createCriteria()
                .andEqualTo("cid", BaiduBeanUtil.copyProperties(SpecGroupDTO,SpecGroupDTO.class).getCid());
        List<SpecGroupEntity> specGroupEntities = specGroupMapper.selectByExample(example);
        return this.setResultSuccess(specGroupEntities);
    }


}
