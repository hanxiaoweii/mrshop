package com.baidu.shop.service.impl;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.service.CategoryService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ObjectUtils;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Override
    public Result<List<CategoryEntity>> getCategoryByBrandId(Integer brandId) {
        List<CategoryEntity> categoryByBrandId = categoryMapper.getCategoryByBrandId(brandId);

        return this.setResultSuccess(categoryByBrandId);
    }

    //save
    @Transactional
    @Override
    public Result<JsonObject> saveCategory(CategoryEntity categoryEntity) {

        CategoryEntity saveCategoryEntity = new CategoryEntity();
        saveCategoryEntity.setId(categoryEntity.getParentId());
        saveCategoryEntity.setIsParent(1);

        categoryMapper.updateByPrimaryKeySelective(saveCategoryEntity);

        categoryMapper.insertSelective(categoryEntity);

        return this.setResultSuccess();
    }




    //update
    @Transactional
    @Override
    public Result<JsonObject> updateCategory(CategoryEntity categoryEntity) {

        categoryMapper.updateByPrimaryKeySelective(categoryEntity);

        return this.setResultSuccess();
    }


    //query
    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity entity = new CategoryEntity();
        entity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(entity);

        return this.setResultSuccess(list);
    }


    //delete
    @Transactional //以后增删改都得加这个注解
    @Override
    public Result<JsonObject> deleteCategoryById(Integer id) {

        //校验ID是否合法
        if(ObjectUtils.isNull(id) || id <= 0) return this.setResultError(HTTPStatus.OPERATION_ERROR ,"id不合法");

        //查询前台传过来得ID得数据
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);

        //如果为空 直接 return 并提示（return之后 后面的代码将不会再去执行）
        if(ObjectUtils.isNull(categoryEntity)) return this.setResultError(HTTPStatus.OPERATION_ERROR , "数据不存在");

        //判断该节点是否为父节点  (数据库字段 parentID 如果为 1 的话 就是父节点  为0的话就是子节点 )
        //为父节点的话就 return 并提示 为父节点 不能被删除  不是父节点得话 继续向下执行
        //不可以直接删除父节点 因为父节点有好多字节点 容易丢失数据 不太安全
        if(categoryEntity.getParentId() ==1) return this.setResultError(HTTPStatus.OPERATION_ERROR , "当前为父节点 不能被删除");

        Example example1 = new Example(CategoryBrandEntity.class);
        example1.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> categoryBrandEntities = categoryBrandMapper.selectByExample(example1);
        if(categoryBrandEntities.size() >= 1 ) return setResultError("当前节点已绑定品牌，还不可以删除");

        //tkmapper推荐这种条件查询
        //拼接sql语句 new一个Example对象 并且把实体类放进去
        //andEqualTo("key" , value)
        //根据实体类的字段然后生成语句    where parent_id = categoryEntity.getParentId()
        //前两句执行完 然后把example对象放进selectByExample参数里 也就是categoryMapper.selectByExample(example)
        // 最终sql语句后面会拼接 where parent_id = categoryEntity.getParentId()
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId" , categoryEntity.getParentId());

        List<CategoryEntity> categoryList = categoryMapper.selectByExample(example);

        if(categoryList.size() <= 1){
            CategoryEntity updateCategoryEntity = new CategoryEntity();
            updateCategoryEntity.setIsParent(0);
            updateCategoryEntity.setId(categoryEntity.getParentId());

            categoryMapper.updateByPrimaryKeySelective(updateCategoryEntity);
        }

        //通过ID删除节点
        categoryMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }
}
