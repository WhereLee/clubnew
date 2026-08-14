package com.club.service.impl;

import com.club.service.RankService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankServiceImpl implements RankService {

    private static final Logger log = LoggerFactory.getLogger(RankServiceImpl.class);
    private static final String CLUB_ACTIVITY_RANK = "rank:club:activity";
    private static final String POST_HOT_RANK = "rank:post:hot";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void incrClubActivityScore(Long clubId, double score) {
        try {
            ZSetOperations<String, String> zsetOps = stringRedisTemplate.opsForZSet();
            if (zsetOps != null) {
                zsetOps.incrementScore(CLUB_ACTIVITY_RANK, clubId.toString(), score);
            }
        } catch (Exception e) {
            log.debug("排行榜加分失败(可忽略): {}", e.getMessage());
        }
    }

    @Override
    public void incrPostHotScore(Long postId, double score) {
        try {
            ZSetOperations<String, String> zsetOps = stringRedisTemplate.opsForZSet();
            if (zsetOps != null) {
                zsetOps.incrementScore(POST_HOT_RANK, postId.toString(), score);
            }
        } catch (Exception e) {
            log.debug("排行榜加分失败(可忽略): {}", e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getClubActivityRank(int days, int limit) {
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    stringRedisTemplate.opsForZSet().reverseRangeWithScores(CLUB_ACTIVITY_RANK, 0, limit - 1);
            if (tuples == null) return Collections.emptyList();
            return tuples.stream().map(t -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", t.getValue());
                map.put("score", t.getScore());
                return map;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getPostHotRank(int days, int limit) {
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    stringRedisTemplate.opsForZSet().reverseRangeWithScores(POST_HOT_RANK, 0, limit - 1);
            if (tuples == null) return Collections.emptyList();
            return tuples.stream().map(t -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", t.getValue());
                map.put("score", t.getScore());
                return map;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
