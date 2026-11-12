package com.example.investigation_case_service.repository;

import com.example.investigation_case_service.model.users.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "region")
    Optional<User> findByLogin(String login);

    boolean existsByLogin(String login);

    @Query("select u from User u join fetch u.region where u.id = :id")
    Optional<User> findWithRegionById(@Param("id") Long id);

    @Query("select u from User u join fetch u.region order by u.id")
    List<User> findAllWithRegion();

    @Query("select u from User u join fetch u.region r where r.id = :regionId order by u.id")
    List<User> findAllByRegion(@Param("regionId") Long regionId);
}
