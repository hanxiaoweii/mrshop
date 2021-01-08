package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.mapper.SpecParamMapper;
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

    @Autowired
    private SpecParamMapper specParamMapper;

    @Transactional
    @Override
    public Result<JSONObject> saveSpecParam(SpecParamDTO specParamDTO) {

        specParamMapper.insertSelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> editSpecParam(SpecParamDTO specParamDTO) {

        specParamMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> deleteSpecParam(Integer id) {

        specParamMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }

    @Override
    public Result<List<SpecParamEntity>> getSpecParamInfo(SpecParamDTO specParamDTO) {

        SpecParamEntity specParamEntity = BaiduBeanUtil.copyProperties(specParamDTO, SpecParamEntity.class);

        Example example = new Example(SpecParamEntity.class);
        Example.Criteria criteria = example.createCriteria();

        if(ObjectUtils.isNotNull(specParamEntity.getGroupId()))
            criteria.andEqualTo("groupId",specParamEntity.getGroupId());
        if(ObjectUtils.isNotNull(specParamEntity.getCid()))
            criteria.andEqualTo("cid" , specParamEntity.getCid());

        List<SpecParamEntity> specParamEntities = specParamMapper.selectByExample(example);

        return this.setResultSuccess(specParamEntities);
    }



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

        //判断规格组下是否有规格参数
        Example example = new Example(SpecParamEntity.class);
        example.createCriteria().andEqualTo("groupId" , id);
        List<SpecParamEntity> specParamEntities = specParamMapper.selectByExample(example);

        if(specParamEntities.size() >= 1 ) return this.setResultError("当前规格组下有规格参数 ，请先删除规格参数");

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
