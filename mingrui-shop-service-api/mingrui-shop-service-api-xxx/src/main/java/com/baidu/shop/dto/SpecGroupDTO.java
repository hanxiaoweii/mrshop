package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "规格组数据传输DTO")
@Data
public class SpecGroupDTO extends BaseDTO {

    @ApiModelProperty(value = "主键" ,example = "1")
    @NotNull(message = "主键不能为空" , groups = {MingruiOperation.Update.class})
    private Integer id;

    @ApiModelProperty(value = "商品分类id，(类型ID)" ,example = "1")
    @NotNull(message = "类型Id不能为空",groups = {MingruiOperation.Add.class})
    private Integer cid;

    @ApiModelProperty(value = "规格组的名称")
    @NotEmpty(message = "规格组的名称不能为空",groups = {MingruiOperation.Add.class})
    private String  name;
}
