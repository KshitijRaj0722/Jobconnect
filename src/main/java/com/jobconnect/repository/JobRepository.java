package com.jobconnect.repository;

import com.jobconnect.model.Job;
import com.jobconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    List<Job> findByEmployerOrderByCreatedAtDesc(User employer);
    
    @Query(value = "SELECT * FROM jobs j WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "ORDER BY j.created_at DESC",
           nativeQuery = true)
    List<Job> searchJobs(@Param("keyword") String keyword, @Param("location") String location);
}
