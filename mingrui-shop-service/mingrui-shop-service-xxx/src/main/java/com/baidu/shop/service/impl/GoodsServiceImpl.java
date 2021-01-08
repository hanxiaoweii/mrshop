package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.dto.SpuDetailDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;


import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private StockMapper stockMapper;


    @Override
    @Transactional
    public Result<JsonObject> loadingOrUnloading(SpuDTO spuDTO) {
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        if(ObjectUtils.isNotNull(spuEntity.getSaleable()) && spuEntity.getSaleable() < 2 ){
            if(spuEntity.getSaleable() == 1){
                spuEntity.setSaleable(0);
            }else{
                spuEntity.setSaleable(1);
            }
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess();
        }
        return this.setResultError("失败了");
    }

    @Override
    @Transactional
    public Result<JsonObject> deleteGoods(Integer spuId) {

        //删除spu
        spuMapper.deleteByPrimaryKey(spuId);

        //删除spuDetail
        spuDetailMapper.deleteByPrimaryKey(spuId);

        //通过spuId查询sku信息
        //删除sku stock
        this.deleteSkusAndStock(spuId);

        return this.setResultSuccess();
    }

    private void deleteSkusAndStock(Integer spuId){
        //通过spuId查询sku信息
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo(spuId);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        //得到skuId集合
        List<Long> collect = skuEntities.stream().map(skuEntity -> skuEntity.getId()).collect(Collectors.toList());
        skuMapper.deleteByIdList(collect);//通过skuId集合删除sku信息
        stockMapper.deleteByIdList(collect);//通过skuId集合删除stock信息
    }

    @Override
    @Transactional
    public Result<JsonObject> edit(SpuDTO spuDTO) {

        final Date date = new Date();

        //修改spu
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);

        //修改spuDetail
        spuDetailMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail() , SpuDetailEntity.class));

        //删除表中信息 再进行新增
        this.deleteSkusAndStock(spuEntity.getId());

        //新增sku 新增stock
        this.saveSkusAndStockInfo(spuDTO,date,spuEntity.getId());

        return this.setResultSuccess();
    }

    private void saveSkusAndStockInfo(SpuDTO spuDTO , Date date,Integer spuId){
        //新增sku List集合 要一条一条添加
        List<SkuDTO> skus = spuDTO.getSkus();
        skus.stream().forEach(skuDTO -> {

            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuId);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            //新增stock stock表中属性SkuId为空 要用skuEntity新增返回主键 赋值
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);

        });
    }

    @Override
    public Result<SpuDetailEntity> getSpuDetailBySpuId(Integer spuId) {

        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);

        return this.setResultSuccess(spuDetailEntity);
    }

    @Override
    public Result<List<SkuDTO>> getSkusBySpuId(Integer spuId) {

        List<SkuDTO> list = skuMapper.getSkusAndStockBySpuId(spuId);

        return this.setResultSuccess(list);
    }

    @Override
    @Transactional
    public Result<JsonObject> save(SpuDTO spuDTO) {
        final Date date = new Date();
        //新增spu  新增返回主键，给必要的字段赋默认值
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        spuMapper.insertSelective(spuEntity);

        //新增spuDetail
        SpuDetailDTO spuDetail = spuDTO.getSpuDetail();
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDetail, SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuEntity.getId());
        spuDetailMapper.insertSelective(spuDetailEntity);
        //新增sku 新增stock
        this.saveSkusAndStockInfo(spuDTO,date,spuEntity.getId());

        return this.setResultSuccess();
    }

    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {

        //分页
        //判断分页参数是否为空 用自己封装得工具类 ObjectUtils的isNotNull方法 参数不为空 则继续执行(拼接sql)
        if(ObjectUtils.isNotNull(spuDTO.getPage()) && ObjectUtils.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        //排序
        if(!StringUtil.isEmpty(spuDTO.getSort()) && !StringUtil.isEmpty(spuDTO.getOrder()))
            PageHelper.orderBy(spuDTO.getOrderBy());

        //将spuEntity放入Example
        //new 一个Example是为了方便以后万一有条件判断的话，可以直接抽取出来使用
        Example example = new Example(SpuEntity.class);
        Example.Criteria criteria = example.createCriteria();

        //前台 上架下架状态的分页
        //判断上架下架属性saleable是否不为空 值是否小于2的  true
        if(ObjectUtils.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() < 2 )
            criteria.andEqualTo("saleable" , spuDTO.getSaleable());

        //条件查询
        //判断标题是否为空  也就是页面的搜索框  用作模糊查询
        if(!StringUtil.isEmpty(spuDTO.getTitle()))
            criteria.andLike("title" , "%" + spuDTO.getTitle() + "%");

        //把查询结果放到List集合中
        List<SpuEntity> spuEntities = spuMapper.selectByExample(example);


        List<SpuDTO> collect1 = spuEntities.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);

            //通过分类Id集合查询数据
            List<CategoryEntity> categoryEntities = categoryMapper.selectByIdList(Arrays.asList(spuEntity.getCid1(), spuEntity.getCid2(),
                    spuEntity.getCid3()));

            //遍历集合并将分类名称用/分割
            String collect = categoryEntities.stream().map(categoryEntity -> categoryEntity.getName())
                    .collect(Collectors.joining("/"));
            spuDTO1.setCategoryName(collect);

            //查询品牌名称 并赋值给spuDTO1
            BrandEntity brandEntity = brandMapper.selectByPrimaryKey(spuEntity.getBrandId());
            spuDTO1.setBrandName(brandEntity.getName());

            return spuDTO1;

        }).collect(Collectors.toList());

        //整理集合的信息
        PageInfo<SpuEntity> spuEntityPageInfo = new PageInfo<>(spuEntities);

        //返回
        return this.setResult(HTTPStatus.OK,spuEntityPageInfo.getTotal()+"",collect1);
    }


}
