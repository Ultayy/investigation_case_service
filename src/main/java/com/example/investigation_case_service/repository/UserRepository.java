package com.example.investigation_case_service.repository;

import com.example.investigation_case_service.model.users.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "region")
    Optional<User> findByLogin(String login);

    boolean existsByLogin(String login);

    boolean existsByIin(String iin);

    @Query("select u from User u join fetch u.region where u.id = :id")
    Optional<User> findWithRegionById(@Param("id") Long id);

    @Query(value = "select u from User u join fetch u.region order by u.id",
            countQuery = "select count(u) from User u")
    Page<User> findAllWithRegion(Pageable pageable);

    @Query(value = "select u from User u join fetch u.region r where r.id = :regionId order by u.id",
            countQuery = "select count(u) from User u where u.region.id = :regionId")
    Page<User> findAllByRegion(@Param("regionId") Long regionId, Pageable pageable);
}
