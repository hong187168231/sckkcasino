package com.qianyi.casinocore.service;

import com.qianyi.casinocore.repository.PlatformGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlatformGameService {

    @Autowired
    private PlatformGameRepository platformGameRepository;
}
