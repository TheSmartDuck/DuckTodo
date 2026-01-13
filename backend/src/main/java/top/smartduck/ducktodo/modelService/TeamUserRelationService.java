package top.smartduck.ducktodo.modelService;

import com.baomidou.mybatisplus.extension.service.IService;
import top.smartduck.ducktodo.model.entity.TeamUserRelation;

public interface TeamUserRelationService extends IService<TeamUserRelation> {
    int countByUserId(String userId);
    boolean swapTeamOrder(String userId, String relationIdA, String relationIdB);
}
