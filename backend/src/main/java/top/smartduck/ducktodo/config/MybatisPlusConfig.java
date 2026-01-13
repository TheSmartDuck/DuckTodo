package top.smartduck.ducktodo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：启用分页拦截器，确保 Page 查询正确生效。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置每页最大条数，可与控制器中的 size 上限保持一致
        paginationInnerInterceptor.setMaxLimit(100L);
        // 溢出页处理：false 表示请求超过最大页时返回空数据而非回到首页
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}