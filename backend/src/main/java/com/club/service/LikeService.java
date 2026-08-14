package com.club.service;

public interface LikeService {
    boolean toggleLike(String bizType, Long bizId, Long userId);
    boolean isLiked(String bizType, Long bizId, Long userId);
}
