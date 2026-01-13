package top.smartduck.ducktodo.modelService;

import com.baomidou.mybatisplus.extension.service.IService;
import top.smartduck.ducktodo.model.entity.TaskGroupUserRelation;

public interface TaskGroupUserRelationService extends IService<TaskGroupUserRelation> {
    int countByUserId(String userId);
    boolean swapGroupOrder(String userId, String relationIdA, String relationIdB);
}
