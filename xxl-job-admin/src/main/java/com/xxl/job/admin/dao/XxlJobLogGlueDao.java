package com.xxl.job.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.xxl.job.admin.core.model.XxlJobLogGlue;

/**
 * job log for glue
 * 
 * @author xuxueli 2016-5-19 18:04:56
 */
@Mapper
public interface XxlJobLogGlueDao {

    public int save(XxlJobLogGlue xxlJobLogGlue);

    public List<XxlJobLogGlue> findByJobId(@Param("jobId") int jobId);

    public int removeOld(@Param("jobId") int jobId, @Param("limit") int limit);

    public int deleteByJobId(@Param("jobId") int jobId);

}
