package com.a.eye.skywalking.web.service.impl;

import com.a.eye.skywalking.web.dao.inter.ITraceNodeDao;
import com.a.eye.skywalking.web.dto.TraceNodeInfo;
import com.a.eye.skywalking.web.dto.TraceNodesResult;
import com.a.eye.skywalking.web.dto.TraceTreeInfo;
import com.a.eye.skywalking.web.service.inter.ITraceTreeService;
import com.a.eye.skywalking.web.util.Constants;
import com.a.eye.skywalking.web.util.ReplaceAddressUtil;
import com.a.eye.skywalking.web.util.SpanLevelIdComparators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by xin on 16-3-30.
 */
@Service
@Transactional
public class TraceTreeService implements ITraceTreeService {

    @Autowired
    private ITraceNodeDao traceTreeDao;

    @Override
    public TraceTreeInfo queryTraceTreeByTraceId(String traceId)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException,
            IOException {
        TraceTreeInfo traceTreeInfo = new TraceTreeInfo(traceId);
        TraceNodesResult traceNodesResult = traceTreeDao.queryTraceNodesByTraceId(traceId);
        List<TraceNodeInfo> traceNodeInfoList = traceNodesResult.getResult();

        if (traceNodeInfoList.size() > 0) {
            final List<Long> endTime = new ArrayList<Long>();
            endTime.add(0, traceNodeInfoList.get(0).getEndDate());


            Collections.sort(traceNodeInfoList, new Comparator<TraceNodeInfo>() {
                @Override
                public int compare(TraceNodeInfo arg0, TraceNodeInfo arg1) {
                    if (endTime.get(0) < arg0.getEndDate()) {
                        endTime.set(0, arg0.getEndDate());
                    }
                    if (endTime.get(0) < arg1.getEndDate()) {
                        endTime.set(0, arg1.getEndDate());
                    }
                    return SpanLevelIdComparators.ascCompare(arg0.getTraceLevelId(), arg1.getTraceLevelId());
                }
            });

            // 截断
            int subIndex = traceNodeInfoList.size();
            if (subIndex > Constants.MAX_SHOW_SPAN_SIZE) {
                subIndex = Constants.MAX_SHOW_SPAN_SIZE;
            }
            traceTreeInfo.setHasBeenSpiltNodes(traceNodeInfoList.subList(0, subIndex));
            traceTreeInfo.setBeginTime(traceNodeInfoList.get(0).getStartDate());
            traceTreeInfo.setEndTime(endTime.get(0));
        }

        return traceTreeInfo;
    }
}
