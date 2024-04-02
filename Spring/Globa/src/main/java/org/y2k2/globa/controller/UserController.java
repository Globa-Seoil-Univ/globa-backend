package org.y2k2.globa.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.RequestUserPostDTO;
import org.y2k2.globa.dto.ResponseUserDTO;
import org.y2k2.globa.dto.ResponseUserNotificationDto;
import org.y2k2.globa.dto.ResponseUserSearchDto;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.service.UserService;
import org.y2k2.globa.util.JwtToken;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.ValidValues;

import java.util.Map;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class UserController {

    private final String PRE_FIX = "/user";
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/auth")
    public ResponseEntity<?> authUser(@RequestBody Map<String, String> requestTokenMap,
                                      @RequestHeader(value = "Authorization", required = false) String accessToken) {
        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        JwtToken jwtToken;

        try {
            if ( requestTokenMap.get("requestToken") == null )
                throw new BadRequestException("Required requestToken ! ");
            jwtToken = userService.reloadRefreshToken(requestTokenMap.get("requestToken"), accessToken);

        } catch (Exception e) {
            throw e;
        }

        return ResponseEntity.status(HttpStatus.OK).body(jwtToken);
    }

    @PostMapping(PRE_FIX)
    public ResponseEntity<?> postUser(@RequestBody RequestUserPostDTO requestUserPostDTO) {

        if ( requestUserPostDTO.getSnsKind() == null )
            throw new BadRequestException("Required snsKind ! ");
        if ( requestUserPostDTO.getSnsId() == null )
            throw new BadRequestException("Required snsId ! ");
        if ( requestUserPostDTO.getName() == null )
            throw new BadRequestException("Required name ! ");

        if ( ValidValues.validSnsIds.contains(requestUserPostDTO.getSnsId()) )
            throw new BadRequestException(" snsId only ' 1001 ~ 1004 ' ");
        if ( requestUserPostDTO.getName().length() > 10 )
            throw new BadRequestException("name too long ! ");

        if ( requestUserPostDTO.getNotification() == null )
            requestUserPostDTO.setNotification(true);

        JwtToken jwtToken = userService.postUser(requestUserPostDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(jwtToken);
    }

    @GetMapping(PRE_FIX)
    public ResponseEntity<?> getUser(@RequestHeader(value = "Authorization", required = false) String accessToken) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");

        ResponseUserDTO result = userService.getUser(accessToken);

        return ResponseEntity.ok(result);
    }


    @GetMapping(PRE_FIX+"/search")
    public ResponseEntity<?> getUserSearch(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                           @RequestParam(value = "code", required = false) String code) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( code == null )
            throw new BadRequestException("Required userCode ! ");

        ResponseUserSearchDto result = userService.getUser(accessToken,code);

        return ResponseEntity.ok(result);
    }
    @GetMapping(PRE_FIX+"/{user_id}/notification")
    public ResponseEntity<?> getUserNotification(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                           @PathVariable(value = "user_id", required = false) Long userId) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( userId == null )
            throw new BadRequestException("Required userId ! ");

        ResponseUserNotificationDto result = userService.getNotification(accessToken,userId);

        return ResponseEntity.ok(result);
    }










    @GetMapping("/login")
    public ResponseEntity<?> getToken(@RequestParam(required = false, value = "userId") Long userId) {
        JwtToken result = jwtTokenProvider.generateToken(userId);

        return ResponseEntity.ok(result);
    }
    @GetMapping("/testToken") // 삭제 해야함
    public ResponseEntity<?> getUserIdByAccessToken(@RequestParam(required = false, value = "token") String token) {
        Long result = jwtTokenProvider.getUserIdByAccessToken(token);

        return ResponseEntity.ok(jwtTokenProvider.validateToken(token));
    }



    @GetMapping("/error/bad-request")
    public ResponseEntity<?> getBadRequest() {
        userService.errorTestBadRequest();

        return ResponseEntity.ok("어라 이게 나오면 안되는뎅");
    }
}


