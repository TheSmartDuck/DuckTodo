package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.TeamMapper;
import top.smartduck.ducktodo.model.entity.Team;
import top.smartduck.ducktodo.modelService.TeamService;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
}
