package com.cs209a.spring_project.service;

import com.cs209a.spring_project.domain.Repo;
import org.springframework.stereotype.Service;

@Service
public class RepoServiceImpl implements RepoService {

    @Override
    public Repo findInfo() {
        return new Repo();
    }
}
