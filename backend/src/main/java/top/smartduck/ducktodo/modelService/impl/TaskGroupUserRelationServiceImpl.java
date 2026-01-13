package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.common.enums.UserStatusEnum;
import top.smartduck.ducktodo.mapper.TaskGroupUserRelationMapper;
import top.smartduck.ducktodo.model.entity.TaskGroupUserRelation;
import top.smartduck.ducktodo.modelService.TaskGroupUserRelationService;

@Service
public class TaskGroupUserRelationServiceImpl extends ServiceImpl<TaskGroupUserRelationMapper, TaskGroupUserRelation> implements TaskGroupUserRelationService {
    @Override
    public int countByUserId(String userId) {
        return (int) this.count(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getUserId, userId)
                .eq(TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean swapGroupOrder(String userId, String relationIdA, String relationIdB) {
        if (userId == null || relationIdA == null || relationIdB == null || relationIdA.equals(relationIdB)) {
            return false;
        }
        TaskGroupUserRelation relA = this.getById(relationIdA);
        TaskGroupUserRelation relB = this.getById(relationIdB);
        if (relA == null || relB == null) return false;
        if (!userId.equals(relA.getUserId()) || !userId.equals(relB.getUserId())) return false;

        Integer idxA = relA.getGroupIndex();
        Integer idxB = relB.getGroupIndex();

        if (idxA == null && idxB == null) {
            Integer max = this.list(new LambdaQueryWrapper<TaskGroupUserRelation>()
                    .eq(TaskGroupUserRelation::getUserId, userId))
                    .stream()
                    .map(TaskGroupUserRelation::getGroupIndex)
                    .filter(i -> i != null)
                    .max(Integer::compareTo)
                    .orElse(0);
            idxA = max + 1;
            idxB = max + 2;
        } else if (idxA == null) {
            Integer max = this.list(new LambdaQueryWrapper<TaskGroupUserRelation>()
                    .eq(TaskGroupUserRelation::getUserId, userId))
                    .stream()
                    .map(TaskGroupUserRelation::getGroupIndex)
                    .filter(i -> i != null)
                    .max(Integer::compareTo)
                    .orElse(0);
            idxA = max + 1;
        } else if (idxB == null) {
            Integer max = this.list(new LambdaQueryWrapper<TaskGroupUserRelation>()
                    .eq(TaskGroupUserRelation::getUserId, userId))
                    .stream()
                    .map(TaskGroupUserRelation::getGroupIndex)
                    .filter(i -> i != null)
                    .max(Integer::compareTo)
                    .orElse(0);
            idxB = max + 1;
        }

        relA.setGroupIndex(idxB);
        relB.setGroupIndex(idxA);
        return this.updateById(relA) && this.updateById(relB);
    }
}
