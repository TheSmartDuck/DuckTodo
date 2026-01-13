package top.smartduck.ducktodo.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于承载当前用户的访问密钥信息。
 * 字段名与后端实体保持一致，确保前端 JSON 兼容：
 * - userAccesskey: 访问密钥
 * - userSecretkey: 密钥秘钥
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessKeyResponse {
    private String userAccesskey;
    private String userSecretkey;
}