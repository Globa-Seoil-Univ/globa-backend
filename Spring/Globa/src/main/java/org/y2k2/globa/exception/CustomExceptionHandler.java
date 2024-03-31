package org.y2k2.globa.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.y2k2.globa.util.Const;
import org.y2k2.globa.util.CustomTimestamp;

@RestControllerAdvice
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private ObjectNode createErrorNode(Exception ex, int status) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();

        errorNode.put("errorCode", status);
        errorNode.put("message",ex.getMessage());
        errorNode.put("timestamp", String.valueOf(new CustomTimestamp()));

        return errorNode;
    }

    @Override
    public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Object> handleGlobalException(GlobalException ex) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UpdateException.class)
    public ResponseEntity<Object> handleUpdateException(UpdateException ex) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Object> handleSQLException(SQLException ex) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RecordNameException.class)
    public ResponseEntity<Object> handleRecordNameException(RecordNameException ex) {
        return new ResponseEntity<>(createErrorNode(ex, Const.CustomErrorCode.RECORD_NAME_DUPLICATED.value()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FolderNameException.class)
    public ResponseEntity<Object> handleFolderNameException(FolderNameException ex) {
        return new ResponseEntity<>(createErrorNode(ex, Const.CustomErrorCode.FOLDER_NAME_DUPLICATED.value()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<Object> handleAuthorizedException(UnAuthorizedException ex) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicatedExcepiton.class)
    public ResponseEntity<Object> handleDuplicatedException(DuplicatedExcepiton ex) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.CONFLICT.value()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CompileException.class)
    public ResponseEntity<Object> handleCompileException(CompileException ex) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Object> handleFileUploadException(FileUploadException ex) {
        return new ResponseEntity<>(createErrorNode(ex, Const.CustomErrorCode.FAILED_FILE_UPLOAD.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException ex) {
        return new ResponseEntity<>(createErrorNode(ex, Const.CustomErrorCode.INVALID_TOKEN.value()), HttpStatus.UNAUTHORIZED);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();

        errorNode.put("errorCode", status.value());
        errorNode.put("message", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        errorNode.put("timestamp", String.valueOf(new CustomTimestamp()));

        return new ResponseEntity<>(errorNode, status);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return new ResponseEntity<>(createErrorNode(ex, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ErrorResponse 클래스 정의

}