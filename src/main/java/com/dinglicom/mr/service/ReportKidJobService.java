package com.dinglicom.mr.service;

import com.dinglicom.mr.entity.ReportKidJob;
import com.dinglicom.mr.entity.page.PageRequest;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ReportKidJobService {
    @Autowired
    private ReportKidJobRepository reportKidJobRepository;

    public Page<ReportKidJob> findByState(int state, int page, int size) {
        PageRequest pageRequest = new PageRequest(page, size, new Sort(Sort.Direction.ASC, "id"));
        if (state == 0 || state ==3){
            Page<ReportKidJob> byState = reportKidJobRepository.findByState(state, pageRequest);
            return byState;
        }
        return null;
    }
}
