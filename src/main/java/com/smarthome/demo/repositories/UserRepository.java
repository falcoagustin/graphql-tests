package com.smarthome.demo.repositories;

import com.smarthome.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT t.email, t.password FROM User t where t.email = :email")
    List<User> findByEmail(@Param("email") String email);
}
