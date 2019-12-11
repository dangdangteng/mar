package com.dinglicom.mr.service;

import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.entity.page.PageRequestEntity;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ReportKidJobService {
    @Autowired
    private ReportKidJobRepository reportKidJobRepository;

    public Page<ReportKidJobEntity> findByState(int state, int page, int size) {
        PageRequestEntity pageRequestEntity = new PageRequestEntity(page, size, new Sort(Sort.Direction.ASC, "id"));
        if (state == 0 || state ==3){
            Page<ReportKidJobEntity> byState = reportKidJobRepository.findByState(state, pageRequestEntity);
            return byState;
        }
        return null;
    }
}
