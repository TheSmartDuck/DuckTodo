package top.smartduck.ducktodo.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建任务族请求体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskGroupRequest {
    /**
     * 任务族名称
     */
    private String groupName;

    /**
     * 任务族描述
     */
    private String groupDescription;

    /**
     * 任务族状态
     */
    private Integer groupStatus;

    /**
     * 任务族主题色
     */
    private String groupColor;
}
