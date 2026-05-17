package org.example.counterparty.controller;

import org.example.counterparty.dto.DaDataResponse;
import org.example.counterparty.service.DaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//тестовый. потом надо удалить
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private DaDataService daDataService;

    @GetMapping("/dadata")
    public DaDataResponse testDaData(@RequestParam String query) {
        return daDataService.findPartyByInnOgrn(query);
    }
}