package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.common.enums.UserStatusEnum;
import top.smartduck.ducktodo.mapper.TeamUserRelationMapper;
import top.smartduck.ducktodo.model.entity.TeamUserRelation;
import top.smartduck.ducktodo.modelService.TeamUserRelationService;

@Service
public class TeamUserRelationServiceImpl extends ServiceImpl<TeamUserRelationMapper, TeamUserRelation> implements TeamUserRelationService {
    @Override
    public int countByUserId(String userId) {
        return (int) this.count(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getUserId, userId)
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean swapTeamOrder(String userId, String relationIdA, String relationIdB) {
        if (userId == null || relationIdA == null || relationIdB == null || relationIdA.equals(relationIdB)) {
            return false;
        }
        TeamUserRelation relA = this.getById(relationIdA);
        TeamUserRelation relB = this.getById(relationIdB);
        if (relA == null || relB == null) return false;
        if (!userId.equals(relA.getUserId()) || !userId.equals(relB.getUserId())) return false;

        Integer idxA = relA.getTeamIndex();
        Integer idxB = relB.getTeamIndex();

        if (idxA == null && idxB == null) {
            Integer max = this.list(new LambdaQueryWrapper<TeamUserRelation>()
                    .eq(TeamUserRelation::getUserId, userId))
                    .stream()
                    .map(TeamUserRelation::getTeamIndex)
                    .filter(i -> i != null)
                    .max(Integer::compareTo)
                    .orElse(0);
            idxA = max + 1;
            idxB = max + 2;
        } else if (idxA == null) {
            Integer max = this.list(new LambdaQueryWrapper<TeamUserRelation>()
                    .eq(TeamUserRelation::getUserId, userId))
                    .stream()
                    .map(TeamUserRelation::getTeamIndex)
                    .filter(i -> i != null)
                    .max(Integer::compareTo)
                    .orElse(0);
            idxA = max + 1;
        } else if (idxB == null) {
            Integer max = this.list(new LambdaQueryWrapper<TeamUserRelation>()
                    .eq(TeamUserRelation::getUserId, userId))
                    .stream()
                    .map(TeamUserRelation::getTeamIndex)
                    .filter(i -> i != null)
                    .max(Integer::compareTo)
                    .orElse(0);
            idxB = max + 1;
        }

        relA.setTeamIndex(idxB);
        relB.setTeamIndex(idxA);
        return this.updateById(relA) && this.updateById(relB);
    }
}
