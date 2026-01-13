package top.smartduck.ducktodo.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交换当前用户两个团队的排序请求体。
 * 包含两个待交换的团队ID：teamIdA 与 teamIdB。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwapMyTeamOrderRequest {
    /** 团队A ID */
    private String teamIdA;
    /** 团队B ID */
    private String teamIdB;
}