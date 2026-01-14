package com.crm.system.modules.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.modules.customer.domain.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户 Mapper 接口
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    // MyBatis-Plus 提供了基础的 CRUD 方法，无需额外编写

    // 如需自定义查询，可在此添加方法并使用 @Select 注解或创建 XML 文件
}
