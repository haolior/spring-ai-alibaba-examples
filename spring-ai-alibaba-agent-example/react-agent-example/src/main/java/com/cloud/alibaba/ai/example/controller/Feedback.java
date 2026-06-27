package com.cloud.alibaba.ai.example.controller;

/**
 * 前端提交的人工审批结果。
 *
 * @param isApproved 是否批准本次工具调用
 * @param feedback 拒绝时填写的原因，批准时可为空
 */
public record Feedback(boolean isApproved, String feedback) {
}
