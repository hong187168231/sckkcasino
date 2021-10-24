package com.qianyi.casinocore.util;

import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;

import java.util.Collections;
import java.util.List;

public class PageUtil {
    public static PageResultVO<?> handlePageResult(List<?> list, PageVo pageVO) {
        // 分页组装
        PageResultVO<?> pageResult = new PageResultVO<>();
        if (list == null || list.size() == 0) {
            pageResult.setContent(Collections.emptyList());
            return pageResult;
        }
        int startIndex = (pageVO.getPageNo() - 1) * pageVO.getPageSize(); // 开始取值
        int endIndex = pageVO.getPageSize() + startIndex; // 结束取值
        List<?> pageList = null;
        pageResult.setTotalElements(Long.parseLong(list.size()+"")); // 总条数
        pageResult.setTotalPages(list.size() % pageVO.getPageSize() == 0 ? list.size() / pageVO.getPageSize() : list.size() / pageVO.getPageSize() + 1); // 总共多少页
        pageResult.setSize(pageVO.getPageSize());
        pageResult.setNumber(pageVO.getPageNo());
        try {
            pageList = list.subList(startIndex, endIndex);
            pageResult.setContent(pageList); // 分页显示的记录
            return pageResult;
        } catch (Exception e) {
            if (pageVO.getPageNo() > 1) {
                try {
                    pageList = list.subList(startIndex, list.size());
                    pageResult.setContent(pageList); // 分页显示的记录
                    return pageResult;
                } catch (Exception e1) {
                    PageResultVO<?> pageResultnew = new PageResultVO<>();
                    pageResultnew.setContent(Collections.emptyList());
                    return pageResultnew;
                }
            } else {
                pageList = list.subList(0, list.size());
                pageResult.setContent(pageList); // 分页显示的记录
                return pageResult;
            }
        }
    }
}
