package org.y2k2.globa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.entity.RankingEntity;
import org.y2k2.globa.repository.RankingRepository;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    public final RankingRepository rankingRepository;

    public ObjectNode getRanking(int page, int count) {
        try {
            Pageable pageable = PageRequest.of(page-1, count);
            Page<RankingEntity> rankingEntities =  rankingRepository.findAll(pageable);


            List<RankingEntity> resultValue = rankingEntities.getContent();
            List<ObjectNode> responseList = new ArrayList<>();

            for (RankingEntity rankingEntity : resultValue) {
                ObjectNode rankingNode = createRankingNode(rankingEntity);
                responseList.add(rankingNode);
            }

            return createResultNode(responseList);

        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectNode createRankingNode(RankingEntity rankingEntity) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rankingNode = objectMapper.createObjectNode();


        rankingNode.put("rankingId", rankingEntity.getRankingId());
        rankingNode.put("solved", rankingEntity.getSolved());

        return rankingNode;
    }

    private ObjectNode createResultNode(List<ObjectNode> responseList) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode codesArrayNode = objectMapper.createArrayNode();

        for (ObjectNode node : responseList) {
            codesArrayNode.add(node);
        }

        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.set("rankings", codesArrayNode);
        resultNode.put("total", responseList.size());

        return resultNode;
    }
}