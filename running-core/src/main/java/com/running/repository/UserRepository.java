package com.running.repository;

import com.running.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUID(String uid);
    Optional<User> findByEmail(String email);
    boolean existsByIdAndRole_Name(Long id, String roleName);
    List<User> findByClubs_Id(Long clubId);
    // Buscar un usuario por primer rol disponible (para “fallback”)
    Optional<User> findFirstByRole_NameOrderByIdAsc(String roleName);
    @Query("select u from User u left join fetch u.clubs where u.UID = :uid")
    Optional<User> findByUIDWithClubs(@Param("uid") String uid);

    // Para ajustar el contador si lo necesitas (ids de clubs del usuario)
    @Query(value = "SELECT club_id FROM user_club WHERE user_id = :userId", nativeQuery = true)
    List<Long> findClubIdsByUserId(@Param("userId") Long userId);

    // Borra todas las membresías del usuario en la tabla puente
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM user_club WHERE user_id = :userId", nativeQuery = true)
    void deleteAllClubsByUserId(@Param("userId") Long userId);
}