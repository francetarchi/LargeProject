package com.wineadvisor.wineadvisor.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wineadvisor.wineadvisor.service.WineryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wineries")
@RequiredArgsConstructor
public class WineryController {
    ////////////////////////////////
    /////////// VARIABLES //////////
    ////////////////////////////////
    private final WineryService wineryService;
    
    
    
    ///////////// POST /////////////
    

    ////////////// GET /////////////
    

    ////////////// PUT /////////////
    

    //////////// DELETE ////////////
}
