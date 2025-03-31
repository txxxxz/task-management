package com.taskManagement.dto;

/**
 * 任务状态统计DTO
 * 用于向前端返回每日任务状态统计数据
 */
public class TaskStatusStatsDTO {
    /**
     * 星期几
     */
    private String day;
    
    /**
     * 待处理任务数量
     */
    private Integer pending;
    
    /**
     * 进行中任务数量
     */
    private Integer inProgress;
    
    /**
     * 已完成任务数量
     */
    private Integer completed;

    /**
     * 已取消任务数量
     */
    private Integer cancelled;

    /**
     * 今日到期任务数量
     */
    private Integer todayExpired;

    /**
     * 任务总数
     */
    private Integer total;

    // Getter and Setter methods
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getPending() {
        return pending;
    }

    public void setPending(Integer pending) {
        this.pending = pending;
    }

    public Integer getInProgress() {
        return inProgress;
    }

    public void setInProgress(Integer inProgress) {
        this.inProgress = inProgress;
    }

    public Integer getCompleted() {
        return completed;
    }

    public void setCompleted(Integer completed) {
        this.completed = completed;
    }

    public Integer getCancelled() {
        return cancelled;
    }

    public void setCancelled(Integer cancelled) {
        this.cancelled = cancelled;
    }

    public Integer getTodayExpired() {
        return todayExpired;
    }

    public void setTodayExpired(Integer todayExpired) {
        this.todayExpired = todayExpired;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
} 