package org.y2k2.globa.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.y2k2.globa.service.RankingService;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking(@RequestParam(required = false, defaultValue = "1", value = "page") int page,
                                        @RequestParam(required = false, defaultValue = "10", value = "count") int count) {

        ObjectNode result = rankingService.getRanking(page, count);

        return ResponseEntity.ok(result);
    }
}


