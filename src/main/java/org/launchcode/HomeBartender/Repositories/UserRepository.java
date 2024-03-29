package org.launchcode.HomeBartender.Repositories;

import org.launchcode.HomeBartender.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    User findByUserName (String UserName);
}
