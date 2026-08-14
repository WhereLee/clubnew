package com.club.service;

import java.util.List;
import java.util.Map;

public interface RankService {
    void incrClubActivityScore(Long clubId, double score);
    void incrPostHotScore(Long postId, double score);
    List<Map<String, Object>> getClubActivityRank(int days, int limit);
    List<Map<String, Object>> getPostHotRank(int days, int limit);
}
