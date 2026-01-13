package top.smartduck.ducktodo.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交换当前用户两个任务族的排序请求体。
 * 包含两个待交换的任务族ID：taskGroupIdA 与 taskGroupIdB。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwapMyTaskGroupOrderRequest {
    /** 任务族A ID */
    private String taskGroupIdA;
    /** 任务族B ID */
    private String taskGroupIdB;
}