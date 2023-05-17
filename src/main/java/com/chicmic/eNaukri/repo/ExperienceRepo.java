package com.chicmic.eNaukri.repo;

import com.chicmic.eNaukri.model.Experience;
import com.chicmic.eNaukri.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepo extends JpaRepository<Experience,Long> {
    Experience findByExpUserAndCurrentlyWorking(Users users, boolean b);
}
