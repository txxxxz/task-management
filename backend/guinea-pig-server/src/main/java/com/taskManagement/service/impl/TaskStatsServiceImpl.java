package com.taskManagement.service.impl;

import com.taskManagement.dto.TaskStatusStatsDTO;
import com.taskManagement.service.TaskStatsService;
import com.taskManagement.vo.TaskStatusStatsVO;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务统计服务实现类
 */
@Service
public class TaskStatsServiceImpl implements TaskStatsService {

    @Override
    public List<TaskStatusStatsDTO> getTaskStatusStatsByDay() {
        // TODO: 实现从数据库获取数据的逻辑
        // 这里先返回模拟数据
        List<TaskStatusStatsDTO> result = new ArrayList<>();
        
        // 获取最近7天的数据
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 7; i++) {
            LocalDateTime date = now.minusDays(i);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            
            TaskStatusStatsDTO stats = new TaskStatusStatsDTO();
            stats.setDay(dayOfWeek.toString());
            stats.setPending(5);
            stats.setInProgress(3);
            stats.setCompleted(2);
            
            result.add(stats);
        }
        
        return result;
    }
} 