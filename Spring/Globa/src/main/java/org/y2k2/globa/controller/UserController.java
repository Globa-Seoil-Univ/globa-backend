package org.y2k2.globa.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.ForbiddenException;
import org.y2k2.globa.service.UserService;
import org.y2k2.globa.util.JwtToken;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.ValidValues;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("user")
@ResponseBody
@RequiredArgsConstructor
public class UserController {
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

    @PostMapping
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

    @GetMapping
    public ResponseEntity<?> getUser(@RequestHeader(value = "Authorization", required = false) String accessToken) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");

        ResponseUserDTO result = userService.getUser(accessToken);

        return ResponseEntity.ok(result);
    }


    @GetMapping("/search")
    public ResponseEntity<?> getUserSearch(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                           @RequestParam(value = "code", required = false) String code) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( code == null )
            throw new BadRequestException("Required userCode ! ");

        ResponseUserSearchDto result = userService.getUser(accessToken,code);

        return ResponseEntity.ok(result);
    }
    @GetMapping("/{user_id}/notification")
    public ResponseEntity<?> getUserNotification(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                           @PathVariable(value = "user_id", required = false) Long userId) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( userId == null )
            throw new BadRequestException("Required userId ! ");

        ResponseUserNotificationDto result = userService.getNotification(accessToken,userId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{user_id}/analysis")
    public ResponseEntity<?> getAnalysis(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                                 @PathVariable(value = "user_id", required = false) Long userId) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( userId == null )
            throw new BadRequestException("Required userId ! ");

        ResponseAnalysisDto result = userService.getAnalysis(accessToken,userId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/login")
    public ResponseEntity<?> getToken(@RequestParam(required = false, value = "userId") Long userId) {
        JwtToken result = jwtTokenProvider.generateToken(userId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{userId}/notification/token")
    public ResponseEntity<?> postNotificationToken(
            @RequestHeader(value = "Authorization") String accessToken,
            @Valid @RequestBody RequestNotificationTokenDto dto,
            @PathVariable(value = "userId", required = false) long userId) {
        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");

        long accessUserId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        if (accessUserId != userId)
            throw new BadRequestException("Invalid userId ! ");

        userService.addAndUpdateNotificationToken(dto, userId);
        return ResponseEntity.created(URI.create("/user")).build();
    }

    @PutMapping("/{userId}/notification/token")
    public ResponseEntity<?> updateNotificationToken(
            @RequestHeader(value = "Authorization") String accessToken,
            @Valid @RequestBody RequestNotificationTokenDto dto,
            @PathVariable(value = "userId", required = false) long userId) {
        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");

        long accessUserId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        if (accessUserId != userId)
            throw new ForbiddenException("Invalid userId ! ");

        userService.addAndUpdateNotificationToken(dto, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{userId}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestParam("profile") MultipartFile file,
            @PathVariable(value = "userId", required = false) long userId) {
        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if (file.isEmpty()) throw new BadRequestException("You must request profile field");

        long accessUserId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        if (accessUserId != userId)
            throw new ForbiddenException("Invalid userId ! ");

        userService.updateProfile(file, userId);
        return ResponseEntity.noContent().build();
    }
}


