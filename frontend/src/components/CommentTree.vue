<template>
  <div class="comment-item" :style="{ marginLeft: level > 0 ? `${level * 20}px` : '0' }">
    <div class="comment-header">
      <el-avatar :size="32" :src="comment.createUserAvatar || '/default-avatar.png'" />
      <span class="username">{{ comment.createUserName }}</span>
      <span class="time">{{ formatTime(comment.createTime) }}</span>
    </div>
    <div class="comment-content">
      <template v-if="comment.parentId && replyToUsername">
        <span class="reply-target">@{{ replyToUsername }}</span>
      </template>
      {{ comment.content }}
    </div>
    <div class="comment-actions">
      <el-button type="text" @click="handleReply(comment)">回复</el-button>
      <el-button 
        v-if="canDelete(comment)" 
        type="text" 
        @click="handleDelete(comment)"
      >删除</el-button>
    </div>
    
    <!-- 递归渲染子评论 -->
    <div v-if="comment.children && comment.children.length > 0" class="comment-children">
      <comment-tree
        v-for="child in comment.children"
        :key="child.id"
        :comment="child"
        :level="level + 1"
        :reply-map="replyMap"
        @reply="$emit('reply', $event)"
        @delete="$emit('delete', $event)"
      ></comment-tree>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import type { PropType } from 'vue';
import { useUserStore } from '../stores/user';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

interface CommentData {
  id: number;
  taskId: number;
  content: string;
  parentId?: number | null;
  createTime: string;
  createUser: number | string;
  createUserName: string;
  createUserAvatar: string;
  children?: CommentData[];
}

// 使用defineComponent替代script setup以正确声明组件名称
export default defineComponent({
  name: 'CommentTree',
  props: {
    comment: {
      type: Object as PropType<CommentData>,
      required: true
    },
    level: {
      type: Number,
      default: 0
    },
    replyMap: {
      type: Object as PropType<Record<number, string>>,
      default: () => ({})
    }
  },
  emits: ['reply', 'delete'],
  setup(props, { emit }) {
    const userStore = useUserStore();

    // 获取该评论回复对象的用户名
    const replyToUsername = computed(() => {
      if (props.comment.parentId) {
        return props.replyMap[props.comment.parentId] || '';
      }
      return '';
    });

    // 格式化相对时间
    const formatTime = (time: string): string => {
      try {
        return dayjs(time).fromNow();
      } catch (e) {
        return time || '未知时间';
      }
    };

    // 判断用户是否可以删除评论
    const canDelete = (comment: CommentData): boolean => {
      const isLeader = userStore.userInfo?.role === 1;
      const isOwnComment = String(comment.createUser) === String(userStore.userInfo?.id);
      return isLeader || isOwnComment;
    };

    // 处理回复操作
    const handleReply = (comment: CommentData) => {
      emit('reply', comment);
    };

    // 处理删除操作
    const handleDelete = (comment: CommentData) => {
      emit('delete', comment);
    };

    return {
      replyToUsername,
      formatTime,
      canDelete,
      handleReply,
      handleDelete
    };
  }
});
</script>

<style scoped>
.comment-item {
  padding: 12px;
  margin-bottom: 16px;
  border-radius: 8px;
  background-color: #f9f9f9;
  transition: background-color 0.3s;
}

.comment-item:hover {
  background-color: #f2f2f2;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.username {
  font-weight: 500;
  margin-left: 8px;
  font-size: 14px;
}

.time {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}

.comment-content {
  margin: 10px 0;
  line-height: 1.5;
  word-break: break-word;
}

.reply-target {
  color: #409EFF;
  margin-right: 4px;
  font-weight: 500;
}

.comment-actions {
  display: flex;
  justify-content: flex-start;
  gap: 8px;
  margin-top: 10px;
}

.comment-children {
  margin-top: 8px;
  margin-left: 20px;
  padding-left: 10px;
  border-left: 2px solid #eaeaea;
}
</style> 