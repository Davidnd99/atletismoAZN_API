package com.running.repository;

import com.running.model.Club;
import com.running.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    List<Club> findByProvinceIgnoreCaseAndNameNotIgnoreCase(String province, String excludedName);

    List<Club> findByNameNotIgnoreCase(String excludedName);

    // NUEVOS (gestión)
    List<Club> findByManager_UIDOrderByNameAsc(String managerUid);   // por UID Firebase
    @Query("select c from Club c left join fetch c.manager where c.id = :id")
    java.util.Optional<Club> findByIdWithManager(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Club c SET c.manager = :newManager WHERE c.manager = :oldManager")
    int reassignManager(@Param("oldManager") User oldManager,
                        @Param("newManager") User newManager);

    @Query("select c.id from Club c where c.manager = :manager")
    List<Long> findIdsByManager(@Param("manager") User manager);

    @Modifying
    @Query("""
           update Club c
           set c.members = case when c.members > 0 then c.members - 1 else 0 end
           where c.id = :id
           """)
    void decrementMembers(@Param("id") Long id);
}
