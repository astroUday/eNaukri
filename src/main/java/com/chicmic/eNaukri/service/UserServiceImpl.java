package com.chicmic.eNaukri.service;

import com.chicmic.eNaukri.model.*;
import com.chicmic.eNaukri.repo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {

    private final UsersRepo usersRepo;
    private final ApplicationRepo applicationRepo;
    private final JobRepo jobRepo;
    private final ExperienceRepo experienceRepo;
    private final SkillsRepo skillsRepo;
    private final CompanyRepo companyRepo;
    private final UserTokenRepo tokenRepo;
    private final JobSkillsRepo jobSkillsRepo;
    @PersistenceContext
    private EntityManager entityManager;

    public void saveUUID(UserToken userToken) {
        tokenRepo.save(userToken);
    }

    public Users getUserByEmail(String username) {
        return usersRepo.findByEmail(username);
    }

    public Users findUserFromUUID(String token) {
        UserToken userToken= tokenRepo.findByToken(token);
        return usersRepo.findById(userToken.getUserr().getUserId()).get();
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies=request.getCookies();
        if(cookies!=null){
            for(Cookie cookie:cookies){
                if("AuthHeader".equals(cookie.getName())){
                    tokenRepo.deleteByToken(cookie.getValue());
                    cookie.setValue(null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);break;
                }
            }
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users user= usersRepo.findByEmail(username);
//        if(user==null)throw new CustomApiExceptionHandler(HttpStatus.NOT_FOUND,username + "doesn't exists, please REGISTER.");

        Collection<Authority> authorites=new ArrayList<>();
        authorites.add(new Authority("USER"));

        return new User(user.getEmail(),user.getPassword(),authorites);
    }

    public String findCurrentCompany(Long id) {
        Users users=usersRepo.findById(id).get();
       return experienceRepo.findByExpUserAndCurrentlyWorking(users,true).getExCompany().getCompanyName();
    }


}
