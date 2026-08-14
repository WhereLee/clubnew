package com.club.controller;

import com.club.common.R;
import com.club.service.RankService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rank")
public class RankController {

    @Resource
    private RankService rankService;

    @GetMapping("/club/activity")
    public R<List<Map<String, Object>>> clubActivityRank(@RequestParam(defaultValue = "7") int days) {
        return R.success(rankService.getClubActivityRank(days, 20));
    }

    @GetMapping("/post/hot")
    public R<List<Map<String, Object>>> postHotRank(@RequestParam(defaultValue = "7") int days) {
        return R.success(rankService.getPostHotRank(days, 20));
    }
}
