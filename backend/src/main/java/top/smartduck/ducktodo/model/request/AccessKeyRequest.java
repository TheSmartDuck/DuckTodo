package top.smartduck.ducktodo.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用于接收更新当前用户 AccessKey 与 SecretKey 的请求体。
 * 字段与实体保持一致：userAccesskey / userSecretkey
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessKeyRequest {
    private String userAccesskey;
    private String userSecretkey;
}