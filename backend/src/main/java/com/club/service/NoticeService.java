package com.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.club.domain.Notice;

public interface NoticeService extends IService<Notice> {
    IPage<Notice> listPage(Integer pageNum, Integer pageSize, String title);
    Long publishNotice(Notice notice);
}
