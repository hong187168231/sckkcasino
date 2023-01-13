package com.qianyi.casinoadmin.util;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.pagehelper.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public class PageResult<T extends Collection> extends PageBaseInfo {

    public static final String ORDER_ASC = "ASC";
    public static final String ORDER_DESC = "DESC";

    /** 总记录数 */
    private int totalElements;

    /** 总页数 */
    private int totalPages;

    /** 分页数据的起始位置 */
    private int offset;

    private int limit;

    /** 排序字段 */
    private String orderBy;

    /** 排序方式：asc, desc */
    private String order;

    /** 返回数据 */
    private T content;

    /**
     * 返回分页起始位置
     *
     * @return
     */
    @JSONField(serialize = false)
    @JsonIgnore
    public int getOffset() {
        return (this.pageNo - 1) * this.pageSize;
    }

    public static PageResult checkAndInit(Integer pageNo, Integer pageSize) {
        PageResult pageResult = new PageResult();
        pageResult.setPageNo(null == pageNo || pageNo <= 0 ? DEFAULT_PAGE_NO : pageNo);
        pageResult.setPageSize(null == pageSize || pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize);
        return pageResult;
    }

    public static PageResult getPageResult(PageBounds pageBounds) {
        PageResult pageResult = new PageResult();
        pageResult.setPageNo(pageBounds.getPageNo());
        pageResult.setPageSize(pageBounds.getPageSize());
        return pageResult;
    }

    /**
     * 从page获取分页总数
     */
    public static PageResult getPageResult(PageBounds pageBounds, Page<?> content) {
        Integer totalElements = null;
        if (null != content && content.getTotal() > 0) {
            totalElements = (int) content.getTotal();
        }
        PageResult pageResult = getPageResult(pageBounds.getPageNo(), pageBounds.getPageSize(), totalElements);
        pageResult.setcontent(content);
        return pageResult;
    }

    public static PageResult getPageResult(PageBounds pageBounds, List content) {
        PageResult pageResult = new PageResult();
        pageResult.setPageNo(pageBounds.getPageNo());
        pageResult.setPageSize(pageBounds.getPageSize());
        pageResult.setcontent(content);
        return pageResult;
    }

    public static PageResult getPageResult(Integer totalElements, PageBounds pageBounds, List content) {
        PageResult pageResult = new PageResult();
        pageResult.setPageNo(pageBounds.getPageNo());
        pageResult.setPageSize(pageBounds.getPageSize());
        pageResult.settotalElements(null == totalElements ? 0 : totalElements);
        pageResult.setcontent(content);
        return pageResult;
    }

    public static PageResult getPageResult(Integer pageNo, Integer pageSize, Integer totalElements) {
        return getPageResult(pageNo, pageSize, totalElements, null, null);
    }

    public static PageResult getPageResult(Integer pageNo, Integer pageSize, Integer totalElements, String orderBy, String order) {
        PageResult pageResult = PageResult.checkAndInit(pageNo, pageSize);
        pageResult.setOrderBy(orderBy);
        pageResult.setOrder(order);
        pageResult.setLimit(pageResult.getPageSize());
        pageResult.settotalElements(totalElements == null ? 0 : totalElements);
        if (pageResult.gettotalElements() == 0) {
            pageResult.settotalPages(0);
        } else {
            int totalPages = pageResult.gettotalElements() / pageResult.getPageSize();
            if (pageResult.gettotalElements() % pageResult.getPageSize() != 0) {
                totalPages += 1;
            }
            pageResult.settotalPages(totalPages);
            //解决 pageNo 越界
            if (pageResult.getPageNo() > totalPages) {
                pageResult.setPageNo(totalPages);
            }
            int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();
            pageResult.setOffset(offset);
        }
        return pageResult;
    }

    public static PageResult getPageResult(Integer pageNo, Integer pageSize, Integer totalElements, List content) {
        PageResult pageResult = PageResult.getPageResult(pageNo, pageSize, totalElements);
        pageResult.setcontent(content);
        return pageResult;
    }

    /**
     * 填充分页相关字段
     *
     * @param pageResult
     * @param map
     */
    public static void fillPageFields(PageResult pageResult, Map map) {
        if (null == pageResult) {
            pageResult = PageResult.checkAndInit(null, null);
        }
        if (null != map) {
            map.put("orderBy", pageResult.getOrderBy());
            map.put("order", pageResult.getOrder());
            map.put("offset", pageResult.getOffset());
            map.put("limit", pageResult.getLimit());
        }
    }

    public void settotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int gettotalElements() {
        return totalElements;
    }

    public void settotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int gettotalPages() {
        if (this.totalElements > 0) {
            this.totalPages = this.totalElements / this.pageSize + (this.totalElements % this.pageSize == 0 ? 0 : 1);
        }
        return totalPages;
    }

    public T getcontent() {
        return content;
    }

    public void setcontent(T content) {
        this.content = content;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @JSONField(serialize = false)
    @JsonIgnore
    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    @JSONField(serialize = false)
    @JsonIgnore
    public String getOrder() {
        return null == order || "".equals(order) ? ORDER_DESC : order;
    }

    public void setOrder(String order) {
        this.order = order;
        if (null != this.order && !"".equals(order)) {
            order = order.toUpperCase();
            if (ORDER_ASC.equals(order) || ORDER_DESC.equals(order)) {
                return;
            }
            this.order = ORDER_ASC;
        }
    }

    @JSONField(serialize = false)
    @JsonIgnore
    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }





}
