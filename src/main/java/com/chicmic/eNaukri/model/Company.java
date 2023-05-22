package com.chicmic.eNaukri.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@RequiredArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    private String companyName;
    private String locatedAt;
    private LocalDate foundedIn;
    private String about;
    private String ppPath;

    @OneToMany(mappedBy = "postFor", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Job> jobList=new ArrayList<>();

    @OneToMany(mappedBy = "usersCompany",orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCompany> userCompanyList=new ArrayList<>();

    @OneToOne(mappedBy = "companyLinks",cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private SocialLink socialLink;
}
